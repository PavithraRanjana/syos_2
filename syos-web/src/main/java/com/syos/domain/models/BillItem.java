package com.syos.domain.models;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDateTime;

/**
 * Entity representing a line item in a bill.
 * Tracks which batch the item was sold from for inventory management.
 */
public class BillItem {

    private Integer billItemId;
    private Integer billId;
    private ProductCode productCode;
    private String productName; // Denormalized for display
    private Integer mainInventoryId; // Batch number - which batch was sold
    private int quantity;
    private Money unitPrice;
    private Money totalPrice;
    private LocalDateTime createdAt;

    public BillItem() {
    }

    public BillItem(ProductCode productCode, String productName, int quantity,
                    Money unitPrice, Integer mainInventoryId) {
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.mainInventoryId = mainInventoryId;
        this.totalPrice = unitPrice.multiply(quantity);
    }

    // Business Methods

    /**
     * Recalculates the total price based on quantity and unit price.
     */
    public void recalculateTotal() {
        if (unitPrice != null && quantity > 0) {
            this.totalPrice = unitPrice.multiply(quantity);
        }
    }

    /**
     * Updates the quantity and recalculates the total.
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        recalculateTotal();
    }

    // Getters and Setters

    public Integer getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(Integer billItemId) {
        this.billItemId = billItemId;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getMainInventoryId() {
        return mainInventoryId;
    }

    public void setMainInventoryId(Integer mainInventoryId) {
        this.mainInventoryId = mainInventoryId;
    }

    /**
     * Alias for mainInventoryId.
     */
    public Integer getBatchNumber() {
        return mainInventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Money getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Money totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Alias for totalPrice (matches database column name).
     */
    public Money getLineTotal() {
        return totalPrice;
    }

    /**
     * Alias for setTotalPrice (matches database column name).
     */
    public void setLineTotal(Money lineTotal) {
        this.totalPrice = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BillItem{" +
                "productCode=" + productCode +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", batchNumber=" + mainInventoryId +
                '}';
    }
}
