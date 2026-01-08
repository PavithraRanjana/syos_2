package com.syos.service.impl;

import com.syos.domain.models.Cart;
import com.syos.domain.models.CartItem;
import com.syos.domain.models.Product;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.service.interfaces.CartService;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of CartService.
 * Uses in-memory storage for cart data (session-based).
 * For production, this should be backed by a database or Redis.
 */
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final ProductService productService;
    private final StoreInventoryService storeInventoryService;

    // In-memory cart storage (keyed by customerId)
    // In production, this should be database-backed or use Redis
    private final Map<Integer, Cart> cartStore = new ConcurrentHashMap<>();

    public CartServiceImpl(ProductService productService, StoreInventoryService storeInventoryService) {
        this.productService = productService;
        this.storeInventoryService = storeInventoryService;
    }

    @Override
    public Cart getOrCreateCart(Integer customerId) {
        validateCustomerId(customerId);
        return cartStore.computeIfAbsent(customerId, Cart::new);
    }

    @Override
    public Optional<Cart> getCart(Integer customerId) {
        validateCustomerId(customerId);
        return Optional.ofNullable(cartStore.get(customerId));
    }

    @Override
    public Cart addItem(Integer customerId, String productCode, int quantity) {
        validateCustomerId(customerId);
        validateQuantity(quantity);

        Product product = productService.findByProductCode(productCode)
            .orElseThrow(() -> new ProductNotFoundException(productCode));

        if (!product.isActive()) {
            throw new ValidationException("Product is not available: " + productCode, "productCode");
        }

        Cart cart = getOrCreateCart(customerId);
        cart.addItem(product, quantity);

        logger.info("Added {} x {} to cart for customer {}",
            quantity, productCode, customerId);

        return cart;
    }

    @Override
    public Cart updateItemQuantity(Integer customerId, String productCode, int quantity) {
        validateCustomerId(customerId);

        Cart cart = getCart(customerId)
            .orElseThrow(() -> new ValidationException("Cart not found", "customerId"));

        if (quantity <= 0) {
            cart.removeItem(productCode);
            logger.info("Removed {} from cart for customer {}", productCode, customerId);
        } else {
            cart.updateItemQuantity(productCode, quantity);
            logger.info("Updated {} quantity to {} for customer {}",
                productCode, quantity, customerId);
        }

        return cart;
    }

    @Override
    public Cart removeItem(Integer customerId, String productCode) {
        validateCustomerId(customerId);

        Cart cart = getCart(customerId)
            .orElseThrow(() -> new ValidationException("Cart not found", "customerId"));

        cart.removeItem(productCode);
        logger.info("Removed {} from cart for customer {}", productCode, customerId);

        return cart;
    }

    @Override
    public void clearCart(Integer customerId) {
        validateCustomerId(customerId);

        Cart cart = cartStore.get(customerId);
        if (cart != null) {
            cart.clear();
            logger.info("Cleared cart for customer {}", customerId);
        }
    }

    @Override
    public int getCartItemCount(Integer customerId) {
        return getCart(customerId)
            .map(Cart::getTotalQuantity)
            .orElse(0);
    }

    @Override
    public boolean validateCartStock(Integer customerId) {
        return validateCartStockDetails(customerId).isValid();
    }

    @Override
    public StockValidationResult validateCartStockDetails(Integer customerId) {
        Optional<Cart> cartOpt = getCart(customerId);
        if (cartOpt.isEmpty() || cartOpt.get().isEmpty()) {
            return StockValidationResult.valid();
        }

        Cart cart = cartOpt.get();
        List<StockIssue> issues = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            int available = storeInventoryService.getOnlineStoreQuantity(item.getProductCodeString());
            if (available < item.getQuantity()) {
                issues.add(new StockIssue(
                    item.getProductCodeString(),
                    item.getProductName(),
                    item.getQuantity(),
                    available
                ));
            }
        }

        if (issues.isEmpty()) {
            return StockValidationResult.valid();
        }

        logger.warn("Stock validation failed for customer {}: {} issues", customerId, issues.size());
        return new StockValidationResult(false, issues);
    }

    private void validateCustomerId(Integer customerId) {
        if (customerId == null) {
            throw new ValidationException("Customer ID is required", "customerId");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive", "quantity");
        }
    }
}
