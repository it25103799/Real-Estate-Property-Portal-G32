package com.realestate.portal.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/deleteReview")
public class DeleteReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session guard – only logged-in SELLERs may delete reviews
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }
        String loggedUser = (String) session.getAttribute("loggedUser");
        String role       = (String) session.getAttribute("loggedRole");
        if (loggedUser == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String reviewId = request.getParameter("reviewId");
        if (reviewId == null || reviewId.trim().isEmpty()) {
            // Nothing to delete – just go back to the reviews section
            response.sendRedirect("sellerDashboard?scrollTo=reviews");
            return;
        }

        String filePath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File file = new File(filePath);
        List<String> lines = new ArrayList<>();

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Keep every line whose first field does NOT match the reviewId
                    if (!line.startsWith(reviewId + ",")) {
                        lines.add(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect back to the seller dashboard and auto-scroll to the reviews card
        response.sendRedirect("sellerDashboard?scrollTo=reviews");
    }
}
