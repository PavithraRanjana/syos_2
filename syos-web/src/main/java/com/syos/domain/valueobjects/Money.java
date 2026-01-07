package com.syos.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing monetary amounts.
 * Immutable with proper decimal handling for financial calculations.
 */
public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public Money(String amount) {
        this(new BigDecimal(amount));
    }

    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money add(Money other) {
        if (other == null) {
            return this;
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (other == null) {
            return this;
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Cannot subtract " + other.amount + " from " + this.amount + ": result would be negative"
            );
        }
        return new Money(result);
    }

    /**
     * Subtracts and allows negative result (for change calculation validation).
     */
    public Money subtractAllowNegative(Money other) {
        if (other == null) {
            return this;
        }
        BigDecimal result = this.amount.subtract(other.amount);
        // Return a special Money for negative (used for validation only)
        return new Money(result.abs());
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money multiply(BigDecimal factor) {
        if (factor == null || factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be null or negative");
        }
        return new Money(this.amount.multiply(factor));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Formats the amount with currency symbol for display.
     * Uses "Rs." prefix for Sri Lankan Rupee.
     */
    public String format() {
        return String.format("Rs. %,.2f", amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return format();
    }
}
