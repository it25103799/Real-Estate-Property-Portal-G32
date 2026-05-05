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

        // Basic validation
        if (propertyId == null || propertyId.trim().isEmpty() ||
            returnDateStr == null || returnDateStr.trim().isEmpty()) {
            response.sendRedirect("properties?error=missing_fields");
            return;
        }

        // Prevent double-booking: check if this buyer already has an active booking for same property
        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File bookingsFile   = new File(bookingsPath);
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
                        if (existPropId.equals(propertyId) &&
                            existBuyer.equals(loggedUser) &&
                            !"COMPLETED".equalsIgnoreCase(existStatus) &&
                            !"CANCELLED".equalsIgnoreCase(existStatus)) {
                            response.sendRedirect("properties?booking=duplicate");
                            return;
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
            rw.println("  Penalty Rate  : $100.00 / overdue day");
            rw.println();
        }
        // ─────────────────────────────────────────────────────────────────────

        // Redirect to buyer dashboard with success flag
        response.sendRedirect("buyerDashboard?booking=success");
    }

    private String sanitise(String s) {
        return s == null ? "" : s.replace("|", "-");
    }
}
