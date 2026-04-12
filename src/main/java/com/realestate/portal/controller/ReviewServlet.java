package com.realestate.portal.controller;

import com.realestate.portal.model.*;
import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/reviews")
public class ReviewServlet extends HttpServlet {

    // READ: Load all reviews from the file
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Review> reviewList = new ArrayList<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 6) {
                        // Polymorphism: Creating specific objects based on type
                        if (data[5].equalsIgnoreCase("VERIFIED")) {
                            reviewList.add(new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        } else {
                            reviewList.add(new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        request.setAttribute("allReviews", reviewList);
        // Pass control to PropertyServlet so properties ALSO load
        request.getRequestDispatcher("/properties").forward(request, response);
    }

    // CREATE, UPDATE, DELETE: Handled via the "action" parameter
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String filePath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        String propId = request.getParameter("propertyId");

        if ("delete".equals(action)) {
            // DELETE LOGIC
            String idToDelete = request.getParameter("reviewId");
            processFile(filePath, idToDelete, null);
        } else if ("update".equals(action)) {
            // UPDATE LOGIC
            String idToUpdate = request.getParameter("reviewId");
            String newComment = request.getParameter("comment");
            processFile(filePath, idToUpdate, newComment);
        } else {
            // CREATE LOGIC (Standard Submit)
            String name = request.getParameter("buyerName");
            String rating = request.getParameter("rating");
            String comment = request.getParameter("comment");
            String reviewId = "REV" + System.currentTimeMillis();

            // 🔥 The "\n" ensures the new review always starts on a fresh line
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8")))) {
                out.println(); // Add an empty line first
                out.print(reviewId + "," + propId + "," + name + "," + rating + "," + comment + ",PUBLIC");
            } catch (Exception e) { e.printStackTrace(); }
        }
        response.sendRedirect("reviews?viewId=" + propId);
    }

    // Helper method to rewrite the file for Delete and Update
    private void processFile(String path, String targetId, String updatedMsg) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(targetId)) {
                    if (updatedMsg != null) { // Update case
                        String[] parts = line.split(",");
                        parts[4] = updatedMsg; // Change only the comment
                        lines.add(String.join(",", parts));
                    }
                    // If delete case, we simply don't add the line back!
                } else { lines.add(line); }
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (String l : lines) pw.println(l);
        }
    }
}