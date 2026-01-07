package com.syos.exception;

import com.syos.domain.valueobjects.Money;

/**
 * Exception thrown for invalid payment operations.
 */
public class InvalidPaymentException extends BillingException {

    private final Money required;
    private final Money tendered;

    public InvalidPaymentException(String message) {
        super(message);
        this.required = null;
        this.tendered = null;
    }

    public InvalidPaymentException(Money required, Money tendered) {
        super(String.format(
            "Insufficient cash. Required: %s, Tendered: %s",
            required.format(), tendered.format()
        ));
        this.required = required;
        this.tendered = tendered;
    }

    public Money getRequired() {
        return required;
    }

    public Money getTendered() {
        return tendered;
    }
}
