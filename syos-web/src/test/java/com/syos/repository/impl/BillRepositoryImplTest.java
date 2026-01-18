package com.syos.repository.impl;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.BillRepository.DailySalesSummary;
import com.syos.repository.interfaces.BillRepository.StoreTypeSalesSummary;
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
 * Unit tests for BillRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private BillRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new BillRepositoryImpl(dataSource);
    }

    private void mockBillResultSet(int id, String serialNumber, StoreType storeType, TransactionType transactionType)
            throws SQLException {
        when(resultSet.getInt("bill_id")).thenReturn(id);
        when(resultSet.getString("serial_number")).thenReturn(serialNumber);
        when(resultSet.getInt("customer_id")).thenReturn(1);
        when(resultSet.getString("store_type")).thenReturn(storeType.name());
        when(resultSet.getString("transaction_type")).thenReturn(transactionType.name());
        when(resultSet.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(100.00));
        when(resultSet.getBigDecimal("discount_amount")).thenReturn(BigDecimal.ZERO);
        when(resultSet.getBigDecimal("tax_amount")).thenReturn(BigDecimal.ZERO);
        when(resultSet.getBigDecimal("tendered_amount")).thenReturn(BigDecimal.valueOf(100.00));
        when(resultSet.getBigDecimal("change_amount")).thenReturn(BigDecimal.ZERO);
        when(resultSet.getString("cashier_id")).thenReturn("admin");
        when(resultSet.getTimestamp("bill_date")).thenReturn(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getString("customer_name")).thenReturn("Test Customer");
        when(resultSet.getString("customer_email")).thenReturn("test@test.com");
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new bill")
        void shouldInsertNewBill() throws Exception {
            Bill bill = new Bill();
            bill.setSerialNumber(new BillSerialNumber("POS-20231026-0001"));
            bill.setCustomerId(1);
            bill.setStoreType(StoreType.PHYSICAL);
            bill.setTransactionType(TransactionType.CASH);
            bill.setTotalAmount(new Money(BigDecimal.valueOf(100.00)));
            bill.setCashierId("admin");

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            Bill saved = repository.save(bill);

            assertEquals(1, saved.getBillId());
        }

        @Test
        @DisplayName("Should update existing bill")
        void shouldUpdateBill() throws Exception {
            Bill bill = new Bill();
            bill.setBillId(1);
            bill.setSerialNumber(new BillSerialNumber("POS-20231026-0001"));
            bill.setTotalAmount(new Money(BigDecimal.valueOf(200.00)));

            // Mock exists check
            PreparedStatement existsStmt = mock(PreparedStatement.class);
            ResultSet existsRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(existsStmt);
            when(existsStmt.executeQuery()).thenReturn(existsRs);
            when(existsRs.next()).thenReturn(true);
            when(existsRs.getInt(1)).thenReturn(1);

            when(connection.prepareStatement(contains("UPDATE bill"))).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            Bill updated = repository.save(bill);
            assertEquals(new BigDecimal("200.00"), updated.getTotalAmount().getAmount());
        }
    }

    @Nested
    @DisplayName("find tests")
    class FindTests {
        @Test
        void shouldFindById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertTrue(repository.findById(1).isPresent());
        }

        @Test
        void shouldFindBySerialNumber() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertTrue(repository.findBySerialNumber("POS-001").isPresent());
        }

        @Test
        void shouldFindByDate() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByDate(LocalDate.now()).size());
        }

        @Test
        void shouldFindByDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByDateRange(LocalDate.now(), LocalDate.now()).size());
        }

        @Test
        void shouldFindByStoreType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByStoreType(StoreType.PHYSICAL).size());
        }

        @Test
        void shouldFindByTransactionType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByTransactionType(TransactionType.CASH).size());
        }

        @Test
        void shouldFindByCustomerId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByCustomerId(1).size());
        }

        @Test
        void shouldFindByCashierId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByCashierId("admin").size());
        }

        @Test
        void shouldFindRecent() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findRecent(10).size());
        }

        @Test
        void shouldFindByStoreTypeAndDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findByStoreTypeAndDateRange(StoreType.PHYSICAL, LocalDate.now(), LocalDate.now())
                    .size());
        }

        @Test
        void shouldFindAll() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findAll().size());
        }

        @Test
        void shouldFindAllPaged() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBillResultSet(1, "POS-001", StoreType.PHYSICAL, TransactionType.CASH);
            assertEquals(1, repository.findAll(0, 10).size());
        }
    }

    @Nested
    @DisplayName("stats tests")
    class StatsTests {

        @Test
        @DisplayName("Should get total sales for date")
        void shouldGetTotalSalesForDate() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(1000.00));

            BigDecimal total = repository.getTotalSalesForDate(LocalDate.now());

            assertEquals(BigDecimal.valueOf(1000.00), total);
        }

        @Test
        void shouldGetTotalSalesForDateRange() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(2000.00));
            BigDecimal total = repository.getTotalSalesForDateRange(LocalDate.now(), LocalDate.now());
            assertEquals(BigDecimal.valueOf(2000.00), total);
        }

        @Test
        @DisplayName("Should get daily sales summary")
        void shouldGetDailySalesSummary() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getDate("sale_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(resultSet.getInt("bill_count")).thenReturn(10);
            when(resultSet.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(1000.00));
            when(resultSet.getBigDecimal("cash_amount")).thenReturn(BigDecimal.valueOf(500.00));
            when(resultSet.getBigDecimal("online_amount")).thenReturn(BigDecimal.valueOf(500.00));

            List<DailySalesSummary> result = repository.getDailySalesSummary(LocalDate.now(), LocalDate.now());

            assertEquals(1, result.size());
            assertEquals(10, result.get(0).billCount());
        }

        @Test
        void shouldGetSalesByStoreType() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString("store_type")).thenReturn("PHYSICAL");
            when(resultSet.getInt("bill_count")).thenReturn(5);
            when(resultSet.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(500.00));

            List<StoreTypeSalesSummary> result = repository.getSalesByStoreType(LocalDate.now(), LocalDate.now());
            assertEquals(1, result.size());
        }

        @Test
        void shouldGetBillCountForDate() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(50);
            int count = repository.getBillCountForDate(LocalDate.now());
            assertEquals(50, count);
        }
    }

    @Nested
    @DisplayName("generateNextSerialNumber tests")
    class GenerateNextSerialNumberTests {

        @Test
        @DisplayName("Should generate first serial number when none exist")
        void shouldGenerateFirstSerialNumber() throws Exception {
            when(resultSet.next()).thenReturn(false);

            String serial = repository.generateNextSerialNumber(StoreType.PHYSICAL);

            assertTrue(serial.endsWith("-0001"));
            assertTrue(serial.startsWith("POS-"));
        }

        @Test
        @DisplayName("Should generate next serial number")
        void shouldGenerateNextSerialNumber() throws Exception {
            String today = LocalDate.now().toString().replace("-", "");
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString(1)).thenReturn("POS-" + today + "-0005");

            String serial = repository.generateNextSerialNumber(StoreType.PHYSICAL);

            assertTrue(serial.endsWith("-0006"));
        }
    }

    @Nested
    @DisplayName("delete/count tests")
    class DeleteCountTests {
        @Test
        void shouldDeleteBillById() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = repository.deleteById(1);
            assertTrue(result);
        }

        @Test
        void shouldReturnBillCount() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(100L);
            long count = repository.count();
            assertEquals(100L, count);
        }

        @Test
        void shouldExistById() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            assertTrue(repository.existsById(1));
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
