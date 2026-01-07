package com.syos.exception;

/**
 * Base exception for all SYOS application exceptions.
 */
public class SyosException extends RuntimeException {

    public SyosException(String message) {
        super(message);
    }

    public SyosException(String message, Throwable cause) {
        super(message, cause);
    }
}
