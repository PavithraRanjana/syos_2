package com.syos.domain.enums;

/**
 * Represents the type of inventory command for the Command pattern.
 * Used for tracking undo-capable operations.
 */
public enum CommandType {
    ADD_BATCH("Add Batch", "Add new inventory batch to main inventory"),
    REMOVE_BATCH("Remove Batch", "Remove inventory batch from main inventory"),
    ISSUE_STOCK("Issue Stock", "Issue stock from main inventory to store"),
    ADD_PRODUCT("Add Product", "Add new product to catalog"),
    UPDATE_STOCK("Update Stock", "Manually update stock quantity");

    private final String displayName;
    private final String description;

    CommandType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
