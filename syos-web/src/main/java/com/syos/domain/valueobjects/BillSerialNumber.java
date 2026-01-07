package com.syos.domain.valueobjects;

import java.util.Objects;

/**
 * Value object representing a unique bill serial number.
 * Format: Simple running number ("1", "2", "3", ..., "100", "101", ...)
 *
 * Immutable value object following DDD principles.
 */
public final class BillSerialNumber {

    private final String serialNumber;

    public BillSerialNumber(String serialNumber) {
        validateSerialNumber(serialNumber);
        this.serialNumber = serialNumber.trim();
    }

    public BillSerialNumber(int serialNumber) {
        if (serialNumber < 1) {
            throw new IllegalArgumentException("Serial number must be positive: " + serialNumber);
        }
        this.serialNumber = String.valueOf(serialNumber);
    }

    private void validateSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty");
        }
    }

    public String getValue() {
        return serialNumber;
    }

    /**
     * Returns the numeric value of the serial number.
     */
    public int getNumericValue() {
        try {
            return Integer.parseInt(serialNumber);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Serial number is not numeric: " + serialNumber);
        }
    }

    /**
     * Creates the next serial number in sequence.
     */
    public BillSerialNumber next() {
        return new BillSerialNumber(getNumericValue() + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillSerialNumber that = (BillSerialNumber) o;
        return Objects.equals(serialNumber, that.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber);
    }

    @Override
    public String toString() {
        return serialNumber;
    }
}
