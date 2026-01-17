package com.syos.service;

import com.syos.domain.models.Cart;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.service.impl.CartServiceImpl;
import com.syos.service.interfaces.ProductService;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.CartService.StockValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private StoreInventoryService storeInventoryService;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(productService, storeInventoryService);
    }

    private Product createTestProduct(String code, String name, BigDecimal price) {
        Product product = new Product(
                new ProductCode(code),
                name,
                1, 1, 1,
                new Money(price));
        product.setActive(true);
        return product;
    }

    @Nested
    @DisplayName("getOrCreateCart tests")
    class GetOrCreateCartTests {

        @Test
        @DisplayName("Should create new cart for customer")
        void shouldCreateNewCartForCustomer() {
            // Act
            Cart cart = cartService.getOrCreateCart(1);

            // Assert
            assertNotNull(cart);
            assertEquals(1, cart.getCustomerId());
            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Should return existing cart for customer")
        void shouldReturnExistingCartForCustomer() {
            // Arrange
            Cart cart1 = cartService.getOrCreateCart(1);

            // Act
            Cart cart2 = cartService.getOrCreateCart(1);

            // Assert
            assertSame(cart1, cart2);
        }

        @Test
        @DisplayName("Should throw exception for null customer ID")
        void shouldThrowForNullCustomerId() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.getOrCreateCart(null));
        }
    }

    @Nested
    @DisplayName("getCart tests")
    class GetCartTests {

        @Test
        @DisplayName("Should return empty optional when cart not found")
        void shouldReturnEmptyWhenCartNotFound() {
            // Act
            Optional<Cart> result = cartService.getCart(999);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return cart when exists")
        void shouldReturnCartWhenExists() {
            // Arrange
            cartService.getOrCreateCart(1);

            // Act
            Optional<Cart> result = cartService.getCart(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getCustomerId());
        }
    }

    @Nested
    @DisplayName("addItem tests")
    class AddItemTests {

        @Test
        @DisplayName("Should add item to cart successfully")
        void shouldAddItemToCartSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));

            // Act
            Cart result = cartService.addItem(1, "TEST-001", 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getItems().size());
            assertEquals(5, result.getTotalQuantity());
        }

        @Test
        @DisplayName("Should throw exception for non-existent product")
        void shouldThrowForNonExistentProduct() {
            // Arrange
            when(productService.findByProductCode("NONEXISTENT")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> cartService.addItem(1, "NONEXISTENT", 5));
        }

        @Test
        @DisplayName("Should throw exception for inactive product")
        void shouldThrowForInactiveProduct() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            product.setActive(false);
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.addItem(1, "TEST-001", 5));
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void shouldThrowForZeroQuantity() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.addItem(1, "TEST-001", 0));
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowForNegativeQuantity() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.addItem(1, "TEST-001", -5));
        }

        @Test
        @DisplayName("Should accumulate quantity for same product")
        void shouldAccumulateQuantityForSameProduct() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));

            // Act
            cartService.addItem(1, "TEST-001", 5);
            Cart result = cartService.addItem(1, "TEST-001", 3);

            // Assert
            assertEquals(1, result.getItems().size());
            assertEquals(8, result.getTotalQuantity());
        }
    }

    @Nested
    @DisplayName("updateItemQuantity tests")
    class UpdateItemQuantityTests {

        @Test
        @DisplayName("Should update item quantity successfully")
        void shouldUpdateItemQuantitySuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            cartService.addItem(1, "TEST-001", 5);

            // Act
            Cart result = cartService.updateItemQuantity(1, "TEST-001", 10);

            // Assert
            assertEquals(10, result.getTotalQuantity());
        }

        @Test
        @DisplayName("Should remove item when quantity is zero")
        void shouldRemoveItemWhenQuantityIsZero() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            cartService.addItem(1, "TEST-001", 5);

            // Act
            Cart result = cartService.updateItemQuantity(1, "TEST-001", 0);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowWhenCartNotFound() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.updateItemQuantity(999, "TEST-001", 5));
        }
    }

    @Nested
    @DisplayName("removeItem tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Should remove item from cart")
        void shouldRemoveItemFromCart() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            cartService.addItem(1, "TEST-001", 5);

            // Act
            Cart result = cartService.removeItem(1, "TEST-001");

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when cart not found")
        void shouldThrowWhenCartNotFoundForRemove() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> cartService.removeItem(999, "TEST-001"));
        }
    }

    @Nested
    @DisplayName("clearCart tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear all items from cart")
        void shouldClearAllItemsFromCart() {
            // Arrange
            Product product1 = createTestProduct("TEST-001", "Product 1", BigDecimal.valueOf(100.00));
            Product product2 = createTestProduct("TEST-002", "Product 2", BigDecimal.valueOf(200.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product1));
            when(productService.findByProductCode("TEST-002")).thenReturn(Optional.of(product2));
            cartService.addItem(1, "TEST-001", 5);
            cartService.addItem(1, "TEST-002", 3);

            // Act
            cartService.clearCart(1);

            // Assert
            Optional<Cart> cart = cartService.getCart(1);
            assertTrue(cart.isPresent());
            assertTrue(cart.get().isEmpty());
        }

        @Test
        @DisplayName("Should not throw when clearing non-existent cart")
        void shouldNotThrowWhenClearingNonExistentCart() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> cartService.clearCart(999));
        }
    }

    @Nested
    @DisplayName("getCartItemCount tests")
    class GetCartItemCountTests {

        @Test
        @DisplayName("Should return zero for non-existent cart")
        void shouldReturnZeroForNonExistentCart() {
            // Act
            int count = cartService.getCartItemCount(999);

            // Assert
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should return total quantity of items")
        void shouldReturnTotalQuantityOfItems() {
            // Arrange
            Product product1 = createTestProduct("TEST-001", "Product 1", BigDecimal.valueOf(100.00));
            Product product2 = createTestProduct("TEST-002", "Product 2", BigDecimal.valueOf(200.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product1));
            when(productService.findByProductCode("TEST-002")).thenReturn(Optional.of(product2));
            cartService.addItem(1, "TEST-001", 5);
            cartService.addItem(1, "TEST-002", 3);

            // Act
            int count = cartService.getCartItemCount(1);

            // Assert
            assertEquals(8, count);
        }
    }

    @Nested
    @DisplayName("validateCartStock tests")
    class ValidateCartStockTests {

        @Test
        @DisplayName("Should return true for empty cart")
        void shouldReturnTrueForEmptyCart() {
            // Act
            boolean result = cartService.validateCartStock(1);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when all items have sufficient stock")
        void shouldReturnTrueWhenAllItemsHaveSufficientStock() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.getOnlineStoreQuantity("TEST-001")).thenReturn(100);
            cartService.addItem(1, "TEST-001", 5);

            // Act
            boolean result = cartService.validateCartStock(1);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when item has insufficient stock")
        void shouldReturnFalseWhenItemHasInsufficientStock() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.getOnlineStoreQuantity("TEST-001")).thenReturn(2);
            cartService.addItem(1, "TEST-001", 5);

            // Act
            boolean result = cartService.validateCartStock(1);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("validateCartStockDetails tests")
    class ValidateCartStockDetailsTests {

        @Test
        @DisplayName("Should return valid result for empty cart")
        void shouldReturnValidResultForEmptyCart() {
            // Act
            StockValidationResult result = cartService.validateCartStockDetails(1);

            // Assert
            assertTrue(result.isValid());
            assertTrue(result.issues().isEmpty());
        }

        @Test
        @DisplayName("Should return issues for items with insufficient stock")
        void shouldReturnIssuesForItemsWithInsufficientStock() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productService.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.getOnlineStoreQuantity("TEST-001")).thenReturn(2);
            cartService.addItem(1, "TEST-001", 5);

            // Act
            StockValidationResult result = cartService.validateCartStockDetails(1);

            // Assert
            assertFalse(result.isValid());
            assertEquals(1, result.issues().size());
            assertEquals("TEST-001", result.issues().get(0).productCode());
            assertEquals(5, result.issues().get(0).requestedQuantity());
            assertEquals(2, result.issues().get(0).availableQuantity());
        }
    }
}
