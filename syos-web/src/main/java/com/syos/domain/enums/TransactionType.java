package com.syos.domain.enums;

/**
 * Represents the type of payment transaction.
 */
public enum TransactionType {
    CASH("Cash Payment"),
    ONLINE("Online Payment");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string to TransactionType, case-insensitive.
     */
    public static TransactionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }
        try {
            return TransactionType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + value + ". Valid values: CASH, ONLINE");
        }
    }
}
