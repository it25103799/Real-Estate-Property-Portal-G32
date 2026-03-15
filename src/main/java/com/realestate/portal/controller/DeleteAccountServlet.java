package com.realestate.portal.controller;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeleteAccountServlet")
public class DeleteAccountServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userEmail = request.getParameter("userEmail");

        if (userEmail == null || userEmail.trim().isEmpty()) {
            response.sendRedirect("buyerDashboard");
            return;
        }

        String filePath = getServletContext().getRealPath("/WEB-INF/users.txt");
        File inputFile = new File(filePath);
        File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;

            // Read every line from users.txt
            while ((currentLine = reader.readLine()) != null) {
                String[] userDetails = currentLine.split(",");

                // If the line matches the email of the person deleting their account, SKIP IT.
                // Otherwise, write the line to the temp file.
                if (userDetails.length >= 2 && userDetails[1].equals(userEmail)) {
                    continue; // Skip this user! They are being deleted.
                }
                writer.write(currentLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }

        // Delete the old file and rename the temp file to "users.txt"
        if (inputFile.delete()) {
            tempFile.renameTo(inputFile);
        }

        // Destroy the user's session so they are completely logged out
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Send them back to the homepage with a goodbye message
        request.setAttribute("successMessage", "Your account has been successfully deleted. We are sorry to see you go!");
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}