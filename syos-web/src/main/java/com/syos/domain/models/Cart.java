package com.syos.domain.models;

import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a shopping cart for online customers.
 * Manages cart items and provides cart-level calculations.
 */
public class Cart {

    private Integer cartId;
    private Integer customerId;
    private final Map<String, CartItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new LinkedHashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Cart(Integer customerId) {
        this();
        this.customerId = customerId;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
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

    /**
     * Adds an item to the cart. If the product already exists, increases quantity.
     */
    public void addItem(CartItem item) {
        String key = item.getProductCodeString();
        if (items.containsKey(key)) {
            items.get(key).incrementQuantity(item.getQuantity());
        } else {
            items.put(key, item);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a product to the cart with specified quantity.
     */
    public void addItem(Product product, int quantity) {
        addItem(new CartItem(product, quantity));
    }

    /**
     * Updates the quantity of an item in the cart.
     */
    public void updateItemQuantity(String productCode, int quantity) {
        CartItem item = items.get(productCode);
        if (item == null) {
            throw new IllegalArgumentException("Product not in cart: " + productCode);
        }
        if (quantity <= 0) {
            items.remove(productCode);
        } else {
            item.setQuantity(quantity);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes an item from the cart.
     */
    public void removeItem(String productCode) {
        if (items.remove(productCode) == null) {
            throw new IllegalArgumentException("Product not in cart: " + productCode);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes an item from the cart by ProductCode.
     */
    public void removeItem(ProductCode productCode) {
        removeItem(productCode.getCode());
    }

    /**
     * Gets an item from the cart by product code.
     */
    public Optional<CartItem> getItem(String productCode) {
        return Optional.ofNullable(items.get(productCode));
    }

    /**
     * Gets all items in the cart.
     */
    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Checks if the cart contains a specific product.
     */
    public boolean containsProduct(String productCode) {
        return items.containsKey(productCode);
    }

    /**
     * Returns the number of unique items in the cart.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Returns the total quantity of all items in the cart.
     */
    public int getTotalQuantity() {
        return items.values().stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    /**
     * Calculates the subtotal of all items in the cart.
     */
    public Money getSubtotal() {
        return items.values().stream()
            .map(CartItem::getLineTotal)
            .reduce(Money.ZERO, Money::add);
    }

    /**
     * Checks if the cart is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Creates a summary of the cart for display purposes.
     */
    public CartSummary getSummary() {
        return new CartSummary(
            getItemCount(),
            getTotalQuantity(),
            getSubtotal()
        );
    }

    public record CartSummary(
        int itemCount,
        int totalQuantity,
        Money subtotal
    ) {}

    @Override
    public String toString() {
        return String.format("Cart{id=%d, customer=%d, items=%d, subtotal=%s}",
            cartId, customerId, getItemCount(), getSubtotal());
    }
}
