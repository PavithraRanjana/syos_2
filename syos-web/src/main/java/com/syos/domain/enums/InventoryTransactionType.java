package com.syos.domain.enums;

/**
 * Represents the type of inventory transaction for audit tracking.
 */
public enum InventoryTransactionType {
    SALE("Sale", "Stock reduced due to sale"),
    RESTOCK_PHYSICAL("Restock Physical", "Stock added to physical store"),
    RESTOCK_ONLINE("Restock Online", "Stock added to online store"),
    ADJUSTMENT("Adjustment", "Manual stock adjustment"),
    RETURN("Return", "Stock returned"),
    EXPIRED("Expired", "Stock removed due to expiration"),
    PURCHASE("Purchase", "New stock received from supplier");

    private final String displayName;
    private final String description;

    InventoryTransactionType(String displayName, String description) {
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
     * Parses a string to InventoryTransactionType, case-insensitive.
     */
    public static InventoryTransactionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Inventory transaction type cannot be null or empty");
        }
        try {
            return InventoryTransactionType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid inventory transaction type: " + value);
        }
    }
}
