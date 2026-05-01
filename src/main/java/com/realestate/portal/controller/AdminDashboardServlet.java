package com.realestate.portal.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.realestate.portal.model.Property;
import com.realestate.portal.model.PublicReview;
import com.realestate.portal.model.Review;
import com.realestate.portal.model.User;
import com.realestate.portal.model.VerifiedReview;

@WebServlet("/adminDashboard")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String loggedRole = (String) session.getAttribute("loggedRole");
        String loggedUser = (String) session.getAttribute("loggedUser");

        // ═════════════════════════════════════════════════════════════
        // ADMIN ONLY ACCESS CONTROL
        // ═════════════════════════════════════════════════════════════
        if (loggedRole == null || !loggedRole.equalsIgnoreCase("ADMIN")) {
            System.out.println("⚠️ UNAUTHORIZED ACCESS ATTEMPT TO ADMIN DASHBOARD!");
            response.sendRedirect("properties");
            return;
        }

        System.out.println("✅ ADMIN LOGGED IN: " + loggedUser);

        // ─── Load all users ───
        List<User> allUsers = new ArrayList<>();
        String usersFilePath = getServletContext().getRealPath("/WEB-INF/users.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(usersFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] userDetails = line.split(",");
                if (userDetails.length == 5) {
                    allUsers.add(new User(userDetails[0], userDetails[1], userDetails[2], userDetails[3], userDetails[4].trim()));
                } else if (userDetails.length == 4) {
                    allUsers.add(new User(userDetails[0], userDetails[1], "", userDetails[2], userDetails[3].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }

        // ─── Load all properties ───
        List<Property> allProperties = new ArrayList<>();
        String propertiesFilePath = getServletContext().getRealPath("/WEB-INF/properties.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(propertiesFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] propDetails = line.split(",");
                if (propDetails.length >= 6) {
                    Property prop = new Property();
                    prop.setId(propDetails[0]);
                    prop.setTitle(propDetails[1]);
                    prop.setPrice(Double.parseDouble(propDetails[2]));
                    prop.setLocation(propDetails[3]);
                    prop.setType(propDetails[4]);
                    prop.setStatus(propDetails[5]);
                    if (propDetails.length > 6) prop.setSellerName(propDetails[6]);
                    allProperties.add(prop);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading properties.txt: " + e.getMessage());
        }

        // ─── Load all reviews ───
        List<Review> allReviews = new ArrayList<>();
        String reviewsFilePath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File reviewsFile = new File(reviewsFilePath);

        if (reviewsFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(reviewsFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] data = line.split(",");
                    if (data.length == 6) {
                        if (data[5].equalsIgnoreCase("VERIFIED")) {
                            allReviews.add(new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        } else {
                            allReviews.add(new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading reviews.txt: " + e.getMessage());
            }
        }

        // ─── Compute statistics ───
        int totalUsers          = allUsers.size();
        int totalBuyers         = (int) allUsers.stream().filter(u -> "BUYER".equalsIgnoreCase(u.getRole())).count();
        int totalSellers        = (int) allUsers.stream().filter(u -> "SELLER".equalsIgnoreCase(u.getRole())).count();
        int totalAdmins         = (int) allUsers.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
        int totalProperties     = allProperties.size();
        double totalPropertyValue = allProperties.stream().mapToDouble(Property::getPrice).sum();
        int totalReviews        = allReviews.size();

        // ─── Set JSP attributes ───
        request.setAttribute("allUsers",           allUsers);
        request.setAttribute("allProperties",      allProperties);
        request.setAttribute("allReviews",         allReviews);
        request.setAttribute("totalUsers",         totalUsers);
        request.setAttribute("totalBuyers",        totalBuyers);
        request.setAttribute("totalSellers",       totalSellers);
        request.setAttribute("totalAdmins",        totalAdmins);
        request.setAttribute("totalProperties",    totalProperties);
        request.setAttribute("totalPropertyValue", totalPropertyValue);
        request.setAttribute("totalReviews",       totalReviews);

        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
