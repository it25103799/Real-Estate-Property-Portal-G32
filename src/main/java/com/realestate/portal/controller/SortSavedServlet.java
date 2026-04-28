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

@WebServlet("/sortSaved")
public class SortSavedServlet extends HttpServlet {

    // ==========================================
    // 🔥 ALGORITHM 2: QUICK SORT 🔥
    // ==========================================

    // Main QuickSort trigger
    private void quickSortProperties(List<Property> list, int low, int high, boolean ascending) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, ascending);
            // Recursively sort elements before and after partition
            quickSortProperties(list, low, pivotIndex - 1, ascending);
            quickSortProperties(list, pivotIndex + 1, high, ascending);
        }
    }

    // The Partition Logic
    private int partition(List<Property> list, int low, int high, boolean ascending) {
        // Choose the rightmost element as pivot
        double pivotPrice = list.get(high).getPrice();
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            // Check sorting condition (Low to High vs High to Low)
            boolean condition = ascending ? (list.get(j).getPrice() < pivotPrice) : (list.get(j).getPrice() > pivotPrice);

            if (condition) {
                i++;
                // Swap elements
                Property temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        // Swap the pivot element with the element at i+1
        Property temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);

        return i + 1;
    }
    // ==========================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");

        // Security check
        if (loggedUser == null) {
            response.sendRedirect("login");
            return;
        }

        // Grab the sorting choice from the dropdown
        String sortOrder = request.getParameter("sortOrder");
        if (sortOrder == null) sortOrder = "default";

        List<String> myFavIds = new ArrayList<>();
        List<Property> allSavedProperties = new ArrayList<>();

        // 1. Read favorites.txt
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

        // 2. Read properties.txt safely with UTF-8
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
                                allSavedProperties.add(p);
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

        // 3. EXECUTE THE QUICK SORT ALGORITHM!
        if (allSavedProperties.size() > 1) {
            if ("priceLow".equals(sortOrder)) {
                quickSortProperties(allSavedProperties, 0, allSavedProperties.size() - 1, true);
            } else if ("priceHigh".equals(sortOrder)) {
                quickSortProperties(allSavedProperties, 0, allSavedProperties.size() - 1, false);
            }
        }

        // Send the sorted list back to the UI!
        request.setAttribute("savedProperties", allSavedProperties);
        request.getRequestDispatcher("/buyer_dashboard.jsp").forward(request, response);
    }
}