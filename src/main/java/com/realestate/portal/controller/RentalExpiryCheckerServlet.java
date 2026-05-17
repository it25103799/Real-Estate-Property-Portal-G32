package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RentalExpiryCheckerServlet
 *
 * Triggered on every page load via a lightweight GET request from the buyer/seller
 * dashboard (or can be called on any request). It scans bookings.txt for any
 * RESERVED rentals whose returnDate has passed, and automatically:
 *   1. Marks the booking status as COMPLETED (rental period over).
 *   2. Restores the property status back to "For Rent" so it is available again.
 *
 * The penalty fee for late cancellation is handled separately in CancelBookingServlet.
 *
 * URL pattern: /checkRentalExpiry  (called silently by dashboards)
 */
@WebServlet("/checkRentalExpiry")
public class RentalExpiryCheckerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        runExpiryCheck(request);
        // Silent response — caller does not need output
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Main expiry-check logic. Can also be called statically from other servlets
     * by instantiating this servlet or copying the logic.
     */
    public void runExpiryCheck(HttpServletRequest request) {
        try {
            String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
            String propPath     = getServletContext().getRealPath("/WEB-INF/properties.txt");
            String soldPath     = getServletContext().getRealPath("/WEB-INF/sold_properties.txt");

            File bookingsFile = new File(bookingsPath);
            File propFile     = new File(propPath);
            if (!bookingsFile.exists() || !propFile.exists()) return;

            LocalDate today = LocalDate.now();

            // ── Pass 1: Find expired rental bookings ─────────────────────────
            List<String> updatedBookings = new ArrayList<>();
            // propertyIds whose rental period has expired AND have no remaining active booking
            Set<String> expiredRentalIds = new HashSet<>();
            // propertyIds that still have an active booking (must NOT restore)
            Set<String> stillActiveIds  = new HashSet<>();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(bookingsFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        updatedBookings.add(line);
                        continue;
                    }
                    String[] d = line.split("\\|", -1);
                    // Format: bookingId|propertyId|title|seller|buyerUser|buyerName|email|phone|bookingDate|returnDate|status
                    if (d.length < 11) {
                        updatedBookings.add(line);
                        continue;
                    }

                    String propId  = d[1];
                    String status  = d[10];
                    String retDate = d[9];

                    if ("RESERVED".equalsIgnoreCase(status) && retDate != null && !retDate.isEmpty()) {
                        try {
                            LocalDate returnDate = LocalDate.parse(retDate.trim());
                            if (!today.isBefore(returnDate)) {
                                // Rental period has ended — auto-complete this booking
                                d[10] = "COMPLETED";
                                updatedBookings.add(String.join("|", d));
                                expiredRentalIds.add(propId);
                                System.out.println("⏰ Rental period expired for booking [" + d[0] + "] property [" + propId + "]. Auto-completed.");
                                continue;
                            } else {
                                // Still active
                                stillActiveIds.add(propId);
                            }
                        } catch (Exception dateEx) {
                            // Date parse failure — leave untouched
                        }
                    } else if (!"COMPLETED".equalsIgnoreCase(status) && !"CANCELLED".equalsIgnoreCase(status)) {
                        stillActiveIds.add(propId);
                    }
                    updatedBookings.add(line);
                }
            }

            // If nothing expired, bail early
            if (expiredRentalIds.isEmpty()) return;

            // Save updated bookings
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    Files.newOutputStream(bookingsFile.toPath(),
                            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8))) {
                for (String l : updatedBookings) pw.println(l);
            }

            // ── Pass 2: Restore eligible properties to "For Rent" ────────────
            // A property is restored ONLY if:
            //   (a) its booking expired, AND
            //   (b) it has no remaining active booking, AND
            //   (c) it has no permanent-sale record in sold_properties.txt
            Set<String> permanentlySold = loadPermanentlySoldIds(soldPath);

            // Remove IDs that still have an active booking
            expiredRentalIds.removeAll(stillActiveIds);
            // Remove IDs that were sold outright (not rentals)
            expiredRentalIds.removeAll(permanentlySold);

            if (expiredRentalIds.isEmpty()) return;

            List<String> updatedProps = new ArrayList<>();
            boolean propsChanged = false;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(propFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",", -1);
                    if (d.length >= 6 && expiredRentalIds.contains(d[0].trim())
                            && "sold".equalsIgnoreCase(d[5].trim())) {
                        // Restore to "For Rent"
                        d[5] = "For Rent";
                        updatedProps.add(String.join(",", d));
                        propsChanged = true;
                        System.out.println("🟢 Property [" + d[0] + "] restored to 'For Rent' after rental expiry.");
                    } else {
                        updatedProps.add(line);
                    }
                }
            }

            if (propsChanged) {
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                        Files.newOutputStream(propFile.toPath(),
                                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                        StandardCharsets.UTF_8))) {
                    for (String l : updatedProps) pw.println(l);
                }
            }

        } catch (Exception e) {
            System.err.println("RentalExpiryCheckerServlet error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the set of propertyIds that have a permanent sale record
     * (written by MarkAsSoldServlet) in sold_properties.txt.
     */
    private Set<String> loadPermanentlySoldIds(String soldPath) {
        Set<String> ids = new HashSet<>();
        try {
            File soldFile = new File(soldPath);
            if (!soldFile.exists()) return ids;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(soldFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Format: timestamp|propertyId|title|price|location|imageUrl
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 2) {
                        ids.add(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) { /* ignore */ }
        return ids;
    }
}
