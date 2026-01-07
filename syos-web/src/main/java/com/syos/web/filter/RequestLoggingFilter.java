package com.syos.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for logging HTTP requests and responses.
 * Adds request IDs for tracing.
 */
@WebFilter(urlPatterns = "/*")
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Request Logging Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip static resources
        String path = httpRequest.getRequestURI();
        if (path.endsWith(".css") || path.endsWith(".js") ||
            path.endsWith(".png") || path.endsWith(".jpg") ||
            path.endsWith(".ico") || path.contains("/static/")) {
            chain.doFilter(request, response);
            return;
        }

        // Generate or use existing request ID
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Add request ID to response header
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        // Add to MDC for logging
        MDC.put("requestId", requestId);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            if (logger.isDebugEnabled()) {
                logger.debug("Request: {} {} from {} [{}]",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpRequest.getRemoteAddr(),
                    requestId
                );
            }

            // Process request
            chain.doFilter(request, response);

        } finally {
            // Log response
            long duration = System.currentTimeMillis() - startTime;

            if (logger.isDebugEnabled()) {
                logger.debug("Response: {} {} - {} ({} ms) [{}]",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration,
                    requestId
                );
            }

            // Log slow requests at WARN level
            if (duration > 1000) {
                logger.warn("Slow request: {} {} took {} ms [{}]",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    duration,
                    requestId
                );
            }

            // Clean up MDC
            MDC.remove("requestId");
        }
    }

    @Override
    public void destroy() {
        logger.info("Request Logging Filter destroyed");
    }
}
