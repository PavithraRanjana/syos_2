package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.AuthenticationResult;
import com.syos.web.dto.request.LoginRequest;
import com.syos.web.dto.request.CustomerRegistrationRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * REST API servlet for authentication operations.
 * Handles user login, logout, and session management.
 */
@WebServlet(urlPatterns = {"/api/auth/*"})
public class AuthApiServlet extends BaseApiServlet {

    private CustomerService customerService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.customerService = ServiceRegistry.get(CustomerService.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : "/";

        try {
            switch (path) {
                case "/login" -> handleLogin(request, response);
                case "/logout" -> handleLogout(request, response);
                case "/register" -> handleRegister(request, response);
                default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error processing auth request: {}", e.getMessage(), e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : "/";

        try {
            switch (path) {
                case "/status" -> handleStatus(request, response);
                case "/me" -> handleCurrentUser(request, response);
                default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            logger.error("Error processing auth request: {}", e.getMessage(), e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving auth status");
        }
    }

    /**
     * POST /api/auth/login - Authenticate user
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LoginRequest loginRequest = parseRequestBody(request, LoginRequest.class);

        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
            return;
        }

        AuthenticationResult result = customerService.authenticate(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        );

        if (result.success()) {
            Customer customer = result.customer();

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", customer.getCustomerId());
            session.setAttribute("userEmail", customer.getEmail());
            session.setAttribute("userName", customer.getCustomerName());
            session.setAttribute("userRole", customer.getRoleName());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            logger.info("User logged in: {} (ID: {}, Role: {})", customer.getEmail(), customer.getCustomerId(), customer.getRoleName());

            sendSuccess(response, new LoginResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getRoleName(),
                session.getId()
            ), "Login successful");
        } else {
            logger.warn("Login failed for email: {}", loginRequest.getEmail());
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, result.errorMessage());
        }
    }

    /**
     * POST /api/auth/logout - End user session
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String email = (String) session.getAttribute("userEmail");
            session.invalidate();
            logger.info("User logged out: {}", email);
        }

        sendSuccess(response, null, "Logout successful");
    }

    /**
     * POST /api/auth/register - Register new customer
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        CustomerRegistrationRequest registerRequest = parseRequestBody(request, CustomerRegistrationRequest.class);

        if (registerRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        // Validate required fields
        if (registerRequest.getCustomerName() == null || registerRequest.getCustomerName().trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Name is required");
            return;
        }
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters");
            return;
        }

        try {
            Customer customer = customerService.register(
                registerRequest.getCustomerName(),
                registerRequest.getEmail(),
                registerRequest.getPhone(),
                registerRequest.getAddress(),
                registerRequest.getPassword()
            );

            // Auto-login after registration
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", customer.getCustomerId());
            session.setAttribute("userEmail", customer.getEmail());
            session.setAttribute("userName", customer.getCustomerName());
            session.setAttribute("userRole", customer.getRoleName());
            session.setMaxInactiveInterval(30 * 60);

            logger.info("New customer registered and logged in: {} (ID: {}, Role: {})",
                customer.getEmail(), customer.getCustomerId(), customer.getRoleName());

            response.setStatus(HttpServletResponse.SC_CREATED);
            sendSuccess(response, new LoginResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getRoleName(),
                session.getId()
            ), "Registration successful");

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * GET /api/auth/status - Check authentication status
     */
    private void handleStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            sendSuccess(response, new AuthStatus(
                true,
                (Integer) session.getAttribute("userId"),
                (String) session.getAttribute("userName"),
                (String) session.getAttribute("userEmail"),
                (String) session.getAttribute("userRole")
            ), null);
        } else {
            sendSuccess(response, new AuthStatus(false, null, null, null, null), null);
        }
    }

    /**
     * GET /api/auth/me - Get current user details
     */
    private void handleCurrentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        var customerOpt = customerService.findById(userId);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            sendSuccess(response, new UserInfo(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate(),
                customer.getRoleName()
            ), null);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    // ==================== Response DTOs ====================

    record LoginResponse(
        Integer userId,
        String name,
        String email,
        String role,
        String sessionId
    ) {}

    record AuthStatus(
        boolean authenticated,
        Integer userId,
        String name,
        String email,
        String role
    ) {}

    record UserInfo(
        Integer userId,
        String name,
        String email,
        String phone,
        String address,
        java.time.LocalDate registrationDate,
        String role
    ) {}
}
