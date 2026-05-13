package com.realestate.portal.controller;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/saveFavorite")
public class SaveFavoriteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String buyerName = (String) session.getAttribute("loggedUser");
        String role = (String) session.getAttribute("loggedRole");

        // Kick them to login if they aren't a buyer!
        if (buyerName == null || !"BUYER".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String propertyId = request.getParameter("propertyId");
        if (propertyId != null && !propertyId.isEmpty()) {
            String filePath = getServletContext().getRealPath("/WEB-INF/favorites.txt");

            // ── DUPLICATE CHECK ──────────────────────────────────────────────
            // Read existing favorites and skip if this buyer already saved it
            boolean alreadySaved = false;
            File favFile = new File(filePath);
            if (favFile.exists()) {
                List<String> lines = Files.readAllLines(favFile.toPath());
                String newEntry = buyerName + "," + propertyId;
                for (String line : lines) {
                    if (line.trim().equalsIgnoreCase(newEntry)) {
                        alreadySaved = true;
                        break;
                    }
                }
            }
            // ─────────────────────────────────────────────────────────────────

            if (!alreadySaved) {
                // Save the match: BuyerName,PropertyID
                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"))) {
                    out.println(buyerName + "," + propertyId);
                } catch (Exception e) {
                    System.out.println("Error saving favorite: " + e.getMessage());
                }
            }
        }

        // Instantly redirect them to their Dashboard to see it!
        response.sendRedirect("buyerDashboard");
    }
}