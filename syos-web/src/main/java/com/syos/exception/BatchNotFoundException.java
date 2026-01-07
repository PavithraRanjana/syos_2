package com.syos.exception;

/**
 * Exception thrown when a batch is not found.
 */
public class BatchNotFoundException extends InventoryException {

    private final Integer batchNumber;

    public BatchNotFoundException(Integer batchNumber) {
        super("Batch not found: " + batchNumber);
        this.batchNumber = batchNumber;
    }

    public BatchNotFoundException(String message, Integer batchNumber) {
        super(message);
        this.batchNumber = batchNumber;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }
}
