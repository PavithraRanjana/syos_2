package com.syos.service.impl;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.*;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.OrderRepository;
import com.syos.service.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderService.
 * Handles online order creation, status updates, and order management.
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;
    private final BillingService billingService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartService cartService,
                            CustomerService customerService,
                            InventoryService inventoryService,
                            BillingService billingService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.customerService = customerService;
        this.inventoryService = inventoryService;
        this.billingService = billingService;
    }

    @Override
    public Order createOrderFromCart(Integer customerId, OrderRequest request) {
        // Validate customer exists
        Customer customer = customerService.findById(customerId)
            .orElseThrow(() -> new ValidationException("Customer not found", "customerId"));

        // Get cart and validate it's not empty
        Cart cart = cartService.getCart(customerId)
            .orElseThrow(() -> new ValidationException("Cart is empty", "cart"));

        if (cart.isEmpty()) {
            throw new ValidationException("Cart is empty", "cart");
        }

        // Validate stock availability
        CartService.StockValidationResult stockResult = cartService.validateCartStockDetails(customerId);
        if (!stockResult.isValid()) {
            StringBuilder message = new StringBuilder("Insufficient stock for: ");
            for (CartService.StockIssue issue : stockResult.issues()) {
                message.append(String.format("%s (need %d, have %d); ",
                    issue.productName(), issue.requestedQuantity(), issue.availableQuantity()));
            }
            throw new ValidationException(message.toString(), "stock");
        }

        // Create order from cart
        Order order = Order.fromCart(cart, customer);

        // Apply request details
        TransactionType paymentMethod = TransactionType.ONLINE;
        if (request != null) {
            if (request.shippingAddress() != null && !request.shippingAddress().isEmpty()) {
                order.setShippingAddress(request.shippingAddress());
            }
            if (request.shippingPhone() != null && !request.shippingPhone().isEmpty()) {
                order.setShippingPhone(request.shippingPhone());
            }
            order.setShippingNotes(request.shippingNotes());
            if (request.paymentMethod() != null) {
                paymentMethod = request.paymentMethod();
            }
            order.setPaymentMethod(paymentMethod);
        } else {
            order.setPaymentMethod(paymentMethod);
        }

        // Create bill for inventory tracking
        Bill bill = billingService.createBill(StoreType.ONLINE, paymentMethod, customerId, null);
        for (CartItem cartItem : cart.getItems()) {
            billingService.addItem(bill.getBillId(), cartItem.getProductCodeString(), cartItem.getQuantity());
        }
        billingService.finalizeBill(bill.getBillId());

        order.setBillId(bill.getBillId());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after successful order
        cartService.clearCart(customerId);

        logger.info("Order created: {} for customer {} with {} items, total: {}",
            savedOrder.getOrderNumber(), customerId,
            savedOrder.getItems().size(), savedOrder.getTotalAmount());

        return savedOrder;
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByCustomerId(Integer customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> findActiveOrders() {
        return orderRepository.findActiveOrders();
    }

    @Override
    public Order confirmOrder(Integer orderId) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        order.confirm();
        orderRepository.save(order);

        logger.info("Order confirmed: {}", order.getOrderNumber());
        return order;
    }

    @Override
    public Order startProcessing(Integer orderId) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        order.startProcessing();
        orderRepository.save(order);

        logger.info("Order processing started: {}", order.getOrderNumber());
        return order;
    }

    @Override
    public Order shipOrder(Integer orderId) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        order.ship();
        orderRepository.save(order);

        logger.info("Order shipped: {}", order.getOrderNumber());
        return order;
    }

    @Override
    public Order deliverOrder(Integer orderId) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        order.deliver();
        orderRepository.save(order);

        logger.info("Order delivered: {}", order.getOrderNumber());
        return order;
    }

    @Override
    public Order cancelOrder(Integer orderId, String reason) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        if (!order.canCancel()) {
            throw new ValidationException("Cannot cancel order in status: " + order.getStatus(), "status");
        }

        order.cancel();
        if (reason != null) {
            order.setShippingNotes((order.getShippingNotes() != null ?
                order.getShippingNotes() + "\n" : "") + "Cancellation reason: " + reason);
        }

        // TODO: Implement inventory restoration for cancelled orders

        orderRepository.save(order);

        logger.info("Order cancelled: {} - Reason: {}", order.getOrderNumber(), reason);
        return order;
    }

    @Override
    public Order updateShippingAddress(Integer orderId, String address, String phone) {
        Order order = findById(orderId)
            .orElseThrow(() -> new ValidationException("Order not found", "orderId"));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ValidationException(
                "Cannot update shipping address for order in status: " + order.getStatus(), "status");
        }

        order.setShippingAddress(address);
        order.setShippingPhone(phone);
        orderRepository.save(order);

        logger.info("Order shipping updated: {}", order.getOrderNumber());
        return order;
    }

    @Override
    public OrderStats getCustomerOrderStats(Integer customerId) {
        List<Order> orders = findByCustomerId(customerId);

        int totalOrders = orders.size();
        int pendingOrders = (int) orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
            .count();
        int completedOrders = (int) orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();
        int cancelledOrders = (int) orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.REFUNDED)
            .count();

        return new OrderStats(totalOrders, pendingOrders, completedOrders, cancelledOrders);
    }
}
