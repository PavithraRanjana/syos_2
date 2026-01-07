package com.syos.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown for input validation failures.
 * Can contain multiple field-level errors.
 */
public class ValidationException extends SyosException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    public ValidationException(String field, String error) {
        super(error);
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, error);
    }

    public void addFieldError(String field, String error) {
        fieldErrors.put(field, error);
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
