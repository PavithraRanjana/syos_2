package com.syos.exception;

/**
 * Exception thrown when a customer is not found.
 */
public class CustomerNotFoundException extends CustomerException {

    private final Integer customerId;
    private final String email;

    public CustomerNotFoundException(Integer customerId) {
        super("Customer not found with ID: " + customerId);
        this.customerId = customerId;
        this.email = null;
    }

    public CustomerNotFoundException(String email) {
        super("Customer not found with email: " + email);
        this.email = email;
        this.customerId = null;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }
}
