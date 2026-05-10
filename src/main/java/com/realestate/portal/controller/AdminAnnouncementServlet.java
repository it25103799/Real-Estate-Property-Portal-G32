package com.realestate.portal.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * AdminAnnouncementServlet — Full CRUD for Platform Announcements
 *
 * Data file  : WEB-INF/announcements.txt
 * Line format: <id>,<title>,<message>,<priority>,<postedBy>,<timestamp>,<category>,audience=<ALL>,<expiryDate>
 * Note: All announcements are broadcast to ALL users (no targeting)
 *
 * URL mapping: /adminAnnouncement
 *   GET  ?action=list              → load all announcements → forward to admin_dashboard.jsp
 *   POST ?action=create            → add new announcement
 *   POST ?action=update            → edit existing announcement
 *   POST ?action=delete            → remove announcement
 */
@WebServlet("/adminAnnouncement")
public class AdminAnnouncementServlet extends HttpServlet {

    // ─────────────────────────────────────────────────────────
    // READ  (GET) — load all announcements into request scope
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<String[]> announcements = loadAnnouncements(request);
        request.setAttribute("announcements", announcements);

        // Forward back to the admin dashboard — the JSP will pick up the new attribute
        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────
    // CREATE / UPDATE / DELETE  (POST)
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "create": handleCreate(request, response, session); break;
            case "update": handleUpdate(request, response, session); break;
            case "delete": handleDelete(request, response, session); break;
            default:
                session.setAttribute("flashMessage", "❌ Unknown action.");
                session.setAttribute("flashMessageType", "error");
                response.sendRedirect(request.getContextPath() + "/adminDashboard");
        }
    }

    // ─────────────────────────────────────────────────────────
    // CREATE — write a new announcement line to the file
    // ─────────────────────────────────────────────────────────
    private void handleCreate(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session) throws IOException {

        String title     = sanitize(request.getParameter("announcementTitle"));
        String message   = sanitize(request.getParameter("announcementMessage"));
        String priority  = sanitize(request.getParameter("announcementPriority")); // HIGH / MEDIUM / LOW
        String category  = sanitize(request.getParameter("announcementCategory")); // MAINTENANCE / UPDATE / EVENT / ALERT / GENERAL
        String expiry    = sanitize(request.getParameter("announcementExpiry"));   // YYYY-MM-DD or empty
        String postedBy  = (String) session.getAttribute("loggedUser");

        if (title.isEmpty() || message.isEmpty()) {
            session.setAttribute("flashMessage", "❌ Title and message are required.");
            session.setAttribute("flashMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/adminDashboard");
            return;
        }

        // Validate and set defaults
        if (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW")) {
            priority = "MEDIUM";
        }
        if (category.isEmpty() || (!category.equals("MAINTENANCE") && !category.equals("UPDATE") && 
            !category.equals("EVENT") && !category.equals("ALERT") && !category.equals("GENERAL"))) {
            category = "GENERAL";
        }
        // All announcements go to ALL users - no targeting
        String audience = "ALL";

        String id        = "ANN" + System.currentTimeMillis();
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
        
        // Save all 9 fields: id,title,message,priority,postedBy,timestamp,category,audience(=ALL),expiry
        String newLine   = String.join(",", id, title, message, priority, postedBy, timestamp, category, audience, expiry);

        String filePath = getServletContext().getRealPath("/WEB-INF/announcements.txt");
        File file = new File(filePath);

        // Create the file if it doesn't exist yet
        if (!file.exists()) {
            file.createNewFile();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(newLine);
            bw.newLine();
        }

        session.setAttribute("flashMessage", "✅ Announcement \"" + title + "\" posted successfully.");
        session.setAttribute("flashMessageType", "success");
        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE — replace an existing line identified by id
    // ─────────────────────────────────────────────────────────
    private void handleUpdate(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session) throws IOException {

        String id       = sanitize(request.getParameter("announcementId"));
        String title    = sanitize(request.getParameter("announcementTitle"));
        String message  = sanitize(request.getParameter("announcementMessage"));
        String priority = sanitize(request.getParameter("announcementPriority"));
        String category = sanitize(request.getParameter("announcementCategory"));
        String expiry   = sanitize(request.getParameter("announcementExpiry"));
        String postedBy = (String) session.getAttribute("loggedUser");

        if (id.isEmpty() || title.isEmpty() || message.isEmpty()) {
            session.setAttribute("flashMessage", "❌ ID, title and message are required for update.");
            session.setAttribute("flashMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/adminDashboard");
            return;
        }

        // Validate and set defaults
        if (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW")) {
            priority = "MEDIUM";
        }
        if (category.isEmpty() || (!category.equals("MAINTENANCE") && !category.equals("UPDATE") && 
            !category.equals("EVENT") && !category.equals("ALERT") && !category.equals("GENERAL"))) {
            category = "GENERAL";
        }
        // All announcements go to ALL users - no targeting
        String audience = "ALL";

        String filePath  = getServletContext().getRealPath("/WEB-INF/announcements.txt");
        File file        = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean found    = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) { lines.add(line); continue; }
                    String[] parts = line.split(",", 9); // Updated to handle 9 fields
                    if (parts.length >= 1 && parts[0].equals(id)) {
                        // Keep original timestamp (parts[5]) so we know when it was first posted
                        String originalTimestamp = parts.length >= 6 ? parts[5] : "—";
                        // Save all 9 fields with updated values (audience always = ALL)
                        lines.add(String.join(",", id, title, message, priority, postedBy, originalTimestamp, category, audience, expiry));
                        found = true;
                    } else {
                        lines.add(line);
                    }
                }
            }
        }

        if (found) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                for (String l : lines) { bw.write(l); bw.newLine(); }
            }
            session.setAttribute("flashMessage", "✅ Announcement \"" + title + "\" updated.");
            session.setAttribute("flashMessageType", "success");
        } else {
            session.setAttribute("flashMessage", "❌ Announcement with ID \"" + id + "\" not found.");
            session.setAttribute("flashMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }

    // ─────────────────────────────────────────────────────────
    // DELETE — remove a line identified by id
    // ─────────────────────────────────────────────────────────
    private void handleDelete(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session) throws IOException {

        String id = sanitize(request.getParameter("announcementId"));

        if (id.isEmpty()) {
            session.setAttribute("flashMessage", "❌ No announcement ID provided.");
            session.setAttribute("flashMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/adminDashboard");
            return;
        }

        String filePath  = getServletContext().getRealPath("/WEB-INF/announcements.txt");
        File file        = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean deleted  = false;

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) { lines.add(line); continue; }
                    String[] parts = line.split(",", 2);
                    if (parts[0].equals(id)) {
                        deleted = true; // skip this line → effectively deletes it
                    } else {
                        lines.add(line);
                    }
                }
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                for (String l : lines) { bw.write(l); bw.newLine(); }
            }
        }

        if (deleted) {
            session.setAttribute("flashMessage", "✅ Announcement \"" + id + "\" removed.");
            session.setAttribute("flashMessageType", "success");
        } else {
            session.setAttribute("flashMessage", "❌ Announcement \"" + id + "\" not found.");
            session.setAttribute("flashMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/adminDashboard");
    }

    // ─────────────────────────────────────────────────────────
    // HELPER — load all announcements from file
    // ─────────────────────────────────────────────────────────
    private List<String[]> loadAnnouncements(HttpServletRequest request) {
        List<String[]> list = new ArrayList<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/announcements.txt");
        File file = new File(filePath);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Format: id,title,message,priority,postedBy,timestamp,category,audience,expiry
                String[] parts = line.split(",", 9); // Updated to handle 9 fields
                if (parts.length >= 6) { // At least 6 fields for backward compatibility
                    // Ensure array has 9 elements (fill missing with defaults)
                    if (parts.length < 9) {
                        String[] fullParts = new String[9];
                        System.arraycopy(parts, 0, fullParts, 0, parts.length);
                        // Fill missing fields with defaults
                        if (parts.length <= 6) fullParts[6] = "GENERAL"; // category
                        if (parts.length <= 7) fullParts[7] = "ALL";     // audience
                        if (parts.length <= 8) fullParts[8] = "";        // expiry
                        list.add(fullParts);
                    } else {
                        list.add(parts);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading announcements.txt: " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // HELPER — check admin session
    // ─────────────────────────────────────────────────────────
    private boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        String role = (String) session.getAttribute("loggedRole");
        return "ADMIN".equalsIgnoreCase(role);
    }

    // ─────────────────────────────────────────────────────────
    // HELPER — strip commas so the CSV format stays intact
    // ─────────────────────────────────────────────────────────
    private String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replace(",", ";");
    }
}
