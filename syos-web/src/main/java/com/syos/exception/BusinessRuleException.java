package com.syos.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessRuleException extends SyosException {

    private final String rule;

    public BusinessRuleException(String message) {
        super(message);
        this.rule = null;
    }

    public BusinessRuleException(String message, String rule) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}
