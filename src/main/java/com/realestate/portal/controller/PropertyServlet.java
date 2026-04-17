package com.realestate.portal.controller;

import com.realestate.portal.model.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/properties")
public class PropertyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. LOAD PROPERTIES
        List<Property> propertyList = new ArrayList<>();
        String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File propFile = new File(propPath);
        if (propFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 8) {
                        propertyList.add(new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7]));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 2. LOAD REVIEWS (Kalhari's Module)
        List<Review> reviewList = new ArrayList<>();
        String revPath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File revFile = new File(revPath);
        if (revFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(revFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 6) {
                        if (data[5].equalsIgnoreCase("VERIFIED")) {
                            reviewList.add(new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        } else {
                            reviewList.add(new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        request.setAttribute("propertyList", propertyList);
        request.setAttribute("allReviews", reviewList);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}