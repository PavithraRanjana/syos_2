package com.syos.repository.impl;

import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnlineStoreInventoryRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlineStoreInventoryRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private OnlineStoreInventoryRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new OnlineStoreInventoryRepositoryImpl(dataSource);
    }

    private void mockInventoryResultSet(int id, String productCode, int quantity) throws SQLException {
        when(resultSet.getInt("online_store_inventory_id")).thenReturn(id);
        when(resultSet.getString("product_code")).thenReturn(productCode);
        when(resultSet.getInt("main_inventory_id")).thenReturn(1);
        when(resultSet.getInt("quantity_available")).thenReturn(quantity);
        when(resultSet.getDate("restocked_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getString("product_name")).thenReturn("Test Product");
        when(resultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(30)));
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new inventory")
        void shouldInsertNewInventory() throws Exception {
            OnlineStoreInventory inventory = new OnlineStoreInventory();
            inventory.setProductCode(new ProductCode("P001"));
            inventory.setMainInventoryId(1);
            inventory.setQuantityAvailable(10);
            inventory.setRestockedDate(LocalDate.now());

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            OnlineStoreInventory saved = repository.save(inventory);

            assertEquals(1, saved.getOnlineInventoryId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find inventory by ID")
        void shouldFindInventoryById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 10);

            Optional<OnlineStoreInventory> result = repository.findById(1);

            assertTrue(result.isPresent());
            assertEquals(10, result.get().getQuantityAvailable());
        }
    }

    @Nested
    @DisplayName("findByProductCode tests")
    class FindByProductCodeTests {

        @Test
        @DisplayName("Should find inventory by product code")
        void shouldFindInventoryByProductCode() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 10);

            List<OnlineStoreInventory> result = repository.findByProductCode("P001");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findAvailableByProductCode tests")
    class FindAvailableByProductCodeTests {

        @Test
        @DisplayName("Should find available inventory by product code")
        void shouldFindAvailableInventory() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 10);

            List<OnlineStoreInventory> result = repository.findAvailableByProductCode("P001");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTotalQuantityAvailable tests")
    class GetTotalQuantityAvailableTests {

        @Test
        @DisplayName("Should get total quantity available")
        void shouldGetTotalQuantityAvailable() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(50);

            int total = repository.getTotalQuantityAvailable("P001");

            assertEquals(50, total);
        }
    }

    @Nested
    @DisplayName("reduceQuantity tests")
    class ReduceQuantityTests {

        @Test
        @DisplayName("Should reduce quantity")
        void shouldReduceQuantity() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.reduceQuantity("P001", 1, 5);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("addQuantity tests")
    class AddQuantityTests {

        @Test
        @DisplayName("Should update quantity when record exists")
        void shouldUpdateQuantityWhenExists() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false); // For findByProductCodeAndBatchId
            mockInventoryResultSet(1, "P001", 10);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.addQuantity("P001", 1, 5);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should insert new record when not exists")
        void shouldInsertNewRecordWhenNotExists() throws Exception {
            when(resultSet.next()).thenReturn(false); // For findByProductCodeAndBatchId
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = repository.addQuantity("P001", 1, 5);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("findLowStock tests")
    class FindLowStockTests {

        @Test
        @DisplayName("Should find low stock items")
        void shouldFindLowStockItems() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_qty")).thenReturn(5);

            List<OnlineStoreInventory> result = repository.findLowStock(10);

            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getQuantityAvailable());
        }
    }

    @Nested
    @DisplayName("getStockSummary tests")
    class GetStockSummaryTests {

        @Test
        @DisplayName("Should get stock summary")
        void shouldGetStockSummary() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_qty")).thenReturn(100);
            when(resultSet.getInt("batch_count")).thenReturn(5);

            List<ProductStockSummary> result = repository.getStockSummary();

            assertEquals(1, result.size());
            assertEquals(100, result.get(0).totalQuantity());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all inventory")
        void shouldFindAllInventory() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockInventoryResultSet(1, "P001", 10);

            List<OnlineStoreInventory> result = repository.findAll();

            assertEquals(1, result.size());
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
