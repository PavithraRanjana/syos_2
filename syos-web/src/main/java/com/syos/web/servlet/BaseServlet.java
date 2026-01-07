package com.syos.web.servlet;

import com.syos.exception.*;
import com.syos.util.JsonUtil;
import com.syos.web.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base servlet with common functionality for all servlets.
 */
public abstract class BaseServlet extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Forwards request to a JSP view.
     */
    protected void forwardToView(HttpServletRequest request, HttpServletResponse response,
                                  String viewPath) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    /**
     * Redirects to a path within the application.
     */
    protected void redirect(HttpServletRequest request, HttpServletResponse response,
                            String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * Sends a JSON response.
     */
    protected void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }

    /**
     * Sends a successful JSON response.
     */
    protected <T> void sendSuccess(HttpServletResponse response, T data) throws IOException {
        sendJson(response, ApiResponse.success(data));
    }

    /**
     * Sends a successful JSON response with message.
     */
    protected <T> void sendSuccess(HttpServletResponse response, T data, String message)
            throws IOException {
        sendJson(response, ApiResponse.success(data, message));
    }

    /**
     * Sends an error JSON response.
     */
    protected void sendError(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        sendJson(response, ApiResponse.error(message));
    }

    /**
     * Reads the request body as a string.
     */
    protected String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    /**
     * Parses the request body as JSON to the specified class.
     */
    protected <T> T parseRequestBody(HttpServletRequest request, Class<T> clazz)
            throws IOException {
        String body = readRequestBody(request);
        if (body.isEmpty()) {
            return null;
        }
        return JsonUtil.fromJson(body, clazz);
    }

    /**
     * Gets a required parameter from the request.
     */
    protected String getRequiredParameter(HttpServletRequest request, String name)
            throws ValidationException {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Missing required parameter: " + name, name);
        }
        return value.trim();
    }

    /**
     * Gets an optional parameter from the request.
     */
    protected String getOptionalParameter(HttpServletRequest request, String name,
                                           String defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Gets an integer parameter.
     */
    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the current user ID from session.
     */
    protected Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute("userId");
    }

    /**
     * Gets the current user role from session.
     */
    protected String getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userRole");
    }

    /**
     * Checks if user is authenticated.
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }

    /**
     * Handles exceptions and sends appropriate response.
     */
    protected void handleException(HttpServletResponse response, Exception e) throws IOException {
        logger.error("Error processing request: {}", e.getMessage(), e);

        if (e instanceof ValidationException) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof ProductNotFoundException) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } else if (e instanceof CustomerNotFoundException) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } else if (e instanceof InsufficientStockException) {
            sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } else if (e instanceof InvalidPaymentException) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof InvalidLoginException) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } else if (e instanceof BusinessRuleException) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof RepositoryException) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database error occurred");
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        }
    }

    /**
     * Extracts path info part after servlet path.
     * For example, if URL is /api/products/BVEDRB001, returns "BVEDRB001".
     */
    protected String getPathPart(HttpServletRequest request, int index) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String[] parts = pathInfo.substring(1).split("/");
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }
}
