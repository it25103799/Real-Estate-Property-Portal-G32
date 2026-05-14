package com.realestate.portal.controller;

import com.realestate.portal.model.*;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
        Set<String> cities = new TreeSet<>(); // Use TreeSet for alphabetical sorting
        String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File propFile = new File(propPath);
        if (propFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // Skip empty lines
                    }
                    String[] data = line.split(",");
                    // A valid property must have at least the first 8 fields (id, title, price, location, type, status, sellerName, imageUrl)
                    if (data.length >= 8) {
                        try {
                            Property p = new Property();
                            p.setId(data[0]);
                            p.setTitle(data[1]);
                            p.setPrice(Double.parseDouble(data[2]));
                            p.setLocation(data[3]);
                            cities.add(data[3]); // Add city to the set
                            p.setType(data[4]);

                            // Normalize status to "For Rent", "Sold", or "For Sale"
                            String status = data[5].trim();
                            if ("for rent".equalsIgnoreCase(status)) {
                                p.setStatus("For Rent");
                            } else if ("sold".equalsIgnoreCase(status)) {
                                p.setStatus("Sold");
                            } else {
                                p.setStatus("For Sale");
                            }

                            // Safely set optional fields based on data length
                            p.setSellerName(data[6]);
                            p.setImageUrl(data[7]);

                            // Safely parse bedrooms and bathrooms if they exist
                            int bedrooms = 0;
                            if (data.length > 8 && data[8] != null && !data[8].trim().isEmpty()) {
                                bedrooms = Integer.parseInt(data[8].trim());
                            }
                            p.setBedrooms(bedrooms);

                            int bathrooms = 0;
                            if (data.length > 9 && data[9] != null && !data[9].trim().isEmpty()) {
                                bathrooms = Integer.parseInt(data[9].trim());
                            }
                            p.setBathrooms(bathrooms);

                            String description = "No Description yet..";
                            if (data.length > 10 && data[10] != null && !data[10].trim().isEmpty()) {
                                description = data[10];
                            }
                            p.setDescription(description);

                            propertyList.add(p);
                        } catch (NumberFormatException e) {
                            System.err.println("CRITICAL: Could not parse number in property line. Skipping. Line: \"" + line + "\" Error: " + e.getMessage());
                        }
                    } else {
                        System.err.println("WARNING: Skipping malformed property line (less than 8 fields). Line: \"" + line + "\"");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.setAttribute("propertyList", propertyList);
        request.setAttribute("allReviews", reviewList);
        request.setAttribute("cities", cities); // Pass the set of cities to the JSP

        // 2.5 LOAD RECENTLY SOLD PROPERTIES (for "JUST SOLD" floating card)
        List<Map<String, String>> recentlySold = new ArrayList<>();
        String soldPath = getServletContext().getRealPath("/WEB-INF/sold_properties.txt");
        File soldFile = new File(soldPath);
        if (soldFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(soldFile), "UTF-8"))) {
                String line;
                List<String> allSoldLines = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    allSoldLines.add(line);
                }

                // Get the last 5 most recent sold properties
                int startIdx = Math.max(0, allSoldLines.size() - 5);
                for (int i = allSoldLines.size() - 1; i >= startIdx; i--) {
                    String soldLine = allSoldLines.get(i);
                    String[] parts = soldLine.split("\\|", -1);
                    if (parts.length >= 6) {
                        Map<String, String> soldProp = new HashMap<>();
                        soldProp.put("timestamp", parts[0]);
                        soldProp.put("propertyId", parts[1]);
                        soldProp.put("title", parts[2]);
                        soldProp.put("price", parts[3]);
                        soldProp.put("location", parts[4]);
                        soldProp.put("imageUrl", parts[5]);
                        recentlySold.add(soldProp);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading sold properties: " + e.getMessage());
            }
        }
        request.setAttribute("recentlySold", recentlySold);

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
                } catch (Exception ignored) {
                }
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
                } catch (Exception ignored) {
                }
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
                        } catch (Exception ignored) {
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
                } catch (Exception ignored) {
                }
            }
        }

        request.setAttribute("allNotifications", allNotifications);

        // 4. LOAD BUYER'S SAVED FAVORITE IDs (for red heart highlighting)
        // Also auto-clean any sold properties out of favorites.txt
        List<String> favPropertyIds = new ArrayList<>();
        if (loggedUser != null) {
            // Build a set of sold property IDs from the already-loaded propertyList
            Set<String> soldIds = new TreeSet<>();
            for (Property p : propertyList) {
                if ("sold".equalsIgnoreCase(p.getStatus())) {
                    soldIds.add(p.getId());
                }
            }

            File favFile = new File(getServletContext().getRealPath("/WEB-INF/favorites.txt"));
            if (favFile.exists()) {
                List<String> allLines = new ArrayList<>();
                List<String> cleanedLines = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(favFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        allLines.add(line);
                        String[] parts = line.split(",");
                        if (parts.length == 2) {
                            String favUser = parts[0].trim();
                            String favPropId = parts[1].trim();
                            // Drop lines where property is sold
                            if (soldIds.contains(favPropId)) continue;
                            cleanedLines.add(line);
                            // Collect IDs for the logged-in buyer
                            if (favUser.equals(loggedUser)) {
                                favPropertyIds.add(favPropId);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Rewrite favorites.txt only if something was actually removed
                if (cleanedLines.size() < allLines.size()) {
                    try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(favFile, false), "UTF-8"))) {
                        for (String l : cleanedLines) pw.println(l);
                    } catch (Exception ignored) {}
                }
            }
        }
        request.setAttribute("favPropertyIds", favPropertyIds);

        // Preserve login error from query parameter across redirect chain
        String loginErrorParam = request.getParameter("loginError");
        if ("true".equals(loginErrorParam)) {
            HttpSession session2 = request.getSession();
            if (session2.getAttribute("loginError") == null) {
                session2.setAttribute("loginError", "Invalid email or password!");
            }
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}