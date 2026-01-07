package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a unique product code.
 * Format: [CATEGORY_CODE][SUBCATEGORY_CODE][BRAND_CODE][SEQUENCE]
 * Example: BVEDRB001 (Beverages-EnergyDrink-RedBull-001)
 *
 * Immutable value object following DDD principles.
 */
public final class ProductCode {

    private static final int MAX_LENGTH = 15;

    private final String code;

    public ProductCode(String code) {
        validateCode(code);
        this.code = code.toUpperCase().trim();
    }

    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        if (code.trim().length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Product code cannot exceed " + MAX_LENGTH + " characters. Got: " + code.length()
            );
        }
    }

    public String getCode() {
        return code;
    }

    /**
     * Returns the category code portion (first 2 characters typically).
     */
    public String getCategoryCode() {
        return code.length() >= 2 ? code.substring(0, 2) : code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCode that = (ProductCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
