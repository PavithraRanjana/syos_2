package com.syos.web.servlet.api;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.CustomerStatistics;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private CustomerService customerService;

    private AdminApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminApiServlet();
        // Inject mock CustomerService via reflection
        java.lang.reflect.Field field = AdminApiServlet.class.getDeclaredField("customerService");
        field.setAccessible(true);
        field.set(servlet, customerService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    private Customer createTestCustomer(Integer id, String name, String email, UserRole role) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setPhone("1234567890");
        customer.setAddress("123 Test St");
        customer.setRole(role);
        customer.setActive(true);
        customer.setRegistrationDate(LocalDate.now());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    @Nested
    @DisplayName("doGet tests - List Users")
    class DoGetListUsersTests {

        @Test
        @DisplayName("Should list all users")
        void shouldListAllUsers() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            when(request.getParameter("role")).thenReturn(null);
            when(request.getParameter("active")).thenReturn(null);

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com", UserRole.CUSTOMER);
            when(customerService.findAll()).thenReturn(List.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("John Doe");
        }

        @Test
        @DisplayName("Should list users with trailing slash")
        void shouldListUsersWithTrailingSlash() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/");
            when(request.getParameter("role")).thenReturn(null);
            when(request.getParameter("active")).thenReturn(null);

            when(customerService.findAll()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll();
        }

        @Test
        @DisplayName("Should filter users by role")
        void shouldFilterUsersByRole() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            when(request.getParameter("role")).thenReturn("CASHIER");
            when(request.getParameter("active")).thenReturn(null);

            Customer cashier = createTestCustomer(1, "Cashier User", "cashier@test.com", UserRole.CASHIER);
            when(customerService.findByRole(UserRole.CASHIER)).thenReturn(List.of(cashier));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findByRole(UserRole.CASHIER);
        }

        @Test
        @DisplayName("Should filter active users only")
        void shouldFilterActiveUsersOnly() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            when(request.getParameter("role")).thenReturn(null);
            when(request.getParameter("active")).thenReturn("true");

            Customer activeUser = createTestCustomer(1, "Active User", "active@test.com", UserRole.CUSTOMER);
            when(customerService.findAllActive()).thenReturn(List.of(activeUser));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAllActive();
        }
    }

    @Nested
    @DisplayName("doGet tests - Get User")
    class DoGetUserTests {

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123");

            Customer customer = createTestCustomer(123, "John Doe", "john@test.com", UserRole.CUSTOMER);
            when(customerService.findById(123)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(123);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("john@test.com");
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/999");
            when(customerService.findById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID")
        void shouldReturn400ForInvalidUserId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doGet tests - Roles and Stats")
    class DoGetRolesAndStatsTests {

        @Test
        @DisplayName("Should list all roles")
        void shouldListAllRoles() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/roles");

            // Act
            servlet.doGet(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("CUSTOMER");
            assert output.contains("ADMIN");
        }

        @Test
        @DisplayName("Should list roles with trailing slash")
        void shouldListRolesWithTrailingSlash() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/roles/");

            // Act
            servlet.doGet(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("CASHIER");
        }

        @Test
        @DisplayName("Should get admin stats")
        void shouldGetAdminStats() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/stats");

            CustomerStatistics stats = new CustomerStatistics(100, 90, 10);
            when(customerService.getStatistics()).thenReturn(stats);
            when(customerService.findByRole(any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).getStatistics();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("totalUsers");
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPost tests - Create User")
    class DoPostCreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            String jsonBody = """
                    {
                        "name": "New Cashier",
                        "email": "cashier@test.com",
                        "phone": "1234567890",
                        "address": "123 Test St",
                        "password": "password123",
                        "role": "CASHIER"
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(1, "New Cashier", "cashier@test.com", UserRole.CASHIER);
            when(customerService.createUserWithRole(
                    "New Cashier", "cashier@test.com", "1234567890", "123 Test St", "password123", UserRole.CASHIER))
                    .thenReturn(customer);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).createUserWithRole(
                    "New Cashier", "cashier@test.com", "1234567890", "123 Test St", "password123", UserRole.CASHIER);
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            String jsonBody = "{\"email\": \"test@test.com\", \"password\": \"password123\", \"role\": \"CASHIER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            String jsonBody = "{\"name\": \"Test\", \"password\": \"password123\", \"role\": \"CASHIER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            String jsonBody = "{\"name\": \"Test\", \"email\": \"test@test.com\", \"password\": \"123\", \"role\": \"CASHIER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when role is missing")
        void shouldReturn400WhenRoleMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users");
            String jsonBody = "{\"name\": \"Test\", \"email\": \"test@test.com\", \"password\": \"password123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for unknown POST endpoint")
        void shouldReturn404ForUnknownPostEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPut tests - Update Role")
    class DoPutUpdateRoleTests {

        @Test
        @DisplayName("Should update user role successfully")
        void shouldUpdateUserRoleSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/role");
            String jsonBody = "{\"role\": \"MANAGER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(999); // Different user
            when(customerService.updateUserRole(123, UserRole.MANAGER)).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).updateUserRole(123, UserRole.MANAGER);
        }

        @Test
        @DisplayName("Should return 400 when role is missing")
        void shouldReturn400WhenRoleMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/role");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when trying to change own role")
        void shouldReturn400WhenChangingOwnRole() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/role");
            String jsonBody = "{\"role\": \"CUSTOMER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(123); // Same user

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 500 when role update fails")
        void shouldReturn500WhenRoleUpdateFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/role");
            String jsonBody = "{\"role\": \"MANAGER\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(null);
            when(customerService.updateUserRole(123, UserRole.MANAGER)).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID in role update")
        void shouldReturn400ForInvalidUserIdInRoleUpdate() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/abc/role");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPut tests - Update Status")
    class DoPutUpdateStatusTests {

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/status");
            String jsonBody = "{\"active\": true}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(null);
            when(customerService.activateAccount(123)).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).activateAccount(123);
        }

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/status");
            String jsonBody = "{\"active\": false}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(999); // Different user
            when(customerService.deactivateAccount(123)).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).deactivateAccount(123);
        }

        @Test
        @DisplayName("Should return 400 when trying to deactivate own account")
        void shouldReturn400WhenDeactivatingOwnAccount() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/status");
            String jsonBody = "{\"active\": false}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(123); // Same user

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 500 when status update fails")
        void shouldReturn500WhenStatusUpdateFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/status");
            String jsonBody = "{\"active\": true}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(request.getSession(false)).thenReturn(null);
            when(customerService.activateAccount(123)).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("doPut tests - Reset Password")
    class DoPutResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/password");
            String jsonBody = "{\"newPassword\": \"newpassword123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.resetPassword(123, "newpassword123")).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).resetPassword(123, "newpassword123");
        }

        @Test
        @DisplayName("Should return 400 when password too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/password");
            String jsonBody = "{\"newPassword\": \"123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when password missing")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/password");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 500 when password reset fails")
        void shouldReturn500WhenPasswordResetFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/password");
            String jsonBody = "{\"newPassword\": \"newpassword123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.resetPassword(123, "newpassword123")).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID in password reset")
        void shouldReturn400ForInvalidUserIdInPasswordReset() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/abc/password");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 for unknown PUT endpoint")
        void shouldReturn404ForUnknownPutEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/users/123/unknown");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
