package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.MainInventory;
import com.syos.service.interfaces.InventoryService;
import com.syos.service.interfaces.InventoryService.ProductInventorySummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API servlet for Main Inventory (batch) operations.
 *
 * Endpoints:
 * GET  /api/inventory                    - List all batches with pagination
 * GET  /api/inventory/summary            - Get inventory summary by product
 * GET  /api/inventory/expiring           - Get batches expiring soon
 * GET  /api/inventory/expired            - Get expired batches
 * GET  /api/inventory/{batchId}          - Get batch by ID
 * GET  /api/inventory/product/{code}     - Get batches for a product
 * POST /api/inventory                    - Add new batch
 */
@WebServlet(urlPatterns = {"/api/inventory", "/api/inventory/*"})
public class InventoryApiServlet extends BaseApiServlet {

    private InventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        inventoryService = ServiceRegistry.get(InventoryService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                handleListBatches(request, response);
            } else if (pathInfo.equals("/summary")) {
                handleGetSummary(response);
            } else if (pathInfo.equals("/expiring")) {
                handleGetExpiring(request, response);
            } else if (pathInfo.equals("/expired")) {
                handleGetExpired(response);
            } else if (pathInfo.startsWith("/product/")) {
                String productCode = pathInfo.substring("/product/".length());
                handleGetByProductCode(productCode, request, response);
            } else {
                // GET /api/inventory/{batchId}
                String batchIdStr = getPathPart(request, 0);
                handleGetBatch(batchIdStr, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleAddBatch(request, response);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleListBatches(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 50);

        List<MainInventory> batches = inventoryService.findAll(page, size);
        List<BatchResponse> responses = batches.stream()
            .map(BatchResponse::fromBatch)
            .toList();

        sendSuccess(response, Map.of(
            "batches", responses,
            "count", responses.size(),
            "page", page,
            "size", size,
            "totalBatches", inventoryService.getBatchCount()
        ));
    }

    private void handleGetSummary(HttpServletResponse response) throws IOException {
        List<ProductInventorySummary> summaries = inventoryService.getInventorySummary();
        sendSuccess(response, Map.of(
            "summary", summaries,
            "count", summaries.size()
        ));
    }

    private void handleGetExpiring(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int days = getIntParameter(request, "days", 7);
        List<MainInventory> batches = inventoryService.findExpiringWithinDays(days);
        List<BatchResponse> responses = batches.stream()
            .map(BatchResponse::fromBatch)
            .toList();

        sendSuccess(response, Map.of(
            "batches", responses,
            "count", responses.size(),
            "daysUntilExpiry", days
        ));
    }

    private void handleGetExpired(HttpServletResponse response) throws IOException {
        List<MainInventory> batches = inventoryService.findExpiredBatches();
        List<BatchResponse> responses = batches.stream()
            .map(BatchResponse::fromBatch)
            .toList();

        sendSuccess(response, Map.of(
            "batches", responses,
            "count", responses.size()
        ));
    }

    private void handleGetByProductCode(String productCode, HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        String availableOnly = request.getParameter("available");

        List<MainInventory> batches;
        if ("true".equalsIgnoreCase(availableOnly)) {
            batches = inventoryService.findAvailableBatches(productCode);
        } else {
            batches = inventoryService.findBatchesByProductCode(productCode);
        }

        List<BatchResponse> responses = batches.stream()
            .map(BatchResponse::fromBatch)
            .toList();

        int totalQuantity = inventoryService.getTotalRemainingQuantity(productCode);

        sendSuccess(response, Map.of(
            "productCode", productCode,
            "batches", responses,
            "batchCount", responses.size(),
            "totalQuantity", totalQuantity
        ));
    }

    private void handleGetBatch(String batchIdStr, HttpServletResponse response)
            throws IOException {
        try {
            Integer batchId = Integer.parseInt(batchIdStr);
            Optional<MainInventory> batch = inventoryService.findBatchById(batchId);

            if (batch.isEmpty()) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "Batch not found: " + batchId);
                return;
            }

            sendSuccess(response, BatchResponse.fromBatch(batch.get()));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid batch ID");
        }
    }

    private void handleAddBatch(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        AddBatchRequest batchRequest = parseRequestBody(request, AddBatchRequest.class);
        if (batchRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        if (batchRequest.productCode == null || batchRequest.quantity == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "productCode and quantity are required");
            return;
        }

        MainInventory batch = inventoryService.addBatch(
            batchRequest.productCode,
            batchRequest.quantity,
            batchRequest.purchasePrice,
            batchRequest.purchaseDate,
            batchRequest.expiryDate,
            batchRequest.supplierName
        );

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, BatchResponse.fromBatch(batch), "Batch added successfully");
    }

    // ==================== Response/Request DTOs ====================

    public static class BatchResponse {
        public Integer batchId;
        public String productCode;
        public String productName;
        public int quantityReceived;
        public int remainingQuantity;
        public BigDecimal purchasePrice;
        public LocalDate purchaseDate;
        public LocalDate expiryDate;
        public String supplierName;

        public static BatchResponse fromBatch(MainInventory batch) {
            BatchResponse response = new BatchResponse();
            response.batchId = batch.getMainInventoryId();
            response.productCode = batch.getProductCodeString();
            response.productName = batch.getProductName();
            response.quantityReceived = batch.getQuantityReceived();
            response.remainingQuantity = batch.getRemainingQuantity();
            response.purchasePrice = batch.getPurchasePrice() != null ?
                batch.getPurchasePrice().getAmount() : null;
            response.purchaseDate = batch.getPurchaseDate();
            response.expiryDate = batch.getExpiryDate();
            response.supplierName = batch.getSupplierName();
            return response;
        }
    }

    public static class AddBatchRequest {
        public String productCode;
        public Integer quantity;
        public BigDecimal purchasePrice;
        public LocalDate purchaseDate;
        public LocalDate expiryDate;
        public String supplierName;
    }
}
