package com.syos.exception;

/**
 * Exception for billing-related errors.
 */
public class BillingException extends SyosException {

    public BillingException(String message) {
        super(message);
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
}
