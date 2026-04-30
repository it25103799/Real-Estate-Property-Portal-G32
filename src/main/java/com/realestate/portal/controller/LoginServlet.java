package com.realestate.portal.controller;

import com.realestate.portal.model.User;
import com.realestate.portal.service.LoginService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String filePath = getServletContext().getRealPath("/WEB-INF/users.txt");
        LoginService loginService = new LoginService(filePath);
        User user = loginService.authenticate(email, password);

        if (user != null) {

            HttpSession session = request.getSession();
            session.setAttribute("loggedUser", user.getUsername());
            session.setAttribute("loggedRole", user.getRole());
            session.setAttribute("loggedEmail", user.getEmail());

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                System.out.println("🔐 ADMIN LOGIN SUCCESSFUL: " + user.getUsername());
                response.sendRedirect("adminDashboard");
            } else if ("SELLER".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect("sellerHome");
            } else {
                response.sendRedirect("buyerDashboard");
            }

        } else {
            request.setAttribute("errorMessage", "Invalid email or password!");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}