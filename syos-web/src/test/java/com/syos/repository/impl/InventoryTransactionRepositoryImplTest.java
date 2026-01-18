package com.syos.repository.impl;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.InventoryTransaction;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.InventoryTransactionRepository.DailyTransactionSummary;
import com.syos.repository.interfaces.InventoryTransactionRepository.TransactionTypeSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryTransactionRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryTransactionRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private InventoryTransactionRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new InventoryTransactionRepositoryImpl(dataSource);
    }

    private void mockTransactionResultSet(int id, String productCode, InventoryTransactionType type, int qty)
            throws SQLException {
        when(resultSet.getInt("transaction_id")).thenReturn(id);
        when(resultSet.getString("product_code")).thenReturn(productCode);
        when(resultSet.getInt("main_inventory_id")).thenReturn(1);
        when(resultSet.getString("transaction_type")).thenReturn(type.name());
        when(resultSet.getString("store_type")).thenReturn(StoreType.PHYSICAL.name());
        when(resultSet.getInt("quantity_changed")).thenReturn(qty);
        when(resultSet.getTimestamp("transaction_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("remarks")).thenReturn("Test");
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new transaction")
        void shouldInsertNewTransaction() throws Exception {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProductCode(new ProductCode("P001"));
            transaction.setMainInventoryId(1);
            transaction.setTransactionType(InventoryTransactionType.RESTOCK_PHYSICAL);
            transaction.setStoreType(StoreType.PHYSICAL);
            transaction.setQuantityChanged(10);
            transaction.setRemarks("Initial stock");
            transaction.setTransactionDate(LocalDateTime.now());

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            InventoryTransaction saved = repository.save(transaction);

            assertEquals(1, saved.getTransactionId());
        }

        @Test
        @DisplayName("Should update existing transaction")
        void shouldUpdateTransaction() throws Exception {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setTransactionId(1);
            transaction.setProductCode(new ProductCode("P001")); // Needed for object state but not update SQL
            transaction.setRemarks("Updated");

            // Mock existsById check
            PreparedStatement existsStmt = mock(PreparedStatement.class);
            ResultSet existsRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(existsStmt);
            when(existsStmt.executeQuery()).thenReturn(existsRs);
            when(existsRs.next()).thenReturn(true);
            when(existsRs.getInt(1)).thenReturn(1);

            // Mock update
            when(connection.prepareStatement(contains("UPDATE inventory_transaction"))).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            InventoryTransaction updated = repository.save(transaction);

            assertEquals("Updated", updated.getRemarks());
        }
    }

    @Nested
    @DisplayName("find tests")
    class FindTests {
        @Test
        void shouldFindById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            Optional<InventoryTransaction> result = repository.findById(1);
            assertTrue(result.isPresent());
        }

        @Test
        void shouldFindByProductCode() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findByProductCode("P001");
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByMainInventoryId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findByMainInventoryId(1);
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByTransactionType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository
                    .findByTransactionType(InventoryTransactionType.RESTOCK_PHYSICAL);
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByStoreType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findByStoreType(StoreType.PHYSICAL);
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByBillId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.SALE, -1);
            List<InventoryTransaction> result = repository.findByBillId(1);
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.SALE, -1);
            List<InventoryTransaction> result = repository.findByDateRange(LocalDateTime.now(), LocalDateTime.now());
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindByProductCodeAndDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.SALE, -1);
            List<InventoryTransaction> result = repository.findByProductCodeAndDateRange("P001", LocalDateTime.now(),
                    LocalDateTime.now());
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindRecentTransactions() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findRecent(10);
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindAll() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findAll();
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindAllPaged() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockTransactionResultSet(1, "P001", InventoryTransactionType.RESTOCK_PHYSICAL, 10);
            List<InventoryTransaction> result = repository.findAll(0, 10);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTotalQuantityChange tests")
    class GetTotalQuantityChangeTests {

        @Test
        @DisplayName("Should get total quantity change")
        void shouldGetTotalQuantityChange() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(100);

            int total = repository.getTotalQuantityChange("P001", LocalDateTime.now(), LocalDateTime.now());

            assertEquals(100, total);
        }
    }

    @Nested
    @DisplayName("getSummaryByType tests")
    class GetSummaryByTypeTests {

        @Test
        @DisplayName("Should get summary by type")
        void shouldGetSummaryByType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("transaction_type")).thenReturn("RESTOCK_PHYSICAL");
            when(resultSet.getInt("transaction_count")).thenReturn(5);
            when(resultSet.getInt("total_quantity")).thenReturn(50);

            List<TransactionTypeSummary> result = repository.getSummaryByType(LocalDateTime.now(), LocalDateTime.now());

            assertEquals(1, result.size());
            assertEquals(50, result.get(0).totalQuantity());
        }
    }

    @Nested
    @DisplayName("getDailySummary tests")
    class GetDailySummaryTests {

        @Test
        @DisplayName("Should get daily summary")
        void shouldGetDailySummary() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getDate("trans_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(resultSet.getInt("transaction_count")).thenReturn(10);
            when(resultSet.getInt("sales_qty")).thenReturn(5);
            when(resultSet.getInt("restock_qty")).thenReturn(5);

            List<DailyTransactionSummary> result = repository.getDailySummary(LocalDate.now(), LocalDate.now());

            assertEquals(1, result.size());
            assertEquals(10, result.get(0).transactionCount());
        }
    }

    @Nested
    @DisplayName("delete/count tests")
    class DeleteCountTests {
        @Test
        void shouldDeleteById() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = repository.deleteById(1);
            assertTrue(result);
        }

        @Test
        void shouldCount() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(20L);
            long count = repository.count();
            assertEquals(20L, count);
        }

        @Test
        void shouldExistsById() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            boolean exists = repository.existsById(1);
            assertTrue(exists);
        }
    }

    @Nested
    @DisplayName("error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RepositoryException on SQL error")
        void shouldThrowRepositoryExceptionOnSqlError() throws Exception {
            when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

            assertThrows(RepositoryException.class, () -> repository.findAll());
        }
    }
}
