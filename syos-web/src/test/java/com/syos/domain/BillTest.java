package com.syos.domain;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Bill entity.
 */
class BillTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create bill with serial number, store type, and transaction type")
        void shouldCreateBillWithSerialNumberStoreTypeAndTransactionType() {
            // Arrange
            BillSerialNumber serialNumber = new BillSerialNumber(12345);
            StoreType storeType = StoreType.PHYSICAL;
            TransactionType transactionType = TransactionType.CASH;

            // Act
            Bill bill = new Bill(serialNumber, storeType, transactionType);

            // Assert
            assertNotNull(bill);
            assertEquals(serialNumber, bill.getSerialNumber());
            assertEquals(storeType, bill.getStoreType());
            assertEquals(transactionType, bill.getTransactionType());
            assertNotNull(bill.getBillDate()); // billDate is set in default constructor
            assertTrue(bill.isEmpty()); // No items initially
        }

        @Test
        @DisplayName("Should create bill for online store with online transaction type")
        void shouldCreateBillForOnlineStore() {
            // Arrange
            BillSerialNumber serialNumber = new BillSerialNumber(99999);
            StoreType storeType = StoreType.ONLINE;
            TransactionType transactionType = TransactionType.ONLINE;

            // Act
            Bill bill = new Bill(serialNumber, storeType, transactionType);

            // Assert
            assertEquals(StoreType.ONLINE, bill.getStoreType());
            assertEquals(TransactionType.ONLINE, bill.getTransactionType());
            assertEquals("99999", bill.getSerialNumberString());
        }

        @Test
        @DisplayName("Should initialize default money values to zero")
        void shouldInitializeDefaultMoneyValuesToZero() {
            // Arrange & Act
            Bill bill = new Bill(new BillSerialNumber(1), StoreType.PHYSICAL, TransactionType.CASH);

            // Assert
            assertNotNull(bill.getSubtotal());
            assertNotNull(bill.getTotalAmount());
            assertNotNull(bill.getDiscountAmount());
            assertEquals(0, bill.getSubtotal().getAmount().intValue());
            assertEquals(0, bill.getTotalAmount().getAmount().intValue());
            assertEquals(0, bill.getDiscountAmount().getAmount().intValue());
        }
    }

    @Nested
    @DisplayName("getBillTime tests")
    class GetBillTimeTests {

        @Test
        @DisplayName("Should return formatted time as HH:mm")
        void shouldReturnFormattedTime() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime specificDate = LocalDateTime.of(2026, 1, 18, 14, 30, 45);
            bill.setBillDate(specificDate);

            // Act
            String result = bill.getBillTime();

            // Assert
            assertEquals("14:30", result);
        }

        @Test
        @DisplayName("Should return empty string when bill date is null")
        void shouldReturnEmptyStringWhenBillDateIsNull() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(null);

            // Act
            String result = bill.getBillTime();

            // Assert
            assertEquals("", result);
        }

        @Test
        @DisplayName("Should format morning time correctly with leading zero")
        void shouldFormatMorningTimeCorrectly() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(LocalDateTime.of(2026, 1, 18, 9, 5, 0));

            // Act
            String result = bill.getBillTime();

            // Assert
            assertEquals("09:05", result);
        }

        @Test
        @DisplayName("Should format midnight correctly")
        void shouldFormatMidnightCorrectly() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(LocalDateTime.of(2026, 1, 18, 0, 0, 0));

            // Act
            String result = bill.getBillTime();

            // Assert
            assertEquals("00:00", result);
        }
    }

    @Nested
    @DisplayName("getBillDateFormatted tests")
    class GetBillDateFormattedTests {

        @Test
        @DisplayName("Should return formatted date as yyyy-MM-dd")
        void shouldReturnFormattedDate() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime specificDate = LocalDateTime.of(2026, 1, 18, 14, 30, 45);
            bill.setBillDate(specificDate);

            // Act
            String result = bill.getBillDateFormatted();

            // Assert
            assertEquals("2026-01-18", result);
        }

        @Test
        @DisplayName("Should return empty string when bill date is null")
        void shouldReturnEmptyStringWhenBillDateIsNull() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(null);

            // Act
            String result = bill.getBillDateFormatted();

            // Assert
            assertEquals("", result);
        }

        @Test
        @DisplayName("Should format single digit month and day with leading zeros")
        void shouldFormatSingleDigitMonthAndDayWithLeadingZeros() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(LocalDateTime.of(2026, 3, 5, 12, 0, 0));

            // Act
            String result = bill.getBillDateFormatted();

            // Assert
            assertEquals("2026-03-05", result);
        }
    }

    @Nested
    @DisplayName("getBillDateTimeFormatted tests")
    class GetBillDateTimeFormattedTests {

        @Test
        @DisplayName("Should return formatted date and time as yyyy-MM-dd HH:mm")
        void shouldReturnFormattedDateTime() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime specificDate = LocalDateTime.of(2026, 1, 18, 14, 30, 45);
            bill.setBillDate(specificDate);

            // Act
            String result = bill.getBillDateTimeFormatted();

            // Assert
            assertEquals("2026-01-18 14:30", result);
        }

        @Test
        @DisplayName("Should return empty string when bill date is null")
        void shouldReturnEmptyStringWhenBillDateIsNull() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(null);

            // Act
            String result = bill.getBillDateTimeFormatted();

            // Assert
            assertEquals("", result);
        }

        @Test
        @DisplayName("Should format date and time with leading zeros")
        void shouldFormatDateAndTimeWithLeadingZeros() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(LocalDateTime.of(2026, 3, 5, 9, 5, 0));

            // Act
            String result = bill.getBillDateTimeFormatted();

            // Assert
            assertEquals("2026-03-05 09:05", result);
        }

        @Test
        @DisplayName("Should format end of year date correctly")
        void shouldFormatEndOfYearDateCorrectly() {
            // Arrange
            Bill bill = new Bill();
            bill.setBillDate(LocalDateTime.of(2026, 12, 31, 23, 59, 59));

            // Act
            String result = bill.getBillDateTimeFormatted();

            // Assert
            assertEquals("2026-12-31 23:59", result);
        }
    }

    @Nested
    @DisplayName("processCashPayment tests")
    class ProcessCashPaymentTests {

        @Test
        @DisplayName("Should process cash payment with exact amount")
        void shouldProcessCashPaymentWithExactAmount() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 2, 100.00);
            bill.addItem(item);
            Money tendered = new Money(java.math.BigDecimal.valueOf(200.00));

            // Act
            bill.processCashPayment(tendered);

            // Assert
            assertEquals(tendered, bill.getTenderedAmount());
            assertEquals(0, bill.getChangeAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should calculate correct change when overpaying")
        void shouldCalculateCorrectChangeWhenOverpaying() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 1, 75.00);
            bill.addItem(item);
            Money tendered = new Money(java.math.BigDecimal.valueOf(100.00));

            // Act
            bill.processCashPayment(tendered);

            // Assert
            assertEquals(tendered, bill.getTenderedAmount());
            assertEquals(25, bill.getChangeAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should throw exception when tendered amount is null")
        void shouldThrowExceptionWhenTenderedAmountIsNull() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 1, 100.00);
            bill.addItem(item);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> bill.processCashPayment(null));
        }

        @Test
        @DisplayName("Should throw exception when tendered amount is insufficient")
        void shouldThrowExceptionWhenTenderedAmountIsInsufficient() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 1, 100.00);
            bill.addItem(item);
            Money tendered = new Money(java.math.BigDecimal.valueOf(50.00));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> bill.processCashPayment(tendered));
        }
    }

    @Nested
    @DisplayName("removeItem tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Should remove item from bill")
        void shouldRemoveItemFromBill() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 2, 50.00);
            bill.addItem(item);
            assertEquals(1, bill.getItemCount());

            // Act
            bill.removeItem(item);

            // Assert
            assertEquals(0, bill.getItemCount());
            assertTrue(bill.isEmpty());
        }

        @Test
        @DisplayName("Should recalculate totals after removing item")
        void shouldRecalculateTotalsAfterRemovingItem() {
            // Arrange
            Bill bill = new Bill();
            BillItem item1 = createTestBillItem("P1", 1, 100.00);
            BillItem item2 = createTestBillItem("P2", 1, 50.00);
            bill.addItem(item1);
            bill.addItem(item2);
            assertEquals(150, bill.getTotalAmount().getAmount().intValue());

            // Act
            bill.removeItem(item1);

            // Assert
            assertEquals(50, bill.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should handle removing non-existent item gracefully")
        void shouldHandleRemovingNonExistentItemGracefully() {
            // Arrange
            Bill bill = new Bill();
            BillItem item1 = createTestBillItem("P1", 1, 100.00);
            BillItem item2 = createTestBillItem("P2", 1, 50.00);
            bill.addItem(item1);

            // Act - remove item that was never added
            bill.removeItem(item2);

            // Assert - should still have original item
            assertEquals(1, bill.getItemCount());
        }
    }

    @Nested
    @DisplayName("removeItemByProductCode tests")
    class RemoveItemByProductCodeTests {

        @Test
        @DisplayName("Should remove item by product code")
        void shouldRemoveItemByProductCode() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 2, 50.00);
            bill.addItem(item);

            // Act
            bill.removeItemByProductCode("P1");

            // Assert
            assertTrue(bill.isEmpty());
        }

        @Test
        @DisplayName("Should only remove matching product code")
        void shouldOnlyRemoveMatchingProductCode() {
            // Arrange
            Bill bill = new Bill();
            BillItem item1 = createTestBillItem("P1", 1, 100.00);
            BillItem item2 = createTestBillItem("P2", 1, 50.00);
            bill.addItem(item1);
            bill.addItem(item2);

            // Act
            bill.removeItemByProductCode("P1");

            // Assert
            assertEquals(1, bill.getItemCount());
            assertEquals(50, bill.getTotalAmount().getAmount().intValue());
        }

        @Test
        @DisplayName("Should handle removing non-existent product code")
        void shouldHandleRemovingNonExistentProductCode() {
            // Arrange
            Bill bill = new Bill();
            BillItem item = createTestBillItem("P1", 1, 100.00);
            bill.addItem(item);

            // Act
            bill.removeItemByProductCode("P999");

            // Assert - item should still exist
            assertEquals(1, bill.getItemCount());
        }
    }

    @Nested
    @DisplayName("getTotalQuantity tests")
    class GetTotalQuantityTests {

        @Test
        @DisplayName("Should return zero for empty bill")
        void shouldReturnZeroForEmptyBill() {
            // Arrange
            Bill bill = new Bill();

            // Act
            int result = bill.getTotalQuantity();

            // Assert
            assertEquals(0, result);
        }

        @Test
        @DisplayName("Should return sum of all item quantities")
        void shouldReturnSumOfAllItemQuantities() {
            // Arrange
            Bill bill = new Bill();
            bill.addItem(createTestBillItem("P1", 3, 10.00));
            bill.addItem(createTestBillItem("P2", 5, 20.00));
            bill.addItem(createTestBillItem("P3", 2, 30.00));

            // Act
            int result = bill.getTotalQuantity();

            // Assert
            assertEquals(10, result);
        }

        @Test
        @DisplayName("Should return single item quantity")
        void shouldReturnSingleItemQuantity() {
            // Arrange
            Bill bill = new Bill();
            bill.addItem(createTestBillItem("P1", 7, 15.00));

            // Act
            int result = bill.getTotalQuantity();

            // Assert
            assertEquals(7, result);
        }
    }

    @Nested
    @DisplayName("getCreatedAt and getUpdatedAt tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set and get createdAt timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 10, 30, 0);

            // Act
            bill.setCreatedAt(createdAt);

            // Assert
            assertEquals(createdAt, bill.getCreatedAt());
        }

        @Test
        @DisplayName("Should return null when createdAt is not set")
        void shouldReturnNullWhenCreatedAtIsNotSet() {
            // Arrange
            Bill bill = new Bill();

            // Act & Assert
            assertNull(bill.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 18, 15, 45, 30);

            // Act
            bill.setUpdatedAt(updatedAt);

            // Assert
            assertEquals(updatedAt, bill.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return null when updatedAt is not set")
        void shouldReturnNullWhenUpdatedAtIsNotSet() {
            // Arrange
            Bill bill = new Bill();

            // Act & Assert
            assertNull(bill.getUpdatedAt());
        }

        @Test
        @DisplayName("Should allow updatedAt to be after createdAt")
        void shouldAllowUpdatedAtToBeAfterCreatedAt() {
            // Arrange
            Bill bill = new Bill();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 10, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 18, 12, 30, 0);

            // Act
            bill.setCreatedAt(createdAt);
            bill.setUpdatedAt(updatedAt);

            // Assert
            assertTrue(bill.getUpdatedAt().isAfter(bill.getCreatedAt()));
        }
    }

    // Helper method to create test bill items
    private BillItem createTestBillItem(String productCode, int quantity, double unitPrice) {
        BillItem item = new BillItem();
        item.setProductCode(new com.syos.domain.valueobjects.ProductCode(productCode));
        item.setQuantity(quantity);
        item.setUnitPrice(new Money(java.math.BigDecimal.valueOf(unitPrice)));
        item.setTotalPrice(new Money(java.math.BigDecimal.valueOf(unitPrice * quantity)));
        return item;
    }
}
