package com.syos.exception;

/**
 * Exception for inventory-related errors.
 */
public class InventoryException extends SyosException {

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
