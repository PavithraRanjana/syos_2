package com.syos.domain.models;

import java.time.LocalDateTime;

/**
 * Entity representing a product brand.
 */
public class Brand {

    private Integer brandId;
    private String brandName;
    private String brandCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Brand() {
    }

    public Brand(Integer brandId, String brandName, String brandCode) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.brandCode = brandCode;
    }

    // Getters and Setters

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
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

    @Override
    public String toString() {
        return "Brand{" +
                "brandId=" + brandId +
                ", brandName='" + brandName + '\'' +
                ", brandCode='" + brandCode + '\'' +
                '}';
    }
}
