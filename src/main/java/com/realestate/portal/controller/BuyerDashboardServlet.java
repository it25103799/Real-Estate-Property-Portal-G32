package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
import com.realestate.portal.model.InquiryMessage;
import com.realestate.portal.model.InquiryThread;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/buyerDashboard")
public class BuyerDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            response.sendRedirect("login");
            return;
        }

        List<String> myFavIds = new ArrayList<>();
        List<Property> savedProperties = new ArrayList<>();

        // 1. Read favorites.txt and find IDs belonging to this specific buyer
        File favFile = new File(getServletContext().getRealPath("/WEB-INF/favorites.txt"));
        if (favFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(favFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 2 && data[0].equals(loggedUser)) {
                        myFavIds.add(data[1]); // Save the Property ID
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading favorites: " + e.getMessage());
            }
        }

        // 2. Read properties.txt, grab the full details for those specific IDs
        File propFile = new File(getServletContext().getRealPath("/WEB-INF/properties.txt"));
        if (propFile.exists() && !myFavIds.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(propFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (myFavIds.contains(data[0])) {
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
                                savedProperties.add(p);
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
        // --- INQUIRY THREAD READER ENGINE (new threaded system) ---
        List<Map<String, String>> myInquiries = new ArrayList<>();
        List<InquiryThread> buyerThreads = new ArrayList<>();
        Map<String, InquiryThread> byId = new HashMap<>();

        File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
        if (threadsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    // threadId, propertyId, propertyTitle, sellerName, buyerAccount, buyerName, buyerEmail, buyerPhone, createdDate, status
                    if (data.length >= 10 && loggedUser.equals(data[4])) {
                        Map<String, String> inq = new HashMap<>();
                        inq.put("threadId", data[0]);
                        inq.put("propertyId", data[1]);
                        inq.put("propertyTitle", data[2]);
                        inq.put("agentName", data[3]);
                        inq.put("buyerName", data[5]);
                        inq.put("buyerEmail", data[6]);
                        inq.put("buyerPhone", data[7]);
                        inq.put("date", data[8]);
                        inq.put("status", data[9]);
                        myInquiries.add(inq);

                        InquiryThread t = new InquiryThread(
                                data[0], data[1], data[2], data[3],
                                data[4], data[5], data[6], data[7],
                                data[8], data[9]
                        );
                        buyerThreads.add(t);
                        byId.put(t.getId(), t);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading inquiry threads: " + e.getMessage());
            }

            File messagesFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv"));
            if (messagesFile.exists() && !byId.isEmpty()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(messagesFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split("\t", -1);
                        if (data.length >= 5) {
                            InquiryThread t = byId.get(data[0]);
                            if (t == null) continue;

                            String content = "";
                            try {
                                byte[] decoded = Base64.getDecoder().decode(data[4]);
                                content = new String(decoded, StandardCharsets.UTF_8);
                            } catch (Exception ignored) {}

                            t.getMessages().add(new InquiryMessage(data[0], data[1], data[2], data[3], content));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading inquiry messages: " + e.getMessage());
                }
            }
        } else {
            // Backward compat: read old inquiries.txt if new threads file doesn't exist
            File inqFile = new File(getServletContext().getRealPath("/WEB-INF/inquiries.txt"));
            if (inqFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(inqFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 5 && data[0].equals(loggedUser)) {
                            Map<String, String> inq = new HashMap<>();
                            inq.put("agentName", data[1]);
                            inq.put("propertyTitle", data[2]);
                            inq.put("date", data[3]);
                            inq.put("status", data[4]);
                            myInquiries.add(inq);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading inquiries: " + e.getMessage());
                }
            }
        }

        request.setAttribute("myInquiries", myInquiries);
        request.setAttribute("buyerThreads", buyerThreads);

        request.setAttribute("savedProperties", savedProperties);
        request.getRequestDispatcher("/buyer_dashboard.jsp").forward(request, response);
    }
}