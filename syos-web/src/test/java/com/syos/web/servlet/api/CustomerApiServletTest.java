package com.syos.web.servlet.api;

import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.AuthenticationResult;
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
 * Unit tests for CustomerApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private CustomerService customerService;

    private CustomerApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CustomerApiServlet();
        // Inject mock CustomerService via reflection
        java.lang.reflect.Field field = CustomerApiServlet.class.getDeclaredField("customerService");
        field.setAccessible(true);
        field.set(servlet, customerService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    private Customer createTestCustomer(Integer id, String name, String email) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setPhone("1234567890");
        customer.setAddress("123 Test St");
        customer.setActive(true);
        customer.setRegistrationDate(LocalDate.now());
        customer.setCreatedAt(LocalDateTime.now());
        return customer;
    }

    @Nested
    @DisplayName("doGet tests - List and Search")
    class DoGetListAndSearchTests {

        @Test
        @DisplayName("Should list all customers")
        void shouldListAllCustomers() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("page")).thenReturn("0");
            when(request.getParameter("size")).thenReturn("50");
            when(request.getParameter("active")).thenReturn(null);

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.findAll(0, 50)).thenReturn(List.of(customer));
            when(customerService.getCustomerCount()).thenReturn(1L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll(0, 50);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("customers");
        }

        @Test
        @DisplayName("Should list active customers only")
        void shouldListActiveCustomersOnly() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("active")).thenReturn("true");

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.findAllActive()).thenReturn(List.of(customer));
            when(customerService.getCustomerCount()).thenReturn(1L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAllActive();
        }

        @Test
        @DisplayName("Should search customers by name")
        void shouldSearchCustomersByName() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/search");
            when(request.getParameter("q")).thenReturn("John");

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.searchByName("John")).thenReturn(List.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).searchByName("John");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("query");
        }

        @Test
        @DisplayName("Should return empty list for empty search query")
        void shouldReturnEmptyListForEmptySearchQuery() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/search");
            when(request.getParameter("q")).thenReturn("");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService, never()).searchByName(anyString());
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("count");
        }

        @Test
        @DisplayName("Should get customer statistics")
        void shouldGetCustomerStatistics() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/statistics");

            CustomerStatistics stats = new CustomerStatistics(100, 90, 10);
            when(customerService.getStatistics()).thenReturn(stats);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).getStatistics();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("totalCustomers");
        }
    }

    @Nested
    @DisplayName("doGet tests - Get Customer")
    class DoGetCustomerTests {

        @Test
        @DisplayName("Should get customer by ID")
        void shouldGetCustomerById() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123");

            Customer customer = createTestCustomer(123, "John Doe", "john@test.com");
            when(customerService.findById(123)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(123);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("customerId");
        }

        @Test
        @DisplayName("Should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/999");
            when(customerService.findById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 for invalid customer ID")
        void shouldReturn400ForInvalidCustomerId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should get current logged in user")
        void shouldGetCurrentLoggedInUser() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("customerId")).thenReturn(1);

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.findById(1)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(1);
        }

        @Test
        @DisplayName("Should return 401 when not logged in for /me")
        void shouldReturn401WhenNotLoggedIn() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 401 when session has no customerId")
        void shouldReturn401WhenNoCustomerIdInSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/me");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("customerId")).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("doGet tests - Validation Endpoints")
    class DoGetValidationTests {

        @Test
        @DisplayName("Should check email availability - available")
        void shouldCheckEmailAvailabilityAvailable() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/check-email");
            when(request.getParameter("email")).thenReturn("new@test.com");
            when(customerService.isEmailAvailable("new@test.com")).thenReturn(true);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).isEmailAvailable("new@test.com");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("available");
        }

        @Test
        @DisplayName("Should check email availability - taken")
        void shouldCheckEmailAvailabilityTaken() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/check-email");
            when(request.getParameter("email")).thenReturn("taken@test.com");
            when(customerService.isEmailAvailable("taken@test.com")).thenReturn(false);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).isEmailAvailable("taken@test.com");
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/check-email");
            when(request.getParameter("email")).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should check phone availability")
        void shouldCheckPhoneAvailability() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/check-phone");
            when(request.getParameter("phone")).thenReturn("1234567890");
            when(customerService.isPhoneAvailable("1234567890")).thenReturn(true);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).isPhoneAvailable("1234567890");
        }

        @Test
        @DisplayName("Should return 400 when phone is missing")
        void shouldReturn400WhenPhoneMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/check-phone");
            when(request.getParameter("phone")).thenReturn("");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPost tests - Registration")
    class DoPostRegistrationTests {

        @Test
        @DisplayName("Should register customer successfully")
        void shouldRegisterCustomerSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = """
                    {
                        "name": "John Doe",
                        "email": "john@test.com",
                        "phone": "1234567890",
                        "address": "123 Test St",
                        "password": "password123"
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.register("John Doe", "john@test.com", "1234567890", "123 Test St", "password123"))
                    .thenReturn(customer);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).register("John Doe", "john@test.com", "1234567890", "123 Test St", "password123");
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 when required fields missing")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            String jsonBody = "{\"name\": \"John\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 500 for invalid JSON body")
        void shouldReturn500ForInvalidJsonBody() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/register");
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("invalid")));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("doPost tests - Login/Logout")
    class DoPostLoginLogoutTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"john@test.com\", \"password\": \"password123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            AuthenticationResult result = AuthenticationResult.success(customer);
            when(customerService.authenticate("john@test.com", "password123")).thenReturn(result);
            when(request.getSession(true)).thenReturn(session);
            when(session.getId()).thenReturn("session123");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).authenticate("john@test.com", "password123");
            verify(session).setAttribute("customerId", 1);
            verify(session).setAttribute("customerEmail", "john@test.com");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("sessionId");
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"john@test.com\", \"password\": \"wrong\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            AuthenticationResult result = AuthenticationResult.failure("Invalid credentials");
            when(customerService.authenticate("john@test.com", "wrong")).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 400 when login credentials missing")
        void shouldReturn400WhenLoginCredentialsMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/login");
            String jsonBody = "{\"email\": \"john@test.com\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should logout successfully with session")
        void shouldLogoutSuccessfullyWithSession() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/logout");
            when(request.getSession(false)).thenReturn(session);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(session).invalidate();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("loggedOut");
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
            assert output.contains("loggedOut");
        }
    }

    @Nested
    @DisplayName("doPost tests - Password Reset")
    class DoPostPasswordResetTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/reset-password");
            String jsonBody = "{\"newPassword\": \"newpass123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.resetPassword(123, "newpass123")).thenReturn(true);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(customerService).resetPassword(123, "newpass123");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("passwordReset");
        }

        @Test
        @DisplayName("Should return 400 when reset password fails")
        void shouldReturn400WhenResetPasswordFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/reset-password");
            String jsonBody = "{\"newPassword\": \"newpass123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.resetPassword(123, "newpass123")).thenReturn(false);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when new password missing")
        void shouldReturn400WhenNewPasswordMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/reset-password");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid customer ID in reset")
        void shouldReturn400ForInvalidCustomerIdInReset() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc/reset-password");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPost tests - Other")
    class DoPostOtherTests {

        @Test
        @DisplayName("Should return 400 for root path")
        void shouldReturn400ForRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

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
    }

    @Nested
    @DisplayName("doPut tests - Profile Update")
    class DoPutProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfileSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123");
            String jsonBody = """
                    {
                        "name": "John Updated",
                        "phone": "9876543210",
                        "address": "456 New St"
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Customer customer = createTestCustomer(123, "John Updated", "john@test.com");
            when(customerService.updateProfile(123, "John Updated", "9876543210", "456 New St"))
                    .thenReturn(customer);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).updateProfile(123, "John Updated", "9876543210", "456 New St");
        }

        @Test
        @DisplayName("Should return 400 for null path")
        void shouldReturn400ForNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid customer ID")
        void shouldReturn400ForInvalidCustomerId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPut tests - Password Change")
    class DoPutPasswordChangeTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/password");
            String jsonBody = "{\"currentPassword\": \"oldpass\", \"newPassword\": \"newpass123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.changePassword(123, "oldpass", "newpass123")).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).changePassword(123, "oldpass", "newpass123");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("passwordChanged");
        }

        @Test
        @DisplayName("Should return 400 when password change fails")
        void shouldReturn400WhenPasswordChangeFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/password");
            String jsonBody = "{\"currentPassword\": \"wrongpass\", \"newPassword\": \"newpass123\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(customerService.changePassword(123, "wrongpass", "newpass123")).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when passwords missing")
        void shouldReturn400WhenPasswordsMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/password");
            String jsonBody = "{\"currentPassword\": \"oldpass\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPut tests - Activation/Deactivation")
    class DoPutActivationTests {

        @Test
        @DisplayName("Should activate account successfully")
        void shouldActivateAccountSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/activate");
            when(customerService.activateAccount(123)).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).activateAccount(123);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("active");
        }

        @Test
        @DisplayName("Should return 404 when activation fails")
        void shouldReturn404WhenActivationFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/999/activate");
            when(customerService.activateAccount(999)).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should deactivate account successfully")
        void shouldDeactivateAccountSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/deactivate");
            when(customerService.deactivateAccount(123)).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(customerService).deactivateAccount(123);
        }

        @Test
        @DisplayName("Should return 404 when deactivation fails")
        void shouldReturn404WhenDeactivationFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/999/deactivate");
            when(customerService.deactivateAccount(999)).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 for unknown PUT endpoint")
        void shouldReturn404ForUnknownPutEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123/unknown");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
