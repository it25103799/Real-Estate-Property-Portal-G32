package com.realestate.portal.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    // ═══════════════════════════════════════════════════════════════
    // ADMIN LICENSE KEY - PROVIDED BY MAIN COMPANY ONLY
    // ═══════════════════════════════════════════════════════════════
    private static final String ADMIN_LICENSE_KEY = "436FD - 7UH5R - F12W3 - 8HY5R";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String adminKey = request.getParameter("adminKey");

        System.out.println("====== NEW REGISTRATION ======");
        System.out.println("Full Name: " + fullName);
        System.out.println("Email: " + email);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Role: " + role);

        // ═════════════════════════════════════════════════════════════
        // ADMIN KEY VALIDATION
        // ═════════════════════════════════════════════════════════════
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (adminKey == null || !adminKey.trim().equalsIgnoreCase(ADMIN_LICENSE_KEY)) {
                System.out.println("❌ ADMIN KEY VALIDATION FAILED!");
                System.out.println("Provided Key: " + adminKey);
                System.out.println("Expected Key: " + ADMIN_LICENSE_KEY);
                request.setAttribute("errorMessage", "Invalid Admin License Key! Registration as Admin is not permitted.");
                request.getRequestDispatcher("/index.jsp").forward(request, response);
                return; // STOP THE REGISTRATION
            }
            System.out.println("✅ ADMIN KEY VALIDATED SUCCESSFULLY!");
        }

        // If password is empty/null for admins, use the key as password (dummy)
        if (password == null || password.trim().isEmpty()) {
            password = adminKey != null ? adminKey : "ADMIN_KEY_PROVIDED";
        }

        String userRecord = fullName + "," + email + "," + phoneNumber + "," + password + "," + role + "\n";

        String filePath = getServletContext().getRealPath("/WEB-INF/users.txt");

        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(userRecord);
            System.out.println("✅ User saved to file successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving user: " + e.getMessage());
        }

        request.setAttribute("successMessage", "Account created successfully! Please Sign In.");
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}