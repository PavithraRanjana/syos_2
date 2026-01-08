package com.syos.domain;

import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainInventory domain model.
 */
class MainInventoryTest {

    private MainInventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new MainInventory(
            new ProductCode("BEV-001"),
            100,
            new Money(50.00),
            LocalDate.now(),
            LocalDate.now().plusMonths(6),
            "Test Supplier"
        );
        inventory.setMainInventoryId(1);
    }

    @Nested
    @DisplayName("Stock management tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should have stock when quantity > 0")
        void shouldHaveStockWhenQuantityPositive() {
            assertTrue(inventory.hasStock());
        }

        @Test
        @DisplayName("Should not have stock when quantity is 0")
        void shouldNotHaveStockWhenQuantityZero() {
            inventory.setRemainingQuantity(0);
            assertFalse(inventory.hasStock());
        }

        @Test
        @DisplayName("Should be able to fulfill smaller quantities")
        void shouldFulfillSmallerQuantities() {
            assertTrue(inventory.canFulfill(50));
            assertTrue(inventory.canFulfill(100));
            assertFalse(inventory.canFulfill(101));
        }

        @Test
        @DisplayName("Should reduce quantity correctly")
        void shouldReduceQuantityCorrectly() {
            inventory.reduceQuantity(30);
            assertEquals(70, inventory.getRemainingQuantity());
        }

        @Test
        @DisplayName("Should throw exception for negative reduction")
        void shouldThrowForNegativeReduction() {
            assertThrows(IllegalArgumentException.class, () -> inventory.reduceQuantity(-10));
        }

        @Test
        @DisplayName("Should throw exception when reducing more than available")
        void shouldThrowWhenReducingMoreThanAvailable() {
            assertThrows(IllegalArgumentException.class, () -> inventory.reduceQuantity(150));
        }

        @Test
        @DisplayName("Should increase quantity correctly")
        void shouldIncreaseQuantityCorrectly() {
            inventory.increaseQuantity(20);
            assertEquals(120, inventory.getRemainingQuantity());
        }

        @Test
        @DisplayName("Should throw exception for negative increase")
        void shouldThrowForNegativeIncrease() {
            assertThrows(IllegalArgumentException.class, () -> inventory.increaseQuantity(-10));
        }
    }

    @Nested
    @DisplayName("Expiry management tests")
    class ExpiryManagementTests {

        @Test
        @DisplayName("Should calculate days until expiry")
        void shouldCalculateDaysUntilExpiry() {
            Long daysUntilExpiry = inventory.getDaysUntilExpiry();
            assertNotNull(daysUntilExpiry);
            assertTrue(daysUntilExpiry > 0);
        }

        @Test
        @DisplayName("Should return null for no expiry date")
        void shouldReturnNullForNoExpiryDate() {
            inventory.setExpiryDate(null);
            assertNull(inventory.getDaysUntilExpiry());
        }

        @Test
        @DisplayName("Should identify expiring soon")
        void shouldIdentifyExpiringSoon() {
            inventory.setExpiryDate(LocalDate.now().plusDays(5));
            assertTrue(inventory.isExpiringSoon(7));
            assertFalse(inventory.isExpiringSoon(3));
        }

        @Test
        @DisplayName("Should not be expiring soon without expiry date")
        void shouldNotBeExpiringSoonWithoutExpiryDate() {
            inventory.setExpiryDate(null);
            assertFalse(inventory.isExpiringSoon(7));
        }

        @Test
        @DisplayName("Should identify expired batch")
        void shouldIdentifyExpiredBatch() {
            inventory.setExpiryDate(LocalDate.now().minusDays(1));
            assertTrue(inventory.isExpired());
        }

        @Test
        @DisplayName("Should identify non-expired batch")
        void shouldIdentifyNonExpiredBatch() {
            inventory.setExpiryDate(LocalDate.now().plusDays(1));
            assertFalse(inventory.isExpired());
        }

        @Test
        @DisplayName("Should not be expired without expiry date")
        void shouldNotBeExpiredWithoutExpiryDate() {
            inventory.setExpiryDate(null);
            assertFalse(inventory.isExpired());
        }
    }

    @Nested
    @DisplayName("Batch number tests")
    class BatchNumberTests {

        @Test
        @DisplayName("Batch number should be same as inventory ID")
        void batchNumberShouldBeSameAsInventoryId() {
            assertEquals(inventory.getMainInventoryId(), inventory.getBatchNumber());
        }
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize remaining quantity to received quantity")
        void shouldInitializeRemainingQuantity() {
            MainInventory newInventory = new MainInventory(
                new ProductCode("TEST-001"),
                50,
                new Money(25.00),
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                "Supplier"
            );
            assertEquals(50, newInventory.getRemainingQuantity());
            assertEquals(50, newInventory.getQuantityReceived());
        }

        @Test
        @DisplayName("Default constructor should create empty inventory")
        void defaultConstructorShouldCreateEmptyInventory() {
            MainInventory empty = new MainInventory();
            assertNull(empty.getProductCode());
            assertEquals(0, empty.getRemainingQuantity());
        }
    }

    @Nested
    @DisplayName("Getter/Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get product code string")
        void shouldGetProductCodeString() {
            assertEquals("BEV-001", inventory.getProductCodeString());
        }

        @Test
        @DisplayName("Should return null for product code string when no code")
        void shouldReturnNullForProductCodeStringWhenNoCode() {
            MainInventory empty = new MainInventory();
            assertNull(empty.getProductCodeString());
        }

        @Test
        @DisplayName("Should set and get supplier name")
        void shouldSetAndGetSupplierName() {
            inventory.setSupplierName("New Supplier");
            assertEquals("New Supplier", inventory.getSupplierName());
        }

        @Test
        @DisplayName("Should set and get product name")
        void shouldSetAndGetProductName() {
            inventory.setProductName("Test Product");
            assertEquals("Test Product", inventory.getProductName());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain batch number")
        void toStringShouldContainBatchNumber() {
            String str = inventory.toString();
            assertTrue(str.contains("batchNumber=1"));
        }

        @Test
        @DisplayName("toString should contain product code")
        void toStringShouldContainProductCode() {
            String str = inventory.toString();
            assertTrue(str.contains("BEV-001"));
        }
    }
}
