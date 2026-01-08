package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Cart;
import com.syos.domain.models.CartItem;
import com.syos.service.interfaces.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for shopping cart operations.
 *
 * Endpoints:
 * GET    /api/cart           - Get current cart
 * POST   /api/cart/items     - Add item to cart
 * PUT    /api/cart/items/{productCode} - Update item quantity
 * DELETE /api/cart/items/{productCode} - Remove item from cart
 * DELETE /api/cart           - Clear cart
 * GET    /api/cart/validate  - Validate cart stock
 */
@WebServlet(urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartApiServlet extends BaseApiServlet {

    private CartService cartService;

    @Override
    public void init() throws ServletException {
        super.init();
        cartService = ServiceRegistry.get(CartService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = getCurrentUserId(request);
            if (customerId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/cart - Get cart
                handleGetCart(customerId, response);
            } else if (pathInfo.equals("/validate")) {
                // GET /api/cart/validate - Validate stock
                handleValidateCart(customerId, response);
            } else if (pathInfo.equals("/count")) {
                // GET /api/cart/count - Get cart item count
                handleGetCartCount(customerId, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = getCurrentUserId(request);
            if (customerId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.equals("/items")) {
                // POST /api/cart/items - Add item
                handleAddItem(customerId, request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = getCurrentUserId(request);
            if (customerId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                // PUT /api/cart/items/{productCode} - Update quantity
                String productCode = pathInfo.substring("/items/".length());
                handleUpdateItem(customerId, productCode, request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = getCurrentUserId(request);
            if (customerId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // DELETE /api/cart - Clear cart
                handleClearCart(customerId, response);
            } else if (pathInfo.startsWith("/items/")) {
                // DELETE /api/cart/items/{productCode} - Remove item
                String productCode = pathInfo.substring("/items/".length());
                handleRemoveItem(customerId, productCode, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleGetCart(Integer customerId, HttpServletResponse response) throws IOException {
        Cart cart = cartService.getOrCreateCart(customerId);
        sendSuccess(response, toCartResponse(cart));
    }

    private void handleGetCartCount(Integer customerId, HttpServletResponse response) throws IOException {
        int count = cartService.getCartItemCount(customerId);
        sendSuccess(response, Map.of("count", count));
    }

    private void handleValidateCart(Integer customerId, HttpServletResponse response) throws IOException {
        CartService.StockValidationResult result = cartService.validateCartStockDetails(customerId);

        if (result.isValid()) {
            sendSuccess(response, Map.of("valid", true, "issues", List.of()));
        } else {
            List<Map<String, Object>> issues = result.issues().stream()
                .map(issue -> Map.<String, Object>of(
                    "productCode", issue.productCode(),
                    "productName", issue.productName(),
                    "requested", issue.requestedQuantity(),
                    "available", issue.availableQuantity()
                ))
                .toList();

            sendSuccess(response, Map.of("valid", false, "issues", issues));
        }
    }

    private void handleAddItem(Integer customerId, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        AddItemRequest addRequest = parseRequestBody(request, AddItemRequest.class);

        if (addRequest == null || addRequest.productCode == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Product code is required");
            return;
        }

        int quantity = addRequest.quantity != null ? addRequest.quantity : 1;
        if (quantity <= 0) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Quantity must be positive");
            return;
        }

        Cart cart = cartService.addItem(customerId, addRequest.productCode, quantity);

        logger.info("Added {} x {} to cart for customer {}", quantity, addRequest.productCode, customerId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, toCartResponse(cart), "Item added to cart");
    }

    private void handleUpdateItem(Integer customerId, String productCode,
                                   HttpServletRequest request, HttpServletResponse response) throws IOException {
        UpdateItemRequest updateRequest = parseRequestBody(request, UpdateItemRequest.class);

        if (updateRequest == null || updateRequest.quantity == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Quantity is required");
            return;
        }

        Cart cart = cartService.updateItemQuantity(customerId, productCode, updateRequest.quantity);

        logger.info("Updated {} quantity to {} for customer {}", productCode, updateRequest.quantity, customerId);

        sendSuccess(response, toCartResponse(cart), "Cart updated");
    }

    private void handleRemoveItem(Integer customerId, String productCode,
                                   HttpServletResponse response) throws IOException {
        Cart cart = cartService.removeItem(customerId, productCode);

        logger.info("Removed {} from cart for customer {}", productCode, customerId);

        sendSuccess(response, toCartResponse(cart), "Item removed from cart");
    }

    private void handleClearCart(Integer customerId, HttpServletResponse response) throws IOException {
        cartService.clearCart(customerId);

        logger.info("Cleared cart for customer {}", customerId);

        sendSuccess(response, Map.of("items", List.of(), "itemCount", 0, "totalQuantity", 0, "subtotal", "0.00"),
            "Cart cleared");
    }

    private Map<String, Object> toCartResponse(Cart cart) {
        List<Map<String, Object>> items = cart.getItems().stream()
            .map(this::toCartItemResponse)
            .toList();

        return Map.of(
            "items", items,
            "itemCount", cart.getItemCount(),
            "totalQuantity", cart.getTotalQuantity(),
            "subtotal", cart.getSubtotal().toString()
        );
    }

    private Map<String, Object> toCartItemResponse(CartItem item) {
        return Map.of(
            "productCode", item.getProductCodeString(),
            "productName", item.getProductName(),
            "unitPrice", item.getUnitPrice().toString(),
            "quantity", item.getQuantity(),
            "lineTotal", item.getLineTotal().toString()
        );
    }

    // ==================== Request DTOs ====================

    public static class AddItemRequest {
        public String productCode;
        public Integer quantity;
    }

    public static class UpdateItemRequest {
        public Integer quantity;
    }
}
