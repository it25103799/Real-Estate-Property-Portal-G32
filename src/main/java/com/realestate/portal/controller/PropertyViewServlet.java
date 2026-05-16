package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

    /**
     * PropertyViewTrackServlet
     *
     * Called via a lightweight POST from the property listing page (index.jsp)
     * each time a buyer opens a property detail card / modal.
     *
     * Flat-file column order (pipe-delimited):
     *   propertyId | viewerUsername | timestamp (yyyy-MM-dd HH:mm:ss)
     *
     * File: WEB-INF/property_views.txt
     *
     * The seller's analytics dashboard reads this file to show per-property
     * view counts.
     */
    @WebServlet("/trackView")
    public class PropertyViewServlet extends HttpServlet {

        private static final DateTimeFormatter DTF =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            HttpSession session = request.getSession(false);
            String viewer = (session != null) ? (String) session.getAttribute("loggedUser") : null;

            // Allow anonymous views; label them as "guest"
            if (viewer == null || viewer.trim().isEmpty()) {
                viewer = "guest";
            }

            String propertyId = request.getParameter("propertyId");
            if (propertyId == null || propertyId.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"propertyId is required\"}");
                return;
            }

            propertyId = propertyId.trim();

            // Sellers viewing their own listing don't inflate the count
            String role = (session != null) ? (String) session.getAttribute("loggedRole") : null;
            if ("SELLER".equalsIgnoreCase(role)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"status\":\"skipped\"}");
                return;
            }

            String timestamp = LocalDateTime.now().format(DTF);
            String entry = propertyId + "|" + viewer + "|" + timestamp;

            File viewsFile = new File(getServletContext().getRealPath("/WEB-INF/property_views.txt"));
            try (PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(viewsFile, true), StandardCharsets.UTF_8))) {
                pw.println(entry);
            } catch (IOException e) {
                System.err.println("PropertyViewTrackServlet: failed to write view entry – " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"write failed\"}");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"ok\"}");
        }

        /** Allow GET as a no-op ping so clients don't get 405 errors */
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"ok\"}");
        }
    }


