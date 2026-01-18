package com.syos.domain;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.InventoryTransaction;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InventoryTransaction model.
 */
class InventoryTransactionTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create transaction with default constructor")
        void shouldCreateTransactionWithDefaultConstructor() {
            InventoryTransaction tx = new InventoryTransaction();

            assertNull(tx.getTransactionId());
            assertNull(tx.getProductCode());
            assertNotNull(tx.getTransactionDate());
            assertEquals(0, tx.getQuantityChanged());
        }

        @Test
        @DisplayName("Should create transaction with all parameters")
        void shouldCreateTransactionWithAllParameters() {
            ProductCode code = new ProductCode("P001");

            InventoryTransaction tx = new InventoryTransaction(
                    code, 10, InventoryTransactionType.SALE, StoreType.PHYSICAL, -5);

            assertEquals(code, tx.getProductCode());
            assertEquals("P001", tx.getProductCodeString());
            assertEquals(10, tx.getMainInventoryId());
            assertEquals(InventoryTransactionType.SALE, tx.getTransactionType());
            assertEquals(StoreType.PHYSICAL, tx.getStoreType());
            assertEquals(-5, tx.getQuantityChanged());
            assertNotNull(tx.getTransactionDate());
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get transactionId")
        void shouldSetAndGetTransactionId() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setTransactionId(100);
            assertEquals(100, tx.getTransactionId());
        }

        @Test
        @DisplayName("Should set and get productCode")
        void shouldSetAndGetProductCode() {
            InventoryTransaction tx = new InventoryTransaction();
            ProductCode code = new ProductCode("ABC");
            tx.setProductCode(code);
            assertEquals(code, tx.getProductCode());
            assertEquals("ABC", tx.getProductCodeString());
        }

        @Test
        @DisplayName("Should set and get mainInventoryId")
        void shouldSetAndGetMainInventoryId() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setMainInventoryId(25);
            assertEquals(25, tx.getMainInventoryId());
        }

        @Test
        @DisplayName("Should set and get transactionType")
        void shouldSetAndGetTransactionType() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setTransactionType(InventoryTransactionType.RESTOCK_PHYSICAL);
            assertEquals(InventoryTransactionType.RESTOCK_PHYSICAL, tx.getTransactionType());
        }

        @Test
        @DisplayName("Should set and get storeType")
        void shouldSetAndGetStoreType() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setStoreType(StoreType.ONLINE);
            assertEquals(StoreType.ONLINE, tx.getStoreType());
        }

        @Test
        @DisplayName("Should set and get quantityChanged")
        void shouldSetAndGetQuantityChanged() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setQuantityChanged(-10);
            assertEquals(-10, tx.getQuantityChanged());
        }

        @Test
        @DisplayName("Should set and get billId")
        void shouldSetAndGetBillId() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setBillId(500);
            assertEquals(500, tx.getBillId());
        }

        @Test
        @DisplayName("Should set and get transactionDate")
        void shouldSetAndGetTransactionDate() {
            InventoryTransaction tx = new InventoryTransaction();
            LocalDateTime date = LocalDateTime.of(2026, 1, 18, 15, 30);
            tx.setTransactionDate(date);
            assertEquals(date, tx.getTransactionDate());
        }

        @Test
        @DisplayName("Should set and get remarks")
        void shouldSetAndGetRemarks() {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setRemarks("Test remark");
            assertEquals("Test remark", tx.getRemarks());
        }

        @Test
        @DisplayName("getProductCodeString should return null when productCode is null")
        void getProductCodeStringShouldReturnNullWhenProductCodeIsNull() {
            InventoryTransaction tx = new InventoryTransaction();
            assertNull(tx.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            InventoryTransaction tx = new InventoryTransaction(
                    new ProductCode("P001"), 10, InventoryTransactionType.SALE, StoreType.PHYSICAL, -5);
            tx.setTransactionId(1);

            String result = tx.toString();
            assertTrue(result.contains("InventoryTransaction{"));
            assertTrue(result.contains("transactionId=1"));
            assertTrue(result.contains("transactionType=SALE"));
            assertTrue(result.contains("storeType=PHYSICAL"));
            assertTrue(result.contains("quantityChanged=-5"));
        }
    }
}
