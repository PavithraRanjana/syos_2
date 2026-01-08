package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.OrderStatus;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Order;
import com.syos.domain.models.OrderItem;
import com.syos.service.interfaces.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for order operations.
 *
 * Endpoints:
 * GET    /api/orders              - Get customer's orders
 * GET    /api/orders/{id}         - Get order by ID
 * POST   /api/orders              - Create order from cart
 * PUT    /api/orders/{id}/cancel  - Cancel order
 * PUT    /api/orders/{id}/shipping - Update shipping address
 * GET    /api/orders/stats        - Get order statistics
 */
@WebServlet(urlPatterns = {"/api/orders", "/api/orders/*"})
public class OrderApiServlet extends BaseApiServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = ServiceRegistry.get(OrderService.class);
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
                // GET /api/orders - Get customer's orders
                handleGetOrders(customerId, response);
            } else if (pathInfo.equals("/stats")) {
                // GET /api/orders/stats - Get order statistics
                handleGetOrderStats(customerId, response);
            } else {
                // GET /api/orders/{id} - Get specific order
                String orderId = pathInfo.substring(1);
                handleGetOrder(customerId, orderId, response);
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

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/orders - Create order from cart
                handleCreateOrder(customerId, request, response);
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
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Order ID required");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length < 2) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Action required");
                return;
            }

            int orderId = Integer.parseInt(parts[0]);
            String action = parts[1];

            // Verify order belongs to customer
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            if (!order.getCustomerId().equals(customerId)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            switch (action) {
                case "cancel" -> handleCancelOrder(orderId, request, response);
                case "shipping" -> handleUpdateShipping(orderId, request, response);
                default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Action not found");
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleGetOrders(Integer customerId, HttpServletResponse response) throws IOException {
        List<Order> orders = orderService.findByCustomerId(customerId);

        List<Map<String, Object>> orderList = orders.stream()
            .map(this::toOrderSummaryResponse)
            .toList();

        sendSuccess(response, Map.of(
            "orders", orderList,
            "count", orders.size()
        ));
    }

    private void handleGetOrder(Integer customerId, String orderIdStr, HttpServletResponse response) throws IOException {
        try {
            int orderId = Integer.parseInt(orderIdStr);
            Order order = orderService.findById(orderId).orElse(null);

            if (order == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            // Verify order belongs to customer
            if (!order.getCustomerId().equals(customerId)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            sendSuccess(response, toOrderDetailResponse(order));

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
        }
    }

    private void handleGetOrderStats(Integer customerId, HttpServletResponse response) throws IOException {
        OrderService.OrderStats stats = orderService.getCustomerOrderStats(customerId);

        sendSuccess(response, Map.of(
            "totalOrders", stats.totalOrders(),
            "pendingOrders", stats.pendingOrders(),
            "completedOrders", stats.completedOrders(),
            "cancelledOrders", stats.cancelledOrders()
        ));
    }

    private void handleCreateOrder(Integer customerId, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        CreateOrderRequest createRequest = parseRequestBody(request, CreateOrderRequest.class);

        OrderService.OrderRequest orderRequest = null;
        if (createRequest != null) {
            TransactionType paymentMethod = TransactionType.ONLINE;
            if (createRequest.paymentMethod != null) {
                try {
                    paymentMethod = TransactionType.valueOf(createRequest.paymentMethod.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    // Use default
                }
            }

            orderRequest = new OrderService.OrderRequest(
                createRequest.shippingAddress,
                createRequest.shippingPhone,
                createRequest.shippingNotes,
                paymentMethod
            );
        }

        Order order = orderService.createOrderFromCart(customerId, orderRequest);

        logger.info("Order created: {} for customer {}", order.getOrderNumber(), customerId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, toOrderDetailResponse(order), "Order created successfully");
    }

    private void handleCancelOrder(Integer orderId, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        CancelOrderRequest cancelRequest = parseRequestBody(request, CancelOrderRequest.class);
        String reason = cancelRequest != null ? cancelRequest.reason : null;

        Order order = orderService.cancelOrder(orderId, reason);

        logger.info("Order cancelled: {}", order.getOrderNumber());

        sendSuccess(response, toOrderSummaryResponse(order), "Order cancelled successfully");
    }

    private void handleUpdateShipping(Integer orderId, HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        UpdateShippingRequest shippingRequest = parseRequestBody(request, UpdateShippingRequest.class);

        if (shippingRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Shipping details required");
            return;
        }

        Order order = orderService.updateShippingAddress(
            orderId,
            shippingRequest.shippingAddress,
            shippingRequest.shippingPhone
        );

        sendSuccess(response, toOrderSummaryResponse(order), "Shipping address updated");
    }

    private Map<String, Object> toOrderSummaryResponse(Order order) {
        return Map.of(
            "orderId", order.getOrderId(),
            "orderNumber", order.getOrderNumber(),
            "status", order.getStatus().name(),
            "statusDisplay", order.getStatus().getDisplayName(),
            "totalAmount", order.getTotalAmount().toString(),
            "itemCount", order.getTotalItemCount(),
            "orderDate", order.getOrderDate().toString(),
            "canCancel", order.canCancel()
        );
    }

    private Map<String, Object> toOrderDetailResponse(Order order) {
        List<Map<String, Object>> items = order.getItems().stream()
            .map(this::toOrderItemResponse)
            .toList();

        return Map.ofEntries(
            Map.entry("orderId", order.getOrderId()),
            Map.entry("orderNumber", order.getOrderNumber()),
            Map.entry("status", order.getStatus().name()),
            Map.entry("statusDisplay", order.getStatus().getDisplayName()),
            Map.entry("statusDescription", order.getStatus().getDescription()),
            Map.entry("paymentMethod", order.getPaymentMethod() != null ?
                order.getPaymentMethod().name() : "ONLINE"),
            Map.entry("shippingAddress", order.getShippingAddress() != null ?
                order.getShippingAddress() : ""),
            Map.entry("shippingPhone", order.getShippingPhone() != null ?
                order.getShippingPhone() : ""),
            Map.entry("shippingNotes", order.getShippingNotes() != null ?
                order.getShippingNotes() : ""),
            Map.entry("subtotal", order.getSubtotal().toString()),
            Map.entry("shippingFee", order.getShippingFee().toString()),
            Map.entry("discountAmount", order.getDiscountAmount().toString()),
            Map.entry("taxAmount", order.getTaxAmount().toString()),
            Map.entry("totalAmount", order.getTotalAmount().toString()),
            Map.entry("items", items),
            Map.entry("itemCount", order.getTotalItemCount()),
            Map.entry("orderDate", order.getOrderDate().toString()),
            Map.entry("canCancel", order.canCancel()),
            Map.entry("isComplete", order.isComplete())
        );
    }

    private Map<String, Object> toOrderItemResponse(OrderItem item) {
        return Map.of(
            "productCode", item.getProductCodeString(),
            "productName", item.getProductName(),
            "quantity", item.getQuantity(),
            "unitPrice", item.getUnitPrice().toString(),
            "lineTotal", item.getLineTotal().toString()
        );
    }

    // ==================== Request DTOs ====================

    public static class CreateOrderRequest {
        public String shippingAddress;
        public String shippingPhone;
        public String shippingNotes;
        public String paymentMethod;
    }

    public static class CancelOrderRequest {
        public String reason;
    }

    public static class UpdateShippingRequest {
        public String shippingAddress;
        public String shippingPhone;
    }
}
