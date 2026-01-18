package com.syos.web.servlet.api;

import com.syos.domain.models.Cart;
import com.syos.domain.models.CartItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.CartService;
import jakarta.servlet.ServletException;
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
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for CartApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CartApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private CartService cartService;

    private CartApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CartApiServlet();
        // Inject mock CartService via reflection
        java.lang.reflect.Field field = CartApiServlet.class.getDeclaredField("cartService");
        field.setAccessible(true);
        field.set(servlet, cartService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
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
        @DisplayName("Should return 401 when user not authenticated")
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            // Arrange
            mockUnauthenticatedUser();

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should get cart successfully")
        void shouldGetCartSuccessfully() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn(null);

            Cart cart = new Cart(1);
            when(cartService.getOrCreateCart(1)).thenReturn(cart);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(cartService).getOrCreateCart(1);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("items");
        }

        @Test
        @DisplayName("Should get cart with root path")
        void shouldGetCartWithRootPath() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/");

            Cart cart = new Cart(1);
            when(cartService.getOrCreateCart(1)).thenReturn(cart);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(cartService).getOrCreateCart(1);
        }

        @Test
        @DisplayName("Should get cart count")
        void shouldGetCartCount() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/count");
            when(cartService.getCartItemCount(1)).thenReturn(5);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(cartService).getCartItemCount(1);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("count");
        }

        @Test
        @DisplayName("Should validate cart when valid")
        void shouldValidateCartWhenValid() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/validate");

            CartService.StockValidationResult result = new CartService.StockValidationResult(true, List.of());
            when(cartService.validateCartStockDetails(1)).thenReturn(result);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(cartService).validateCartStockDetails(1);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("valid");
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPost tests")
    class DoPostTests {

        @Test
        @DisplayName("Should return 401 when user not authenticated")
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            // Arrange
            mockUnauthenticatedUser();

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should add item to cart")
        void shouldAddItemToCart() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items");

            String jsonBody = "{\"productCode\":\"P001\",\"quantity\":2}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Cart cart = new Cart(1);
            cart.addItem(createTestCartItem("P001", 2, 25.00));
            when(cartService.addItem(1, "P001", 2)).thenReturn(cart);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(cartService).addItem(1, "P001", 2);
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 when product code missing")
        void shouldReturn400WhenProductCodeMissing() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items");

            String jsonBody = "{\"quantity\":2}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when quantity is not positive")
        void shouldReturn400WhenQuantityNotPositive() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items");

            String jsonBody = "{\"productCode\":\"P001\",\"quantity\":0}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for non-items endpoint")
        void shouldReturn404ForNonItemsEndpoint() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/other");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPut tests")
    class DoPutTests {

        @Test
        @DisplayName("Should return 401 when user not authenticated")
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            // Arrange
            mockUnauthenticatedUser();

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should update item quantity")
        void shouldUpdateItemQuantity() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items/P001");

            String jsonBody = "{\"quantity\":5}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Cart cart = new Cart(1);
            when(cartService.updateItemQuantity(1, "P001", 5)).thenReturn(cart);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(cartService).updateItemQuantity(1, "P001", 5);
        }

        @Test
        @DisplayName("Should return 400 when quantity missing")
        void shouldReturn400WhenQuantityMissing() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items/P001");

            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for non-items path")
        void shouldReturn404ForNonItemsPath() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/other");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doDelete tests")
    class DoDeleteTests {

        @Test
        @DisplayName("Should return 401 when user not authenticated")
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            // Arrange
            mockUnauthenticatedUser();

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should clear cart")
        void shouldClearCart() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(cartService).clearCart(1);
        }

        @Test
        @DisplayName("Should clear cart with root path")
        void shouldClearCartWithRootPath() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/");

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(cartService).clearCart(1);
        }

        @Test
        @DisplayName("Should remove item from cart")
        void shouldRemoveItemFromCart() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/items/P001");

            Cart cart = new Cart(1);
            when(cartService.removeItem(1, "P001")).thenReturn(cart);

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(cartService).removeItem(1, "P001");
        }

        @Test
        @DisplayName("Should return 404 for unknown path")
        void shouldReturn404ForUnknownPath() throws Exception {
            // Arrange
            mockAuthenticatedUser(1);
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Helper method
    private CartItem createTestCartItem(String code, int qty, double price) {
        return new CartItem(new ProductCode(code), "Product " + code,
                new Money(BigDecimal.valueOf(price)), qty);
    }
}
