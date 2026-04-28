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

@WebServlet("/deleteUser")
public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("loggedRole");

        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String emailToDelete = request.getParameter("userEmail");
        String filePath = getServletContext().getRealPath("/WEB-INF/users.txt");
        File inputFile = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        if (inputFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    String[] userData = currentLine.split(",");
                    if (userData.length > 1 && userData[1].equals(emailToDelete)) {
                        userFound = true;
                    } else {
                        lines.add(currentLine);
                    }
                }
            }
        }

        if (userFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, false))) { // false to overwrite
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            session.setAttribute("flashMessage", "✅ User '" + emailToDelete + "' was successfully deleted.");
            session.setAttribute("flashMessageType", "success");
        } else {
            session.setAttribute("flashMessage", "❌ Error: User '" + emailToDelete + "' not found.");
            session.setAttribute("flashMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }
}