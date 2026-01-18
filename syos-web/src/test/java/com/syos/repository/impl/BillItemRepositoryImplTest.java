package com.syos.repository.impl;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.BillItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.BillItemRepository.ProductSalesSummary;
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
 * Unit tests for BillItemRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillItemRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private BillItemRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new BillItemRepositoryImpl(dataSource);
    }

    private void mockBillItemResultSet(int id, int billId, String productCode, int quantity) throws SQLException {
        when(resultSet.getInt("bill_item_id")).thenReturn(id);
        when(resultSet.getInt("bill_id")).thenReturn(billId);
        when(resultSet.getString("product_code")).thenReturn(productCode);
        when(resultSet.getInt("main_inventory_id")).thenReturn(1);
        when(resultSet.getInt("quantity")).thenReturn(quantity);
        when(resultSet.getBigDecimal("unit_price")).thenReturn(BigDecimal.valueOf(10.00));
        when(resultSet.getBigDecimal("line_total")).thenReturn(BigDecimal.valueOf(quantity * 10.00));
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getString("product_name")).thenReturn("Test Product");
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new bill item")
        void shouldInsertNewBillItem() throws Exception {
            BillItem item = new BillItem();
            item.setBillId(1);
            item.setProductCode(new ProductCode("P001"));
            item.setProductName("Test Product");
            item.setMainInventoryId(1);
            item.setQuantity(2);
            item.setUnitPrice(new Money(BigDecimal.valueOf(10.00)));
            item.setLineTotal(new Money(BigDecimal.valueOf(20.00)));

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            BillItem saved = repository.save(item);

            assertEquals(1, saved.getBillItemId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {
        @Test
        void shouldFindById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            Optional<BillItem> result = repository.findById(1);
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("findByBillId tests")
    class FindByBillIdTests {
        @Test
        void shouldFindBillItemsByBillId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            List<BillItem> result = repository.findByBillId(1);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findByProductCode tests")
    class FindByProductCodeTests {
        @Test
        void shouldFindByProductCode() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            List<BillItem> result = repository.findByProductCode("P001");
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("findByProductCodeAndDateRange tests")
    class FindByProductCodeAndDateRangeTests {
        @Test
        void shouldFindByProductCodeAndDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            List<BillItem> result = repository.findByProductCodeAndDateRange("P001", LocalDate.now(), LocalDate.now());
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTotalQuantitySoldForDate tests")
    class GetTotalQuantitySoldForDateTests {
        @Test
        void shouldGetTotalQuantitySoldForDate() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(10);
            int total = repository.getTotalQuantitySoldForDate("P001", LocalDate.now());
            assertEquals(10, total);
        }

        @Test
        void shouldGetTotalQuantitySoldForDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(20);
            int total = repository.getTotalQuantitySoldForDateRange("P001", LocalDate.now(), LocalDate.now());
            assertEquals(20, total);
        }
    }

    @Nested
    @DisplayName("getTotalRevenueForProduct tests")
    class GetTotalRevenueForProductTests {
        @Test
        void shouldGetTotalRevenue() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(500.00));
            BigDecimal total = repository.getTotalRevenueForProduct("P001", LocalDate.now(), LocalDate.now());
            assertEquals(BigDecimal.valueOf(500.00), total);
        }
    }

    @Nested
    @DisplayName("getTopSellingProducts tests")
    class GetTopSellingProductsTests {
        @Test
        void shouldGetTopSellingProducts() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_quantity")).thenReturn(100);
            when(resultSet.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(1000.00));
            List<ProductSalesSummary> result = repository.getTopSellingProducts(LocalDate.now(), LocalDate.now(), 5);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getProductSalesSummary tests")
    class GetProductSalesSummaryTests {
        @Test
        void shouldGetProductSalesSummary() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_quantity")).thenReturn(100);
            when(resultSet.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(1000.00));
            List<ProductSalesSummary> result = repository.getProductSalesSummary(LocalDate.now(), LocalDate.now());
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTopSellingProductsByStoreType tests")
    class GetTopSellingProductsByStoreTypeTests {
        @Test
        void shouldGetTopSellingProductsByStoreType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_quantity")).thenReturn(50);
            when(resultSet.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(500.00));
            List<ProductSalesSummary> result = repository.getTopSellingProductsByStoreType(LocalDate.now(),
                    LocalDate.now(), 5, StoreType.PHYSICAL);
            assertEquals(1, result.size());
        }

        @Test
        void shouldGetProductSalesSummaryByStoreType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("product_code")).thenReturn("P001");
            when(resultSet.getString("product_name")).thenReturn("Test Product");
            when(resultSet.getInt("total_quantity")).thenReturn(50);
            when(resultSet.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(500.00));
            List<ProductSalesSummary> result = repository.getProductSalesSummaryByStoreType(LocalDate.now(),
                    LocalDate.now(), StoreType.PHYSICAL);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("delete tests")
    class DeleteTests {
        @Test
        void shouldDeleteByBillId() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(5);
            int count = repository.deleteByBillId(1);
            assertEquals(5, count);
        }

        @Test
        void shouldDeleteById() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = repository.deleteById(1);
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("count/exists tests")
    class CountExistsTests {
        @Test
        void shouldCount() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(10L);
            long count = repository.count();
            assertEquals(10L, count);
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
    @DisplayName("findAll tests")
    class FindAllTests {
        @Test
        void shouldFindAll() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            List<BillItem> result = repository.findAll();
            assertEquals(1, result.size());
        }

        @Test
        void shouldFindAllPaged() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillItemResultSet(1, 1, "P001", 2);
            List<BillItem> result = repository.findAll(0, 10);
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
