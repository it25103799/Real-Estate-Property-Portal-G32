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

@WebServlet("/adminDeleteReview")
public class AdminDeleteReviewServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // ADMIN ONLY
        if (session == null) {
            response.sendRedirect("login");
            return;
        }
        String role = (String) session.getAttribute("loggedRole");
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String reviewId = request.getParameter("reviewId");
        if (reviewId == null || reviewId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/adminDashboard");
            return;
        }

        String filePath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File file = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean deleted = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty() && line.startsWith(reviewId + ",")) {
                        deleted = true; // Skip this line (delete it)
                    } else {
                        lines.add(line);
                    }
                }
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                pw.println(line);
            }
        }

        if (deleted) {
            session.setAttribute("flashMessage", "✅ Review '" + reviewId + "' was successfully removed.");
            session.setAttribute("flashMessageType", "success");
        } else {
            session.setAttribute("flashMessage", "❌ Review '" + reviewId + "' not found.");
            session.setAttribute("flashMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }
}
