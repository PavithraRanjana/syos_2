package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.AuthenticationResult;
import com.syos.service.interfaces.CustomerService.CustomerStatistics;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API servlet for Customer operations.
 *
 * Endpoints:
 * POST /api/customers/register           - Register new customer
 * POST /api/customers/login              - Authenticate customer
 * POST /api/customers/logout             - Logout customer
 * GET  /api/customers/me                 - Get current logged-in customer
 * GET  /api/customers                    - List all customers
 * GET  /api/customers/search             - Search customers
 * GET  /api/customers/statistics         - Get customer statistics
 * GET  /api/customers/{id}               - Get customer by ID
 * PUT  /api/customers/{id}               - Update customer profile
 * PUT  /api/customers/{id}/password      - Change password
 * PUT  /api/customers/{id}/activate      - Activate customer
 * PUT  /api/customers/{id}/deactivate    - Deactivate customer
 * POST /api/customers/{id}/reset-password - Reset password (admin)
 *
 * Validation endpoints:
 * GET  /api/customers/check-email        - Check if email is available
 * GET  /api/customers/check-phone        - Check if phone is available
 */
@WebServlet(urlPatterns = {"/api/customers", "/api/customers/*"})
public class CustomerApiServlet extends BaseApiServlet {

    private CustomerService customerService;

    @Override
    public void init() throws ServletException {
        super.init();
        customerService = ServiceRegistry.get(CustomerService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/customers - List all customers
                handleListCustomers(request, response);
            } else if (pathInfo.equals("/me")) {
                // GET /api/customers/me - Get current user
                handleGetCurrentUser(request, response);
            } else if (pathInfo.equals("/search")) {
                // GET /api/customers/search?q=...
                handleSearchCustomers(request, response);
            } else if (pathInfo.equals("/statistics")) {
                // GET /api/customers/statistics
                handleGetStatistics(response);
            } else if (pathInfo.equals("/check-email")) {
                // GET /api/customers/check-email?email=...
                handleCheckEmail(request, response);
            } else if (pathInfo.equals("/check-phone")) {
                // GET /api/customers/check-phone?phone=...
                handleCheckPhone(request, response);
            } else {
                // GET /api/customers/{id}
                String customerId = getPathPart(request, 0);
                handleGetCustomer(customerId, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Use /register, /login, or /logout");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");

            if (parts[0].equals("register")) {
                handleRegister(request, response);
            } else if (parts[0].equals("login")) {
                handleLogin(request, response);
            } else if (parts[0].equals("logout")) {
                handleLogout(request, response);
            } else if (parts.length == 2 && parts[1].equals("reset-password")) {
                // POST /api/customers/{id}/reset-password
                handleResetPassword(parts[0], request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Customer ID required");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            String customerIdStr = parts[0];

            Integer customerId = parseIntOrNull(customerIdStr);
            if (customerId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid customer ID");
                return;
            }

            if (parts.length == 1) {
                // PUT /api/customers/{id} - Update profile
                handleUpdateProfile(customerId, request, response);
            } else {
                switch (parts[1]) {
                    case "password" -> handleChangePassword(customerId, request, response);
                    case "activate" -> handleActivate(customerId, response);
                    case "deactivate" -> handleDeactivate(customerId, response);
                    default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
                }
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    // ==================== GET Handlers ====================

    private void handleListCustomers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 50);
        String activeOnly = request.getParameter("active");

        List<Customer> customers;
        if ("true".equalsIgnoreCase(activeOnly)) {
            customers = customerService.findAllActive();
        } else {
            customers = customerService.findAll(page, size);
        }

        List<CustomerResponse> responses = customers.stream()
            .map(CustomerResponse::fromCustomer)
            .toList();

        sendSuccess(response, Map.of(
            "customers", responses,
            "count", responses.size(),
            "page", page,
            "size", size,
            "total", customerService.getCustomerCount()
        ));
    }

    private void handleGetCurrentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("customerId");
        Optional<Customer> customer = customerService.findById(customerId);

        if (customer.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
            return;
        }

        sendSuccess(response, CustomerResponse.fromCustomer(customer.get()));
    }

    private void handleSearchCustomers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String query = getOptionalParameter(request, "q", "");
        if (query.isEmpty()) {
            sendSuccess(response, Map.of("customers", List.of(), "count", 0));
            return;
        }

        List<Customer> customers = customerService.searchByName(query);
        List<CustomerResponse> responses = customers.stream()
            .map(CustomerResponse::fromCustomer)
            .toList();

        sendSuccess(response, Map.of(
            "customers", responses,
            "count", responses.size(),
            "query", query
        ));
    }

    private void handleGetStatistics(HttpServletResponse response) throws IOException {
        CustomerStatistics stats = customerService.getStatistics();
        sendSuccess(response, Map.of(
            "totalCustomers", stats.totalCustomers(),
            "activeCustomers", stats.activeCustomers(),
            "newCustomersThisMonth", stats.newCustomersThisMonth()
        ));
    }

    private void handleCheckEmail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String email = request.getParameter("email");
        if (email == null || email.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }

        boolean available = customerService.isEmailAvailable(email);
        sendSuccess(response, Map.of("email", email, "available", available));
    }

    private void handleCheckPhone(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String phone = request.getParameter("phone");
        if (phone == null || phone.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Phone is required");
            return;
        }

        boolean available = customerService.isPhoneAvailable(phone);
        sendSuccess(response, Map.of("phone", phone, "available", available));
    }

    private void handleGetCustomer(String customerIdStr, HttpServletResponse response)
            throws IOException {
        Integer customerId = parseIntOrNull(customerIdStr);
        if (customerId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid customer ID");
            return;
        }

        Optional<Customer> customer = customerService.findById(customerId);
        if (customer.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "Customer not found: " + customerId);
            return;
        }

        sendSuccess(response, CustomerResponse.fromCustomer(customer.get()));
    }

    // ==================== POST Handlers ====================

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        RegisterRequest registerRequest = parseRequestBody(request, RegisterRequest.class);
        if (registerRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        if (registerRequest.name == null || registerRequest.email == null ||
            registerRequest.password == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Name, email, and password are required");
            return;
        }

        Customer customer = customerService.register(
            registerRequest.name,
            registerRequest.email,
            registerRequest.phone,
            registerRequest.address,
            registerRequest.password
        );

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, CustomerResponse.fromCustomer(customer),
            "Registration successful");
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LoginRequest loginRequest = parseRequestBody(request, LoginRequest.class);
        if (loginRequest == null || loginRequest.email == null || loginRequest.password == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Email and password are required");
            return;
        }

        AuthenticationResult result = customerService.authenticate(
            loginRequest.email, loginRequest.password);

        if (result.success()) {
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("customerId", result.customer().getCustomerId());
            session.setAttribute("customerEmail", result.customer().getEmail());
            session.setAttribute("customerName", result.customer().getCustomerName());

            sendSuccess(response, Map.of(
                "customer", CustomerResponse.fromCustomer(result.customer()),
                "sessionId", session.getId()
            ), "Login successful");
        } else {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, result.errorMessage());
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(response, Map.of("loggedOut", true), "Logout successful");
    }

    private void handleResetPassword(String customerIdStr, HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        Integer customerId = parseIntOrNull(customerIdStr);
        if (customerId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid customer ID");
            return;
        }

        ResetPasswordRequest resetRequest = parseRequestBody(request, ResetPasswordRequest.class);
        if (resetRequest == null || resetRequest.newPassword == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "New password is required");
            return;
        }

        boolean reset = customerService.resetPassword(customerId, resetRequest.newPassword);
        if (reset) {
            sendSuccess(response, Map.of("customerId", customerId, "passwordReset", true),
                "Password reset successfully");
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to reset password");
        }
    }

    // ==================== PUT Handlers ====================

    private void handleUpdateProfile(Integer customerId, HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        UpdateProfileRequest updateRequest = parseRequestBody(request, UpdateProfileRequest.class);
        if (updateRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        Customer updated = customerService.updateProfile(
            customerId,
            updateRequest.name,
            updateRequest.phone,
            updateRequest.address
        );

        sendSuccess(response, CustomerResponse.fromCustomer(updated), "Profile updated");
    }

    private void handleChangePassword(Integer customerId, HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        ChangePasswordRequest changeRequest = parseRequestBody(request, ChangePasswordRequest.class);
        if (changeRequest == null || changeRequest.currentPassword == null ||
            changeRequest.newPassword == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Current password and new password are required");
            return;
        }

        boolean changed = customerService.changePassword(
            customerId, changeRequest.currentPassword, changeRequest.newPassword);

        if (changed) {
            sendSuccess(response, Map.of("customerId", customerId, "passwordChanged", true),
                "Password changed successfully");
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Failed to change password. Check current password.");
        }
    }

    private void handleActivate(Integer customerId, HttpServletResponse response)
            throws IOException {
        boolean activated = customerService.activateAccount(customerId);
        if (activated) {
            sendSuccess(response, Map.of("customerId", customerId, "active", true),
                "Account activated");
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
        }
    }

    private void handleDeactivate(Integer customerId, HttpServletResponse response)
            throws IOException {
        boolean deactivated = customerService.deactivateAccount(customerId);
        if (deactivated) {
            sendSuccess(response, Map.of("customerId", customerId, "active", false),
                "Account deactivated");
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
        }
    }

    // ==================== Helper Methods ====================

    private Integer parseIntOrNull(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== Request/Response DTOs ====================

    public static class RegisterRequest {
        public String name;
        public String email;
        public String phone;
        public String address;
        public String password;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class UpdateProfileRequest {
        public String name;
        public String phone;
        public String address;
    }

    public static class ChangePasswordRequest {
        public String currentPassword;
        public String newPassword;
    }

    public static class ResetPasswordRequest {
        public String newPassword;
    }

    public record CustomerResponse(
        Integer customerId,
        String name,
        String email,
        String phone,
        String address,
        boolean active,
        LocalDate registrationDate,
        LocalDateTime createdAt
    ) {
        public static CustomerResponse fromCustomer(Customer customer) {
            return new CustomerResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.isActive(),
                customer.getRegistrationDate(),
                customer.getCreatedAt()
            );
        }
    }
}
