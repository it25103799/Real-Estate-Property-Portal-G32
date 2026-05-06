package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet("/bookProperty")
public class BookPropertyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String loggedUser   = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole   = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (loggedUser == null || !"BUYER".equalsIgnoreCase(loggedRole)) {
            response.sendRedirect("login");
            return;
        }

        String propertyId    = request.getParameter("propertyId");
        String propertyTitle = request.getParameter("propertyTitle");
        String sellerName    = request.getParameter("sellerName");
        String buyerName     = request.getParameter("buyerName");
        String buyerEmail    = request.getParameter("buyerEmail");
        String buyerPhone    = request.getParameter("buyerPhone");
        String returnDateStr = request.getParameter("returnDate");   // yyyy-MM-dd
        String propertyPriceStr = request.getParameter("propertyPrice"); // daily rate = penalty rate

        // Basic validation
        if (propertyId == null || propertyId.trim().isEmpty() ||
                returnDateStr == null || returnDateStr.trim().isEmpty()) {
            response.sendRedirect("properties?error=missing_fields");
            return;
        }

        // Prevent double-booking: check if this buyer already has an active booking for same property
        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File bookingsFile   = new File(bookingsPath);

        // ENHANCEMENT 1: Check for date range conflicts
        if (bookingsFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(bookingsFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                    String[] d = line.split("\\|", -1);
                    if (d.length >= 11) {
                        String existPropId  = d[1];
                        String existBuyer   = d[4];
                        String existStatus  = d[10];
                        String existReturnDate = d[9];

                        // Check for duplicate active booking
                        if (existPropId.equals(propertyId) &&
                                existBuyer.equals(loggedUser) &&
                                !"COMPLETED".equalsIgnoreCase(existStatus)) {
                            response.sendRedirect("buyerDashboard?booking=duplicate");
                            return;
                        }

                        // ENHANCEMENT: Check if property is already booked by ANYONE for overlapping dates
                        if (existPropId.equals(propertyId) &&
                                !"COMPLETED".equalsIgnoreCase(existStatus) &&
                                !"CANCELLED".equalsIgnoreCase(existStatus)) {
                            // Simple check: if return date is after today, property is still booked
                            try {
                                java.time.LocalDate existingReturn = java.time.LocalDate.parse(existReturnDate);
                                java.time.LocalDate requestedReturn = java.time.LocalDate.parse(returnDateStr);
                                java.time.LocalDate today = java.time.LocalDate.now();

                                if (existingReturn.isAfter(today)) {
                                    response.sendRedirect("buyerDashboard?booking=unavailable");
                                    return;
                                }
                            } catch (Exception e) {
                                // If date parsing fails, continue
                            }
                        }
                    }
                }
            }
        }

        // Generate booking ID
        String bookingId   = "BK_" + System.currentTimeMillis();
        String bookingDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Sanitise pipe characters from user input to protect the delimiter
        propertyTitle = sanitise(propertyTitle);
        sellerName    = sanitise(sellerName);
        buyerName     = sanitise(buyerName);
        buyerEmail    = sanitise(buyerEmail);
        buyerPhone    = sanitise(buyerPhone);

        String record = String.join("|",
                bookingId,
                propertyId,
                propertyTitle,
                sellerName,
                loggedUser,      // buyerUsername (account key)
                buyerName,
                buyerEmail,
                buyerPhone,
                bookingDate,
                returnDateStr,
                "RESERVED"       // initial status
        );

        // Append to bookings.txt (create if absent)
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(bookingsFile, true), StandardCharsets.UTF_8))) {
            out.println(record);
        }

        // ENHANCEMENT 2: Create notification for seller about new booking
        createSellerBookingNotification(request, bookingId, propertyTitle, sellerName, loggedUser, buyerName, buyerEmail, buyerPhone, returnDateStr);

        // ── Write human-readable booking report ──────────────────────────────
        String reportPath = getServletContext().getRealPath("/WEB-INF/booking_report.txt");
        File   reportFile = new File(reportPath);
        try (PrintWriter rw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(reportFile, true), StandardCharsets.UTF_8))) {
            rw.println("=========================================================");
            rw.println("  NEW BOOKING — " + bookingDate);
            rw.println("=========================================================");
            rw.println("  Booking ID    : " + bookingId);
            rw.println("  Property      : " + propertyTitle + "  [" + propertyId + "]");
            rw.println("  Seller        : " + sellerName);
            rw.println("  Buyer Account : " + loggedUser);
            rw.println("  Buyer Name    : " + buyerName);
            rw.println("  Buyer Email   : " + buyerEmail);
            rw.println("  Buyer Phone   : " + (buyerPhone.isEmpty() ? "N/A" : buyerPhone));
            rw.println("  Booked On     : " + bookingDate);
            rw.println("  Return Date   : " + returnDateStr);
            rw.println("  Status        : RESERVED");
            rw.println("  Penalty Rate  : $" + (propertyPriceStr != null && !propertyPriceStr.isEmpty() ? String.format("%.2f", Double.parseDouble(propertyPriceStr)) : "100.00") + " / overdue day (= daily rental fee)");
            rw.println();
        }
        // ─────────────────────────────────────────────────────────────────────

        // Redirect to buyer dashboard with success flag
        response.sendRedirect("buyerDashboard?booking=success");
    }

    private String sanitise(String s) {
        return s == null ? "" : s.replace("|", "-");
    }

    // ENHANCEMENT 2: Create booking notification for seller
    private void createSellerBookingNotification(HttpServletRequest request, String bookingId,
                                                 String propertyTitle, String sellerName,
                                                 String buyerUsername, String buyerName,
                                                 String buyerEmail, String buyerPhone,
                                                 String returnDate) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String message = "🔔 New Booking Received!\n\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "Property: " + propertyTitle + "\n" +
                    "Buyer: " + buyerName + "\n" +
                    "Email: " + buyerEmail + "\n" +
                    "Phone: " + (buyerPhone.isEmpty() ? "N/A" : buyerPhone) + "\n" +
                    "Booked On: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n" +
                    "Return Date: " + returnDate + "\n\n" +
                    "Please prepare the property for the tenant.";

            // Encode message in Base64
            String encodedMessage = java.util.Base64.getEncoder()
                    .encodeToString(message.getBytes(StandardCharsets.UTF_8));

            // Write notification to inquiry_messages.tsv
            String messagesPath = getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv");
            File messagesFile = new File(messagesPath);

            String threadId = "BOOKING_NEW_" + bookingId;

            // Append to messages file
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
                pw.println(threadId + "\t" + timestamp + "\tSYSTEM\tSystem Notification\t" + encodedMessage);
            }

            // Create thread entry
            String threadsPath = getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv");
            File threadsFile = new File(threadsPath);

            boolean threadExists = false;
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(threadsFile.toPath()), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split("\t", -1);
                        if (d.length >= 1 && d[0].equals(threadId)) {
                            threadExists = true;
                            break;
                        }
                    }
                }
            }

            if (!threadExists) {
                String threadData = threadId + "\t" + timestamp + "\t" + propertyTitle + "\t" +
                        "System" + "\t" + sellerName + "\t" + buyerName + "\t" +
                        "SELLER" + "\t" + timestamp + "\tPENDING";

                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(threadsFile, true), StandardCharsets.UTF_8))) {
                    pw.println(threadData);
                }
            }

            System.out.println("✅ Booking notification created for seller: " + sellerName);
        } catch (Exception e) {
            System.err.println("Error creating booking notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
