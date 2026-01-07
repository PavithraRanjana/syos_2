package com.syos.exception;

/**
 * Exception thrown for database/repository errors.
 */
public class RepositoryException extends SyosException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
