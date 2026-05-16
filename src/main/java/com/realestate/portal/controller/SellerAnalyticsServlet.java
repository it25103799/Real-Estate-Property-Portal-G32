package com.realestate.portal.controller;

import com.realestate.portal.model.Property;
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
import java.util.*;

    /**
     * SellerAnalyticsServlet
     *
     * Builds per-property analytics for the logged-in seller and forwards to
     * seller_analytics.jsp.
     *
     * Metrics computed per property:
     *   - viewCount      – rows in property_views.txt for this property id
     *   - inquiryCount   – inquiry threads in inquiry_threads.tsv for this property id
     *   - bookingCount   – bookings in bookings.txt (all statuses) for this property id
     *   - favoriteCount  – rows in favorites.txt for this property id
     *
     * Request attributes set:
     *   analyticsRows   – List<Map<String,String>>  (one map per property)
     *   totalViews      – int
     *   totalInquiries  – int
     *   totalBookings   – int
     *   totalFavorites  – int
     */
    @WebServlet("/sellerAnalytics")
    public class SellerAnalyticsServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            HttpSession session = request.getSession(false);
            String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
            String role       = (session != null) ? (String) session.getAttribute("loggedRole") : null;

            if (loggedUser == null || !"SELLER".equalsIgnoreCase(role)) {
                response.sendRedirect("login");
                return;
            }

            // ── 1. Load this seller's properties ──────────────────────────────────
            List<Property> myProperties = new ArrayList<>();
            Set<String> myPropIds = new HashSet<>();

            File propFile = new File(getServletContext().getRealPath("/WEB-INF/properties.txt"));
            if (propFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(propFile.getAbsolutePath())),
                                StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split(",", -1);
                        if (d.length >= 8 && loggedUser.equals(d[6].trim())) {
                            Property p = new Property();
                            p.setId(d[0].trim());
                            p.setTitle(d[1].trim());
                            try { p.setPrice(Double.parseDouble(d[2].trim())); } catch (NumberFormatException ignored) {}
                            p.setLocation(d[3].trim());
                            p.setType(d[4].trim());
                            p.setStatus(d[5].trim());
                            p.setSellerName(d[6].trim());
                            myProperties.add(p);
                            myPropIds.add(p.getId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("SellerAnalyticsServlet: error reading properties – " + e.getMessage());
                }
            }

            // ── 2. Count views per property ───────────────────────────────────────
            Map<String, Integer> viewCounts = new HashMap<>();
            for (String id : myPropIds) viewCounts.put(id, 0);

            File viewsFile = new File(getServletContext().getRealPath("/WEB-INF/property_views.txt"));
            if (viewsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(viewsFile.getAbsolutePath())),
                                StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split("\\|", -1);
                        if (d.length >= 1) {
                            String pid = d[0].trim();
                            if (myPropIds.contains(pid)) {
                                viewCounts.merge(pid, 1, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("SellerAnalyticsServlet: error reading views – " + e.getMessage());
                }
            }

            // ── 3. Count inquiries per property ───────────────────────────────────
            Map<String, Integer> inquiryCounts = new HashMap<>();
            for (String id : myPropIds) inquiryCounts.put(id, 0);

            File threadsFile = new File(getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv"));
            if (threadsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(threadsFile.getAbsolutePath())),
                                StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split("\t", -1);
                        // col 1 = propertyId, col 3 = sellerName
                        if (d.length >= 4 && loggedUser.equals(d[3].trim())) {
                            String pid = d[1].trim();
                            if (myPropIds.contains(pid)) {
                                inquiryCounts.merge(pid, 1, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("SellerAnalyticsServlet: error reading inquiry threads – " + e.getMessage());
                }
            }

            // ── 4. Count bookings per property ────────────────────────────────────
            Map<String, Integer> bookingCounts = new HashMap<>();
            for (String id : myPropIds) bookingCounts.put(id, 0);

            File bookingsFile = new File(getServletContext().getRealPath("/WEB-INF/bookings.txt"));
            if (bookingsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(bookingsFile.getAbsolutePath())),
                                StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                        String[] d = line.split("\\|", -1);
                        // col 1 = propertyId, col 3 = sellerUsername
                        if (d.length >= 4 && loggedUser.equals(d[3].trim())) {
                            String pid = d[1].trim();
                            if (myPropIds.contains(pid)) {
                                bookingCounts.merge(pid, 1, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("SellerAnalyticsServlet: error reading bookings – " + e.getMessage());
                }
            }

            // ── 5. Count favorites per property ───────────────────────────────────
            Map<String, Integer> favCounts = new HashMap<>();
            for (String id : myPropIds) favCounts.put(id, 0);

            File favsFile = new File(getServletContext().getRealPath("/WEB-INF/favorites.txt"));
            if (favsFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(favsFile.getAbsolutePath())),
                                StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] d = line.split(",", -1);
                        if (d.length >= 2) {
                            String pid = d[1].trim();
                            if (myPropIds.contains(pid)) {
                                favCounts.merge(pid, 1, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("SellerAnalyticsServlet: error reading favorites – " + e.getMessage());
                }
            }

            // ── 6. Assemble analytics rows ────────────────────────────────────────
            List<Map<String, String>> analyticsRows = new ArrayList<>();
            int totalViews = 0, totalInquiries = 0, totalBookings = 0, totalFavorites = 0;

            for (Property p : myProperties) {
                String id = p.getId();
                int views     = viewCounts.getOrDefault(id, 0);
                int inquiries = inquiryCounts.getOrDefault(id, 0);
                int bookings  = bookingCounts.getOrDefault(id, 0);
                int favs      = favCounts.getOrDefault(id, 0);

                totalViews     += views;
                totalInquiries += inquiries;
                totalBookings  += bookings;
                totalFavorites += favs;

                Map<String, String> row = new LinkedHashMap<>();
                row.put("propertyId",    id);
                row.put("title",         p.getTitle());
                row.put("location",      p.getLocation());
                row.put("type",          p.getType());
                row.put("status",        p.getStatus());
                row.put("price",         String.valueOf(p.getPrice()));
                row.put("viewCount",     String.valueOf(views));
                row.put("inquiryCount",  String.valueOf(inquiries));
                row.put("bookingCount",  String.valueOf(bookings));
                row.put("favoriteCount", String.valueOf(favs));
                analyticsRows.add(row);
            }

            // Sort by views descending so best-performing property is on top
            analyticsRows.sort((a, b) ->
                    Integer.parseInt(b.get("viewCount")) - Integer.parseInt(a.get("viewCount")));

            // ── 7. Set attributes and forward ─────────────────────────────────────
            request.setAttribute("analyticsRows",   analyticsRows);
            request.setAttribute("totalViews",      totalViews);
            request.setAttribute("totalInquiries",  totalInquiries);
            request.setAttribute("totalBookings",   totalBookings);
            request.setAttribute("totalFavorites",  totalFavorites);
            request.setAttribute("loggedUser",      loggedUser);

            request.getRequestDispatcher("/seller_analytics.jsp").forward(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            doGet(request, response);
        }
    }


