package com.syos.exception;

/**
 * Exception thrown when a bill is not found.
 */
public class BillNotFoundException extends BillingException {

    private final Integer billId;
    private final String serialNumber;

    public BillNotFoundException(Integer billId) {
        super("Bill not found with ID: " + billId);
        this.billId = billId;
        this.serialNumber = null;
    }

    public BillNotFoundException(String serialNumber) {
        super("Bill not found with serial number: " + serialNumber);
        this.billId = null;
        this.serialNumber = serialNumber;
    }

    public Integer getBillId() {
        return billId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
