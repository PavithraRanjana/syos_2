package com.syos.domain.enums;

/**
 * Represents the status of an online order.
 */
public enum OrderStatus {
    PENDING("Pending", "Order placed, awaiting payment confirmation"),
    CONFIRMED("Confirmed", "Payment confirmed, preparing for fulfillment"),
    PROCESSING("Processing", "Order is being prepared"),
    SHIPPED("Shipped", "Order has been shipped"),
    DELIVERED("Delivered", "Order has been delivered"),
    CANCELLED("Cancelled", "Order has been cancelled"),
    REFUNDED("Refunded", "Order has been refunded");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if the order can be cancelled from this status.
     */
    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }

    /**
     * Checks if the order is in a terminal state.
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Checks if the order is active (not terminal).
     */
    public boolean isActive() {
        return !isTerminal();
    }

    /**
     * Gets the next logical status in the order workflow.
     */
    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> CONFIRMED;
            case CONFIRMED -> PROCESSING;
            case PROCESSING -> SHIPPED;
            case SHIPPED -> DELIVERED;
            default -> this;
        };
    }

    public static OrderStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PENDING;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
