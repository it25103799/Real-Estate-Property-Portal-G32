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
        String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole = (session != null) ? (String) session.getAttribute("loggedRole") : null;

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
        File bookingsFile = new File(bookingsPath);

        List<String> updatedLines = new ArrayList<>();
        boolean cancelled = false;
        String[] bookingData = null; // Store booking data before deletion for notification

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
                        // Store booking data for notification before deletion
                        bookingData = d.clone();
                        // DO NOT add this line - effectively deleting it
                        cancelled = true;
                    } else {
                        updatedLines.add(line);
                    }
                }
            }
        }

        if (cancelled) {
            // Save updated bookings (without the cancelled booking)
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    Files.newOutputStream(bookingsFile.toPath(),
                            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8))) {
                for (String l : updatedLines) pw.println(l);
            }

            // ── Penalty + property restoration for rentable properties ────────
            String redirectParam = "cancel=success";
            if (bookingData != null) {
                String propertyId = bookingData[1];
                String returnDateStr = bookingData[9];
                String priceStr = "";

                // Look up the property to get its daily rate and check if rentable
                boolean isRentable = restoreRentalPropertyIfEligible(request, propertyId, priceStr);

                // Calculate penalty if cancellation is after return date
                if (isRentable && returnDateStr != null && !returnDateStr.isEmpty()) {
                    try {
                        java.time.LocalDate returnDate = java.time.LocalDate.parse(returnDateStr);
                        java.time.LocalDate today = java.time.LocalDate.now();
                        if (today.isAfter(returnDate)) {
                            // Fetch daily rate from properties.txt
                            double dailyRate = getPropertyDailyRate(request, propertyId);
                            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(returnDate, today);
                            double penalty = daysOverdue * dailyRate;
                            // Pass penalty info to redirect
                            redirectParam = "cancel=success&penaltyDays=" + daysOverdue
                                    + "&penaltyFee=" + String.format("%.2f", penalty)
                                    + "&penaltyRate=" + String.format("%.2f", dailyRate);
                        }
                    } catch (Exception e) {
                        System.err.println("Error computing penalty: " + e.getMessage());
                    }
                }

                // Create notifications for both buyer and seller
                createCancellationNotifications(request, bookingId, bookingData);
            }

            response.sendRedirect("buyerDashboard?" + redirectParam);
        } else {
            response.sendRedirect("buyerDashboard?cancel=notfound");
        }
    }

    private void createCancellationNotifications(HttpServletRequest request, String bookingId, String[] bookingData) {
        try {
            String sellerName = bookingData[3];      // seller name
            String propertyTitle = bookingData[2];   // property title
            String buyerName = bookingData[5];       // buyer name
            String returnDate = bookingData[9];      // return date

            String timestamp = String.valueOf(System.currentTimeMillis());
            String message = "🔔 Booking CANCELLED\n\n" +
                    "Property: " + propertyTitle + "\n" +
                    "Buyer: " + buyerName + "\n" +
                    "Seller: " + sellerName + "\n" +
                    "Return Date: " + returnDate + "\n\n" +
                    "This booking has been completely removed from the system.";

            // Encode message in Base64
            String encodedMessage = java.util.Base64.getEncoder()
                    .encodeToString(message.getBytes(StandardCharsets.UTF_8));

            // 1. Create notification for SELLER
            String messagesPath = getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv");
            File messagesFile = new File(messagesPath);

            String sellerThreadId = "BOOKING_CANCEL_SELLER_" + bookingId;

            // Append seller notification to messages file
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
                pw.println(sellerThreadId + "\t" + timestamp + "\tSYSTEM\tSystem Notification\t" + encodedMessage);
            }

            // Create seller thread entry
            String threadsPath = getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv");
            File threadsFile = new File(threadsPath);

            boolean sellerThreadExists = false;
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(threadsFile.toPath()), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split("\t", -1);
                        if (d.length >= 1 && d[0].equals(sellerThreadId)) {
                            sellerThreadExists = true;
                            break;
                        }
                    }
                }
            }

            if (!sellerThreadExists) {
                String sellerThreadData = sellerThreadId + "\t" + timestamp + "\t" + propertyTitle + "\t" +
                        "System" + "\t" + sellerName + "\t" + buyerName + "\t" +
                        "SELLER" + "\t" + timestamp + "\tPENDING";

                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(threadsFile, true), StandardCharsets.UTF_8))) {
                    pw.println(sellerThreadData);
                }
            }

            // 2. Create notification for BUYER
            String buyerThreadId = "BOOKING_CANCEL_BUYER_" + bookingId;

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
                pw.println(buyerThreadId + "\t" + timestamp + "\tSYSTEM\tSystem Notification\t" + encodedMessage);
            }

            // Create buyer thread entry
            boolean buyerThreadExists = false;
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(threadsFile.toPath()), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split("\t", -1);
                        if (d.length >= 1 && d[0].equals(buyerThreadId)) {
                            buyerThreadExists = true;
                            break;
                        }
                    }
                }
            }

            if (!buyerThreadExists) {
                String buyerThreadData = buyerThreadId + "\t" + timestamp + "\t" + propertyTitle + "\t" +
                        "System" + "\t" + buyerName + "\t" + buyerName + "\t" +
                        "BUYER" + "\t" + timestamp + "\tPENDING";

                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(threadsFile, true), StandardCharsets.UTF_8))) {
                    pw.println(buyerThreadData);
                }
            }

        } catch (Exception e) {
            System.err.println("Error creating cancellation notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * If the cancelled property was originally "For Rent" (now "Sold" due to the booking),
     * restore it back to "For Rent" status so it becomes available again.
     * Returns true if the property is/was a rental property.
     */
    private boolean restoreRentalPropertyIfEligible(HttpServletRequest request, String propertyId, String unused) {
        boolean isRental = false;
        try {
            String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
            File propFile = new File(propPath);
            if (!propFile.exists()) return false;

            List<String> lines = new ArrayList<>();

            // Check bookings.txt: if ANY OTHER active (non-cancelled, non-completed) booking
            // still references this property, do NOT restore yet.
            String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
            File bookingsFile = new File(bookingsPath);
            if (bookingsFile.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(Files.newInputStream(bookingsFile.toPath()),
                                java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                        String[] d = line.split("\\|", -1);
                        if (d.length >= 11 && d[1].equals(propertyId)
                                && !"COMPLETED".equalsIgnoreCase(d[10])
                                && !"CANCELLED".equalsIgnoreCase(d[10])) {
                            // Another active booking exists — do not restore
                            return true; // still a rental, but skip restore
                        }
                    }
                }
            }

            // Restore the property to "For Rent"
            boolean updated = false;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(Files.newInputStream(propFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",", -1);
                    if (d.length >= 6 && d[0].trim().equals(propertyId)) {
                        // We restore only if originally a rental (check sold_properties.txt
                        // does NOT have a permanent-sale entry for this property)
                        boolean hasPermanentSale = checkPermanentSale(request, propertyId);
                        if (!hasPermanentSale && "sold".equalsIgnoreCase(d[5].trim())) {
                            d[5] = "For Rent";
                            lines.add(String.join(",", d));
                            updated = true;
                            isRental = true;
                        } else {
                            lines.add(line);
                            isRental = hasPermanentSale ? false : true;
                        }
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (updated) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(
                        new java.io.OutputStreamWriter(
                                Files.newOutputStream(propFile.toPath(),
                                        java.nio.file.StandardOpenOption.WRITE,
                                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING),
                                java.nio.charset.StandardCharsets.UTF_8))) {
                    for (String l : lines) pw.println(l);
                }
                System.out.println("🟢 Rental property [" + propertyId + "] restored to 'For Rent' after cancellation.");
            }
        } catch (Exception e) {
            System.err.println("Error restoring rental property: " + e.getMessage());
        }
        return isRental;
    }

    /**
     * Returns true if this property has a permanent sold record in sold_properties.txt
     * (i.e., it was sold outright by the seller, not just rented out temporarily).
     */
    private boolean checkPermanentSale(HttpServletRequest request, String propertyId) {
        try {
            String soldPath = getServletContext().getRealPath("/WEB-INF/sold_properties.txt");
            File soldFile = new File(soldPath);
            if (!soldFile.exists()) return false;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(Files.newInputStream(soldFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("|" + propertyId + "|")) return true;
                }
            }
        } catch (Exception e) { /* ignore */ }
        return false;
    }

    /**
     * Fetches the daily rate (price field) for a property from properties.txt.
     */
    private double getPropertyDailyRate(HttpServletRequest request, String propertyId) {
        try {
            String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
            File propFile = new File(propPath);
            if (!propFile.exists()) return 0.0;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(Files.newInputStream(propFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",", -1);
                    if (d.length >= 3 && d[0].trim().equals(propertyId)) {
                        return Double.parseDouble(d[2].trim());
                    }
                }
            }
        } catch (Exception e) { /* ignore */ }
        return 0.0;
    }
}

