package com.syos.domain;

import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhysicalStoreInventory model.
 */
class PhysicalStoreInventoryTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create inventory with all parameters")
        void shouldCreateInventoryWithAllParameters() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            Integer mainInventoryId = 10;
            int quantity = 50;
            LocalDate restockDate = LocalDate.now();

            // Act
            PhysicalStoreInventory inv = new PhysicalStoreInventory(productCode, mainInventoryId, quantity,
                    restockDate);

            // Assert
            assertEquals(productCode, inv.getProductCode());
            assertEquals("P001", inv.getProductCodeString());
            assertEquals(mainInventoryId, inv.getMainInventoryId());
            assertEquals(quantity, inv.getQuantityOnShelf());
            assertEquals(restockDate, inv.getRestockedDate());
        }

        @Test
        @DisplayName("Should create empty inventory with default constructor")
        void shouldCreateEmptyInventoryWithDefaultConstructor() {
            // Act
            PhysicalStoreInventory inv = new PhysicalStoreInventory();

            // Assert
            assertNull(inv.getPhysicalInventoryId());
            assertNull(inv.getProductCode());
            assertEquals(0, inv.getQuantityOnShelf());
        }
    }

    @Nested
    @DisplayName("hasStock tests")
    class HasStockTests {

        @Test
        @DisplayName("Should return true when quantity is positive")
        void shouldReturnTrueWhenQuantityIsPositive() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.hasStock());
        }

        @Test
        @DisplayName("Should return false when quantity is zero")
        void shouldReturnFalseWhenQuantityIsZero() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 0, LocalDate.now());
            assertFalse(inv.hasStock());
        }
    }

    @Nested
    @DisplayName("canFulfill tests")
    class CanFulfillTests {

        @Test
        @DisplayName("Should return true when quantity equals required")
        void shouldReturnTrueWhenQuantityEqualsRequired() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.canFulfill(10));
        }

        @Test
        @DisplayName("Should return true when quantity exceeds required")
        void shouldReturnTrueWhenQuantityExceedsRequired() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertTrue(inv.canFulfill(5));
        }

        @Test
        @DisplayName("Should return false when quantity is less than required")
        void shouldReturnFalseWhenQuantityIsLessThanRequired() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertFalse(inv.canFulfill(15));
        }
    }

    @Nested
    @DisplayName("reduceQuantity tests")
    class ReduceQuantityTests {

        @Test
        @DisplayName("Should reduce quantity by specified amount")
        void shouldReduceQuantityBySpecifiedAmount() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 100, LocalDate.now());
            inv.reduceQuantity(30);
            assertEquals(70, inv.getQuantityOnShelf());
        }

        @Test
        @DisplayName("Should throw when reducing by negative amount")
        void shouldThrowWhenReducingByNegativeAmount() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 100, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.reduceQuantity(-5));
        }

        @Test
        @DisplayName("Should throw when reducing by more than on shelf")
        void shouldThrowWhenReducingByMoreThanOnShelf() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 10, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.reduceQuantity(15));
        }
    }

    @Nested
    @DisplayName("addQuantity tests")
    class AddQuantityTests {

        @Test
        @DisplayName("Should add quantity by specified amount")
        void shouldAddQuantityBySpecifiedAmount() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 50, LocalDate.now());
            inv.addQuantity(25);
            assertEquals(75, inv.getQuantityOnShelf());
        }

        @Test
        @DisplayName("Should throw when adding negative amount")
        void shouldThrowWhenAddingNegativeAmount() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P1"), 1, 50, LocalDate.now());
            assertThrows(IllegalArgumentException.class, () -> inv.addQuantity(-10));
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get product name")
        void shouldSetAndGetProductName() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory();
            inv.setProductName("Test Product");
            assertEquals("Test Product", inv.getProductName());
        }

        @Test
        @DisplayName("Should set and get expiry date")
        void shouldSetAndGetExpiryDate() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory();
            LocalDate expiry = LocalDate.of(2026, 12, 31);
            inv.setExpiryDate(expiry);
            assertEquals(expiry, inv.getExpiryDate());
        }

        @Test
        @DisplayName("Should set and get timestamps")
        void shouldSetAndGetTimestamps() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory();
            LocalDateTime now = LocalDateTime.now();
            inv.setCreatedAt(now);
            inv.setUpdatedAt(now);
            assertEquals(now, inv.getCreatedAt());
            assertEquals(now, inv.getUpdatedAt());
        }

        @Test
        @DisplayName("getProductCodeString should return null when productCode is null")
        void getProductCodeStringShouldReturnNullWhenProductCodeIsNull() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory();
            assertNull(inv.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            PhysicalStoreInventory inv = new PhysicalStoreInventory(new ProductCode("P001"), 5, 100, LocalDate.now());
            inv.setPhysicalInventoryId(1);
            String result = inv.toString();
            assertTrue(result.contains("PhysicalStoreInventory{"));
            assertTrue(result.contains("physicalInventoryId=1"));
            assertTrue(result.contains("quantityOnShelf=100"));
        }
    }
}
