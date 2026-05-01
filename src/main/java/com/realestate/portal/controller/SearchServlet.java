package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Grab what the user typed in the search bar
        String searchLocation = request.getParameter("location");
        String searchType = request.getParameter("type");

        List<Property> searchResults = new ArrayList<>();
        Set<String> cities = new java.util.TreeSet<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/properties.txt");
        File file = new File(filePath);

        // 2. Open the database and start filtering
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] data = line.split(",");

                    // Accept any property with at least the 8 required fields
                    if (data.length >= 8) {
                        String propLocation = data[3];
                        String propType = data[4];
                        cities.add(propLocation); // Collect all cities for the Browse sidebar

                        // 3. THE FILTERING LOGIC
                        boolean matchesLocation = (searchLocation == null || searchLocation.trim().isEmpty())
                                || propLocation.toLowerCase().contains(searchLocation.toLowerCase().trim());

                        boolean matchesType = (searchType == null || searchType.trim().isEmpty())
                                || propType.equalsIgnoreCase(searchType);

                        if (matchesLocation && matchesType) {
                            try {
                                double price = Double.parseDouble(data[2]);
                                int bedrooms = (data.length > 8 && data[8] != null && !data[8].trim().isEmpty()) ? Integer.parseInt(data[8].trim()) : 0;
                                int bathrooms = (data.length > 9 && data[9] != null && !data[9].trim().isEmpty()) ? Integer.parseInt(data[9].trim()) : 0;
                                String description = (data.length > 10 && data[10] != null && !data[10].trim().isEmpty()) ? data[10] : "";
                                String imageUrl = (data[7] == null || data[7].trim().isEmpty()) ? "https://images.unsplash.com/photo-1600607687644-c7171b42498b?w=900&q=80" : data[7];
                                // Normalize status
                                String status = data[5].trim();
                                if ("for rent".equalsIgnoreCase(status)) status = "For Rent";
                                else status = "For Sale";

                                Property p = new Property(data[0], data[1], price, propLocation, propType, status, data[6], imageUrl, bedrooms, bathrooms, description);
                                searchResults.add(p);
                            } catch (NumberFormatException e) {
                                System.err.println("SearchServlet: Skipping malformed line: " + line);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error searching properties: " + e.getMessage());
            }
        }

        // 4. Send the filtered list back to the homepage to display!
        request.setAttribute("propertyList", searchResults);
        request.setAttribute("cities", cities); // Pass cities so Browse sidebar filters work

        // Add a message so the user knows they are looking at search results
        request.setAttribute("searchMessage", "Showing results for your search:");

        // Notifications for bell icon (same engine as PropertyServlet)
        HttpSession session = request.getSession(false);
        String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String loggedRole = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        List<Map<String, String>> allNotifications = new ArrayList<>();
        if (loggedUser != null && loggedRole != null) {
            File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
            File messagesFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv"));
            File readsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_reads.tsv"));

            Map<String, String[]> threads = new HashMap<>();
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
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
            if (readsFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(readsFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
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
                            receiver = t[4];
                            sender = t[3];
                        } else {
                            receiver = t[3];
                            sender = t[5];
                        }

                        if (!loggedUser.equals(receiver)) continue;

                        String ts = msg[1];
                        String lr = lastRead.get(loggedUser + "|" + msg[0]);
                        if (lr != null && ts != null && ts.compareTo(lr) <= 0) continue;

                        String content = "";
                        try {
                            content = new String(Base64.getDecoder().decode(msg[4]), StandardCharsets.UTF_8);
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