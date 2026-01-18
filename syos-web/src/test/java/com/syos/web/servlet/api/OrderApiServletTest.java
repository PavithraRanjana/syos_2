package com.syos.web.servlet.api;

import com.syos.domain.models.Order;
import com.syos.service.interfaces.OrderService;
import com.syos.service.interfaces.OrderService.OrderStats;
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

import java.io.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class OrderApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private OrderService orderService;

    private OrderApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new OrderApiServlet();
        java.lang.reflect.Field field = OrderApiServlet.class.getDeclaredField("orderService");
        field.setAccessible(true);
        field.set(servlet, orderService);

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    private void mockAuthenticatedUser(Integer userId) {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
    }

    private void mockUnauthenticatedUser() {
        when(request.getSession(false)).thenReturn(null);
    }

    @Nested
    @DisplayName("doGet tests")
    class DoGetTests {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockUnauthenticatedUser();

            servlet.doGet(request, response);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should get customer orders")
        void shouldGetCustomerOrders() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn(null);
            when(orderService.findByCustomerId(1)).thenReturn(List.of());

            servlet.doGet(request, response);

            verify(orderService).findByCustomerId(1);
        }

        @Test
        @DisplayName("Should get order by id")
        void shouldGetOrderById() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/123");
            Order order = new Order(1);
            order.setOrderId(123);
            when(orderService.findById(123)).thenReturn(Optional.of(order));

            servlet.doGet(request, response);

            verify(orderService).findById(123);
        }

        @Test
        @DisplayName("Should return 404 for non-existent order")
        void shouldReturn404ForNonExistentOrder() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/999");
            when(orderService.findById(999)).thenReturn(Optional.empty());

            servlet.doGet(request, response);

            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should get order stats")
        void shouldGetOrderStats() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/stats");
            OrderStats stats = new OrderStats(10, 5, 3, 2);
            when(orderService.getCustomerOrderStats(1)).thenReturn(stats);

            servlet.doGet(request, response);

            verify(orderService).getCustomerOrderStats(1);
        }
    }

    @Nested
    @DisplayName("doPost tests")
    class DoPostTests {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockUnauthenticatedUser();

            servlet.doPost(request, response);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should create order")
        void shouldCreateOrder() throws Exception {
            mockAuthenticatedUser(1);
            String jsonBody = "{\"shippingAddress\":\"123 Main St\",\"paymentMethod\":\"CASH\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Order order = new Order(1);
            order.setOrderId(1);
            when(orderService.createOrderFromCart(eq(1), any())).thenReturn(order);

            servlet.doPost(request, response);

            verify(orderService).createOrderFromCart(eq(1), any());
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }
    }

    @Nested
    @DisplayName("doPut tests")
    class DoPutTests {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockUnauthenticatedUser();

            servlet.doPut(request, response);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should cancel order")
        void shouldCancelOrder() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/123/cancel");
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));

            Order order = new Order(1);
            order.setOrderId(123);
            // findById is called to verify order ownership
            when(orderService.findById(123)).thenReturn(Optional.of(order));
            when(orderService.cancelOrder(eq(123), any())).thenReturn(order);

            servlet.doPut(request, response);

            verify(orderService).cancelOrder(eq(123), any());
        }

        @Test
        @DisplayName("Should update shipping")
        void shouldUpdateShipping() throws Exception {
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/123/shipping");
            String jsonBody = "{\"shippingAddress\":\"456 New St\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Order order = new Order(1);
            order.setOrderId(123);
            // findById is called to verify order ownership
            when(orderService.findById(123)).thenReturn(Optional.of(order));
            when(orderService.updateShippingAddress(eq(123), any(), any())).thenReturn(order);

            servlet.doPut(request, response);

            verify(orderService).updateShippingAddress(eq(123), any(), any());
        }
    }
}
