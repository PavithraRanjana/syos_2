package com.syos.domain.enums;

/**
 * Represents the type of store inventory or transaction origin.
 */
public enum StoreType {
    PHYSICAL("Physical Store"),
    ONLINE("Online Store");

    private final String displayName;

    StoreType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string to StoreType, case-insensitive.
     */
    public static StoreType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Store type cannot be null or empty");
        }
        try {
            return StoreType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid store type: " + value + ". Valid values: PHYSICAL, ONLINE");
        }
    }
}
