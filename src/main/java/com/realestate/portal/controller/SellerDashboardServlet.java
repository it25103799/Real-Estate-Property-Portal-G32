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
                    String[] data = line.split("\t", -1);
                    // threadId, timestamp, senderRole, senderName, contentB64
                    if (data.length >= 5) {
                        InquiryThread t = byId.get(data[0]);
                        if (t == null) continue;

                        String content = "";
                        try {
                            byte[] decoded = Base64.getDecoder().decode(data[4]);
                            content = new String(decoded, StandardCharsets.UTF_8);
                        } catch (Exception ignored) {}

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
        if (threadsFile.exists() && messagesFile.exists()) {
            Map<String, String[]> threads = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    if (data.length >= 10) threads.put(data[0], data);
                }
            } catch (Exception ignored) {}

            Map<String, String> lastRead = new HashMap<>();
            if (readsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(readsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] r = line.split("\t", -1);
                        if (r.length >= 3) lastRead.put(r[0] + "|" + r[1], r[2]);
                    }
                } catch (Exception ignored) {}
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(messagesFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
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
                    try { content = new String(Base64.getDecoder().decode(msg[4]), StandardCharsets.UTF_8); } catch (Exception ignored) {}

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
}