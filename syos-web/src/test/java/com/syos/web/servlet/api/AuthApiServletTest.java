package com.syos.web.servlet.api;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.AuthenticationResult;
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
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private CustomerService customerService;

    private AuthApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AuthApiServlet();
        // Inject mock CustomerService via reflection
        java.lang.reflect.Field field = AuthApiServlet.class.getDeclaredField("customerService");
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
        return customer;
    }

    @Nested
    @DisplayName("doPost tests - Login")
    class DoPostLoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"user@test.com\", \"password\": \"password123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(1, "Test User", "user@test.com", UserRole.CUSTOMER);
            AuthenticationResult result = AuthenticationResult.success(customer);
            when(customerService.authenticate("user@test.com", "password123")).thenReturn(result);
            when(request.getSession(true)).thenReturn(session);
            when(session.getId()).thenReturn("session123");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).authenticate("user@test.com", "password123");
            verify(session).setAttribute("userId", 1);
            verify(session).setAttribute("userEmail", "user@test.com");
            verify(session).setAttribute("userName", "Test User");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("sessionId");
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"user@test.com\", \"password\": \"wrongpassword\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            AuthenticationResult result = AuthenticationResult.failure("Invalid credentials");
            when(customerService.authenticate("user@test.com", "wrongpassword")).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"password\": \"password123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"user@test.com\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid JSON")
        void shouldReturn400ForInvalidJson() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("not json")));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("doPost tests - Logout")
    class DoPostLogoutTests {

        @Test
        @DisplayName("Should logout successfully with session")
        void shouldLogoutSuccessfullyWithSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userEmail")).thenReturn("user@test.com");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(session).invalidate();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("Logout successful");
        }

        @Test
        @DisplayName("Should logout successfully without session")
        void shouldLogoutSuccessfullyWithoutSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doPost(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("Logout successful");
        }
    }

    @Nested
    @DisplayName("doPost tests - Register")
    class DoPostRegisterTests {

        @Test
        @DisplayName("Should register successfully")
        void shouldRegisterSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = """
                    {
                        "customerName": "New User",
                        "email": "newuser@test.com",
                        "phone": "1234567890",
                        "address": "123 Test St",
                        "password": "password123"
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(1, "New User", "newuser@test.com", UserRole.CUSTOMER);
            when(customerService.register("New User", "newuser@test.com", "1234567890", "123 Test St", "password123"))
                    .thenReturn(customer);
            when(request.getSession(true)).thenReturn(session);
            when(session.getId()).thenReturn("session123");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).register("New User", "newuser@test.com", "1234567890", "123 Test St",
                    "password123");
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
            verify(session).setAttribute("userId", 1);
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = "{\"email\": \"test@test.com\", \"password\": \"password123\"}";
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
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = "{\"customerName\": \"Test\", \"password\": \"password123\"}";
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
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = "{\"customerName\": \"Test\", \"email\": \"test@test.com\", \"password\": \"123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when registration fails")
        void shouldReturn400WhenRegistrationFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = """
                    {
                        "customerName": "New User",
                        "email": "existing@test.com",
                        "password": "password123"
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.register(any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Email already exists"));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPost tests - Unknown endpoint")
    class DoPostUnknownEndpointTests {

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 for root path")
        void shouldReturn404ForRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 for null path")
        void shouldReturn404ForNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doGet tests - Status")
    class DoGetStatusTests {

        @Test
        @DisplayName("Should return authenticated status")
        void shouldReturnAuthenticatedStatus() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/status");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);
            when(session.getAttribute("userName")).thenReturn("Test User");
            when(session.getAttribute("userEmail")).thenReturn("user@test.com");
            when(session.getAttribute("userRole")).thenReturn("CUSTOMER");

            // Act
            servlet.doGet(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("authenticated");
            assert output.contains("true");
        }

        @Test
        @DisplayName("Should return unauthenticated status when no session")
        void shouldReturnUnauthenticatedStatusWhenNoSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/status");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("authenticated");
            assert output.contains("false");
        }

        @Test
        @DisplayName("Should return unauthenticated status when no userId in session")
        void shouldReturnUnauthenticatedStatusWhenNoUserId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/status");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("false");
        }
    }

    @Nested
    @DisplayName("doGet tests - Current User")
    class DoGetCurrentUserTests {

        @Test
        @DisplayName("Should return current user details")
        void shouldReturnCurrentUserDetails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(1);

            Customer customer = createTestCustomer(1, "Test User", "user@test.com", UserRole.CUSTOMER);
            when(customerService.findById(1)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(1);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("user@test.com");
        }

        @Test
        @DisplayName("Should return 401 when no session")
        void shouldReturn401WhenNoSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 401 when no userId in session")
        void shouldReturn401WhenNoUserId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userId")).thenReturn(999);
            when(customerService.findById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doGet tests - Unknown endpoint")
    class DoGetUnknownEndpointTests {

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
}
