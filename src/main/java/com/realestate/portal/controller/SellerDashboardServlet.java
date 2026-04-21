package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
import com.realestate.portal.model.InquiryMessage;
import com.realestate.portal.model.InquiryThread;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");

                    // UPGRADED to check for 8 columns and match the Seller Name
                    if (data.length == 8 && data[6].equals(loggedUser)) {
                        try {
                            double price = Double.parseDouble(data[2]);
                            String imageUrl = (data[7] == null || data[7].trim().isEmpty()) ? "https://images.unsplash.com/photo-1600607687644-c7171b42498b?w=900&q=80" : data[7];

                            // Pass all 8 items!
                            Property p = new Property(data[0], data[1], price, data[3], data[4], data[5], data[6], imageUrl);
                            myProperties.add(p);
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping invalid price: " + data[2]);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading properties: " + e.getMessage());
            }
        }

        // --- INQUIRY INBOX (seller side) ---
        List<InquiryThread> sellerThreads = new ArrayList<>();
        Map<String, InquiryThread> byId = new HashMap<>();

        File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
        if (threadsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(threadsFile), "UTF-8"))) {
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
                System.out.println("Error reading inquiry threads: " + e.getMessage());
            }
        }

        File messagesFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv"));
        if (messagesFile.exists() && !byId.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(messagesFile), "UTF-8"))) {
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
                            content = new String(decoded, "UTF-8");
                        } catch (Exception ignored) {}

                        InquiryMessage msg = new InquiryMessage(data[0], data[1], data[2], data[3], content);
                        t.getMessages().add(msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading inquiry messages: " + e.getMessage());
            }
        }

        // Notifications (unread messages for bell bubble)
        List<Map<String, String>> allNotifications = new ArrayList<>();
        File readsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_reads.tsv"));
        if (threadsFile.exists() && messagesFile.exists()) {
            Map<String, String[]> threads = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(threadsFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    if (data.length >= 10) threads.put(data[0], data);
                }
            } catch (Exception ignored) {}

            Map<String, String> lastRead = new HashMap<>();
            if (readsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(readsFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] r = line.split("\t", -1);
                        if (r.length >= 3) lastRead.put(r[0] + "|" + r[1], r[2]);
                    }
                } catch (Exception ignored) {}
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(messagesFile), "UTF-8"))) {
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
                    try { content = new String(Base64.getDecoder().decode(msg[4]), "UTF-8"); } catch (Exception ignored) {}

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
        request.setAttribute("sellerThreads", sellerThreads);
        request.setAttribute("allNotifications", allNotifications);
        request.getRequestDispatcher("/seller_dashboard.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
