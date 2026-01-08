package com.syos.repository.interfaces;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.models.Order;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order persistence operations.
 */
public interface OrderRepository extends Repository<Order, Integer> {

    /**
     * Finds an order by its order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Finds all orders for a customer.
     */
    List<Order> findByCustomerId(Integer customerId);

    /**
     * Finds orders by status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds all active (non-terminal) orders.
     */
    List<Order> findActiveOrders();

    /**
     * Finds recent orders for a customer.
     */
    List<Order> findRecentByCustomerId(Integer customerId, int limit);

    /**
     * Generates the next order number.
     */
    String generateOrderNumber();

    /**
     * Updates the order status.
     */
    void updateStatus(Integer orderId, OrderStatus status);

    /**
     * Counts orders by status for a customer.
     */
    int countByCustomerIdAndStatus(Integer customerId, OrderStatus status);
}
