package com.syos.util;

import com.syos.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{1,15}$"
    );

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Validates that a string is not null or empty.
     */
    public static String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required", fieldName);
        }
        return value.trim();
    }

    /**
     * Validates that a string doesn't exceed max length.
     */
    public static String requireMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(
                fieldName + " cannot exceed " + maxLength + " characters",
                fieldName
            );
        }
        return value;
    }

    /**
     * Validates email format.
     */
    public static String validateEmail(String email) {
        String trimmed = requireNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid email format", "email");
        }
        return trimmed.toLowerCase();
    }

    /**
     * Validates phone number format.
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Phone is optional
        }
        String cleaned = phone.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new ValidationException("Invalid phone number format", "phone");
        }
        return cleaned;
    }

    /**
     * Validates a positive integer.
     */
    public static int requirePositiveInt(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        try {
            int num = Integer.parseInt(value.trim());
            if (num <= 0) {
                throw new ValidationException(fieldName + " must be a positive number", fieldName);
            }
            return num;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number", fieldName);
        }
    }

    /**
     * Validates a non-negative integer.
     */
    public static int requireNonNegativeInt(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        try {
            int num = Integer.parseInt(value.trim());
            if (num < 0) {
                throw new ValidationException(fieldName + " cannot be negative", fieldName);
            }
            return num;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number", fieldName);
        }
    }

    /**
     * Validates a positive BigDecimal (for money).
     */
    public static BigDecimal requirePositiveBigDecimal(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        try {
            BigDecimal num = new BigDecimal(value.trim());
            if (num.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(fieldName + " must be a positive amount", fieldName);
            }
            return num;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid amount", fieldName);
        }
    }

    /**
     * Validates a non-negative BigDecimal.
     */
    public static BigDecimal requireNonNegativeBigDecimal(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        try {
            BigDecimal num = new BigDecimal(value.trim());
            if (num.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException(fieldName + " cannot be negative", fieldName);
            }
            return num;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid amount", fieldName);
        }
    }

    /**
     * Parses and validates a date string (yyyy-MM-dd format).
     */
    public static LocalDate parseDate(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                fieldName + " must be in yyyy-MM-dd format",
                fieldName
            );
        }
    }

    /**
     * Parses and validates a date that cannot be in the future.
     */
    public static LocalDate parsePastOrTodayDate(String value, String fieldName) {
        LocalDate date = parseDate(value, fieldName);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(fieldName + " cannot be in the future", fieldName);
        }
        return date;
    }

    /**
     * Parses an optional date (returns null if empty).
     */
    public static LocalDate parseOptionalDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return parseDate(value, fieldName);
    }

    /**
     * Validates a product code format.
     */
    public static String validateProductCode(String code) {
        String trimmed = requireNonEmpty(code, "Product code").toUpperCase();
        if (!PRODUCT_CODE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException(
                "Product code must be alphanumeric and up to 15 characters",
                "productCode"
            );
        }
        return trimmed;
    }

    /**
     * Validates password requirements.
     */
    public static String validatePassword(String password) {
        requireNonEmpty(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException(
                "Password must be at least 6 characters",
                "password"
            );
        }
        return password;
    }

    /**
     * Parses an integer from string, returning default if null/empty.
     */
    public static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
