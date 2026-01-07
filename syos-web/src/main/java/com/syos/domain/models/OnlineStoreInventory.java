package com.syos.domain.models;

import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing inventory available for online orders.
 * Links to a specific batch in main inventory.
 */
public class OnlineStoreInventory {

    private Integer onlineInventoryId;
    private ProductCode productCode;
    private Integer mainInventoryId; // Batch Number
    private int quantityAvailable;
    private LocalDate restockedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional display fields
    private String productName;
    private LocalDate expiryDate;

    public OnlineStoreInventory() {
    }

    public OnlineStoreInventory(ProductCode productCode, Integer mainInventoryId,
                                 int quantityAvailable, LocalDate restockedDate) {
        this.productCode = productCode;
        this.mainInventoryId = mainInventoryId;
        this.quantityAvailable = quantityAvailable;
        this.restockedDate = restockedDate;
    }

    // Business Methods

    public boolean hasStock() {
        return quantityAvailable > 0;
    }

    public boolean canFulfill(int required) {
        return quantityAvailable >= required;
    }

    public void reduceQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to reduce cannot be negative");
        }
        if (amount > quantityAvailable) {
            throw new IllegalArgumentException(
                "Cannot reduce by " + amount + ". Only " + quantityAvailable + " available"
            );
        }
        this.quantityAvailable -= amount;
    }

    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        this.quantityAvailable += amount;
    }

    // Getters and Setters

    public Integer getOnlineInventoryId() {
        return onlineInventoryId;
    }

    public void setOnlineInventoryId(Integer onlineInventoryId) {
        this.onlineInventoryId = onlineInventoryId;
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

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public LocalDate getRestockedDate() {
        return restockedDate;
    }

    public void setRestockedDate(LocalDate restockedDate) {
        this.restockedDate = restockedDate;
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

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "OnlineStoreInventory{" +
                "onlineInventoryId=" + onlineInventoryId +
                ", productCode=" + productCode +
                ", mainInventoryId=" + mainInventoryId +
                ", quantityAvailable=" + quantityAvailable +
                '}';
    }
}
