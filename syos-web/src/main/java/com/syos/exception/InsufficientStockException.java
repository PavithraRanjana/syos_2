package com.syos.exception;

/**
 * Exception thrown when requested quantity exceeds available stock.
 */
public class InsufficientStockException extends InventoryException {

    private final int availableStock;
    private final int requestedQuantity;
    private final String productCode;

    public InsufficientStockException(String message, int availableStock, int requestedQuantity) {
        super(message);
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
        this.productCode = null;
    }

    private InsufficientStockException(String productCode, int availableStock, int requestedQuantity, boolean forProduct) {
        super(String.format(
            "Insufficient stock for product %s. Available: %d, Requested: %d",
            productCode, availableStock, requestedQuantity
        ));
        this.productCode = productCode;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    /**
     * Creates an exception for a specific product code.
     */
    public static InsufficientStockException forProduct(String productCode, int availableStock, int requestedQuantity) {
        return new InsufficientStockException(productCode, availableStock, requestedQuantity, true);
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getShortage() {
        return requestedQuantity - availableStock;
    }
}
