package com.syos.exception;

/**
 * Exception thrown when attempting to register with an email that already exists.
 */
public class DuplicateEmailException extends CustomerException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
