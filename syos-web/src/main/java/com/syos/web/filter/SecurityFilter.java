package com.syos.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Security filter for authentication and authorization.
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    // Paths that don't require authentication
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/",
        "/index.jsp",
        "/login",
        "/register",
        "/api/auth/login",
        "/api/auth/register",
        "/static",
        "/css",
        "/js",
        "/images",
        "/error"
    );

    // Paths that require specific roles
    private static final Set<String> CASHIER_PATHS = Set.of("/cashier", "/api/billing");
    private static final Set<String> INVENTORY_PATHS = Set.of("/inventory", "/api/inventory");
    private static final Set<String> MANAGER_PATHS = Set.of("/manager", "/api/reports");
    private static final Set<String> ONLINE_PATHS = Set.of("/shop", "/cart", "/checkout", "/api/online");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Security Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Allow public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow static resources
        if (path.startsWith("/static/") || path.endsWith(".css") ||
            path.endsWith(".js") || path.endsWith(".png") ||
            path.endsWith(".jpg") || path.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // Check session authentication
        HttpSession session = httpRequest.getSession(false);

        // For now, allow all requests until authentication is implemented
        // This will be updated when the authentication system is complete
        if (session == null || session.getAttribute("userId") == null) {
            // For API requests, return JSON error
            if (path.startsWith("/api/")) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"error\":\"Authentication required\"}"
                );
                return;
            }

            // For view requests during development, allow access
            // TODO: Uncomment redirect when auth is implemented
            // httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            // return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Checks if the path is publicly accessible.
     */
    private boolean isPublicPath(String path) {
        if (path.equals("/") || path.isEmpty()) {
            return true;
        }

        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        logger.info("Security Filter destroyed");
    }
}
