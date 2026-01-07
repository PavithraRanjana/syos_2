package com.syos.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification.
 * Uses BCrypt for secure password hashing.
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain text password using BCrypt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain text password against a stored hash.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }

    /**
     * Checks if a password meets minimum requirements.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Checks if a hash is in valid BCrypt format.
     */
    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != 60) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
