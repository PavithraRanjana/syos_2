package com.syos.web.filter;

import com.syos.domain.enums.UserRole;
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
 * Security filter for role-based access control.
 *
 * Roles and their access:
 * - CUSTOMER: Shop, cart, checkout, orders, profile
 * - CASHIER: POS transactions, product search (view-only), NO shopping
 * - INVENTORY_MANAGER: All inventory functions
 * - MANAGER: View all reports, NO inventory functions
 * - ADMIN: Assign roles + full access
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    // Paths accessible without authentication
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/",
            "/index.jsp",
            "/login",
            "/register",
            "/logout",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/auth/status",
            "/api/products",
            "/api/categories",
            "/api/store-inventory/online", // Allow public stock check for online store
            "/shop");

    // Paths for CUSTOMER role (shopping)
    private static final Set<String> CUSTOMER_PATHS = Set.of(
            "/cart",
            "/checkout",
            "/orders",
            "/profile",
            "/api/cart",
            "/api/orders",
            "/api/auth/me");

    // Paths for CASHIER role (POS only - uses /pos/stock for physical stock)
    private static final Set<String> CASHIER_PATHS = Set.of(
            "/pos",
            "/api/billing");

    // Paths for INVENTORY_MANAGER role
    private static final Set<String> INVENTORY_PATHS = Set.of(
            "/inventory",
            "/store-stock",
            "/api/inventory",
            "/api/store-inventory",
            "/reports/reshelve",
            "/reports/reorder-level",
            "/reports/batch-stock");

    // Paths for MANAGER role (reports only)
    private static final Set<String> MANAGER_PATHS = Set.of(
            "/reports",
            "/api/reports");

    // Paths for ADMIN role only
    private static final Set<String> ADMIN_PATHS = Set.of(
            "/admin",
            "/api/admin",
            "/dashboard",
            "/products",
            "/customers");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Security Filter initialized with RBAC");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Allow static resources
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check session authentication
        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute("userId") != null;

        if (!isAuthenticated) {
            handleUnauthenticated(httpRequest, httpResponse, path);
            return;
        }

        // Get user role from session
        String roleStr = (String) session.getAttribute("userRole");
        UserRole userRole = UserRole.fromString(roleStr);

        // Check role-based access
        if (!hasAccess(path, userRole)) {
            handleUnauthorized(httpRequest, httpResponse, path, userRole);
            return;
        }

        // Add user info to request for convenience
        httpRequest.setAttribute("currentUserId", session.getAttribute("userId"));
        httpRequest.setAttribute("currentUserName", session.getAttribute("userName"));
        httpRequest.setAttribute("currentUserEmail", session.getAttribute("userEmail"));
        httpRequest.setAttribute("currentUserRole", userRole.name());

        chain.doFilter(request, response);
    }

    /**
     * Checks if the path is a static resource.
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".gif") ||
                path.endsWith(".svg") ||
                path.endsWith(".ico") ||
                path.endsWith(".woff") ||
                path.endsWith(".woff2");
    }

    /**
     * Checks if the path is publicly accessible.
     */
    private boolean isPublicPath(String path) {
        if (path.equals("/") || path.isEmpty()) {
            return true;
        }

        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the user role has access to the given path.
     */
    private boolean hasAccess(String path, UserRole role) {
        // ADMIN has access to everything
        if (role == UserRole.ADMIN) {
            return true;
        }

        // Check admin-only paths (only ADMIN can access)
        if (isInPathSet(path, ADMIN_PATHS)) {
            return false; // Non-admin cannot access admin paths
        }

        // Check role-specific access
        switch (role) {
            case CUSTOMER:
                // Customers can only access customer paths
                return isInPathSet(path, CUSTOMER_PATHS);

            case CASHIER:
                // Cashiers can access POS paths and view products (already public)
                // They CANNOT access customer shopping paths
                return isInPathSet(path, CASHIER_PATHS);

            case INVENTORY_MANAGER:
                // Inventory managers can only access inventory paths
                return isInPathSet(path, INVENTORY_PATHS);

            case MANAGER:
                // Managers can only access report paths
                return isInPathSet(path, MANAGER_PATHS);

            default:
                return false;
        }
    }

    /**
     * Checks if a path matches any path in the given set.
     */
    private boolean isInPathSet(String path, Set<String> pathSet) {
        for (String allowedPath : pathSet) {
            if (path.equals(allowedPath) || path.startsWith(allowedPath + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles unauthenticated requests.
     */
    private void handleUnauthenticated(HttpServletRequest request,
            HttpServletResponse response,
            String path) throws IOException {
        logger.debug("Unauthenticated access attempt to: {}", path);

        if (path.startsWith("/api/")) {
            // Return JSON error for API requests
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Authentication required\",\"code\":\"UNAUTHORIZED\"}");
        } else {
            // Redirect to login for view requests, preserving the intended destination
            String redirectUrl = request.getContextPath() + "/login";
            String originalUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                originalUrl += "?" + request.getQueryString();
            }
            redirectUrl += "?redirect=" + java.net.URLEncoder.encode(originalUrl, "UTF-8");
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Handles unauthorized (insufficient permissions) requests.
     */
    private void handleUnauthorized(HttpServletRequest request,
            HttpServletResponse response,
            String path,
            UserRole userRole) throws IOException {
        logger.warn("Unauthorized access attempt to: {} by user: {} (role: {})",
                path, request.getSession().getAttribute("userEmail"), userRole);

        if (path.startsWith("/api/")) {
            // Return JSON error for API requests
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Access denied. Your role (" + userRole.getDisplayName()
                            + ") does not have permission to access this resource.\",\"code\":\"FORBIDDEN\"}");
        } else {
            // Redirect based on role
            String redirectPath = getDefaultRedirectForRole(userRole);
            response.sendRedirect(request.getContextPath() + redirectPath + "?error=access_denied");
        }
    }

    /**
     * Gets the default redirect path for a given role.
     */
    private String getDefaultRedirectForRole(UserRole role) {
        return switch (role) {
            case CUSTOMER -> "/shop";
            case CASHIER -> "/pos";
            case INVENTORY_MANAGER -> "/inventory/reports";
            case MANAGER -> "/reports";
            case ADMIN -> "/admin";
        };
    }

    @Override
    public void destroy() {
        logger.info("Security Filter destroyed");
    }
}
