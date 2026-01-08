package com.syos.service.interfaces;

import com.syos.domain.models.Cart;
import com.syos.domain.models.CartItem;

import java.util.Optional;

/**
 * Service interface for shopping cart operations.
 */
public interface CartService {

    /**
     * Gets the cart for a customer, creating one if it doesn't exist.
     */
    Cart getOrCreateCart(Integer customerId);

    /**
     * Gets the cart for a customer.
     */
    Optional<Cart> getCart(Integer customerId);

    /**
     * Adds an item to the customer's cart.
     */
    Cart addItem(Integer customerId, String productCode, int quantity);

    /**
     * Updates the quantity of an item in the cart.
     */
    Cart updateItemQuantity(Integer customerId, String productCode, int quantity);

    /**
     * Removes an item from the cart.
     */
    Cart removeItem(Integer customerId, String productCode);

    /**
     * Clears all items from the cart.
     */
    void clearCart(Integer customerId);

    /**
     * Gets the number of items in the cart.
     */
    int getCartItemCount(Integer customerId);

    /**
     * Validates that all items in the cart are available in stock.
     * Returns true if all items are available.
     */
    boolean validateCartStock(Integer customerId);

    /**
     * Validates stock and returns details about any unavailable items.
     */
    StockValidationResult validateCartStockDetails(Integer customerId);

    /**
     * Represents the result of stock validation.
     */
    record StockValidationResult(
        boolean isValid,
        java.util.List<StockIssue> issues
    ) {
        public static StockValidationResult valid() {
            return new StockValidationResult(true, java.util.List.of());
        }
    }

    /**
     * Represents a stock availability issue.
     */
    record StockIssue(
        String productCode,
        String productName,
        int requestedQuantity,
        int availableQuantity
    ) {}
}
