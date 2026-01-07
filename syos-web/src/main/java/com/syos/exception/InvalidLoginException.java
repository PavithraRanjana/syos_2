package com.syos.exception;

/**
 * Exception thrown when login authentication fails.
 */
public class InvalidLoginException extends CustomerException {

    public InvalidLoginException() {
        super("Invalid email or password");
    }

    public InvalidLoginException(String message) {
        super(message);
    }
}
