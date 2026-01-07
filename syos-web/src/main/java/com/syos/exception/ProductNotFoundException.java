package com.syos.exception;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends InventoryException {

    private final String productCode;

    public ProductNotFoundException(String productCode) {
        super("Product not found: " + productCode);
        this.productCode = productCode;
    }

    public ProductNotFoundException(String message, String productCode) {
        super(message);
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }
}
