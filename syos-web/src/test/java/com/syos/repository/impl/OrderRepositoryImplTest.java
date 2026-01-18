package com.syos.repository.impl;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Order;
import com.syos.domain.models.OrderItem;
import com.syos.domain.valueobjects.Money;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private OrderRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new OrderRepositoryImpl(dataSource);
    }

    private void mockOrderResultSet(ResultSet rs, int id, String orderNumber, OrderStatus status) throws SQLException {
        when(rs.getInt("order_id")).thenReturn(id);
        when(rs.getString("order_number")).thenReturn(orderNumber);
        when(rs.getInt("customer_id")).thenReturn(1);
        when(rs.getString("customer_name")).thenReturn("Test Customer");
        when(rs.getString("customer_email")).thenReturn("test@test.com");
        when(rs.getString("status")).thenReturn(status.name());
        when(rs.getString("payment_method")).thenReturn("ONLINE");
        when(rs.getString("shipping_address")).thenReturn("123 Main St");
        when(rs.getString("shipping_phone")).thenReturn("1234567890");
        when(rs.getString("shipping_notes")).thenReturn("Note");
        when(rs.getBigDecimal("subtotal")).thenReturn(BigDecimal.valueOf(100.00));
        when(rs.getBigDecimal("shipping_fee")).thenReturn(BigDecimal.valueOf(10.00));
        when(rs.getBigDecimal("discount_amount")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("tax_amount")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(110.00));
        when(rs.getTimestamp("order_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(rs.getTimestamp("confirmed_at")).thenReturn(null);
        when(rs.getTimestamp("shipped_at")).thenReturn(null);
        when(rs.getTimestamp("delivered_at")).thenReturn(null);
        when(rs.getTimestamp("cancelled_at")).thenReturn(null);
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new order")
        void shouldInsertNewOrder() throws Exception {
            Order order = new Order();
            order.setCustomerId(1);
            order.setCustomerName("Test Customer");
            order.setCustomerEmail("test@test.com");
            order.setStatus(OrderStatus.PENDING);
            order.setSubtotal(new Money(BigDecimal.valueOf(100.00)));
            order.setShippingFee(new Money(BigDecimal.valueOf(10.00)));
            order.setDiscountAmount(new Money(BigDecimal.ZERO));
            order.setTaxAmount(new Money(BigDecimal.ZERO));
            order.setTotalAmount(new Money(BigDecimal.valueOf(110.00)));
            order.setShippingAddress("123 Main St");
            order.setShippingPhone("1234567890");
            order.setShippingNotes("Notes");
            order.setOrderDate(LocalDateTime.now());

            List<OrderItem> items = new ArrayList<>();
            OrderItem item = new OrderItem();
            item.setProductCode("P001");
            item.setProductName("Test Product");
            item.setMainInventoryId(1);
            item.setQuantity(1);
            item.setUnitPrice(new Money(BigDecimal.valueOf(100.00)));
            item.setLineTotal(new Money(BigDecimal.valueOf(100.00)));
            items.add(item);
            order.setItems(items);

            // Mock generation of order number
            PreparedStatement seqUpdateStmt = mock(PreparedStatement.class);
            PreparedStatement seqSelectStmt = mock(PreparedStatement.class);
            ResultSet seqResultSet = mock(ResultSet.class);

            when(connection.prepareStatement(contains("UPDATE order_sequence"))).thenReturn(seqUpdateStmt);
            when(connection.prepareStatement(contains("SELECT last_number"))).thenReturn(seqSelectStmt);
            when(seqSelectStmt.executeQuery()).thenReturn(seqResultSet);
            when(seqResultSet.next()).thenReturn(true);
            when(seqResultSet.getInt(1)).thenReturn(100);

            // Mock insert order
            when(connection.prepareStatement(contains("INSERT INTO orders"), anyInt())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            // Mock insert items
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(contains("INSERT INTO order_item"))).thenReturn(itemStmt);

            Order saved = repository.save(order);

            assertNotNull(saved.getOrderNumber());
            assertEquals(1, saved.getOrderId());
            verify(connection).commit();
        }

        @Test
        @DisplayName("Should update existing order")
        void shouldUpdateExistingOrder() throws Exception {
            Order order = new Order();
            order.setOrderId(1);
            order.setCustomerName("Updated Name");
            order.setCustomerEmail("test@test.com");
            order.setStatus(OrderStatus.CONFIRMED);
            order.setSubtotal(new Money(BigDecimal.valueOf(100.00)));
            order.setShippingFee(new Money(BigDecimal.valueOf(10.00)));
            order.setDiscountAmount(new Money(BigDecimal.ZERO));
            order.setTaxAmount(new Money(BigDecimal.ZERO));
            order.setTotalAmount(new Money(BigDecimal.valueOf(110.00)));
            order.setShippingAddress("123 Main St");
            order.setShippingPhone("1234567890");
            order.setShippingNotes("Notes");

            when(connection.prepareStatement(contains("UPDATE orders"))).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            Order updated = repository.save(order);

            assertEquals("Updated Name", updated.getCustomerName());
        }
    }

    @Nested
    @DisplayName("find tests")
    class FindTests {
        @Test
        void shouldFindById() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("SELECT * FROM orders WHERE order_id"))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);

            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            Optional<Order> result = repository.findById(1);
            assertTrue(result.isPresent());
        }

        @Test
        void shouldFindByCustomer() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("WHERE customer_id"))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);
            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            List<Order> results = repository.findByCustomerId(1);
            assertEquals(1, results.size());
        }

        @Test
        void shouldFindByStatus() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("WHERE status = "))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);
            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            List<Order> results = repository.findByStatus(OrderStatus.PENDING);
            assertEquals(1, results.size());
        }

        @Test
        void shouldFindActiveOrders() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("status NOT IN"))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);
            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            List<Order> results = repository.findActiveOrders();
            assertEquals(1, results.size());
        }

        @Test
        void shouldFindRecentByCustomerId() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("LIMIT"))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);
            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            List<Order> results = repository.findRecentByCustomerId(1, 10);
            assertEquals(1, results.size());
        }

        @Test
        void shouldFindAll() throws Exception {
            PreparedStatement orderStmt = mock(PreparedStatement.class);
            ResultSet orderRs = mock(ResultSet.class);
            PreparedStatement itemStmt = mock(PreparedStatement.class);
            ResultSet itemRs = mock(ResultSet.class);

            when(connection.prepareStatement(contains("SELECT * FROM orders ORDER BY"))).thenReturn(orderStmt);
            when(orderStmt.executeQuery()).thenReturn(orderRs);
            when(orderRs.next()).thenReturn(true).thenReturn(false);
            mockOrderResultSet(orderRs, 1, "ORD-01", OrderStatus.PENDING);

            when(connection.prepareStatement(contains("SELECT * FROM order_item"))).thenReturn(itemStmt);
            when(itemStmt.executeQuery()).thenReturn(itemRs);

            List<Order> results = repository.findAll();
            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("updateStatus tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update status")
        void shouldUpdateStatus() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);

            repository.updateStatus(1, OrderStatus.CONFIRMED);

            verify(preparedStatement).setString(1, "CONFIRMED");
            verify(preparedStatement).setInt(2, 1);
        }
    }

    @Nested
    @DisplayName("count/exists tests")
    class CountExistsTests {
        @Test
        void shouldCountByCustomerIdAndStatus() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(5);
            int count = repository.countByCustomerIdAndStatus(1, OrderStatus.DELIVERED);
            assertEquals(5, count);
        }

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
            // when(resultSet.getBoolean(1)).thenReturn(true); // SELECT 1 ...
            when(resultSet.next()).thenReturn(true);

            boolean exists = repository.existsById(1);
            assertTrue(exists);
        }

        @Test
        void shouldDeleteById() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = repository.deleteById(1);
            assertTrue(result);
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
