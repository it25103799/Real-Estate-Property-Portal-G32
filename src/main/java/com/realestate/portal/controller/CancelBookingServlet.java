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

            // Create notifications for both buyer and seller
            if (bookingData != null) {
                createCancellationNotifications(request, bookingId, bookingData);
            }

            response.sendRedirect("buyerDashboard?cancel=success");
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
}
