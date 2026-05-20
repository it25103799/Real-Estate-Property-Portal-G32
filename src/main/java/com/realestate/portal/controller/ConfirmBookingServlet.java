package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/confirmBooking")
public class ConfirmBookingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session  = request.getSession(false);
        String loggedUser    = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole    = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (loggedUser == null || !"SELLER".equalsIgnoreCase(loggedRole)) {
            response.sendRedirect("login");
            return;
        }

        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect("sellerDashboard?error=missing_id");
            return;
        }

        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File   bookingsFile = new File(bookingsPath);

        List<String> updatedLines     = new ArrayList<>();
        String       confirmedPropId  = null;
        boolean      found            = false;

        if (bookingsFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(bookingsFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        updatedLines.add(line);
                        continue;
                    }
                    String[] d = line.split("\\|", -1);
                    // Only confirm RESERVED bookings belonging to this seller
                    if (d.length >= 11 && d[0].equals(bookingId) && d[3].equals(loggedUser)
                            && "RESERVED".equalsIgnoreCase(d[10])) {
                        d[10]           = "CONFIRMED";
                        confirmedPropId = d[1].trim();
                        updatedLines.add(String.join("|", d));
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }
            }
        }

        if (found) {
            // Persist updated bookings
            try (PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(bookingsFile.toPath(),
                                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                            StandardCharsets.UTF_8))) {
                for (String l : updatedLines) pw.println(l);
            }

            // Ensure the property is marked as Sold (it may already be if it was "For Rent")
            if (confirmedPropId != null) {
                ensurePropertySold(confirmedPropId);
            }
        }

        response.sendRedirect("sellerDashboard?confirmed=success");
    }

    /**
     * Updates properties.txt: if the property is NOT already Sold, mark it as Sold.
     * This handles cases where a "For Sale" or manually-listed property needs confirmation.
     */
    private void ensurePropertySold(String propertyId) {
        try {
            String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
            File   propFile = new File(propPath);
            if (!propFile.exists()) return;

            List<String> lines   = new ArrayList<>();
            boolean      updated = false;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(propFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",", -1);
                    if (d.length >= 6 && d[0].trim().equals(propertyId)
                            && !"Sold".equalsIgnoreCase(d[5].trim())) {
                        d[5] = "Sold";
                        lines.add(String.join(",", d));
                        updated = true;
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (updated) {
                try (PrintWriter pw = new PrintWriter(
                        new OutputStreamWriter(
                                Files.newOutputStream(propFile.toPath(),
                                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                                StandardCharsets.UTF_8))) {
                    for (String l : lines) pw.println(l);
                }
                System.out.println("✅ Property [" + propertyId + "] confirmed and marked as Sold.");
            }
        } catch (Exception e) {
            System.err.println("ConfirmBookingServlet: error marking property as Sold: " + e.getMessage());
        }
    }
}
