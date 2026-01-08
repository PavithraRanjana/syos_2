package com.syos.web.servlet.view;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Base servlet for view controllers providing common functionality.
 */
public abstract class BaseViewServlet extends HttpServlet {

    protected static final String VIEWS_PATH = "/WEB-INF/views/";

    /**
     * Forwards to a JSP view.
     */
    protected void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(VIEWS_PATH + viewPath);
        dispatcher.forward(request, response);
    }

    /**
     * Sets a success message to be displayed.
     */
    protected void setSuccessMessage(HttpServletRequest request, String message) {
        request.setAttribute("successMessage", message);
    }

    /**
     * Sets an error message to be displayed.
     */
    protected void setErrorMessage(HttpServletRequest request, String message) {
        request.setAttribute("errorMessage", message);
    }

    /**
     * Sets the page title.
     */
    protected void setPageTitle(HttpServletRequest request, String title) {
        request.setAttribute("pageTitle", title);
    }

    /**
     * Sets the active navigation item.
     */
    protected void setActiveNav(HttpServletRequest request, String navItem) {
        request.setAttribute("activeNav", navItem);
    }

    /**
     * Redirects to another URL.
     */
    protected void redirectTo(HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(path);
    }

    /**
     * Gets an integer parameter with a default value.
     */
    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a string parameter with a default value.
     */
    protected String getStringParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Gets a path segment by index.
     */
    protected String getPathSegment(HttpServletRequest request, int index) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String[] segments = pathInfo.substring(1).split("/");
        if (index >= 0 && index < segments.length) {
            return segments[index];
        }
        return null;
    }
}
