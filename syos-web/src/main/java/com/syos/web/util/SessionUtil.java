package com.syos.web.util;

import com.syos.domain.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

/**
 * Utility class for session management operations.
 * Provides convenient methods for accessing session data.
 */
public final class SessionUtil {

    public static final String USER_ID = "userId";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_NAME = "userName";
    public static final String USER_ROLE = "userRole";
    public static final int DEFAULT_SESSION_TIMEOUT = 30 * 60; // 30 minutes

    private SessionUtil() {
        // Prevent instantiation
    }

    /**
     * Checks if the current request has an authenticated session.
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_ID) != null;
    }

    /**
     * Gets the current user ID from the session.
     */
    public static Optional<Integer> getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object userId = session.getAttribute(USER_ID);
        return userId instanceof Integer ? Optional.of((Integer) userId) : Optional.empty();
    }

    /**
     * Gets the current user email from the session.
     */
    public static Optional<String> getCurrentUserEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((String) session.getAttribute(USER_EMAIL));
    }

    /**
     * Gets the current user name from the session.
     */
    public static Optional<String> getCurrentUserName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((String) session.getAttribute(USER_NAME));
    }

    /**
     * Gets the current user role from the session.
     */
    public static Optional<String> getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((String) session.getAttribute(USER_ROLE));
    }

    /**
     * Checks if the current user has the specified role.
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        return getCurrentUserRole(request)
            .map(r -> r.equals(role))
            .orElse(false);
    }

    /**
     * Checks if the current user has the specified UserRole.
     */
    public static boolean hasRole(HttpServletRequest request, UserRole role) {
        return hasRole(request, role.name());
    }

    /**
     * Gets the current user's UserRole enum value.
     */
    public static Optional<UserRole> getCurrentUserRoleEnum(HttpServletRequest request) {
        return getCurrentUserRole(request).map(UserRole::fromString);
    }

    /**
     * Checks if the current user is a customer.
     */
    public static boolean isCustomer(HttpServletRequest request) {
        return hasRole(request, UserRole.CUSTOMER);
    }

    /**
     * Checks if the current user is a cashier.
     */
    public static boolean isCashier(HttpServletRequest request) {
        return hasRole(request, UserRole.CASHIER);
    }

    /**
     * Checks if the current user is an inventory manager.
     */
    public static boolean isInventoryManager(HttpServletRequest request) {
        return hasRole(request, UserRole.INVENTORY_MANAGER);
    }

    /**
     * Checks if the current user is a manager.
     */
    public static boolean isManager(HttpServletRequest request) {
        return hasRole(request, UserRole.MANAGER);
    }

    /**
     * Checks if the current user is an admin.
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, UserRole.ADMIN);
    }

    /**
     * Checks if the current user can shop (CUSTOMER or ADMIN).
     */
    public static boolean canShop(HttpServletRequest request) {
        return getCurrentUserRoleEnum(request)
            .map(UserRole::canShop)
            .orElse(false);
    }

    /**
     * Checks if the current user can access POS (CASHIER or ADMIN).
     */
    public static boolean canAccessPOS(HttpServletRequest request) {
        return getCurrentUserRoleEnum(request)
            .map(UserRole::canAccessPOS)
            .orElse(false);
    }

    /**
     * Checks if the current user can manage inventory (INVENTORY_MANAGER or ADMIN).
     */
    public static boolean canManageInventory(HttpServletRequest request) {
        return getCurrentUserRoleEnum(request)
            .map(UserRole::canManageInventory)
            .orElse(false);
    }

    /**
     * Checks if the current user can view reports (MANAGER or ADMIN).
     */
    public static boolean canViewReports(HttpServletRequest request) {
        return getCurrentUserRoleEnum(request)
            .map(UserRole::canViewReports)
            .orElse(false);
    }

    /**
     * Checks if the current user can manage users (ADMIN only).
     */
    public static boolean canManageUsers(HttpServletRequest request) {
        return getCurrentUserRoleEnum(request)
            .map(UserRole::canManageUsers)
            .orElse(false);
    }

    /**
     * Creates a new authenticated session for the user.
     */
    public static HttpSession createSession(HttpServletRequest request,
                                             Integer userId,
                                             String email,
                                             String name,
                                             String role) {
        // Invalidate any existing session
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        // Create new session
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, userId);
        session.setAttribute(USER_EMAIL, email);
        session.setAttribute(USER_NAME, name);
        session.setAttribute(USER_ROLE, role);
        session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);

        return session;
    }

    /**
     * Invalidates the current session (logout).
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Extends the session timeout.
     */
    public static void extendSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);
        }
    }

    /**
     * Gets a session attribute of the specified type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getAttribute(HttpServletRequest request, String name, Class<T> type) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object value = session.getAttribute(name);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Sets a session attribute.
     */
    public static void setAttribute(HttpServletRequest request, String name, Object value) {
        HttpSession session = request.getSession(true);
        session.setAttribute(name, value);
    }

    /**
     * Removes a session attribute.
     */
    public static void removeAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }
}
