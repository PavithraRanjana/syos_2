package com.syos.domain;

import com.syos.domain.models.Cart;
import com.syos.domain.models.CartItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cart model.
 */
class CartTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty cart with default constructor")
        void shouldCreateEmptyCartWithDefaultConstructor() {
            Cart cart = new Cart();
            assertNull(cart.getCartId());
            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getItemCount());
            assertNotNull(cart.getCreatedAt());
            assertNotNull(cart.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create cart with customer id")
        void shouldCreateCartWithCustomerId() {
            Cart cart = new Cart(42);
            assertEquals(42, cart.getCustomerId());
            assertTrue(cart.isEmpty());
        }
    }

    @Nested
    @DisplayName("addItem tests")
    class AddItemTests {

        @Test
        @DisplayName("Should add new item to cart")
        void shouldAddNewItemToCart() {
            Cart cart = new Cart();
            CartItem item = createTestCartItem("P1", 2, 25.00);

            cart.addItem(item);

            assertEquals(1, cart.getItemCount());
            assertTrue(cart.containsProduct("P1"));
        }

        @Test
        @DisplayName("Should increase quantity for existing product")
        void shouldIncreaseQuantityForExistingProduct() {
            Cart cart = new Cart();
            CartItem item1 = createTestCartItem("P1", 2, 25.00);
            CartItem item2 = createTestCartItem("P1", 3, 25.00);

            cart.addItem(item1);
            cart.addItem(item2);

            assertEquals(1, cart.getItemCount());
            assertEquals(5, cart.getItem("P1").get().getQuantity());
        }
    }

    @Nested
    @DisplayName("removeItem tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Should remove item by product code string")
        void shouldRemoveItemByProductCodeString() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            cart.removeItem("P1");

            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Should remove item by ProductCode object")
        void shouldRemoveItemByProductCodeObject() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            cart.removeItem(new ProductCode("P1"));

            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Should throw when removing non-existent item")
        void shouldThrowWhenRemovingNonExistentItem() {
            Cart cart = new Cart();
            assertThrows(IllegalArgumentException.class, () -> cart.removeItem("P999"));
        }
    }

    @Nested
    @DisplayName("updateItemQuantity tests")
    class UpdateItemQuantityTests {

        @Test
        @DisplayName("Should update quantity of existing item")
        void shouldUpdateQuantityOfExistingItem() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            cart.updateItemQuantity("P1", 5);

            assertEquals(5, cart.getItem("P1").get().getQuantity());
        }

        @Test
        @DisplayName("Should remove item when quantity is zero or less")
        void shouldRemoveItemWhenQuantityIsZeroOrLess() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            cart.updateItemQuantity("P1", 0);

            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Should throw when updating non-existent item")
        void shouldThrowWhenUpdatingNonExistentItem() {
            Cart cart = new Cart();
            assertThrows(IllegalArgumentException.class, () -> cart.updateItemQuantity("P999", 5));
        }
    }

    @Nested
    @DisplayName("Calculation tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate total quantity")
        void shouldCalculateTotalQuantity() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 3, 10.00));
            cart.addItem(createTestCartItem("P2", 5, 20.00));

            assertEquals(8, cart.getTotalQuantity());
        }

        @Test
        @DisplayName("Should calculate subtotal")
        void shouldCalculateSubtotal() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00)); // 50
            cart.addItem(createTestCartItem("P2", 3, 10.00)); // 30

            Money subtotal = cart.getSubtotal();
            assertEquals(80, subtotal.getAmount().intValue());
        }

        @Test
        @DisplayName("Should return zero subtotal for empty cart")
        void shouldReturnZeroSubtotalForEmptyCart() {
            Cart cart = new Cart();
            assertEquals(0, cart.getSubtotal().getAmount().intValue());
        }
    }

    @Nested
    @DisplayName("Utility methods tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("getItem should return Optional with item")
        void getItemShouldReturnOptionalWithItem() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            assertTrue(cart.getItem("P1").isPresent());
            assertFalse(cart.getItem("P999").isPresent());
        }

        @Test
        @DisplayName("clear should remove all items")
        void clearShouldRemoveAllItems() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));
            cart.addItem(createTestCartItem("P2", 3, 10.00));

            cart.clear();

            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getItemCount());
        }

        @Test
        @DisplayName("getSummary should return correct values")
        void getSummaryShouldReturnCorrectValues() {
            Cart cart = new Cart();
            cart.addItem(createTestCartItem("P1", 2, 25.00));
            cart.addItem(createTestCartItem("P2", 3, 10.00));

            Cart.CartSummary summary = cart.getSummary();

            assertEquals(2, summary.itemCount());
            assertEquals(5, summary.totalQuantity());
            assertEquals(80, summary.subtotal().getAmount().intValue());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            Cart cart = new Cart(100);
            cart.setCartId(1);
            cart.addItem(createTestCartItem("P1", 2, 25.00));

            String result = cart.toString();
            assertTrue(result.contains("Cart{"));
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("customer=100"));
        }
    }

    private CartItem createTestCartItem(String code, int qty, double price) {
        return new CartItem(new ProductCode(code), "Product " + code,
                new Money(BigDecimal.valueOf(price)), qty);
    }
}
