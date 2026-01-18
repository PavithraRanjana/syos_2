package com.syos.repository.impl;

import com.syos.domain.models.MainInventory;
import com.syos.exception.RepositoryException;
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
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MainInventoryRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MainInventoryRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private MainInventoryRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new MainInventoryRepositoryImpl(dataSource);
    }

    private void mockInventoryResultSet(int id, String productCode, int quantity) throws SQLException {
        when(resultSet.getInt("main_inventory_id")).thenReturn(id);
        when(resultSet.getString("product_code")).thenReturn(productCode);
        when(resultSet.getInt("quantity_received")).thenReturn(quantity);
        when(resultSet.getBigDecimal("purchase_price")).thenReturn(BigDecimal.valueOf(10.00));
        when(resultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(30)));
        when(resultSet.getString("supplier_name")).thenReturn("Supplier Inc");
        when(resultSet.getInt("remaining_quantity")).thenReturn(quantity);
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getString("product_name")).thenReturn("Test Product");
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new inventory batch")
        void shouldInsertNewInventoryBatch() throws Exception {
            MainInventory inventory = new MainInventory();
            inventory.setProductCode(new com.syos.domain.valueobjects.ProductCode("P001"));
            inventory.setQuantityReceived(100);
            inventory.setPurchasePrice(new com.syos.domain.valueobjects.Money(BigDecimal.valueOf(10.00)));
            inventory.setPurchaseDate(LocalDate.now());
            inventory.setRemainingQuantity(100);

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            MainInventory saved = repository.save(inventory);

            assertEquals(1, saved.getMainInventoryId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find inventory by ID")
        void shouldFindInventoryById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 100);

            Optional<MainInventory> result = repository.findById(1);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getRemainingQuantity());
        }

        @Test
        @DisplayName("Should return empty when inventory not found")
        void shouldReturnEmptyWhenNotFound() throws Exception {
            when(resultSet.next()).thenReturn(false);

            Optional<MainInventory> result = repository.findById(999);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByProductCode tests")
    class FindByProductCodeTests {

        @Test
        @DisplayName("Should find inventory batches by product code")
        void shouldFindInventoryByProductCode() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 100);

            List<MainInventory> result = repository.findByProductCode("P001");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findAvailableBatchesByProductCode tests")
    class FindAvailableBatchesTests {

        @Test
        @DisplayName("Should find available batches by product code")
        void shouldFindAvailableBatches() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findAvailableBatchesByProductCode("P001");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findNextBatchForSale tests")
    class FindNextBatchForSaleTests {

        @Test
        @DisplayName("Should find next batch for sale")
        void shouldFindNextBatchForSale() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            Optional<MainInventory> result = repository.findNextBatchForSale("P001", 10);

            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("findExpiringWithinDays tests")
    class FindExpiringWithinDaysTests {

        @Test
        @DisplayName("Should find expiring inventory")
        void shouldFindExpiringInventory() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findExpiringWithinDays(7);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findExpiredBatches tests")
    class FindExpiredBatchesTests {

        @Test
        @DisplayName("Should find expired batches")
        void shouldFindExpiredBatches() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findExpiredBatches();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findBySupplier tests")
    class FindBySupplierTests {

        @Test
        @DisplayName("Should find inventory by supplier")
        void shouldFindInventoryBySupplier() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findBySupplier("Supplier");

            assertEquals(1, result.size());
            verify(preparedStatement).setString(1, "%Supplier%");
        }
    }

    @Nested
    @DisplayName("getTotalRemainingQuantity tests")
    class GetTotalRemainingQuantityTests {

        @Test
        @DisplayName("Should get total remaining quantity")
        void shouldGetTotalRemainingQuantity() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(500);

            int total = repository.getTotalRemainingQuantity("P001");

            assertEquals(500, total);
        }
    }

    @Nested
    @DisplayName("reduceQuantity tests")
    class ReduceQuantityTests {

        @Test
        @DisplayName("Should reduce quantity")
        void shouldReduceQuantity() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.reduceQuantity(1, 10);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when insufficient quantity")
        void shouldReturnFalseWhenInsufficientQuantity() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = repository.reduceQuantity(1, 1000);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("increaseQuantity tests")
    class IncreaseQuantityTests {

        @Test
        @DisplayName("Should increase quantity")
        void shouldIncreaseQuantity() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.increaseQuantity(1, 10);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("findByPurchaseDateRange tests")
    class FindByPurchaseDateRangeTests {

        @Test
        @DisplayName("Should find inventory by purchase date range")
        void shouldFindByPurchaseDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            LocalDate start = LocalDate.now().minusDays(30);
            LocalDate end = LocalDate.now();
            List<MainInventory> result = repository.findByPurchaseDateRange(start, end);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all inventory")
        void shouldFindAllInventory() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findAll();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find all inventory with pagination")
        void shouldFindAllInventoryWithPagination() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 50);

            List<MainInventory> result = repository.findAll(0, 10);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("deleteById tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete inventory by ID")
        void shouldDeleteInventoryById() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.deleteById(1);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("count tests")
    class CountTests {

        @Test
        @DisplayName("Should return inventory count")
        void shouldReturnInventoryCount() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(100L);

            long count = repository.count();

            assertEquals(100L, count);
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
