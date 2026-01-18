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
 * Unit tests for CustomerViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private CustomerService customerService;

    private CustomerViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CustomerViewServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = CustomerViewServlet.class.getDeclaredField("customerService");
        field.setAccessible(true);
        field.set(servlet, customerService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private Customer createTestCustomer(Integer id, String name, String email) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setPhone("1234567890");
        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(true);
        customer.setRegistrationDate(LocalDate.now());
        return customer;
    }

    @Nested
    @DisplayName("doGet tests - List Customers")
    class DoGetListCustomersTests {

        @Test
        @DisplayName("Should list customers with null path")
        void shouldListCustomersWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("page")).thenReturn(null);
            when(request.getParameter("size")).thenReturn(null);
            when(request.getParameter("search")).thenReturn(null);

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.findAll(0, 20)).thenReturn(List.of(customer));
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(100, 90, 10));
            when(customerService.getCustomerCount()).thenReturn(100L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll(0, 20);
            verify(request).setAttribute(eq("customers"), any());
            verify(request).getRequestDispatcher("/WEB-INF/views/customers/list.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should list customers with pagination")
        void shouldListCustomersWithPagination() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("page")).thenReturn("2");
            when(request.getParameter("size")).thenReturn("10");
            when(request.getParameter("search")).thenReturn(null);

            when(customerService.findAll(2, 10)).thenReturn(List.of());
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(50, 45, 5));
            when(customerService.getCustomerCount()).thenReturn(50L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findAll(2, 10);
            verify(request).setAttribute("currentPage", 2);
            verify(request).setAttribute("pageSize", 10);
        }

        @Test
        @DisplayName("Should search customers by name")
        void shouldSearchCustomersByName() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("search")).thenReturn("John");

            Customer customer = createTestCustomer(1, "John Doe", "john@test.com");
            when(customerService.searchByName("John")).thenReturn(List.of(customer));
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(100, 90, 10));
            when(customerService.getCustomerCount()).thenReturn(100L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).searchByName("John");
            verify(request).setAttribute("search", "John");
        }
    }

    @Nested
    @DisplayName("doGet tests - Add Form")
    class DoGetAddFormTests {

        @Test
        @DisplayName("Should show add form")
        void shouldShowAddForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/add");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute("isEdit", false);
            verify(request).getRequestDispatcher("/WEB-INF/views/customers/form.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - View Customer")
    class DoGetViewCustomerTests {

        @Test
        @DisplayName("Should view customer details")
        void shouldViewCustomerDetails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/123");

            Customer customer = createTestCustomer(123, "John Doe", "john@test.com");
            when(customerService.findById(123)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(customerService).findById(123);
            verify(request).setAttribute("customer", customer);
            verify(request).getRequestDispatcher("/WEB-INF/views/customers/view.jsp");
        }

        @Test
        @DisplayName("Should handle customer not found")
        void shouldHandleCustomerNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/999");
            when(customerService.findById(999)).thenReturn(Optional.empty());
            when(customerService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(0, 0, 0));
            when(customerService.getCustomerCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
        }

        @Test
        @DisplayName("Should handle invalid customer ID")
        void shouldHandleInvalidCustomerId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/abc");
            when(customerService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(0, 0, 0));
            when(customerService.getCustomerCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), eq("Invalid customer ID"));
        }
    }

    @Nested
    @DisplayName("doGet tests - Edit Form")
    class DoGetEditFormTests {

        @Test
        @DisplayName("Should show edit form")
        void shouldShowEditForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/edit/123");

            Customer customer = createTestCustomer(123, "John Doe", "john@test.com");
            when(customerService.findById(123)).thenReturn(Optional.of(customer));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute("customer", customer);
            verify(request).setAttribute("isEdit", true);
            verify(request).getRequestDispatcher("/WEB-INF/views/customers/form.jsp");
        }

        @Test
        @DisplayName("Should handle edit customer not found")
        void shouldHandleEditCustomerNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/edit/999");
            when(customerService.findById(999)).thenReturn(Optional.empty());
            when(customerService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(customerService.getStatistics()).thenReturn(new CustomerStatistics(0, 0, 0));
            when(customerService.getCustomerCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
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
