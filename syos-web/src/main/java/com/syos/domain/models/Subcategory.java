package com.syos.domain.models;

import java.time.LocalDateTime;

/**
 * Entity representing a product subcategory within a category.
 */
public class Subcategory {

    private Integer subcategoryId;
    private String subcategoryName;
    private String subcategoryCode;
    private Integer categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Subcategory() {
    }

    public Subcategory(Integer subcategoryId, String subcategoryName, String subcategoryCode, Integer categoryId) {
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.subcategoryCode = subcategoryCode;
        this.categoryId = categoryId;
    }

    // Getters and Setters

    public Integer getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Integer subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

    public String getSubcategoryCode() {
        return subcategoryCode;
    }

    public void setSubcategoryCode(String subcategoryCode) {
        this.subcategoryCode = subcategoryCode;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
        return "Subcategory{" +
                "subcategoryId=" + subcategoryId +
                ", subcategoryName='" + subcategoryName + '\'' +
                ", subcategoryCode='" + subcategoryCode + '\'' +
                ", categoryId=" + categoryId +
                '}';
    }
}
