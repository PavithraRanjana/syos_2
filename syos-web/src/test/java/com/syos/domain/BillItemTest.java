package com.syos.domain;

import com.syos.domain.models.BillItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BillItem model.
 */
class BillItemTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create bill item with all parameters")
        void shouldCreateBillItemWithAllParameters() {
            // Arrange
            ProductCode productCode = new ProductCode("P001");
            String productName = "Test Product";
            int quantity = 3;
            Money unitPrice = new Money(BigDecimal.valueOf(25.00));
            Integer mainInventoryId = 101;

            // Act
            BillItem item = new BillItem(productCode, productName, quantity, unitPrice, mainInventoryId);

            // Assert
            assertEquals(productCode, item.getProductCode());
            assertEquals("P001", item.getProductCodeString());
            assertEquals(productName, item.getProductName());
            assertEquals(quantity, item.getQuantity());
            assertEquals(unitPrice, item.getUnitPrice());
            assertEquals(mainInventoryId, item.getMainInventoryId());
            assertEquals(75, item.getTotalPrice().getAmount().intValue()); // 3 * 25
        }

        @Test
        @DisplayName("Should create empty bill item with default constructor")
        void shouldCreateEmptyBillItemWithDefaultConstructor() {
            // Arrange & Act
            BillItem item = new BillItem();

            // Assert
            assertNull(item.getBillItemId());
            assertNull(item.getBillId());
            assertNull(item.getProductCode());
            assertNull(item.getProductName());
            assertEquals(0, item.getQuantity());
            assertNull(item.getUnitPrice());
            assertNull(item.getTotalPrice());
        }

        @Test
        @DisplayName("Should calculate total price automatically in constructor")
        void shouldCalculateTotalPriceAutomatically() {
            // Arrange
            Money unitPrice = new Money(BigDecimal.valueOf(10.50));

            // Act
            BillItem item = new BillItem(new ProductCode("P1"), "Product", 4, unitPrice, 1);

            // Assert - 4 * 10.50 = 42.00
            assertEquals(new BigDecimal("42.00"), item.getTotalPrice().getAmount());
        }
    }

    @Nested
    @DisplayName("recalculateTotal tests")
    class RecalculateTotalTests {

        @Test
        @DisplayName("Should recalculate total when quantity changes")
        void shouldRecalculateTotalWhenQuantityChanges() {
            // Arrange
            BillItem item = new BillItem();
            item.setUnitPrice(new Money(BigDecimal.valueOf(20.00)));
            item.setQuantity(5);

            // Act
            item.recalculateTotal();

            // Assert - 5 * 20.00 = 100.00
            assertEquals(100, item.getTotalPrice().getAmount().intValue());
        }

        @Test
        @DisplayName("Should not recalculate when unit price is null")
        void shouldNotRecalculateWhenUnitPriceIsNull() {
            // Arrange
            BillItem item = new BillItem();
            item.setQuantity(5);
            // unitPrice is null

            // Act
            item.recalculateTotal();

            // Assert - totalPrice should remain null
            assertNull(item.getTotalPrice());
        }

        @Test
        @DisplayName("Should not recalculate when quantity is zero or less")
        void shouldNotRecalculateWhenQuantityIsZeroOrLess() {
            // Arrange
            BillItem item = new BillItem();
            item.setUnitPrice(new Money(BigDecimal.valueOf(20.00)));
            item.setQuantity(0);
            item.setTotalPrice(new Money(BigDecimal.valueOf(999))); // Set a value

            // Act
            item.recalculateTotal();

            // Assert - totalPrice should remain unchanged
            assertEquals(999, item.getTotalPrice().getAmount().intValue());
        }
    }

    @Nested
    @DisplayName("updateQuantity tests")
    class UpdateQuantityTests {

        @Test
        @DisplayName("Should update quantity and recalculate total")
        void shouldUpdateQuantityAndRecalculateTotal() {
            // Arrange
            BillItem item = new BillItem(new ProductCode("P1"), "Product", 2,
                    new Money(BigDecimal.valueOf(15.00)), 1);
            assertEquals(30, item.getTotalPrice().getAmount().intValue()); // 2 * 15

            // Act
            item.updateQuantity(5);

            // Assert
            assertEquals(5, item.getQuantity());
            assertEquals(75, item.getTotalPrice().getAmount().intValue()); // 5 * 15
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void shouldThrowExceptionWhenQuantityIsZero() {
            // Arrange
            BillItem item = new BillItem(new ProductCode("P1"), "Product", 2,
                    new Money(BigDecimal.valueOf(15.00)), 1);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> item.updateQuantity(0));
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            // Arrange
            BillItem item = new BillItem(new ProductCode("P1"), "Product", 2,
                    new Money(BigDecimal.valueOf(15.00)), 1);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> item.updateQuantity(-3));
        }
    }

    @Nested
    @DisplayName("Alias methods tests")
    class AliasMethodsTests {

        @Test
        @DisplayName("getBatchNumber should return mainInventoryId")
        void getBatchNumberShouldReturnMainInventoryId() {
            // Arrange
            BillItem item = new BillItem();
            item.setMainInventoryId(42);

            // Act & Assert
            assertEquals(42, item.getBatchNumber());
            assertEquals(item.getMainInventoryId(), item.getBatchNumber());
        }

        @Test
        @DisplayName("getLineTotal should return totalPrice")
        void getLineTotalShouldReturnTotalPrice() {
            // Arrange
            BillItem item = new BillItem();
            Money totalPrice = new Money(BigDecimal.valueOf(150.00));
            item.setTotalPrice(totalPrice);

            // Act & Assert
            assertEquals(totalPrice, item.getLineTotal());
            assertEquals(item.getTotalPrice(), item.getLineTotal());
        }

        @Test
        @DisplayName("setLineTotal should set totalPrice")
        void setLineTotalShouldSetTotalPrice() {
            // Arrange
            BillItem item = new BillItem();
            Money lineTotal = new Money(BigDecimal.valueOf(200.00));

            // Act
            item.setLineTotal(lineTotal);

            // Assert
            assertEquals(lineTotal, item.getTotalPrice());
        }

        @Test
        @DisplayName("getProductCodeString should return code or null")
        void getProductCodeStringShouldReturnCodeOrNull() {
            // Arrange
            BillItem itemWithCode = new BillItem();
            itemWithCode.setProductCode(new ProductCode("ABC123"));

            BillItem itemWithoutCode = new BillItem();

            // Act & Assert
            assertEquals("ABC123", itemWithCode.getProductCodeString());
            assertNull(itemWithoutCode.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get billItemId")
        void shouldSetAndGetBillItemId() {
            BillItem item = new BillItem();
            item.setBillItemId(100);
            assertEquals(100, item.getBillItemId());
        }

        @Test
        @DisplayName("Should set and get billId")
        void shouldSetAndGetBillId() {
            BillItem item = new BillItem();
            item.setBillId(50);
            assertEquals(50, item.getBillId());
        }

        @Test
        @DisplayName("Should set and get createdAt")
        void shouldSetAndGetCreatedAt() {
            BillItem item = new BillItem();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 14, 30);
            item.setCreatedAt(createdAt);
            assertEquals(createdAt, item.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string with all fields")
        void shouldReturnFormattedStringWithAllFields() {
            // Arrange
            BillItem item = new BillItem(
                    new ProductCode("P001"),
                    "Test Product",
                    3,
                    new Money(BigDecimal.valueOf(25.00)),
                    101);

            // Act
            String result = item.toString();

            // Assert
            assertTrue(result.contains("productCode="));
            assertTrue(result.contains("productName='Test Product'"));
            assertTrue(result.contains("quantity=3"));
            assertTrue(result.contains("batchNumber=101"));
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            // Arrange
            BillItem item = new BillItem();

            // Act
            String result = item.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("productCode=null"));
            assertTrue(result.contains("quantity=0"));
        }
    }
}
