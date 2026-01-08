package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * View servlet for admin dashboard and user management.
 * Only accessible by ADMIN role.
 */
@WebServlet(urlPatterns = {"/admin", "/admin/*"})
public class AdminViewServlet extends BaseViewServlet {

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

        setActiveNav(request, "admin");

        switch (path) {
            case "/", "" -> showDashboard(request, response);
            case "/users" -> showUsers(request, response);
            default -> {
                if (path.startsWith("/users/")) {
                    String userId = getPathSegment(request, 1);
                    if (userId != null) {
                        showUserDetail(request, response, userId);
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
    }

    /**
     * Shows admin dashboard with statistics and quick actions.
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setPageTitle(request, "Admin Dashboard");

        // Get statistics
        var stats = customerService.getStatistics();
        request.setAttribute("stats", stats);

        // Get user counts by role
        long customerCount = customerService.findByRole(UserRole.CUSTOMER).size();
        long cashierCount = customerService.findByRole(UserRole.CASHIER).size();
        long inventoryManagerCount = customerService.findByRole(UserRole.INVENTORY_MANAGER).size();
        long managerCount = customerService.findByRole(UserRole.MANAGER).size();
        long adminCount = customerService.findByRole(UserRole.ADMIN).size();

        request.setAttribute("customerCount", customerCount);
        request.setAttribute("cashierCount", cashierCount);
        request.setAttribute("inventoryManagerCount", inventoryManagerCount);
        request.setAttribute("managerCount", managerCount);
        request.setAttribute("adminCount", adminCount);

        // Get all roles for dropdown
        request.setAttribute("roles", Arrays.asList(UserRole.values()));

        // Get all users for the table
        String roleFilter = getStringParameter(request, "role", null);
        List<Customer> users;
        if (roleFilter != null && !roleFilter.isEmpty()) {
            users = customerService.findByRole(UserRole.fromString(roleFilter));
            request.setAttribute("selectedRole", roleFilter);
        } else {
            users = customerService.findAll();
        }
        request.setAttribute("users", users);
        request.setAttribute("totalUsers", customerService.getCustomerCount());

        render(request, response, "admin/index.jsp");
    }

    /**
     * Shows user list page.
     */
    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setPageTitle(request, "User Management");

        String roleFilter = getStringParameter(request, "role", null);
        List<Customer> users;
        if (roleFilter != null && !roleFilter.isEmpty()) {
            users = customerService.findByRole(UserRole.fromString(roleFilter));
            request.setAttribute("selectedRole", roleFilter);
        } else {
            users = customerService.findAll();
        }

        request.setAttribute("users", users);
        request.setAttribute("roles", Arrays.asList(UserRole.values()));
        request.setAttribute("totalUsers", customerService.getCustomerCount());

        render(request, response, "admin/users.jsp");
    }

    /**
     * Shows user detail page.
     */
    private void showUserDetail(HttpServletRequest request, HttpServletResponse response, String userId)
            throws ServletException, IOException {
        try {
            Integer id = Integer.parseInt(userId);
            var userOpt = customerService.findById(id);

            if (userOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            setPageTitle(request, "User Details");
            request.setAttribute("user", userOpt.get());
            request.setAttribute("roles", Arrays.asList(UserRole.values()));

            render(request, response, "admin/user-detail.jsp");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }
}
