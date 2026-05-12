package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/deleteCompletedBooking")
public class DeleteCompletedBookingServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");
        String role = (String) session.getAttribute("loggedRole");

        if (loggedUser == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        String bookingId = request.getParameter("bookingId");
        
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect("sellerDashboard?delete=error");
            return;
        }

        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File bookingsFile = new File(bookingsPath);

        List<String> updatedLines = new ArrayList<>();
        boolean deleted = false;

        if (bookingsFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(Paths.get(bookingsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        updatedLines.add(line);
                        continue;
                    }
                    String[] d = line.split("\\|", -1);
                    // Only delete if it matches bookingId, belongs to this seller, and is COMPLETED
                    if (d.length >= 11 && d[0].equals(bookingId) && d[3].equals(loggedUser) && "COMPLETED".equalsIgnoreCase(d[10])) {
                        deleted = true;
                        // Skip this line (don't add to updatedLines)
                        System.out.println("✅ Deleted completed booking: " + bookingId);
                    } else {
                        updatedLines.add(line);
                    }
                }
            }
        }

        if (deleted) {
            try (PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(bookingsFile.toPath(),
                                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                            StandardCharsets.UTF_8))) {
                for (String l : updatedLines) {
                    pw.println(l);
                }
            }
            response.sendRedirect("sellerDashboard?delete=success");
        } else {
            response.sendRedirect("sellerDashboard?delete=error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
