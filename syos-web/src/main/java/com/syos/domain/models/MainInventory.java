package com.syos.domain.models;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Entity representing a batch in the main inventory (central stock).
 * The main_inventory_id serves as the BATCH NUMBER.
 */
public class MainInventory {

    private Integer mainInventoryId; // Batch Number
    private ProductCode productCode;
    private int quantityReceived;
    private Money purchasePrice;
    private LocalDate purchaseDate;
    private LocalDate expiryDate; // nullable for non-perishables
    private String supplierName;
    private int remainingQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional display field
    private String productName;

    public MainInventory() {
    }

    public MainInventory(ProductCode productCode, int quantityReceived, Money purchasePrice,
                         LocalDate purchaseDate, LocalDate expiryDate, String supplierName) {
        this.productCode = productCode;
        this.quantityReceived = quantityReceived;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.supplierName = supplierName;
        this.remainingQuantity = quantityReceived;
    }

    // Business Methods

    /**
     * Checks if this batch has available stock.
     */
    public boolean hasStock() {
        return remainingQuantity > 0;
    }

    /**
     * Checks if this batch can fulfill the required quantity.
     */
    public boolean canFulfill(int required) {
        return remainingQuantity >= required;
    }

    /**
     * Reduces the remaining quantity. Does not allow negative quantities.
     */
    public void reduceQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to reduce cannot be negative");
        }
        if (amount > remainingQuantity) {
            throw new IllegalArgumentException(
                "Cannot reduce by " + amount + ". Only " + remainingQuantity + " remaining"
            );
        }
        this.remainingQuantity -= amount;
    }

    /**
     * Increases the remaining quantity (for undo operations).
     */
    public void increaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to increase cannot be negative");
        }
        this.remainingQuantity += amount;
    }

    /**
     * Calculates days until expiry. Returns null if no expiry date.
     */
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Checks if this batch is expiring soon (within 7 days, for JSP/EL access).
     */
    public boolean isExpiringSoon() {
        return isExpiringSoon(7);
    }

    /**
     * Checks if this batch is expiring soon (within threshold days).
     */
    public boolean isExpiringSoon(int thresholdDays) {
        Long daysLeft = getDaysUntilExpiry();
        return daysLeft != null && daysLeft <= thresholdDays;
    }

    /**
     * Checks if this batch is expired.
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Returns the batch number (alias for mainInventoryId).
     */
    public Integer getBatchNumber() {
        return mainInventoryId;
    }

    // Getters and Setters

    public Integer getMainInventoryId() {
        return mainInventoryId;
    }

    public void setMainInventoryId(Integer mainInventoryId) {
        this.mainInventoryId = mainInventoryId;
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

    public int getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(int quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public Money getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Money purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Returns the purchase date formatted as MMM d, yyyy for display.
     */
    public String getPurchaseDateFormatted() {
        return purchaseDate != null ? purchaseDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "";
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns the expiry date formatted as MMM d, yyyy for display.
     */
    public String getExpiryDateFormatted() {
        return expiryDate != null ? expiryDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "";
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "MainInventory{" +
                "batchNumber=" + mainInventoryId +
                ", productCode=" + productCode +
                ", remainingQuantity=" + remainingQuantity +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
