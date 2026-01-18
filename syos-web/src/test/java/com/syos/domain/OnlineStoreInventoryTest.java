package com.syos.domain;

import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OnlineStoreInventory model.
 */
class OnlineStoreInventoryTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create inventory with all parameters")
        void shouldCreateInventoryWithAllParameters() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Integer mainInventoryId = 10;
            int quantity = 100;
            LocalDate restockDate = LocalDate.now();

            // Act
            OnlineStoreInventory inv = new OnlineStoreInventory(productCode, mainInventoryId, quantity, restockDate);

            // Assert
            assertEquals(productCode, inv.getProductCode());
            assertEquals("P001", inv.getProductCodeString());
            assertEquals(mainInventoryId, inv.getMainInventoryId());
            assertEquals(quantity, inv.getQuantityAvailable());
            assertEquals(restockDate, inv.getRestockedDate());
        }

        @Test
        @DisplayName("Should create empty inventory with default constructor")
        void shouldCreateEmptyInventoryWithDefaultConstructor() {
            // Act
            OnlineStoreInventory inv = new OnlineStoreInventory();

            // Assert
            assertNull(inv.getOnlineInventoryId());
            assertNull(inv.getProductCode());
            assertEquals(0, inv.getQuantityAvailable());
        }
    }

    @Nested
    @DisplayName("hasStock tests")
    class HasStockTests {

        @Test
        @DisplayName("Should return true when quantity is positive")
        void shouldReturnTrueWhenQuantityIsPositive() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.hasStock());
        }

        @Test
        @DisplayName("Should return false when quantity is zero")
        void shouldReturnFalseWhenQuantityIsZero() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 0, LocalDate.now());
            assertFalse(inv.hasStock());
        }
    }

    @Nested
    @DisplayName("canFulfill tests")
    class CanFulfillTests {

        @Test
        @DisplayName("Should return true when quantity equals required")
        void shouldReturnTrueWhenQuantityEqualsRequired() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.canFulfill(10));
        }

        @Test
        @DisplayName("Should return true when quantity exceeds required")
        void shouldReturnTrueWhenQuantityExceedsRequired() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.canFulfill(5));
        }

        @Test
        @DisplayName("Should return false when quantity is less than required")
        void shouldReturnFalseWhenQuantityIsLessThanRequired() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertFalse(inv.canFulfill(15));
        }
    }

    @Nested
    @DisplayName("reduceQuantity tests")
    class ReduceQuantityTests {

        @Test
        @DisplayName("Should reduce quantity by specified amount")
        void shouldReduceQuantityBySpecifiedAmount() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 100, LocalDate.now());
            inv.reduceQuantity(30);
            assertEquals(70, inv.getQuantityAvailable());
        }

        @Test
        @DisplayName("Should throw when reducing by negative amount")
        void shouldThrowWhenReducingByNegativeAmount() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 100, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.reduceQuantity(-5));
        }

        @Test
        @DisplayName("Should throw when reducing by more than available")
        void shouldThrowWhenReducingByMoreThanAvailable() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.reduceQuantity(15));
        }
    }

    @Nested
    @DisplayName("addQuantity tests")
    class AddQuantityTests {

        @Test
        @DisplayName("Should add quantity by specified amount")
        void shouldAddQuantityBySpecifiedAmount() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 50, LocalDate.now());
            inv.addQuantity(25);
            assertEquals(75, inv.getQuantityAvailable());
        }

        @Test
        @DisplayName("Should throw when adding negative amount")
        void shouldThrowWhenAddingNegativeAmount() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P1"), 1, 50, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.addQuantity(-10));
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get product name")
        void shouldSetAndGetProductName() {
            OnlineStoreInventory inv = new OnlineStoreInventory();
            inv.setProductName("Test Product");
            assertEquals("Test Product", inv.getProductName());
        }

        @Test
        @DisplayName("Should set and get expiry date")
        void shouldSetAndGetExpiryDate() {
            OnlineStoreInventory inv = new OnlineStoreInventory();
            LocalDate expiry = LocalDate.of(2026, 12, 31);
            inv.setExpiryDate(expiry);
            assertEquals(expiry, inv.getExpiryDate());
        }

        @Test
        @DisplayName("Should set and get timestamps")
        void shouldSetAndGetCreatedAt() {
            OnlineStoreInventory inv = new OnlineStoreInventory();
            LocalDateTime now = LocalDateTime.now();
            inv.setCreatedAt(now);
            inv.setUpdatedAt(now);
            assertEquals(now, inv.getCreatedAt());
            assertEquals(now, inv.getUpdatedAt());
        }

        @Test
        @DisplayName("getProductCodeString should return null when productCode is null")
        void getProductCodeStringShouldReturnNullWhenProductCodeIsNull() {
            OnlineStoreInventory inv = new OnlineStoreInventory();
            assertNull(inv.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            OnlineStoreInventory inv = new OnlineStoreInventory(new ProductCode("P001"), 5, 100, LocalDate.now());
            inv.setOnlineInventoryId(1);
            String result = inv.toString();
            assertTrue(result.contains("OnlineStoreInventory{"));
            assertTrue(result.contains("onlineInventoryId=1"));
            assertTrue(result.contains("quantityAvailable=100"));
        }
    }
}
