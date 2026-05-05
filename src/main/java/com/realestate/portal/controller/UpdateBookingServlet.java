package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/updateBooking")
public class UpdateBookingServlet extends HttpServlet {

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

        String bookingId   = request.getParameter("bookingId");
        String returnDate  = request.getParameter("returnDate");

        if (bookingId == null || bookingId.trim().isEmpty() || 
            returnDate == null || returnDate.trim().isEmpty()) {
            response.sendRedirect("buyerDashboard?update=error");
            return;
        }

        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File   bookingsFile = new File(bookingsPath);

        List<String> updatedLines = new ArrayList<>();
        boolean      updated      = false;
        String[] oldBookingData = null;

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
                    if (d.length >= 11
                            && d[0].equals(bookingId)
                            && d[4].equals(loggedUser)
                            && !"COMPLETED".equalsIgnoreCase(d[10])
                            && !"CANCELLED".equalsIgnoreCase(d[10])) {
                        
                        // Store old data for notification
                        oldBookingData = d.clone();
                        
                        // Update the return date (index 9)
                        d[9] = returnDate;
                        
                        // If status was OVERDUE, reset it back to RESERVED since we're updating
                        if ("OVERDUE".equalsIgnoreCase(d[10])) {
                            d[10] = "RESERVED";
                        }
                        
                        updatedLines.add(String.join("|", d));
                        updated = true;
                    } else {
                        updatedLines.add(line);
                    }
                }
            }
        }

        if (updated) {
            // Save updated bookings
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    Files.newOutputStream(bookingsFile.toPath(),
                            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8))) {
                for (String l : updatedLines) pw.println(l);
            }

            // Create notification for seller about the update
            if (oldBookingData != null) {
                createSellerNotification(request, bookingId, oldBookingData[9], returnDate);
            }

            response.sendRedirect("buyerDashboard?update=success");
        } else {
            response.sendRedirect("buyerDashboard?update=notfound");
        }
    }

    private void createSellerNotification(HttpServletRequest request, String bookingId, 
                                         String oldReturnDate, String newReturnDate) {
        try {
            String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
            File bookingsFile = new File(bookingsPath);

            String sellerName = null;
            String propertyTitle = null;
            String buyerName = null;

            // Read booking details to get seller and property info
            if (bookingsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(bookingsFile.toPath()), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                        String[] d = line.split("\\|", -1);
                        if (d.length >= 11 && d[0].equals(bookingId)) {
                            sellerName = d[3];      // seller name
                            propertyTitle = d[2];   // property title
                            buyerName = d[5];       // buyer name
                            break;
                        }
                    }
                }
            }

            if (sellerName == null || propertyTitle == null || buyerName == null) {
                System.err.println("Could not find booking details for notification: " + bookingId);
                return;
            }

            // Write notification to inquiry_messages.tsv
            String messagesPath = getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv");
            File messagesFile = new File(messagesPath);

            String threadId = "BOOKING_UPDATE_" + bookingId;
            String timestamp = String.valueOf(System.currentTimeMillis());
            String senderRole = "SYSTEM";
            String senderName = "System Notification";
            String message = "Booking updated by " + buyerName + " for property: " + propertyTitle + 
                           ". Return date changed from " + oldReturnDate + " to " + newReturnDate;

            // Encode message in Base64
            String encodedMessage = java.util.Base64.getEncoder()
                    .encodeToString(message.getBytes(StandardCharsets.UTF_8));

            // Append to messages file
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(messagesFile, true), StandardCharsets.UTF_8))) {
                pw.println(threadId + "\t" + timestamp + "\t" + senderRole + "\t" + senderName + "\t" + encodedMessage);
            }

            // Also update/create inquiry_threads.tsv entry
            String threadsPath = getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv");
            File threadsFile = new File(threadsPath);

            // Check if thread already exists
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

            // Create new thread if it doesn't exist
            if (!threadExists) {
                String threadData = threadId + "\t" + timestamp + "\t" + propertyTitle + "\t" +
                                   sellerName + "\t" + buyerName + "\t" + buyerName + "\t" +
                                   "SELLER" + "\t" + timestamp + "\t" + "PENDING";

                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(threadsFile, true), StandardCharsets.UTF_8))) {
                    pw.println(threadData);
                }
            }

        } catch (Exception e) {
            System.err.println("Error creating seller notification for booking update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
