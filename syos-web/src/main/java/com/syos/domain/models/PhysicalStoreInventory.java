package com.syos.domain.models;

import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing inventory on the physical store shelf.
 * Links to a specific batch in main inventory.
 */
public class PhysicalStoreInventory {

    private Integer physicalInventoryId;
    private ProductCode productCode;
    private Integer mainInventoryId; // Batch Number
    private int quantityOnShelf;
    private LocalDate restockedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional display fields
    private String productName;
    private LocalDate expiryDate;

    public PhysicalStoreInventory() {
    }

    public PhysicalStoreInventory(ProductCode productCode, Integer mainInventoryId,
                                   int quantityOnShelf, LocalDate restockedDate) {
        this.productCode = productCode;
        this.mainInventoryId = mainInventoryId;
        this.quantityOnShelf = quantityOnShelf;
        this.restockedDate = restockedDate;
    }

    // Business Methods

    public boolean hasStock() {
        return quantityOnShelf > 0;
    }

    public boolean canFulfill(int required) {
        return quantityOnShelf >= required;
    }

    public void reduceQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to reduce cannot be negative");
        }
        if (amount > quantityOnShelf) {
            throw new IllegalArgumentException(
                "Cannot reduce by " + amount + ". Only " + quantityOnShelf + " on shelf"
            );
        }
        this.quantityOnShelf -= amount;
    }

    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        this.quantityOnShelf += amount;
    }

    // Getters and Setters

    public Integer getPhysicalInventoryId() {
        return physicalInventoryId;
    }

    public void setPhysicalInventoryId(Integer physicalInventoryId) {
        this.physicalInventoryId = physicalInventoryId;
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

    public int getQuantityOnShelf() {
        return quantityOnShelf;
    }

    public void setQuantityOnShelf(int quantityOnShelf) {
        this.quantityOnShelf = quantityOnShelf;
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
        return "PhysicalStoreInventory{" +
                "physicalInventoryId=" + physicalInventoryId +
                ", productCode=" + productCode +
                ", mainInventoryId=" + mainInventoryId +
                ", quantityOnShelf=" + quantityOnShelf +
                '}';
    }
}
