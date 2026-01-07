package com.syos.exception;

/**
 * Base exception for customer-related errors.
 */
public class CustomerException extends SyosException {

    public CustomerException(String message) {
        super(message);
    }

    public CustomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
