package com.syos.domain.models;

import com.syos.domain.enums.UnitOfMeasure;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.time.LocalDateTime;

/**
 * Entity representing a product in the catalog.
 * Product code format: [CATEGORY_CODE][SUBCATEGORY_CODE][BRAND_CODE][SEQUENCE]
 */
public class Product {

    private ProductCode productCode;
    private String productName;
    private Integer categoryId;
    private Integer subcategoryId;
    private Integer brandId;
    private Money unitPrice;
    private String description;
    private UnitOfMeasure unitOfMeasure;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for display (populated from joins)
    private String categoryName;
    private String subcategoryName;
    private String brandName;

    public Product() {
        this.active = true;
    }

    public Product(ProductCode productCode, String productName, Integer categoryId,
                   Integer subcategoryId, Integer brandId, Money unitPrice) {
        this.productCode = productCode;
        this.productName = productName;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.brandId = brandId;
        this.unitPrice = unitPrice;
        this.active = true;
        this.unitOfMeasure = UnitOfMeasure.PCS;
    }

    // Business Methods

    public void updatePrice(Money newPrice) {
        if (newPrice == null || newPrice.isZero()) {
            throw new IllegalArgumentException("Price cannot be null or zero");
        }
        this.unitPrice = newPrice;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters and Setters

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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Integer subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productCode=" + productCode +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", active=" + active +
                '}';
    }
}
