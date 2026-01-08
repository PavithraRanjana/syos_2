package com.syos.repository.impl;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Order;
import com.syos.domain.models.OrderItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.RepositoryException;
import com.syos.repository.interfaces.OrderRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of OrderRepository.
 */
public class OrderRepositoryImpl implements OrderRepository {

    private final DataSource dataSource;

    public OrderRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            return insert(order);
        } else {
            return update(order);
        }
    }

    private Order insert(Order order) {
        String sql = """
            INSERT INTO orders (order_number, customer_id, customer_name, customer_email,
                status, payment_method, shipping_address, shipping_phone, shipping_notes,
                subtotal, shipping_fee, discount_amount, tax_amount, total_amount,
                bill_id, order_date, confirmed_at, shipped_at, delivered_at, cancelled_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Generate order number if not set
                if (order.getOrderNumber() == null) {
                    order.setOrderNumber(generateOrderNumber(conn));
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setOrderParameters(stmt, order);

                    int affected = stmt.executeUpdate();
                    if (affected == 0) {
                        throw new RepositoryException("Creating order failed, no rows affected");
                    }

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            order.setOrderId(generatedKeys.getInt(1));
                        }
                    }
                }

                // Insert order items
                insertOrderItems(conn, order);

                conn.commit();
                return order;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert order", e);
        }
    }

    private Order update(Order order) {
        String sql = """
            UPDATE orders SET
                customer_name = ?, customer_email = ?, status = ?, payment_method = ?,
                shipping_address = ?, shipping_phone = ?, shipping_notes = ?,
                subtotal = ?, shipping_fee = ?, discount_amount = ?, tax_amount = ?, total_amount = ?,
                bill_id = ?, confirmed_at = ?, shipped_at = ?, delivered_at = ?, cancelled_at = ?
            WHERE order_id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int idx = 1;
            stmt.setString(idx++, order.getCustomerName());
            stmt.setString(idx++, order.getCustomerEmail());
            stmt.setString(idx++, order.getStatus().name());
            stmt.setString(idx++, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
            stmt.setString(idx++, order.getShippingAddress());
            stmt.setString(idx++, order.getShippingPhone());
            stmt.setString(idx++, order.getShippingNotes());
            stmt.setBigDecimal(idx++, order.getSubtotal().getAmount());
            stmt.setBigDecimal(idx++, order.getShippingFee().getAmount());
            stmt.setBigDecimal(idx++, order.getDiscountAmount().getAmount());
            stmt.setBigDecimal(idx++, order.getTaxAmount().getAmount());
            stmt.setBigDecimal(idx++, order.getTotalAmount().getAmount());
            stmt.setObject(idx++, order.getBillId());
            stmt.setTimestamp(idx++, order.getConfirmedAt() != null ? Timestamp.valueOf(order.getConfirmedAt()) : null);
            stmt.setTimestamp(idx++, order.getShippedAt() != null ? Timestamp.valueOf(order.getShippedAt()) : null);
            stmt.setTimestamp(idx++, order.getDeliveredAt() != null ? Timestamp.valueOf(order.getDeliveredAt()) : null);
            stmt.setTimestamp(idx++, order.getCancelledAt() != null ? Timestamp.valueOf(order.getCancelledAt()) : null);
            stmt.setInt(idx++, order.getOrderId());

            stmt.executeUpdate();
            return order;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to update order", e);
        }
    }

    private void setOrderParameters(PreparedStatement stmt, Order order) throws SQLException {
        int idx = 1;
        stmt.setString(idx++, order.getOrderNumber());
        stmt.setInt(idx++, order.getCustomerId());
        stmt.setString(idx++, order.getCustomerName());
        stmt.setString(idx++, order.getCustomerEmail());
        stmt.setString(idx++, order.getStatus().name());
        stmt.setString(idx++, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "ONLINE");
        stmt.setString(idx++, order.getShippingAddress());
        stmt.setString(idx++, order.getShippingPhone());
        stmt.setString(idx++, order.getShippingNotes());
        stmt.setBigDecimal(idx++, order.getSubtotal().getAmount());
        stmt.setBigDecimal(idx++, order.getShippingFee().getAmount());
        stmt.setBigDecimal(idx++, order.getDiscountAmount().getAmount());
        stmt.setBigDecimal(idx++, order.getTaxAmount().getAmount());
        stmt.setBigDecimal(idx++, order.getTotalAmount().getAmount());
        stmt.setObject(idx++, order.getBillId());
        stmt.setTimestamp(idx++, Timestamp.valueOf(order.getOrderDate()));
        stmt.setTimestamp(idx++, order.getConfirmedAt() != null ? Timestamp.valueOf(order.getConfirmedAt()) : null);
        stmt.setTimestamp(idx++, order.getShippedAt() != null ? Timestamp.valueOf(order.getShippedAt()) : null);
        stmt.setTimestamp(idx++, order.getDeliveredAt() != null ? Timestamp.valueOf(order.getDeliveredAt()) : null);
        stmt.setTimestamp(idx++, order.getCancelledAt() != null ? Timestamp.valueOf(order.getCancelledAt()) : null);
    }

    private void insertOrderItems(Connection conn, Order order) throws SQLException {
        String sql = """
            INSERT INTO order_item (order_id, product_code, product_name, main_inventory_id,
                quantity, unit_price, line_total)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : order.getItems()) {
                stmt.setInt(1, order.getOrderId());
                stmt.setString(2, item.getProductCodeString());
                stmt.setString(3, item.getProductName());
                stmt.setObject(4, item.getMainInventoryId());
                stmt.setInt(5, item.getQuantity());
                stmt.setBigDecimal(6, item.getUnitPrice().getAmount());
                stmt.setBigDecimal(7, item.getLineTotal().getAmount());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Optional<Order> findById(Integer id) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findOrderItems(conn, id));
                    return Optional.of(order);
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find order by ID", e);
        }
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = "SELECT * FROM orders WHERE order_number = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findOrderItems(conn, order.getOrderId()));
                    return Optional.of(order);
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find order by number", e);
        }
    }

    @Override
    public List<Order> findByCustomerId(Integer customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find orders by customer", e);
        }
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find orders by status", e);
        }
    }

    @Override
    public List<Order> findActiveOrders() {
        String sql = """
            SELECT * FROM orders
            WHERE status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED')
            ORDER BY order_date DESC
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find active orders", e);
        }
    }

    @Override
    public List<Order> findRecentByCustomerId(Integer customerId, int limit) {
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC LIMIT ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, limit);
            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find recent orders", e);
        }
    }

    private List<Order> executeOrderListQuery(Connection conn, PreparedStatement stmt) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(conn, order.getOrderId()));
                orders.add(order);
            }
        }
        return orders;
    }

    private List<OrderItem> findOrderItems(Connection conn, Integer orderId) throws SQLException {
        String sql = "SELECT * FROM order_item WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public String generateOrderNumber() {
        try (Connection conn = dataSource.getConnection()) {
            return generateOrderNumber(conn);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to generate order number", e);
        }
    }

    private String generateOrderNumber(Connection conn) throws SQLException {
        String updateSql = "UPDATE order_sequence SET last_number = last_number + 1 WHERE id = 1";
        String selectSql = "SELECT last_number FROM order_sequence WHERE id = 1";

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            updateStmt.executeUpdate();
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int number = rs.getInt(1);
                    return String.format("ORD-%06d", number);
                }
            }
        }
        throw new RepositoryException("Failed to generate order number");
    }

    @Override
    public void updateStatus(Integer orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Failed to update order status", e);
        }
    }

    @Override
    public int countByCustomerIdAndStatus(Integer customerId, OrderStatus status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE customer_id = ? AND status = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to count orders", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find all orders", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete order", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RepositoryException("Failed to check order existence", e);
        }
    }

    @Override
    public List<Order> findAll(int offset, int limit) {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeOrderListQuery(conn, stmt);

        } catch (SQLException e) {
            throw new RepositoryException("Failed to find orders", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to count orders", e);
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));

        String paymentMethod = rs.getString("payment_method");
        if (paymentMethod != null) {
            order.setPaymentMethod(TransactionType.valueOf(paymentMethod));
        }

        order.setShippingAddress(rs.getString("shipping_address"));
        order.setShippingPhone(rs.getString("shipping_phone"));
        order.setShippingNotes(rs.getString("shipping_notes"));
        order.setSubtotal(new Money(rs.getBigDecimal("subtotal")));
        order.setShippingFee(new Money(rs.getBigDecimal("shipping_fee")));
        order.setDiscountAmount(new Money(rs.getBigDecimal("discount_amount")));
        order.setTaxAmount(new Money(rs.getBigDecimal("tax_amount")));
        order.setTotalAmount(new Money(rs.getBigDecimal("total_amount")));

        Integer billId = rs.getInt("bill_id");
        if (!rs.wasNull()) {
            order.setBillId(billId);
        }

        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDateTime());
        }

        Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
        if (confirmedAt != null) {
            order.setConfirmedAt(confirmedAt.toLocalDateTime());
        }

        Timestamp shippedAt = rs.getTimestamp("shipped_at");
        if (shippedAt != null) {
            order.setShippedAt(shippedAt.toLocalDateTime());
        }

        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            order.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
        if (cancelledAt != null) {
            order.setCancelledAt(cancelledAt.toLocalDateTime());
        }

        return order;
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductCode(rs.getString("product_code"));
        item.setProductName(rs.getString("product_name"));

        Integer mainInventoryId = rs.getInt("main_inventory_id");
        if (!rs.wasNull()) {
            item.setMainInventoryId(mainInventoryId);
        }

        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(new Money(rs.getBigDecimal("unit_price")));
        item.setLineTotal(new Money(rs.getBigDecimal("line_total")));

        return item;
    }
}
