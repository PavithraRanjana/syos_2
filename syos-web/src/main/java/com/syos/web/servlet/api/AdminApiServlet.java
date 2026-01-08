package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * REST API servlet for admin operations.
 * Handles user management and role assignment.
 * Only accessible by ADMIN role.
 */
@WebServlet(urlPatterns = {"/api/admin/*"})
public class AdminApiServlet extends BaseApiServlet {

    private CustomerService customerService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.customerService = ServiceRegistry.get(CustomerService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : "/";

        try {
            if (path.equals("/users") || path.equals("/users/")) {
                handleListUsers(request, response);
            } else if (path.startsWith("/users/")) {
                String userId = getPathPart(request, 1);
                if (userId != null) {
                    handleGetUser(request, response, userId);
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "User ID required");
                }
            } else if (path.equals("/roles") || path.equals("/roles/")) {
                handleListRoles(response);
            } else if (path.equals("/stats") || path.equals("/stats/")) {
                handleGetStats(response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error processing admin GET request: {}", e.getMessage(), e);
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : "/";

        try {
            if (path.equals("/users") || path.equals("/users/")) {
                handleCreateUser(request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error processing admin POST request: {}", e.getMessage(), e);
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : "/";

        try {
            // PUT /api/admin/users/{id}/role
            if (path.matches("/users/\\d+/role/?")) {
                String userId = getPathPart(request, 1);
                handleUpdateRole(request, response, userId);
            }
            // PUT /api/admin/users/{id}/status
            else if (path.matches("/users/\\d+/status/?")) {
                String userId = getPathPart(request, 1);
                handleUpdateStatus(request, response, userId);
            }
            // PUT /api/admin/users/{id}/password
            else if (path.matches("/users/\\d+/password/?")) {
                String userId = getPathPart(request, 1);
                handleResetPassword(request, response, userId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error processing admin PUT request: {}", e.getMessage(), e);
            handleException(response, e);
        }
    }

    /**
     * GET /api/admin/users - List all users
     */
    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String roleFilter = request.getParameter("role");
        String activeFilter = request.getParameter("active");

        List<Customer> users;

        if (roleFilter != null && !roleFilter.isEmpty()) {
            UserRole role = UserRole.fromString(roleFilter);
            users = customerService.findByRole(role);
        } else if ("true".equalsIgnoreCase(activeFilter)) {
            users = customerService.findAllActive();
        } else {
            users = customerService.findAll();
        }

        List<UserListItem> userList = users.stream()
            .map(c -> new UserListItem(
                c.getCustomerId(),
                c.getCustomerName(),
                c.getEmail(),
                c.getRole().name(),
                c.getRole().getDisplayName(),
                c.isActive(),
                c.getRegistrationDate()
            ))
            .toList();

        sendSuccess(response, userList);
    }

    /**
     * GET /api/admin/users/{id} - Get user details
     */
    private void handleGetUser(HttpServletRequest request, HttpServletResponse response, String userId)
            throws IOException {
        try {
            Integer id = Integer.parseInt(userId);
            var userOpt = customerService.findById(id);

            if (userOpt.isEmpty()) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            Customer c = userOpt.get();
            sendSuccess(response, new UserDetail(
                c.getCustomerId(),
                c.getCustomerName(),
                c.getEmail(),
                c.getPhone(),
                c.getAddress(),
                c.getRole().name(),
                c.getRole().getDisplayName(),
                c.isActive(),
                c.getRegistrationDate(),
                c.getCreatedAt(),
                c.getUpdatedAt()
            ));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    /**
     * GET /api/admin/roles - List available roles
     */
    private void handleListRoles(HttpServletResponse response) throws IOException {
        List<RoleInfo> roles = Arrays.stream(UserRole.values())
            .map(r -> new RoleInfo(r.name(), r.getDisplayName(), r.getDescription()))
            .toList();

        sendSuccess(response, roles);
    }

    /**
     * GET /api/admin/stats - Get admin statistics
     */
    private void handleGetStats(HttpServletResponse response) throws IOException {
        var stats = customerService.getStatistics();

        // Count users by role
        long customerCount = customerService.findByRole(UserRole.CUSTOMER).size();
        long cashierCount = customerService.findByRole(UserRole.CASHIER).size();
        long inventoryManagerCount = customerService.findByRole(UserRole.INVENTORY_MANAGER).size();
        long managerCount = customerService.findByRole(UserRole.MANAGER).size();
        long adminCount = customerService.findByRole(UserRole.ADMIN).size();

        sendSuccess(response, new AdminStats(
            stats.totalCustomers(),
            stats.activeCustomers(),
            stats.newCustomersThisMonth(),
            new RoleCounts(customerCount, cashierCount, inventoryManagerCount, managerCount, adminCount)
        ));
    }

    /**
     * POST /api/admin/users - Create new user with role
     */
    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        CreateUserRequest createRequest = parseRequestBody(request, CreateUserRequest.class);

        if (createRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        // Validate required fields
        if (createRequest.name == null || createRequest.name.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Name is required");
            return;
        }
        if (createRequest.email == null || createRequest.email.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }
        if (createRequest.password == null || createRequest.password.length() < 6) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters");
            return;
        }
        if (createRequest.role == null || createRequest.role.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Role is required");
            return;
        }

        UserRole role = UserRole.fromString(createRequest.role);

        Customer customer = customerService.createUserWithRole(
            createRequest.name,
            createRequest.email,
            createRequest.phone,
            createRequest.address,
            createRequest.password,
            role
        );

        logger.info("Admin created new user: {} with role {}", customer.getEmail(), role);

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, new UserListItem(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getEmail(),
            customer.getRole().name(),
            customer.getRole().getDisplayName(),
            customer.isActive(),
            customer.getRegistrationDate()
        ), "User created successfully");
    }

    /**
     * PUT /api/admin/users/{id}/role - Update user role
     */
    private void handleUpdateRole(HttpServletRequest request, HttpServletResponse response, String userId)
            throws IOException {
        try {
            Integer id = Integer.parseInt(userId);

            UpdateRoleRequest updateRequest = parseRequestBody(request, UpdateRoleRequest.class);

            if (updateRequest == null || updateRequest.role == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Role is required");
                return;
            }

            UserRole newRole = UserRole.fromString(updateRequest.role);

            // Prevent admin from changing their own role
            Integer currentUserId = getCurrentUserId(request);
            if (currentUserId != null && currentUserId.equals(id)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot change your own role");
                return;
            }

            boolean updated = customerService.updateUserRole(id, newRole);

            if (updated) {
                logger.info("Admin updated role for user {} to {}", id, newRole);
                sendSuccess(response, null, "Role updated to " + newRole.getDisplayName());
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update role");
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    /**
     * PUT /api/admin/users/{id}/status - Activate/deactivate user
     */
    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response, String userId)
            throws IOException {
        try {
            Integer id = Integer.parseInt(userId);

            UpdateStatusRequest updateRequest = parseRequestBody(request, UpdateStatusRequest.class);

            if (updateRequest == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
                return;
            }

            // Prevent admin from deactivating themselves
            Integer currentUserId = getCurrentUserId(request);
            if (currentUserId != null && currentUserId.equals(id) && !updateRequest.active) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot deactivate your own account");
                return;
            }

            boolean success;
            if (updateRequest.active) {
                success = customerService.activateAccount(id);
            } else {
                success = customerService.deactivateAccount(id);
            }

            if (success) {
                String status = updateRequest.active ? "activated" : "deactivated";
                logger.info("Admin {} user {}", status, id);
                sendSuccess(response, null, "User " + status);
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update status");
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    /**
     * PUT /api/admin/users/{id}/password - Reset user password
     */
    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, String userId)
            throws IOException {
        try {
            Integer id = Integer.parseInt(userId);

            ResetPasswordRequest resetRequest = parseRequestBody(request, ResetPasswordRequest.class);

            if (resetRequest == null || resetRequest.newPassword == null || resetRequest.newPassword.length() < 6) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters");
                return;
            }

            boolean success = customerService.resetPassword(id, resetRequest.newPassword);

            if (success) {
                logger.info("Admin reset password for user {}", id);
                sendSuccess(response, null, "Password reset successfully");
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to reset password");
            }
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    // ==================== Request/Response DTOs ====================

    static class CreateUserRequest {
        public String name;
        public String email;
        public String phone;
        public String address;
        public String password;
        public String role;
    }

    static class UpdateRoleRequest {
        public String role;
    }

    static class UpdateStatusRequest {
        public boolean active;
    }

    static class ResetPasswordRequest {
        public String newPassword;
    }

    record UserListItem(
        Integer id,
        String name,
        String email,
        String role,
        String roleDisplayName,
        boolean active,
        LocalDate registrationDate
    ) {}

    record UserDetail(
        Integer id,
        String name,
        String email,
        String phone,
        String address,
        String role,
        String roleDisplayName,
        boolean active,
        LocalDate registrationDate,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {}

    record RoleInfo(
        String value,
        String displayName,
        String description
    ) {}

    record AdminStats(
        long totalUsers,
        long activeUsers,
        long newUsersThisMonth,
        RoleCounts byRole
    ) {}

    record RoleCounts(
        long customers,
        long cashiers,
        long inventoryManagers,
        long managers,
        long admins
    ) {}
}
