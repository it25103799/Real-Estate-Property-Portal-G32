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

@WebServlet("/changeRole")
public class ChangeRoleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("loggedRole");

        // ADMIN ONLY
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String targetEmail = request.getParameter("userEmail");
        String newRole     = request.getParameter("newRole");

        // Validate newRole input
        if (newRole == null || (!newRole.equalsIgnoreCase("BUYER") && !newRole.equalsIgnoreCase("SELLER"))) {
            session.setAttribute("flashMessage", "❌ Invalid role specified.");
            session.setAttribute("flashMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/adminDashboard");
            return;
        }

        String filePath = getServletContext().getRealPath("/WEB-INF/users.txt");
        File inputFile  = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean userFound  = false;

        if (inputFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.trim().isEmpty()) {
                        lines.add(currentLine);
                        continue;
                    }
                    String[] userData = currentLine.split(",");
                    // Format: username,email,phone,password,role
                    if (userData.length >= 4 && userData[1].equals(targetEmail)) {
                        userFound = true;
                        // Rebuild line with updated role
                        userData[userData.length - 1] = newRole.toUpperCase();
                        lines.add(String.join(",", userData));
                    } else {
                        lines.add(currentLine);
                    }
                }
            }
        }

        if (userFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            session.setAttribute("flashMessage", "✅ Role updated to " + newRole.toUpperCase() + " for " + targetEmail);
            session.setAttribute("flashMessageType", "success");
        } else {
            session.setAttribute("flashMessage", "❌ User '" + targetEmail + "' not found.");
            session.setAttribute("flashMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }
}
