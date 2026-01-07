package com.syos.exception;

/**
 * Exception thrown during customer registration failures.
 */
public class CustomerRegistrationException extends CustomerException {

    private final String field;

    public CustomerRegistrationException(String message) {
        super(message);
        this.field = null;
    }

    public CustomerRegistrationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
