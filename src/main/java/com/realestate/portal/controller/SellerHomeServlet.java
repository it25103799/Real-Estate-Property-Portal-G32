package com.realestate.portal.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/sellerHome")
public class SellerHomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String loggedUser = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String role = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (loggedUser == null || role == null || !"SELLER".equalsIgnoreCase(role)) {
            response.sendRedirect("properties");
            return;
        }

        request.getRequestDispatcher("/seller_home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

