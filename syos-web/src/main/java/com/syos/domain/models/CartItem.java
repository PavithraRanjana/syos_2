package com.syos.domain.models;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart.
 * Immutable value object that tracks product, quantity, and price.
 */
public class CartItem {

    private final ProductCode productCode;
    private final String productName;
    private final Money unitPrice;
    private int quantity;

    public CartItem(ProductCode productCode, String productName, Money unitPrice, int quantity) {
        if (productCode == null) {
            throw new IllegalArgumentException("Product code cannot be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.productCode = productCode;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public CartItem(Product product, int quantity) {
        this(product.getProductCode(), product.getProductName(), product.getUnitPrice(), quantity);
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public String getProductCodeString() {
        return productCode.getCode();
    }

    public String getProductName() {
        return productName;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    public void incrementQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }

    public void decrementQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity - amount < 1) {
            throw new IllegalArgumentException("Cannot reduce quantity below 1");
        }
        this.quantity -= amount;
    }

    public Money getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return productCode.equals(cartItem.productCode);
    }

    @Override
    public int hashCode() {
        return productCode.hashCode();
    }

    @Override
    public String toString() {
        return String.format("CartItem{product=%s, name='%s', qty=%d, unit=%s, total=%s}",
            productCode, productName, quantity, unitPrice, getLineTotal());
    }
}
