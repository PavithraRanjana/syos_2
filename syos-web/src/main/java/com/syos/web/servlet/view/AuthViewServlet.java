package com.syos.web.servlet.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * View servlet for authentication pages.
 * Handles login, registration, and logout.
 */
@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthViewServlet extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";
    private static final String REGISTER_VIEW = "/WEB-INF/views/auth/register.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Handle logout via GET as well (for direct links)
        if ("/logout".equals(path)) {
            handleLogout(request, response);
            return;
        }

        // If user is already logged in, redirect based on role
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            String redirectPath = getRedirectForRole((String) session.getAttribute("userRole"));
            response.sendRedirect(request.getContextPath() + redirectPath);
            return;
        }

        switch (path) {
            case "/login" -> request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
            case "/register" -> request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    /**
     * Gets the default redirect path based on user role.
     */
    private String getRedirectForRole(String role) {
        if (role == null) return "/shop";
        return switch (role) {
            case "CUSTOMER" -> "/shop";
            case "CASHIER" -> "/pos";
            case "INVENTORY_MANAGER" -> "/inventory";
            case "MANAGER" -> "/reports";
            case "ADMIN" -> "/admin";
            default -> "/shop";
        };
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            handleLogout(request, response);
        } else {
            // Other POST requests redirect to GET
            doGet(request, response);
        }
    }

    /**
     * Handles logout - invalidates session and redirects to shop.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/shop");
    }
}
