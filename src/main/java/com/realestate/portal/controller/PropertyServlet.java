package com.realestate.portal.controller;

import com.realestate.portal.model.*;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/properties")
public class PropertyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. LOAD PROPERTIES
        List<Property> propertyList = new ArrayList<>();
        String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File propFile = new File(propPath);
        if (propFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 8) {
                        propertyList.add(new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7]));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 2. LOAD REVIEWS (Kalhari's Module)
        List<Review> reviewList = new ArrayList<>();
        String revPath = getServletContext().getRealPath("/WEB-INF/reviews.txt");
        File revFile = new File(revPath);
        if (revFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(revFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 6) {
                        if (data[5].equalsIgnoreCase("VERIFIED")) {
                            reviewList.add(new VerifiedReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        } else {
                            reviewList.add(new PublicReview(data[0], data[1], data[2], Integer.parseInt(data[3]), data[4]));
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        request.setAttribute("propertyList", propertyList);
        request.setAttribute("allReviews", reviewList);

        // 3. LOAD NOTIFICATIONS (inquiry messages -> bell icon)
        HttpSession session = request.getSession(false);
        String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        List<Map<String, String>> allNotifications = new ArrayList<>();
        if (loggedUser != null && loggedRole != null) {
            File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
            File messagesFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv"));
            File readsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_reads.tsv"));

            Map<String, String[]> threads = new HashMap<>();
            // threadId -> data array
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(threadsFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split("\t", -1);
                        if (data.length >= 10) {
                            threads.put(data[0], data);
                        }
                    }
                } catch (Exception ignored) {}
            }

            Map<String, String> lastRead = new HashMap<>();
            // key: user|threadId -> timestamp
            if (readsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(readsFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] r = line.split("\t", -1);
                        if (r.length >= 3) {
                            lastRead.put(r[0] + "|" + r[1], r[2]);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (messagesFile.exists() && !threads.isEmpty()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(messagesFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] msg = line.split("\t", -1);
                        if (msg.length < 5) continue;
                        String[] t = threads.get(msg[0]);
                        if (t == null) continue;

                        // t: [0]=threadId [2]=propTitle [3]=sellerName [4]=buyerAccount [5]=buyerName
                        String senderRole = msg[2];
                        String receiver;
                        String sender;

                        if ("SELLER".equalsIgnoreCase(senderRole)) {
                            receiver = t[4]; // buyerAccount
                            sender = t[3];   // sellerName
                        } else {
                            receiver = t[3]; // sellerName
                            sender = t[5];   // buyerName
                        }

                        if (!loggedUser.equals(receiver)) continue;

                        // unread filter
                        String ts = msg[1];
                        String lr = lastRead.get(loggedUser + "|" + msg[0]);
                        if (lr != null && ts != null && ts.compareTo(lr) <= 0) continue;

                        String content = "";
                        try {
                            content = new String(Base64.getDecoder().decode(msg[4]), "UTF-8");
                        } catch (Exception ignored) {}

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
        }

        request.setAttribute("allNotifications", allNotifications);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}