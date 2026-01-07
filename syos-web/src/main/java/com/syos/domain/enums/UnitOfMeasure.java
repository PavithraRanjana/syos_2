package com.syos.domain.enums;

/**
 * Represents the unit of measurement for products.
 */
public enum UnitOfMeasure {
    KILOGRAM("kg", "Kilogram"),
    GRAM("g", "Gram"),
    LITER("L", "Liter"),
    MILLILITER("ml", "Milliliter"),
    UNIT("unit", "Unit"),
    PACK("pack", "Pack"),
    CAN("can", "Can"),
    BOTTLE("bottle", "Bottle"),
    BAR("bar", "Bar"),
    BAG("bag", "Bag"),
    JAR("jar", "Jar"),
    PCS("pcs", "Pieces");

    private final String symbol;
    private final String fullName;

    UnitOfMeasure(String symbol, String fullName) {
        this.symbol = symbol;
        this.fullName = fullName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Parses a string to UnitOfMeasure.
     * Accepts both the enum name and the symbol.
     */
    public static UnitOfMeasure fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PCS; // Default to pieces
        }

        String normalized = value.trim().toLowerCase();

        // Try matching by symbol first
        for (UnitOfMeasure unit : values()) {
            if (unit.symbol.equalsIgnoreCase(normalized)) {
                return unit;
            }
        }

        // Try matching by enum name
        try {
            return UnitOfMeasure.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Default to PCS if not found
            return PCS;
        }
    }

    @Override
    public String toString() {
        return symbol;
    }
}
