package com.syos.service.interfaces;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Order;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for order management operations.
 */
public interface OrderService {

    /**
     * Creates a new order from the customer's cart.
     */
    Order createOrderFromCart(Integer customerId, OrderRequest request);

    /**
     * Gets an order by ID.
     */
    Optional<Order> findById(Integer orderId);

    /**
     * Gets an order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Gets all orders for a customer.
     */
    List<Order> findByCustomerId(Integer customerId);

    /**
     * Gets orders by status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Gets active (non-terminal) orders.
     */
    List<Order> findActiveOrders();

    /**
     * Confirms an order (payment received).
     */
    Order confirmOrder(Integer orderId);

    /**
     * Marks an order as processing.
     */
    Order startProcessing(Integer orderId);

    /**
     * Marks an order as shipped.
     */
    Order shipOrder(Integer orderId);

    /**
     * Marks an order as delivered.
     */
    Order deliverOrder(Integer orderId);

    /**
     * Cancels an order.
     */
    Order cancelOrder(Integer orderId, String reason);

    /**
     * Updates the shipping address for an order.
     */
    Order updateShippingAddress(Integer orderId, String address, String phone);

    /**
     * Gets order statistics for a customer.
     */
    OrderStats getCustomerOrderStats(Integer customerId);

    /**
     * Request object for creating an order.
     */
    record OrderRequest(
        String shippingAddress,
        String shippingPhone,
        String shippingNotes,
        TransactionType paymentMethod
    ) {}

    /**
     * Order statistics for a customer.
     */
    record OrderStats(
        int totalOrders,
        int pendingOrders,
        int completedOrders,
        int cancelledOrders
    ) {}
}
