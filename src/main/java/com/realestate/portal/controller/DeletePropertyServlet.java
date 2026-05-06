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

@WebServlet("/deleteProperty")
public class DeletePropertyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("loggedRole");
        String loggedUser = (String) session.getAttribute("loggedUser");

        // SECURITY: Must be a Seller or an Admin to proceed.
        if (role == null || (!"SELLER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role))) {
            response.sendRedirect("login");
            return;
        }

        String idToDelete = request.getParameter("propertyId");
        String filePath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        List<String> remainingLines = new ArrayList<>();
        boolean isDeleted = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] details = line.split(",");
                boolean idMatches = details.length > 0 && details[0].equals(idToDelete);

                // Admin can delete any property.
                // Seller can only delete their own property.
                if (idMatches && ("ADMIN".equalsIgnoreCase(role) || (details.length > 6 && details[6].equals(loggedUser)))) {
                    isDeleted = true; // Found and will be skipped
                } else {
                    remainingLines.add(line); // Keep this line
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading properties for deletion: " + e.getMessage());
        }

        if (isDeleted) {
            try (PrintWriter out = new PrintWriter(new FileWriter(filePath, false))) {
                for (String newLine : remainingLines) {
                    out.println(newLine);
                }
            } catch (IOException e) {
                System.out.println("Error writing after deletion: " + e.getMessage());
            }
            
            // Also remove from sold_properties.txt if it was a sold property
            removeFromSoldProperties(idToDelete);
        }

        // Redirect back to the correct dashboard based on role.
        if ("ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("adminDashboard");
        } else {
            response.sendRedirect("sellerDashboard");
        }
    }
    
    /**
     * Remove the deleted property from sold_properties.txt
     * Format: timestamp|propertyId|title|price|location|imageUrl
     */
    private void removeFromSoldProperties(String propertyId) {
        try {
            String soldFilePath = getServletContext().getRealPath("/WEB-INF/sold_properties.txt");
            File soldFile = new File(soldFilePath);
            
            if (!soldFile.exists()) {
                return;
            }
            
            List<String> remainingLines = new ArrayList<>();
            
            // Read all lines and keep only those that don't match the deleted property
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(soldFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split("\\|", -1);
                    // Format: timestamp|propertyId|title|price|location|imageUrl
                    // propertyId is at index 1
                    if (parts.length >= 2 && !parts[1].equals(propertyId)) {
                        remainingLines.add(line);
                    }
                }
            }
            
            // Write back the remaining lines
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(soldFile, false), "UTF-8"))) {
                for (String l : remainingLines) {
                    out.println(l);
                }
            }
            
            System.out.println("️ Removed property " + propertyId + " from sold_properties.txt");
        } catch (Exception e) {
            System.out.println("Error removing from sold_properties.txt: " + e.getMessage());
        }
    }
}