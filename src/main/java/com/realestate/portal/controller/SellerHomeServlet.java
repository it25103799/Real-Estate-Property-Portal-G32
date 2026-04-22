package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
import com.realestate.portal.model.Review;
import com.realestate.portal.model.PublicReview;
import com.realestate.portal.model.VerifiedReview;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/sellerHome")
public class SellerHomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String role = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (loggedUser == null || role == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("properties");
            return;
        }

        // --- LOAD PROPERTIES FOR THIS SELLER ---
        List<Property> myProperties = new ArrayList<>();
        String path = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File file = new File(path);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    // id, title, price, location, type, status, sellerName, imageUrl
                    if (data.length >= 7 && loggedUser.equals(data[6])) {
                        String imageUrl = (data.length >= 8) ? data[7] : "";
                        myProperties.add(new Property(
                                data[0], data[1], Double.parseDouble(data[2]),
                                data[3], data[4], data[5], data[6], imageUrl
                        ));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading properties: " + e.getMessage());
            }
        }

        // --- LOAD REVIEWS FOR THESE PROPERTIES ---
        Set<String> myPropIds = new HashSet<>();
        for (Property p : myProperties) myPropIds.add(p.getId());

        Map<String, List<Review>> reviewsByProperty = new HashMap<>();
        for (String propId : myPropIds) reviewsByProperty.put(propId, new ArrayList<>());

        String revPath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File revFile = new File(revPath);
        if (revFile.exists() && !myPropIds.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(revFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 6 && myPropIds.contains(data[1])) {
                        Review r;
                        if ("VERIFIED".equalsIgnoreCase(data[5])) {
                            r = new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]);
                        } else {
                            r = new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]);
                        }
                        reviewsByProperty.computeIfAbsent(data[1], k -> new ArrayList<>()).add(r);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading reviews: " + e.getMessage());
            }
        }

        request.setAttribute("myProperties", myProperties);
        request.setAttribute("reviewsByProperty", reviewsByProperty);

        request.getRequestDispatcher("/seller_home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

