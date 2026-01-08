package com.syos.domain.models;

import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an online order.
 * Contains order details, items, and status tracking.
 */
public class Order {

    private Integer orderId;
    private String orderNumber;
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;
    private TransactionType paymentMethod;

    // Shipping details
    private String shippingAddress;
    private String shippingPhone;
    private String shippingNotes;

    // Financial details
    private Money subtotal;
    private Money shippingFee;
    private Money discountAmount;
    private Money taxAmount;
    private Money totalAmount;

    // Timestamps
    private LocalDateTime orderDate;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    // Items
    private List<OrderItem> items;

    // Related bill (for inventory/financial tracking)
    private Integer billId;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDateTime.now();
        this.subtotal = Money.ZERO;
        this.shippingFee = Money.ZERO;
        this.discountAmount = Money.ZERO;
        this.taxAmount = Money.ZERO;
        this.totalAmount = Money.ZERO;
    }

    public Order(Integer customerId) {
        this();
        this.customerId = customerId;
    }

    /**
     * Creates an order from a shopping cart.
     */
    public static Order fromCart(Cart cart, Customer customer) {
        Order order = new Order(customer.getCustomerId());
        order.setCustomerName(customer.getCustomerName());
        order.setCustomerEmail(customer.getEmail());
        order.setShippingAddress(customer.getAddress());
        order.setShippingPhone(customer.getPhone());

        for (CartItem cartItem : cart.getItems()) {
            order.addItem(new OrderItem(cartItem));
        }

        order.calculateTotals();
        return order;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        updateStatusTimestamp(status);
    }

    public TransactionType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(TransactionType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingNotes() {
        return shippingNotes;
    }

    public void setShippingNotes(String shippingNotes) {
        this.shippingNotes = shippingNotes;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Money subtotal) {
        this.subtotal = subtotal;
    }

    public Money getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Money shippingFee) {
        this.shippingFee = shippingFee;
        calculateTotal();
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Money discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotal();
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
        calculateTotal();
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    /**
     * Adds an item to the order.
     */
    public void addItem(OrderItem item) {
        item.setOrderId(this.orderId);
        this.items.add(item);
    }

    /**
     * Calculates subtotal from items.
     */
    public void calculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(Money.ZERO, Money::add);
        calculateTotal();
    }

    /**
     * Calculates final total including shipping and discounts.
     */
    private void calculateTotal() {
        this.totalAmount = subtotal
            .add(shippingFee != null ? shippingFee : Money.ZERO)
            .add(taxAmount != null ? taxAmount : Money.ZERO)
            .subtract(discountAmount != null ? discountAmount : Money.ZERO);
    }

    /**
     * Updates status-specific timestamps.
     */
    private void updateStatusTimestamp(OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case CONFIRMED -> this.confirmedAt = now;
            case SHIPPED -> this.shippedAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case CANCELLED, REFUNDED -> this.cancelledAt = now;
            default -> {}
        }
    }

    /**
     * Confirms the order (payment received).
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending orders");
        }
        setStatus(OrderStatus.CONFIRMED);
    }

    /**
     * Marks the order as processing.
     */
    public void startProcessing() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only process confirmed orders");
        }
        setStatus(OrderStatus.PROCESSING);
    }

    /**
     * Marks the order as shipped.
     */
    public void ship() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Can only ship orders in processing");
        }
        setStatus(OrderStatus.SHIPPED);
    }

    /**
     * Marks the order as delivered.
     */
    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Can only deliver shipped orders");
        }
        setStatus(OrderStatus.DELIVERED);
    }

    /**
     * Cancels the order.
     */
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        setStatus(OrderStatus.CANCELLED);
    }

    /**
     * Gets the total number of items.
     */
    public int getTotalItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * Checks if the order can be cancelled.
     */
    public boolean canCancel() {
        return status.canCancel();
    }

    /**
     * Checks if the order is in a terminal state.
     */
    public boolean isComplete() {
        return status.isTerminal();
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, number=%s, customer=%d, status=%s, total=%s, items=%d}",
            orderId, orderNumber, customerId, status, totalAmount, items.size());
    }
}
