package com.realestate.portal.controller;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/bulkRemoveFavorite")
public class BulkRemoveFavoriteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String buyerName = (String) session.getAttribute("loggedUser");

        if (buyerName == null) {
            response.sendRedirect("login");
            return;
        }

        String mode = request.getParameter("mode"); // "selected" or "all"
        String[] selectedIds = request.getParameterValues("selectedIds"); // only used in "selected" mode

        File favFile = new File(getServletContext().getRealPath("/WEB-INF/favorites.txt"));

        if (favFile.exists()) {
            List<String> remaining = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(favFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length != 2) { remaining.add(line); continue; }

                    String lineUser = parts[0].trim();
                    String linePropId = parts[1].trim();

                    // Always keep other buyers' favorites untouched
                    if (!lineUser.equals(buyerName)) {
                        remaining.add(line);
                        continue;
                    }

                    // "all" mode — drop every line belonging to this buyer
                    if ("all".equals(mode)) continue;

                    // "selected" mode — drop only the checked ones
                    if ("selected".equals(mode) && selectedIds != null) {
                        boolean isSelected = false;
                        for (String id : selectedIds) {
                            if (id.equals(linePropId)) { isSelected = true; break; }
                        }
                        if (isSelected) continue;
                    }

                    remaining.add(line);
                }
            } catch (Exception e) {
                System.err.println("BulkRemove read error: " + e.getMessage());
            }

            // Rewrite the file with remaining lines
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(favFile, false), "UTF-8"))) {
                for (String l : remaining) pw.println(l);
            } catch (Exception e) {
                System.err.println("BulkRemove write error: " + e.getMessage());
            }
        }

        response.sendRedirect("buyerDashboard");
    }
}
