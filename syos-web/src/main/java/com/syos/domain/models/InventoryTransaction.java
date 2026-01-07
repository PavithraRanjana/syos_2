package com.syos.domain.models;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDateTime;

/**
 * Entity representing an inventory movement transaction for audit tracking.
 */
public class InventoryTransaction {

    private Integer transactionId;
    private ProductCode productCode;
    private Integer mainInventoryId; // Batch Number
    private InventoryTransactionType transactionType;
    private StoreType storeType;
    private int quantityChanged; // Positive for additions, negative for reductions
    private Integer billId; // Reference to bill if sale transaction
    private LocalDateTime transactionDate;
    private String remarks;

    public InventoryTransaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public InventoryTransaction(ProductCode productCode, Integer mainInventoryId,
                                 InventoryTransactionType transactionType, StoreType storeType,
                                 int quantityChanged) {
        this();
        this.productCode = productCode;
        this.mainInventoryId = mainInventoryId;
        this.transactionType = transactionType;
        this.storeType = storeType;
        this.quantityChanged = quantityChanged;
    }

    // Getters and Setters

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
    }

    public String getProductCodeString() {
        return productCode != null ? productCode.getCode() : null;
    }

    public Integer getMainInventoryId() {
        return mainInventoryId;
    }

    public void setMainInventoryId(Integer mainInventoryId) {
        this.mainInventoryId = mainInventoryId;
    }

    public InventoryTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(InventoryTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public int getQuantityChanged() {
        return quantityChanged;
    }

    public void setQuantityChanged(int quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "transactionId=" + transactionId +
                ", productCode=" + productCode +
                ", transactionType=" + transactionType +
                ", storeType=" + storeType +
                ", quantityChanged=" + quantityChanged +
                '}';
    }
}
