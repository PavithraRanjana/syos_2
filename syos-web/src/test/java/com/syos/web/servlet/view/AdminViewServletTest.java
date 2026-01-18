package com.syos.web.servlet.view;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.CustomerStatistics;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private CustomerService customerService;

    private AdminViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminViewServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = AdminViewServlet.class.getDeclaredField("customerService");
        field.setAccessible(true);
        field.set(servlet, customerService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private Customer createTestUser(Integer id, String name, String email, UserRole role) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setRole(role);
        customer.setActive(true);
        customer.setRegistrationDate(LocalDate.now());
        return customer;
    }

    @Nested
    @DisplayName("doGet tests - Dashboard")
    class DoGetDashboardTests {

        @Test
        @DisplayName("Should show dashboard with null path")
        void shouldShowDashboardWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("role")).thenReturn(null);

            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(100, 90, 10));
            when(customerService.findByRole(any())).thenReturn(List.of());
            when(customerService.findAll()).thenReturn(List.of());
            when(customerService.getCustomerCount()).thenReturn(100L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).getStatistics();
            verify(request).getRequestDispatcher("/WEB-INF/views/admin/index.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should show dashboard with root path")
        void shouldShowDashboardWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("role")).thenReturn(null);

            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(50, 45, 5));
            when(customerService.findByRole(any())).thenReturn(List.of());
            when(customerService.findAll()).thenReturn(List.of());
            when(customerService.getCustomerCount()).thenReturn(50L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/admin/index.jsp");
        }

        @Test
        @DisplayName("Should filter users by role on dashboard")
        void shouldFilterUsersByRoleOnDashboard() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("role")).thenReturn("CASHIER");

            Customer cashier = createTestUser(1, "John", "john@test.com", UserRole.CASHIER);
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(10, 9, 1));
            when(customerService.findByRole(any())).thenReturn(List.of());
            when(customerService.findByRole(UserRole.CASHIER)).thenReturn(List.of(cashier));
            when(customerService.getCustomerCount()).thenReturn(10L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute("selectedRole", "CASHIER");
        }
    }

    @Nested
    @DisplayName("doGet tests - Users List")
    class DoGetUsersListTests {

        @Test
        @DisplayName("Should show users list")
        void shouldShowUsersList() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            when(request.getParameter("role")).thenReturn(null);

            when(customerService.findAll()).thenReturn(List.of());
            when(customerService.getCustomerCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll();
            verify(request).getRequestDispatcher("/WEB-INF/views/admin/users.jsp");
        }

        @Test
        @DisplayName("Should filter users list by role")
        void shouldFilterUsersListByRole() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            when(request.getParameter("role")).thenReturn("ADMIN");

            Customer admin = createTestUser(1, "Admin", "admin@test.com", UserRole.ADMIN);
            when(customerService.findByRole(UserRole.ADMIN)).thenReturn(List.of(admin));
            when(customerService.getCustomerCount()).thenReturn(1L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findByRole(UserRole.ADMIN);
            verify(request).setAttribute("selectedRole", "ADMIN");
        }
    }

    @Nested
    @DisplayName("doGet tests - User Detail")
    class DoGetUserDetailTests {

        @Test
        @DisplayName("Should show user detail")
        void shouldShowUserDetail() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123");

            Customer user = createTestUser(123, "John Doe", "john@test.com", UserRole.CUSTOMER);
            when(customerService.findById(123)).thenReturn(Optional.of(user));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(123);
            verify(request).setAttribute("user", user);
            verify(request).getRequestDispatcher("/WEB-INF/views/admin/user-detail.jsp");
        }

        @Test
        @DisplayName("Should return 404 for user not found")
        void shouldReturn404ForUserNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/999");
            when(customerService.findById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID")
        void shouldReturn400ForInvalidUserId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
        }
    }

    @Nested
    @DisplayName("doGet tests - Error Handling")
    class DoGetErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for unknown path")
        void shouldReturn404ForUnknownPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
