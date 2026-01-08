package com.syos.domain.enums;

/**
 * User roles for role-based access control.
 */
public enum UserRole {
    /**
     * Online customer - can shop, checkout, view orders
     */
    CUSTOMER("Customer", "Shop, cart, checkout, orders, profile"),

    /**
     * Cashier - can process POS transactions, view products (no shopping)
     */
    CASHIER("Cashier", "POS transactions, product search"),

    /**
     * Inventory Manager - can manage all inventory functions
     */
    INVENTORY_MANAGER("Inventory Manager", "Inventory batches, restocking, store inventory"),

    /**
     * Manager - can view all reports (no inventory functions)
     */
    MANAGER("Manager", "View all reports"),

    /**
     * Admin - can assign roles and has full access
     */
    ADMIN("Administrator", "Full system access, role assignment");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
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
     * Check if this role can access customer features (shopping).
     */
    public boolean canShop() {
        return this == CUSTOMER || this == ADMIN;
    }

    /**
     * Check if this role can access POS/billing.
     */
    public boolean canAccessPOS() {
        return this == CASHIER || this == ADMIN;
    }

    /**
     * Check if this role can manage inventory.
     */
    public boolean canManageInventory() {
        return this == INVENTORY_MANAGER || this == ADMIN;
    }

    /**
     * Check if this role can view reports.
     */
    public boolean canViewReports() {
        return this == MANAGER || this == ADMIN;
    }

    /**
     * Check if this role can manage users/roles.
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }

    /**
     * Parse role from string (case-insensitive).
     */
    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            return CUSTOMER; // Default
        }
        try {
            return UserRole.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return CUSTOMER; // Default for unknown values
        }
    }
}
