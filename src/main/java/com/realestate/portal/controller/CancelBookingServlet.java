package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cancelBooking")
public class CancelBookingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String loggedUser   = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole   = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (loggedUser == null || !"BUYER".equalsIgnoreCase(loggedRole)) {
            response.sendRedirect("login");
            return;
        }

        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect("buyerDashboard?cancel=error");
            return;
        }

        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File   bookingsFile = new File(bookingsPath);

        List<String> updatedLines = new ArrayList<>();
        boolean      cancelled    = false;

        if (bookingsFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(bookingsFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Preserve blank lines and comment header
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        updatedLines.add(line);
                        continue;
                    }
                    String[] d = line.split("\\|", -1);
                    // Only the owning buyer can cancel; completed bookings cannot be cancelled
                    if (d.length >= 11
                            && d[0].equals(bookingId)
                            && d[4].equals(loggedUser)
                            && !"COMPLETED".equalsIgnoreCase(d[10])) {
                        d[10] = "CANCELLED";
                        updatedLines.add(String.join("|", d));
                        cancelled = true;
                    } else {
                        updatedLines.add(line);
                    }
                }
            }
        }

        if (cancelled) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    Files.newOutputStream(bookingsFile.toPath(),
                            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8))) {
                for (String l : updatedLines) pw.println(l);
            }
            response.sendRedirect("buyerDashboard?cancel=success");
        } else {
            response.sendRedirect("buyerDashboard?cancel=notfound");
        }
    }
}
