package com.realestate.portal.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/updateProperty")
public class UpdatePropertyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");
        String role = (String) session.getAttribute("loggedRole");

        if (loggedUser == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String propertyId = request.getParameter("propertyId");
        String title = request.getParameter("title").replace(",", " ");
        String price = request.getParameter("price").replace(",", "");
        String location = request.getParameter("location").replace(",", " ");
        String type = request.getParameter("type");
        String status = request.getParameter("status");
        String bedrooms = request.getParameter("bedrooms");
        String bathrooms = request.getParameter("bathrooms");
        String description = request.getParameter("description").replace(",", " ");

        File propFile = new File(getServletContext().getRealPath("/WEB-INF/properties.txt"));
        List<String> updatedLines = new ArrayList<>();

        if (propFile.exists() && propertyId != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");

                    // Check if the line has enough fields for the old format (at least 10 for bedrooms/bathrooms)
                    // or 11 for the new format including description.
                    // We need to preserve existing description if it exists and is not being updated.
                    String existingDescription = "";
                    if (data.length > 10) {
                        existingDescription = data[10];
                    }

                    if (data.length >= 8 && data[0].equals(propertyId) && data[6].equals(loggedUser)) {
                        // If a new description is provided, use it. Otherwise, keep the existing one.
                        String descriptionToSave = description.isEmpty() ? existingDescription : description;
                        String updatedRecord = data[0] + "," + title + "," + price + "," + location + "," + type + "," + status + "," + data[6] + "," + data[7] + "," + bedrooms + "," + bathrooms + "," + descriptionToSave;
                        updatedLines.add(updatedRecord);
                    } else {
                        updatedLines.add(line);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading properties for update: " + e.getMessage());
            }

            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(propFile, false), "UTF-8"))) {
                for (String l : updatedLines) {
                    out.println(l);
                }
            } catch (Exception e) {
                System.out.println("Error saving updated properties: " + e.getMessage());
            }
        }

        response.sendRedirect("sellerDashboard");
    }
}