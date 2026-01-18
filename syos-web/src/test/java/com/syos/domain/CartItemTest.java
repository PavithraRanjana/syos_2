package com.syos.domain;

import com.syos.domain.models.CartItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CartItem model.
 */
class CartItemTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create cart item with valid parameters")
        void shouldCreateCartItemWithValidParameters() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            String productName = "Test Product";
            Money unitPrice = new Money(BigDecimal.valueOf(25.50));
            int quantity = 3;

            // Act
            CartItem item = new CartItem(productCode, productName, unitPrice, quantity);

            // Assert
            assertNotNull(item);
            assertEquals(productCode, item.getProductCode());
            assertEquals("P001", item.getProductCodeString());
            assertEquals(productName, item.getProductName());
            assertEquals(unitPrice, item.getUnitPrice());
            assertEquals(quantity, item.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception when product code is null")
        void shouldThrowExceptionWhenProductCodeIsNull() {
            // Arrange
            Money unitPrice = new Money(BigDecimal.valueOf(10.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(null, "Product", unitPrice, 1));
        }

        @Test
        @DisplayName("Should throw exception when product name is null")
        void shouldThrowExceptionWhenProductNameIsNull() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Money unitPrice = new Money(BigDecimal.valueOf(10.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(productCode, null, unitPrice, 1));
        }

        @Test
        @DisplayName("Should throw exception when product name is empty")
        void shouldThrowExceptionWhenProductNameIsEmpty() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Money unitPrice = new Money(BigDecimal.valueOf(10.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(productCode, "  ", unitPrice, 1));
        }

        @Test
        @DisplayName("Should throw exception when unit price is null")
        void shouldThrowExceptionWhenUnitPriceIsNull() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(productCode, "Product", null, 1));
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void shouldThrowExceptionWhenQuantityIsZero() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Money unitPrice = new Money(BigDecimal.valueOf(10.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(productCode, "Product", unitPrice, 0));
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Money unitPrice = new Money(BigDecimal.valueOf(10.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new CartItem(productCode, "Product", unitPrice, -5));
        }
    }

    @Nested
    @DisplayName("decrementQuantity tests")
    class DecrementQuantityTests {

        @Test
        @DisplayName("Should decrement quantity by specified amount")
        void shouldDecrementQuantityBySpecifiedAmount() {
            // Arrange
            CartItem item = createTestCartItem("P1", 10, 5.00);

            // Act
            item.decrementQuantity(3);

            // Assert
            assertEquals(7, item.getQuantity());
        }

        @Test
        @DisplayName("Should decrement quantity to minimum of 1")
        void shouldDecrementQuantityToMinimumOfOne() {
            // Arrange
            CartItem item = createTestCartItem("P1", 5, 10.00);

            // Act
            item.decrementQuantity(4);

            // Assert
            assertEquals(1, item.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowExceptionWhenAmountIsZero() {
            // Arrange
            CartItem item = createTestCartItem("P1", 5, 10.00);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> item.decrementQuantity(0));
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Arrange
            CartItem item = createTestCartItem("P1", 5, 10.00);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> item.decrementQuantity(-2));
        }

        @Test
        @DisplayName("Should throw exception when result would be less than 1")
        void shouldThrowExceptionWhenResultWouldBeLessThanOne() {
            // Arrange
            CartItem item = createTestCartItem("P1", 5, 10.00);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> item.decrementQuantity(5));
        }

        @Test
        @DisplayName("Should throw exception when result would be zero")
        void shouldThrowExceptionWhenResultWouldBeZero() {
            // Arrange
            CartItem item = createTestCartItem("P1", 3, 10.00);

            // Act & Assert - decrementing by 3 from quantity 3 would result in 0
            assertThrows(IllegalArgumentException.class, () -> item.decrementQuantity(3));
        }
    }

    @Nested
    @DisplayName("equals tests")
    class EqualsTests {

        @Test
        @DisplayName("Should return true for same object reference")
        void shouldReturnTrueForSameObjectReference() {
            // Arrange
            CartItem item = createTestCartItem("P1", 2, 10.00);

            // Act & Assert
            assertEquals(item, item);
        }

        @Test
        @DisplayName("Should return true for items with same product code")
        void shouldReturnTrueForItemsWithSameProductCode() {
            // Arrange
            CartItem item1 = createTestCartItem("P1", 2, 10.00);
            CartItem item2 = createTestCartItem("P1", 5, 25.00); // Different quantity and price

            // Act & Assert
            assertEquals(item1, item2);
        }

        @Test
        @DisplayName("Should return false for items with different product codes")
        void shouldReturnFalseForItemsWithDifferentProductCodes() {
            // Arrange
            CartItem item1 = createTestCartItem("P1", 2, 10.00);
            CartItem item2 = createTestCartItem("P2", 2, 10.00);

            // Act & Assert
            assertNotEquals(item1, item2);
        }

        @Test
        @DisplayName("Should return false when compared with null")
        void shouldReturnFalseWhenComparedWithNull() {
            // Arrange
            CartItem item = createTestCartItem("P1", 2, 10.00);

            // Act & Assert
            assertNotEquals(null, item);
        }

        @Test
        @DisplayName("Should return false when compared with different type")
        void shouldReturnFalseWhenComparedWithDifferentType() {
            // Arrange
            CartItem item = createTestCartItem("P1", 2, 10.00);

            // Act & Assert
            assertNotEquals("P1", item);
        }

        @Test
        @DisplayName("Should have same hashCode for equal items")
        void shouldHaveSameHashCodeForEqualItems() {
            // Arrange
            CartItem item1 = createTestCartItem("P1", 2, 10.00);
            CartItem item2 = createTestCartItem("P1", 5, 25.00);

            // Act & Assert
            assertEquals(item1.hashCode(), item2.hashCode());
        }
    }

    @Nested
    @DisplayName("getLineTotal tests")
    class GetLineTotalTests {

        @Test
        @DisplayName("Should calculate line total correctly")
        void shouldCalculateLineTotalCorrectly() {
            // Arrange
            CartItem item = createTestCartItem("P1", 3, 10.00);

            // Act
            Money lineTotal = item.getLineTotal();

            // Assert - 3 * 10.00 = 30.00
            assertEquals(30, lineTotal.getAmount().intValue());
        }

        @Test
        @DisplayName("Should calculate line total for single item")
        void shouldCalculateLineTotalForSingleItem() {
            // Arrange
            CartItem item = createTestCartItem("P1", 1, 49.99);

            // Act
            Money lineTotal = item.getLineTotal();

            // Assert
            assertEquals(new BigDecimal("49.99"), lineTotal.getAmount());
        }

        @Test
        @DisplayName("Should calculate line total with decimal prices")
        void shouldCalculateLineTotalWithDecimalPrices() {
            // Arrange
            CartItem item = createTestCartItem("P1", 2, 15.75);

            // Act
            Money lineTotal = item.getLineTotal();

            // Assert - 2 * 15.75 = 31.50
            assertEquals(new BigDecimal("31.50"), lineTotal.getAmount());
        }

        @Test
        @DisplayName("Should update line total when quantity changes")
        void shouldUpdateLineTotalWhenQuantityChanges() {
            // Arrange
            CartItem item = createTestCartItem("P1", 5, 20.00);
            assertEquals(100, item.getLineTotal().getAmount().intValue());

            // Act
            item.setQuantity(3);

            // Assert - 3 * 20.00 = 60.00
            assertEquals(60, item.getLineTotal().getAmount().intValue());
        }
    }

    // Helper method
    private CartItem createTestCartItem(String code, int quantity, double unitPrice) {
        return new CartItem(
                new ProductCode(code),
                "Test Product " + code,
                new Money(BigDecimal.valueOf(unitPrice)),
                quantity);
    }
}
