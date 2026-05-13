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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/searchSaved")
public class SearchSavedServlet extends HttpServlet {

    // ==========================================
    // 🔥 ALGORITHM 1: BINARY SEARCH TREE (BST) 🔥
    // ==========================================
    static class PropertyNode {
        Property data;
        PropertyNode left, right;
        public PropertyNode(Property data) { this.data = data; }
    }

    static class PropertyBST {
        PropertyNode root;

        public void insert(Property p) {
            root = insertRec(root, p);
        }

        private PropertyNode insertRec(PropertyNode root, Property p) {
            if (root == null) return new PropertyNode(p);
            if (p.getId().compareToIgnoreCase(root.data.getId()) < 0)
                root.left = insertRec(root.left, p);
            else if (p.getId().compareToIgnoreCase(root.data.getId()) > 0)
                root.right = insertRec(root.right, p);
            return root;
        }

        public Property searchById(String id) {
            PropertyNode result = searchRec(root, id);
            return result != null ? result.data : null;
        }

        private PropertyNode searchRec(PropertyNode root, String id) {
            if (root == null || root.data.getId().equalsIgnoreCase(id))
                return root;
            if (root.data.getId().compareToIgnoreCase(id) > 0)
                return searchRec(root.left, id);
            return searchRec(root.right, id);
        }
    }
    // ==========================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            response.sendRedirect("login");
            return;
        }

        String query = request.getParameter("query");
        if (query == null) query = "";
        query = query.trim().toLowerCase();

        List<String> myFavIds = new ArrayList<>();
        List<Property> allSavedProperties = new ArrayList<>();

        // 1. Grab this Buyer's specific favorites from the database
        File favFile = new File(getServletContext().getRealPath("/WEB-INF/favorites.txt"));
        if (favFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(favFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 2 && data[0].equals(loggedUser)) myFavIds.add(data[1]);
                }
            } catch (Exception e) {
                System.err.println("Error reading favorites: " + e.getMessage());
            }
        }

        // 2. Grab the actual Property details for those favorites
        File propFile = new File(getServletContext().getRealPath("/WEB-INF/properties.txt"));
        if (propFile.exists() && !myFavIds.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(propFile.getAbsolutePath())), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (myFavIds.contains(data[0])) {
                        Property p = null;
                        try {
                            if (data.length == 11) {
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], Integer.parseInt(data[8]), Integer.parseInt(data[9]), data[10]);
                            } else if (data.length == 10) {
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], Integer.parseInt(data[8]), Integer.parseInt(data[9]), "");
                            } else if (data.length == 8) {
                                p = new Property(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5], data[6], data[7], 0, 0, "");
                            }
                            if (p != null) allSavedProperties.add(p);
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping malformed property line: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading properties: " + e.getMessage());
            }
        }

        List<Property> searchResults = new ArrayList<>();

        // 3. EXECUTE THE SEARCH
        if (!query.isEmpty()) {
            PropertyBST bst = new PropertyBST();
            for (Property p : allSavedProperties) {
                bst.insert(p);
            }

            // Try exact ID match first using BST
            Property foundById = bst.searchById(query);

            if (foundById != null) {
                searchResults.add(foundById);
            } else {
                // ── UPDATED FALLBACK: now also matches property type ──────────
                for (Property p : allSavedProperties) {
                    if (p.getLocation().toLowerCase().contains(query)
                            || p.getTitle().toLowerCase().contains(query)
                            || p.getType().toLowerCase().contains(query)) {  // ← NEW: type search
                        searchResults.add(p);
                    }
                }
                // ─────────────────────────────────────────────────────────────
            }
        } else {
            searchResults = allSavedProperties;
        }

        request.setAttribute("savedProperties", searchResults);
        request.getRequestDispatcher("/buyer_dashboard.jsp").forward(request, response);
    }
}