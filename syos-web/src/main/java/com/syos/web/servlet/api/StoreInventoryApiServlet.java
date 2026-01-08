package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.StoreInventoryService.RestockResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for Store Inventory operations (Physical and Online stores).
 *
 * Endpoints:
 * GET  /api/store-inventory/physical              - Physical store stock summary
 * GET  /api/store-inventory/physical/{code}       - Physical store stock for product
 * GET  /api/store-inventory/physical/low-stock    - Physical store low stock products
 * POST /api/store-inventory/physical/restock      - Restock physical store
 *
 * GET  /api/store-inventory/online                - Online store stock summary
 * GET  /api/store-inventory/online/{code}         - Online store stock for product
 * GET  /api/store-inventory/online/low-stock      - Online store low stock products
 * POST /api/store-inventory/online/restock        - Restock online store
 */
@WebServlet(urlPatterns = {"/api/store-inventory/*"})
public class StoreInventoryApiServlet extends BaseApiServlet {

    private StoreInventoryService storeInventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        storeInventoryService = ServiceRegistry.get(StoreInventoryService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Specify /physical or /online");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            String storeTypeStr = parts[0];

            if (!storeTypeStr.equals("physical") && !storeTypeStr.equals("online")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid store type. Use 'physical' or 'online'");
                return;
            }

            StoreType storeType = storeTypeStr.equals("physical") ?
                StoreType.PHYSICAL : StoreType.ONLINE;

            if (parts.length == 1) {
                // GET /api/store-inventory/{storeType}
                handleGetStockSummary(storeType, response);
            } else if (parts[1].equals("low-stock")) {
                // GET /api/store-inventory/{storeType}/low-stock
                int threshold = getIntParameter(request, "threshold", 10);
                handleGetLowStock(storeType, threshold, response);
            } else {
                // GET /api/store-inventory/{storeType}/{productCode}
                String productCode = parts[1];
                handleGetProductStock(storeType, productCode, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length < 2 || !parts[1].equals("restock")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Use POST /api/store-inventory/{storeType}/restock");
                return;
            }

            String storeTypeStr = parts[0];
            StoreType storeType = storeTypeStr.equals("physical") ?
                StoreType.PHYSICAL : StoreType.ONLINE;

            handleRestock(storeType, request, response);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleGetStockSummary(StoreType storeType, HttpServletResponse response)
            throws IOException {
        List<ProductStockSummary> summaries;
        if (storeType == StoreType.PHYSICAL) {
            summaries = storeInventoryService.getPhysicalStoreStockSummary();
        } else {
            summaries = storeInventoryService.getOnlineStoreStockSummary();
        }

        List<StockSummaryResponse> responses = summaries.stream()
            .map(s -> new StockSummaryResponse(
                s.productCode(),
                s.productName(),
                s.totalQuantity(),
                s.batchCount()
            ))
            .toList();

        sendSuccess(response, Map.of(
            "storeType", storeType.name(),
            "summary", responses,
            "count", responses.size()
        ));
    }

    private void handleGetProductStock(StoreType storeType, String productCode,
                                        HttpServletResponse response) throws IOException {
        int quantity = storeInventoryService.getAvailableQuantity(productCode, storeType);

        if (storeType == StoreType.PHYSICAL) {
            List<PhysicalStoreInventory> stock =
                storeInventoryService.getPhysicalStoreStock(productCode);
            List<StoreStockResponse> responses = stock.stream()
                .map(s -> new StoreStockResponse(
                    s.getMainInventoryId(),
                    s.getProductCodeString(),
                    s.getQuantityOnShelf(),
                    s.getExpiryDate(),
                    s.getRestockedDate()
                ))
                .toList();

            sendSuccess(response, Map.of(
                "productCode", productCode,
                "storeType", storeType.name(),
                "totalQuantity", quantity,
                "batches", responses
            ));
        } else {
            List<OnlineStoreInventory> stock =
                storeInventoryService.getOnlineStoreStock(productCode);
            List<StoreStockResponse> responses = stock.stream()
                .map(s -> new StoreStockResponse(
                    s.getMainInventoryId(),
                    s.getProductCodeString(),
                    s.getQuantityAvailable(),
                    s.getExpiryDate(),
                    s.getRestockedDate()
                ))
                .toList();

            sendSuccess(response, Map.of(
                "productCode", productCode,
                "storeType", storeType.name(),
                "totalQuantity", quantity,
                "batches", responses
            ));
        }
    }

    private void handleGetLowStock(StoreType storeType, int threshold,
                                    HttpServletResponse response) throws IOException {
        if (storeType == StoreType.PHYSICAL) {
            List<PhysicalStoreInventory> lowStock =
                storeInventoryService.getPhysicalStoreLowStock(threshold);
            List<LowStockResponse> responses = lowStock.stream()
                .map(s -> new LowStockResponse(
                    s.getProductCodeString(),
                    s.getProductName(),
                    s.getQuantityOnShelf()
                ))
                .toList();

            sendSuccess(response, Map.of(
                "storeType", storeType.name(),
                "threshold", threshold,
                "products", responses,
                "count", responses.size()
            ));
        } else {
            List<OnlineStoreInventory> lowStock =
                storeInventoryService.getOnlineStoreLowStock(threshold);
            List<LowStockResponse> responses = lowStock.stream()
                .map(s -> new LowStockResponse(
                    s.getProductCodeString(),
                    s.getProductName(),
                    s.getQuantityAvailable()
                ))
                .toList();

            sendSuccess(response, Map.of(
                "storeType", storeType.name(),
                "threshold", threshold,
                "products", responses,
                "count", responses.size()
            ));
        }
    }

    private void handleRestock(StoreType storeType, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        RestockRequest restockRequest = parseRequestBody(request, RestockRequest.class);
        if (restockRequest == null || restockRequest.productCode == null ||
            restockRequest.quantity == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "productCode and quantity are required");
            return;
        }

        RestockResult result;
        if (restockRequest.batchId != null) {
            // Restock from specific batch
            boolean success;
            if (storeType == StoreType.PHYSICAL) {
                success = storeInventoryService.restockPhysicalStoreFromBatch(
                    restockRequest.productCode, restockRequest.batchId, restockRequest.quantity);
            } else {
                success = storeInventoryService.restockOnlineStoreFromBatch(
                    restockRequest.productCode, restockRequest.batchId, restockRequest.quantity);
            }
            result = success ?
                RestockResult.success(restockRequest.quantity, 1) :
                RestockResult.failure("Failed to restock from batch");
        } else {
            // Auto restock using FIFO
            if (storeType == StoreType.PHYSICAL) {
                result = storeInventoryService.restockPhysicalStore(
                    restockRequest.productCode, restockRequest.quantity);
            } else {
                result = storeInventoryService.restockOnlineStore(
                    restockRequest.productCode, restockRequest.quantity);
            }
        }

        if (result.success()) {
            sendSuccess(response, Map.of(
                "success", true,
                "productCode", restockRequest.productCode,
                "storeType", storeType.name(),
                "quantityRestocked", result.quantityRestocked(),
                "batchesUsed", result.batchesUsed(),
                "message", result.message()
            ), result.message());
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, result.message());
        }
    }

    // ==================== Response/Request DTOs ====================

    public record StockSummaryResponse(
        String productCode,
        String productName,
        int totalQuantity,
        int batchCount
    ) {}

    public record StoreStockResponse(
        Integer batchId,
        String productCode,
        int quantity,
        LocalDate expiryDate,
        LocalDate restockedDate
    ) {}

    public record LowStockResponse(
        String productCode,
        String productName,
        int currentQuantity
    ) {}

    public static class RestockRequest {
        public String productCode;
        public Integer quantity;
        public Integer batchId; // Optional - if provided, restock from specific batch
    }
}
