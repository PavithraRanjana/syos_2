package com.syos.web.dto.response;

import com.syos.domain.models.Product;

import java.math.BigDecimal;

/**
 * DTO for product API responses.
 */
public class ProductResponse {

    private String productCode;
    private String productName;
    private String categoryName;
    private String subcategoryName;
    private String brandName;
    private BigDecimal unitPrice;
    private String unitOfMeasure;
    private String description;
    private boolean active;
    private int availableStock;
    private int minPhysicalStock;
    private int minOnlineStock;

    public ProductResponse() {
    }

    /**
     * Creates a ProductResponse from a Product entity.
     */
    public static ProductResponse fromProduct(Product product) {
        ProductResponse response = new ProductResponse();
        response.productCode = product.getProductCodeString();
        response.productName = product.getProductName();
        response.categoryName = product.getCategoryName();
        response.subcategoryName = product.getSubcategoryName();
        response.brandName = product.getBrandName();
        response.unitPrice = product.getUnitPrice() != null ?
            product.getUnitPrice().getAmount() : null;
        response.unitOfMeasure = product.getUnitOfMeasure() != null ?
            product.getUnitOfMeasure().getSymbol() : null;
        response.description = product.getDescription();
        response.active = product.isActive();
        response.minPhysicalStock = product.getMinPhysicalStock();
        response.minOnlineStock = product.getMinOnlineStock();
        return response;
    }

    /**
     * Creates a ProductResponse from a Product entity with stock info.
     */
    public static ProductResponse fromProduct(Product product, int availableStock) {
        ProductResponse response = fromProduct(product);
        response.availableStock = availableStock;
        return response;
    }

    // Getters and Setters

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getMinPhysicalStock() {
        return minPhysicalStock;
    }

    public void setMinPhysicalStock(int minPhysicalStock) {
        this.minPhysicalStock = minPhysicalStock;
    }

    public int getMinOnlineStock() {
        return minOnlineStock;
    }

    public void setMinOnlineStock(int minOnlineStock) {
        this.minOnlineStock = minOnlineStock;
    }
}
