package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
import com.realestate.portal.model.InquiryMessage;
import com.realestate.portal.model.InquiryThread;
import com.realestate.portal.model.PublicReview;
import com.realestate.portal.model.Review;
import com.realestate.portal.model.VerifiedReview;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/sellerDashboard")
public class SellerDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");
        String role = (String) session.getAttribute("loggedRole");

        if (loggedUser == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("login");
            return;
        }

        List<Property> myProperties = new ArrayList<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split(",");
                    if (data.length >= 7 && data[6].equals(loggedUser)) {
                        Property p = null;
                        try {
                            if (data.length == 11) { // New format with description
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], Integer.parseInt(data[8]), Integer.parseInt(data[9]), data[10]);
                            } else if (data.length == 10) { // Format without description
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], Integer.parseInt(data[8]), Integer.parseInt(data[9]), "");
                            } else if (data.length == 8) { // Old format
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], 0, 0, "");
                            }
                            if (p != null) {
                                myProperties.add(p);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping malformed property line: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading properties: " + e.getMessage());
            }
        }

        // --- PROPERTY REVIEWS (only for this seller's listings) ---
        Set<String> myPropIds = new HashSet<>();
        for (Property p : myProperties) myPropIds.add(p.getId());

        Map<String, List<Review>> reviewsByProperty = new HashMap<>();
        for (String propId : myPropIds) reviewsByProperty.put(propId, new ArrayList<>());

        String revPath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File revFile = new File(revPath);
        if (revFile.exists() && !myPropIds.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(revFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split(",");
                    if (data.length == 6 && myPropIds.contains(data[1])) {
                        Review r;
                        if ("VERIFIED".equalsIgnoreCase(data[5])) {
                            r = new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]);
                        } else {
                            r = new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]);
                        }
                        reviewsByProperty.computeIfAbsent(data[1], k -> new ArrayList<>()).add(r);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading reviews: " + e.getMessage());
            }
        }

        // --- INQUIRY INBOX (seller side) ---
        List<InquiryThread> sellerThreads = new ArrayList<>();
        Map<String, InquiryThread> byId = new HashMap<>();

        File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
        if (threadsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split("\t", -1);
                    // threadId, propertyId, propertyTitle, sellerName, buyerAccount, buyerName, buyerEmail, buyerPhone, createdDate, status
                    if (data.length >= 10 && loggedUser.equals(data[3])) {
                        InquiryThread t = new InquiryThread(
                                data[0], data[1], data[2], data[3],
                                data[4], data[5], data[6], data[7],
                                data[8], data[9]
                        );
                        sellerThreads.add(t);
                        byId.put(t.getId(), t);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading inquiry threads: " + e.getMessage());
            }
        }

        File messagesFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv"));
        if (messagesFile.exists() && !byId.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(messagesFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split("\t", -1);
                    // threadId, timestamp, senderRole, senderName, contentB64
                    if (data.length >= 5) {
                        InquiryThread t = byId.get(data[0]);
                        if (t == null) continue;

                        String content = "";
                        try {
                            byte[] decoded = Base64.getDecoder().decode(data[4]);
                            content = new String(decoded, StandardCharsets.UTF_8);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Skipping malformed Base64 message: " + data[4]);
                        }

                        InquiryMessage msg = new InquiryMessage(data[0], data[1], data[2], data[3], content);
                        t.getMessages().add(msg);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading inquiry messages: " + e.getMessage());
            }
        }

        // Notifications (unread messages for bell bubble)
        List<Map<String, String>> allNotifications = new ArrayList<>();
        File readsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_reads.tsv"));

        // Load read timestamps for checking notification read status
        Map<String, String> lastRead = new HashMap<>();
        if (readsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(readsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] r = line.split("\t", -1);
                    if (r.length >= 3) lastRead.put(r[0] + "|" + r[1], r[2]);
                }
            } catch (Exception ignored) {}
        }
        if (threadsFile.exists() && messagesFile.exists()) {
            Map<String, String[]> threads = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split("\t", -1);
                    if (data.length >= 10) threads.put(data[0], data);
                }
            } catch (Exception ignored) {}

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(messagesFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] msg = line.split("\t", -1);
                    if (msg.length < 5) continue;
                    String[] t = threads.get(msg[0]);
                    if (t == null) continue;

                    String senderRole = msg[2];
                    String receiver;
                    String sender;
                    if ("SELLER".equalsIgnoreCase(senderRole)) {
                        receiver = t[4]; // buyerAccount
                        sender = t[3];   // seller
                    } else {
                        receiver = t[3]; // seller
                        sender = t[5];   // buyer name
                    }
                    if (!loggedUser.equals(receiver)) continue;

                    String ts = msg[1];
                    String lr = lastRead.get(loggedUser + "|" + msg[0]);
                    if (lr != null && ts != null && ts.compareTo(lr) <= 0) continue;

                    String content = "";
                    try {
                        content = new String(Base64.getDecoder().decode(msg[4]), StandardCharsets.UTF_8);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping malformed Base64 message: " + msg[4]);
                    }

                    Map<String, String> n = new HashMap<>();
                    n.put("sender", sender);
                    n.put("receiver", receiver);
                    n.put("content", content);
                    n.put("propTitle", t[2]);
                    n.put("type", "CHAT");
                    n.put("threadId", t[0]);
                    allNotifications.add(n);
                }
            } catch (Exception ignored) {}
        }

        // ─── BOOKINGS MODULE ─────────────────────────────────────────────────────
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.LocalDate today = java.time.LocalDate.now();

        // Build a map of propertyId -> dailyPrice from properties.txt
        java.util.Map<String, Double> propertyPriceMap = new java.util.HashMap<>();
        File propFileForPenalty = new File(getServletContext().getRealPath("/WEB-INF/properties.txt"));
        if (propFileForPenalty.exists()) {
            try (BufferedReader pr = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(Paths.get(propFileForPenalty.getAbsolutePath())),
                            StandardCharsets.UTF_8))) {
                String pl;
                while ((pl = pr.readLine()) != null) {
                    if (pl.trim().isEmpty() || pl.trim().startsWith("#")) continue;
                    String[] pd = pl.split(",", -1);
                    if (pd.length >= 3) {
                        try { propertyPriceMap.put(pd[0].trim(), Double.parseDouble(pd[2].trim())); }
                        catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }

        List<Map<String, String>> activeBookings    = new ArrayList<>();
        List<Map<String, String>> completedBookings = new ArrayList<>();
        Set<String> reservedPropIds = new HashSet<>();

        String bookingsPath = getServletContext().getRealPath("/WEB-INF/bookings.txt");
        File bookingsFile   = new File(bookingsPath);

        if (bookingsFile.exists()) {
            try (BufferedReader bkBr = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(Paths.get(bookingsFile.getAbsolutePath())),
                            StandardCharsets.UTF_8))) {
                String bkLine;
                while ((bkLine = bkBr.readLine()) != null) {
                    if (bkLine.trim().isEmpty()) continue;
                    if (bkLine.trim().startsWith("#")) continue;
                    String[] d = bkLine.split("\\|", -1);
                    if (d.length < 11) continue;
                    if (!loggedUser.equals(d[3])) continue;

                    Map<String, String> bk = new HashMap<>();
                    bk.put("bookingId",     d[0]);
                    bk.put("propertyId",    d[1]);
                    bk.put("propertyTitle", d[2]);
                    bk.put("sellerName",    d[3]);
                    bk.put("buyerUsername", d[4]);
                    bk.put("buyerName",     d[5]);
                    bk.put("buyerEmail",    d[6]);
                    bk.put("buyerPhone",    d[7]);
                    bk.put("bookingDate",   d[8]);
                    bk.put("returnDate",    d[9]);
                    bk.put("status",        d[10]);

                    // Use the property's own daily price as the penalty rate
                    double dailyRate = propertyPriceMap.getOrDefault(d[1].trim(), 100.0);
                    bk.put("dailyRate", String.format("%.2f", dailyRate));

                    double penalty = 0.0;
                    try {
                        java.time.LocalDate returnDate = java.time.LocalDate.parse(d[9], dtf);
                        if (!"COMPLETED".equalsIgnoreCase(d[10]) && today.isAfter(returnDate)) {
                            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(returnDate, today);
                            penalty = daysOverdue * dailyRate;
                            bk.put("status", "OVERDUE");
                            bk.put("daysOverdue", String.valueOf(daysOverdue));
                        }
                    } catch (Exception ignored) {}
                    bk.put("penaltyFee", String.format("%.2f", penalty));

                    // Get property status from properties.txt
                    String propertyStatus = "For Sale"; // Default
                    for (Property p : myProperties) {
                        if (p.getId().equals(d[1])) {
                            propertyStatus = p.getStatus();
                            break;
                        }
                    }
                    bk.put("propertyStatus", propertyStatus);

                    if ("COMPLETED".equalsIgnoreCase(d[10])) {
                        completedBookings.add(bk);
                    } else {
                        activeBookings.add(bk);
                        reservedPropIds.add(d[1]);
                    }

                    // Check for booking update notifications
                    String updateThreadId = "BOOKING_UPDATE_" + d[0];
                    String updateLr = lastRead.get(loggedUser + "|" + updateThreadId);
                    if (updateLr == null && !"COMPLETED".equalsIgnoreCase(d[10]) && !"CANCELLED".equalsIgnoreCase(d[10])) {
                        // This is an active booking that might have been updated
                        // We'll check if there's an update notification in the messages file
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading bookings: " + e.getMessage());
            }
        }

        int totalProperties = myProperties.size();
        int reservedCount   = 0;
        for (Property p : myProperties) {
            if (reservedPropIds.contains(p.getId())) reservedCount++;
        }
        int availableCount = totalProperties - reservedCount;
        int soldCount = 0;
        for (Property p : myProperties) {
            if ("Sold".equals(p.getStatus())) {
                soldCount++;
            }
        }

        // ── Write availability report to disk ─────────────────────────────────
        String reportPath = getServletContext().getRealPath("/WEB-INF/availability_report.txt");
        try (java.io.PrintWriter rw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(reportPath, false),   // overwrite each time
                        StandardCharsets.UTF_8))) {
            rw.println("=========================================================");
            rw.println("  NESTIQ — PROPERTY AVAILABILITY REPORT");
            rw.println("  Seller  : " + loggedUser);
            rw.println("  Date    : " + today.format(dtf));
            rw.println("=========================================================");
            rw.println("  Total Listed   : " + totalProperties);
            rw.println("  Available      : " + availableCount);
            rw.println("  Reserved       : " + reservedCount);
            rw.println();
            rw.println("----- ACTIVE RESERVATIONS (" + activeBookings.size() + ") -----");
            for (java.util.Map<String, String> bk : activeBookings) {
                rw.println("  [" + bk.get("status") + "] " + bk.get("propertyTitle")
                        + " | Buyer: " + bk.get("buyerName")
                        + " | Return: " + bk.get("returnDate")
                        + (bk.get("penaltyFee").equals("0.00") ? "" : " | Penalty: $" + bk.get("penaltyFee")));
            }
            rw.println();
            rw.println("----- COMPLETED TRANSACTIONS (" + completedBookings.size() + ") -----");
            for (java.util.Map<String, String> bk : completedBookings) {
                rw.println("  " + bk.get("propertyTitle")
                        + " | Buyer: " + bk.get("buyerName")
                        + " | Booked: " + bk.get("bookingDate")
                        + " | Returned: " + bk.get("returnDate"));
            }
            rw.println("=========================================================");
        } catch (Exception e) {
            System.err.println("Could not write availability report: " + e.getMessage());
        }
        // ─────────────────────────────────────────────────────────────────────

        request.setAttribute("totalProperties",   totalProperties);
        request.setAttribute("reservedCount",      reservedCount);
        request.setAttribute("availableCount",     availableCount);
        request.setAttribute("soldCount",          soldCount);
        request.setAttribute("activeBookings",     activeBookings);
        request.setAttribute("completedBookings",  completedBookings);
        // ─────────────────────────────────────────────────────────────────────────


        // ── ANNOUNCEMENTS as Notifications (unread only) ─────────────────
        Set<String> readAnnIds = loadReadAnnouncementIds(loggedUser);
        File annFile = new File(getServletContext().getRealPath("/WEB-INF/announcements.txt"));
        if (annFile.exists()) {
            try (java.io.BufferedReader annBr = new java.io.BufferedReader(new java.io.FileReader(annFile))) {
                String annLine;
                while ((annLine = annBr.readLine()) != null) {
                    if (annLine.trim().isEmpty()) continue;
                    String[] parts = annLine.split(",", 6);
                    if (parts.length == 6 && !readAnnIds.contains(parts[0].trim())) {
                        Map<String, String> n = new HashMap<>();
                        n.put("sender",    "System Administration");
                        n.put("receiver",  loggedUser);
                        n.put("title",     parts[1]);
                        n.put("content",   parts[2]);
                        n.put("priority",  parts[3]);
                        n.put("timestamp", parts[5]);
                        n.put("type",      "ANNOUNCEMENT");
                        n.put("threadId",  "");
                        allNotifications.add(n);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading announcements for seller: " + e.getMessage());
            }
        }

        request.setAttribute("myProperties", myProperties);
        request.setAttribute("reviewsByProperty", reviewsByProperty);
        request.setAttribute("sellerThreads", sellerThreads);
        request.setAttribute("allNotifications", allNotifications);
        request.getRequestDispatcher("/seller_dashboard.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


    // ── Load set of announcement IDs already read by this user ────────
    private Set<String> loadReadAnnouncementIds(String username) {
        Set<String> ids = new HashSet<>();
        try {
            String path = getServletContext().getRealPath("/WEB-INF/announcement_reads.txt");
            if (path == null) return ids;
            File f = new File(path);
            if (!f.exists()) return ids;
            try (BufferedReader br = new BufferedReader(new java.io.FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2 && parts[0].trim().equals(username)) {
                        ids.add(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SellerDashboardServlet: error reading announcement_reads: " + e.getMessage());
        }
        return ids;
    }

}