package com.syos.web.servlet.api;

import com.syos.web.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Base servlet for all API endpoints.
 * Provides common API functionality and error handling.
 */
public abstract class BaseApiServlet extends BaseServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set JSON content type for all API responses
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Add CORS headers for development
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {
            super.service(request, response);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    /**
     * Override to handle GET requests.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET not supported");
    }

    /**
     * Override to handle POST requests.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST not supported");
    }

    /**
     * Override to handle PUT requests.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "PUT not supported");
    }

    /**
     * Override to handle DELETE requests.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "DELETE not supported");
    }
}
