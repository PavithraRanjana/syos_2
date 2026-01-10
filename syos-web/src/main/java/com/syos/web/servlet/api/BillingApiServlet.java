package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.BillingService.ValidationResult;
import com.syos.web.dto.response.BillResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API servlet for Billing/POS operations.
 *
 * Endpoints:
 * POST /api/billing - Create new bill
 * GET /api/billing/{id} - Get bill by ID
 * GET /api/billing/serial/{serialNumber} - Get bill by serial number
 * GET /api/billing/today - Get today's bills summary
 * GET /api/billing/recent - Get recent bills
 * GET /api/billing/date/{date} - Get bills by date
 * GET /api/billing/customer/{customerId} - Get bills by customer
 *
 * POST /api/billing/{id}/items - Add item to bill
 * PUT /api/billing/{id}/items/{itemId} - Update item quantity
 * DELETE /api/billing/{id}/items/{itemId} - Remove item from bill
 * DELETE /api/billing/{id}/items - Clear all items
 *
 * POST /api/billing/{id}/discount - Apply discount
 * POST /api/billing/{id}/payment/cash - Process cash payment
 * POST /api/billing/{id}/payment/online - Process online payment
 * POST /api/billing/{id}/finalize - Finalize bill
 * POST /api/billing/{id}/cancel - Cancel bill
 * GET /api/billing/{id}/validate - Validate bill for finalization
 */
@WebServlet(urlPatterns = { "/api/billing", "/api/billing/*" })
public class BillingApiServlet extends BaseApiServlet {

    private BillingService billingService;

    @Override
    public void init() throws ServletException {
        super.init();
        billingService = ServiceRegistry.get(BillingService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/billing - Not allowed without specific path
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Specify a bill ID or use /today, /recent, /date/{date}, /customer/{id}");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");

            if (parts[0].equals("today")) {
                handleGetTodaySummary(response);
            } else if (parts[0].equals("recent")) {
                int limit = getIntParameter(request, "limit", 20);
                handleGetRecentBills(limit, response);
            } else if (parts[0].equals("date") && parts.length > 1) {
                handleGetBillsByDate(parts[1], response);
            } else if (parts[0].equals("serial") && parts.length > 1) {
                handleGetBySerialNumber(parts[1], response);
            } else if (parts[0].equals("customer") && parts.length > 1) {
                handleGetByCustomer(parts[1], response);
            } else if (parts[0].equals("stock") && parts.length > 1) {
                // GET /api/billing/stock/{productCode}?quantity=X&storeType=PHYSICAL
                handleStockCheck(parts[1], request, response);
            } else {
                // GET /api/billing/{id}
                handleGetBill(parts[0], response);
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

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/billing - Create new bill
                handleCreateBill(request, response);
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");

            // Handle /checkout endpoint (no bill ID needed)
            if (parts[0].equals("checkout")) {
                handleCheckout(request, response);
                return;
            }

            String billIdStr = parts[0];

            if (parts.length == 1) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
                return;
            }

            Integer billId = parseIntOrNull(billIdStr);
            if (billId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bill ID");
                return;
            }

            switch (parts[1]) {
                case "items" -> handleAddItem(billId, request, response);
                case "discount" -> handleApplyDiscount(billId, request, response);
                case "payment" -> {
                    if (parts.length > 2) {
                        if (parts[2].equals("cash")) {
                            handleCashPayment(billId, request, response);
                        } else if (parts[2].equals("online")) {
                            handleOnlinePayment(billId, response);
                        } else {
                            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                    "Unknown payment type: use 'cash' or 'online'");
                        }
                    } else {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Specify payment type: /payment/cash or /payment/online");
                    }
                }
                case "finalize" -> handleFinalize(billId, response);
                case "cancel" -> handleCancel(billId, response);
                default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length < 3 || !parts[1].equals("items")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Use PUT /api/billing/{billId}/items/{itemId}");
                return;
            }

            Integer billId = parseIntOrNull(parts[0]);
            Integer itemId = parseIntOrNull(parts[2]);

            if (billId == null || itemId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bill or item ID");
                return;
            }

            handleUpdateItem(billId, itemId, request, response);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length < 2 || !parts[1].equals("items")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Use DELETE /api/billing/{billId}/items or /api/billing/{billId}/items/{itemId}");
                return;
            }

            Integer billId = parseIntOrNull(parts[0]);
            if (billId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bill ID");
                return;
            }

            if (parts.length == 2) {
                // DELETE /api/billing/{id}/items - Clear all items
                handleClearItems(billId, response);
            } else {
                // DELETE /api/billing/{id}/items/{itemId}
                Integer itemId = parseIntOrNull(parts[2]);
                if (itemId == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID");
                    return;
                }
                handleRemoveItem(billId, itemId, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    // ==================== GET Handlers ====================

    private void handleGetBill(String billIdStr, HttpServletResponse response) throws IOException {
        Integer billId = parseIntOrNull(billIdStr);
        if (billId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bill ID");
            return;
        }

        Optional<Bill> bill = billingService.findBillById(billId);
        if (bill.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Bill not found: " + billId);
            return;
        }

        List<BillItem> items = billingService.getBillItems(billId);
        BillDetailResponse billResponse = BillDetailResponse.fromBill(bill.get(), items);
        sendSuccess(response, billResponse);
    }

    private void handleGetBySerialNumber(String serialNumber, HttpServletResponse response)
            throws IOException {
        Optional<Bill> bill = billingService.findBillBySerialNumber(serialNumber);
        if (bill.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "Bill not found: " + serialNumber);
            return;
        }

        List<BillItem> items = billingService.getBillItems(bill.get().getBillId());
        BillDetailResponse billResponse = BillDetailResponse.fromBill(bill.get(), items);
        sendSuccess(response, billResponse);
    }

    private void handleGetTodaySummary(HttpServletResponse response) throws IOException {
        BigDecimal totalSales = billingService.getTodaysSales();
        int billCount = billingService.getTodaysBillCount();
        List<Bill> todayBills = billingService.findBillsByDate(LocalDate.now());

        List<BillSummaryResponse> summaries = todayBills.stream()
                .map(BillSummaryResponse::fromBill)
                .toList();

        sendSuccess(response, Map.of(
                "date", LocalDate.now().toString(),
                "totalSales", totalSales,
                "billCount", billCount,
                "bills", summaries));
    }

    private void handleGetRecentBills(int limit, HttpServletResponse response) throws IOException {
        List<Bill> bills = billingService.findRecentBills(limit);
        List<BillSummaryResponse> summaries = bills.stream()
                .map(BillSummaryResponse::fromBill)
                .toList();

        sendSuccess(response, Map.of(
                "bills", summaries,
                "count", summaries.size()));
    }

    private void handleGetBillsByDate(String dateStr, HttpServletResponse response)
            throws IOException {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<Bill> bills = billingService.findBillsByDate(date);
            List<BillSummaryResponse> summaries = bills.stream()
                    .map(BillSummaryResponse::fromBill)
                    .toList();

            sendSuccess(response, Map.of(
                    "date", dateStr,
                    "bills", summaries,
                    "count", summaries.size()));
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid date format. Use YYYY-MM-DD");
        }
    }

    private void handleGetByCustomer(String customerIdStr, HttpServletResponse response)
            throws IOException {
        Integer customerId = parseIntOrNull(customerIdStr);
        if (customerId == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid customer ID");
            return;
        }

        List<Bill> bills = billingService.findBillsByCustomer(customerId);
        List<BillSummaryResponse> summaries = bills.stream()
                .map(BillSummaryResponse::fromBill)
                .toList();

        sendSuccess(response, Map.of(
                "customerId", customerId,
                "bills", summaries,
                "count", summaries.size()));
    }

    private void handleStockCheck(String productCode, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        int quantity = getIntParameter(request, "quantity", 1);
        String storeTypeStr = request.getParameter("storeType");

        StoreType storeType;
        try {
            storeType = storeTypeStr != null ? StoreType.valueOf(storeTypeStr.toUpperCase()) : StoreType.PHYSICAL;
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid store type. Use 'PHYSICAL' or 'ONLINE'");
            return;
        }

        BillingService.StockCheckResult result = billingService.checkStock(productCode, quantity, storeType);

        if (result.available()) {
            sendSuccess(response, Map.of(
                    "available", true,
                    "productCode", result.productCode(),
                    "productName", result.productName(),
                    "unitPrice", result.unitPrice(),
                    "requestedQuantity", result.requestedQuantity(),
                    "availableQuantity", result.availableQuantity()));
        } else {
            // Return error but with details
            sendError(response, HttpServletResponse.SC_OK, result.message());
        }
    }

    // ==================== POST Handlers ====================

    private void handleCheckout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        CheckoutApiRequest checkoutRequest = parseRequestBody(request, CheckoutApiRequest.class);
        if (checkoutRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        // Parse enums
        StoreType storeType;
        try {
            storeType = StoreType.valueOf(checkoutRequest.storeType.toUpperCase());
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid store type. Use 'PHYSICAL' or 'ONLINE'");
            return;
        }

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(checkoutRequest.transactionType.toUpperCase());
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid transaction type. Use 'CASH' or 'CREDIT'");
            return;
        }

        // Convert API items to service request items
        List<BillingService.ItemRequest> items = new ArrayList<>();
        if (checkoutRequest.items != null) {
            for (CheckoutItemRequest item : checkoutRequest.items) {
                items.add(new BillingService.ItemRequest(item.productCode, item.quantity));
            }
        }

        // Build checkout request
        BillingService.CheckoutRequest serviceRequest = new BillingService.CheckoutRequest(
                storeType,
                transactionType,
                checkoutRequest.customerId,
                checkoutRequest.cashierId,
                items,
                checkoutRequest.discount,
                checkoutRequest.cashTendered);

        // Execute checkout
        BillingService.CheckoutResult result = billingService.checkout(serviceRequest);

        if (result.success()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            sendSuccess(response, Map.of(
                    "billId", result.billId(),
                    "serialNumber", result.serialNumber(),
                    "subtotal", result.subtotal(),
                    "discount", result.discount(),
                    "tax", result.tax(),
                    "total", result.total(),
                    "cashTendered", result.cashTendered(),
                    "change", result.change(),
                    "billDate", result.billDate().toString(),
                    "items", result.items().stream().map(i -> Map.of(
                            "productName", i.productName(),
                            "quantity", i.quantity(),
                            "unitPrice", i.unitPrice(),
                            "lineTotal", i.lineTotal())).toList()),
                    "Checkout successful");
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    String.join("; ", result.errors()));
        }
    }

    private void handleCreateBill(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        CreateBillRequest billRequest = parseRequestBody(request, CreateBillRequest.class);
        if (billRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        StoreType storeType;
        try {
            storeType = StoreType.valueOf(billRequest.storeType.toUpperCase());
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid store type. Use 'PHYSICAL' or 'ONLINE'");
            return;
        }

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(billRequest.transactionType.toUpperCase());
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid transaction type. Use 'CASH' or 'CREDIT'");
            return;
        }

        Bill bill = billingService.createBill(
                storeType, transactionType, billRequest.customerId, billRequest.cashierId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, BillSummaryResponse.fromBill(bill), "Bill created successfully");
    }

    private void handleAddItem(Integer billId, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        AddItemRequest itemRequest = parseRequestBody(request, AddItemRequest.class);
        if (itemRequest == null || itemRequest.productCode == null || itemRequest.quantity == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "productCode and quantity are required");
            return;
        }

        BillItem item = billingService.addItem(billId, itemRequest.productCode, itemRequest.quantity);

        // Get updated bill
        Optional<Bill> bill = billingService.findBillById(billId);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, Map.of(
                "item", BillItemResponse.fromItem(item),
                "bill", BillDetailResponse.fromBill(bill.get(), items)), "Item added successfully");
    }

    private void handleApplyDiscount(Integer billId, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        DiscountRequest discountRequest = parseRequestBody(request, DiscountRequest.class);
        if (discountRequest == null || discountRequest.amount == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Discount amount is required");
            return;
        }

        Bill bill = billingService.applyDiscount(billId, discountRequest.amount);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, BillDetailResponse.fromBill(bill, items), "Discount applied");
    }

    private void handleCashPayment(Integer billId, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        CashPaymentRequest paymentRequest = parseRequestBody(request, CashPaymentRequest.class);
        if (paymentRequest == null || paymentRequest.tenderedAmount == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Tendered amount is required");
            return;
        }

        Bill bill = billingService.processCashPayment(billId, paymentRequest.tenderedAmount);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, BillDetailResponse.fromBill(bill, items), "Payment processed");
    }

    private void handleOnlinePayment(Integer billId, HttpServletResponse response)
            throws IOException {
        Bill bill = billingService.processOnlinePayment(billId);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, BillDetailResponse.fromBill(bill, items), "Online payment processed");
    }

    private void handleFinalize(Integer billId, HttpServletResponse response) throws IOException {
        // First validate
        ValidationResult validation = billingService.validateBillForFinalization(billId);
        if (!validation.isValid()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Validation failed: " + String.join(", ", validation.errors()));
            return;
        }

        Bill bill = billingService.finalizeBill(billId);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, BillDetailResponse.fromBill(bill, items), "Bill finalized successfully");
    }

    private void handleCancel(Integer billId, HttpServletResponse response) throws IOException {
        boolean cancelled = billingService.cancelBill(billId);
        if (cancelled) {
            sendSuccess(response, Map.of("billId", billId, "cancelled", true),
                    "Bill cancelled successfully");
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to cancel bill");
        }
    }

    // ==================== PUT/DELETE Handlers ====================

    private void handleUpdateItem(Integer billId, Integer itemId, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        UpdateItemRequest updateRequest = parseRequestBody(request, UpdateItemRequest.class);
        if (updateRequest == null || updateRequest.quantity == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Quantity is required");
            return;
        }

        BillItem item = billingService.updateItemQuantity(itemId, updateRequest.quantity);

        // Get updated bill
        Optional<Bill> bill = billingService.findBillById(billId);
        List<BillItem> items = billingService.getBillItems(billId);

        sendSuccess(response, Map.of(
                "item", BillItemResponse.fromItem(item),
                "bill", BillDetailResponse.fromBill(bill.get(), items)), "Item updated");
    }

    private void handleRemoveItem(Integer billId, Integer itemId, HttpServletResponse response)
            throws IOException {
        boolean removed = billingService.removeItem(itemId);
        if (removed) {
            Optional<Bill> bill = billingService.findBillById(billId);
            List<BillItem> items = billingService.getBillItems(billId);

            sendSuccess(response, BillDetailResponse.fromBill(bill.get(), items),
                    "Item removed successfully");
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to remove item");
        }
    }

    private void handleClearItems(Integer billId, HttpServletResponse response) throws IOException {
        billingService.clearItems(billId);

        Optional<Bill> bill = billingService.findBillById(billId);
        sendSuccess(response, BillDetailResponse.fromBill(bill.get(), List.of()),
                "All items cleared");
    }

    // ==================== Helper Methods ====================

    private Integer parseIntOrNull(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== Request/Response DTOs ====================

    public static class CreateBillRequest {
        public String storeType; // "PHYSICAL" or "ONLINE"
        public String transactionType; // "CASH" or "CREDIT"
        public Integer customerId; // Required for ONLINE
        public String cashierId; // Optional
    }

    public static class AddItemRequest {
        public String productCode;
        public Integer quantity;
    }

    public static class UpdateItemRequest {
        public Integer quantity;
    }

    public static class DiscountRequest {
        public BigDecimal amount;
    }

    public static class CashPaymentRequest {
        public BigDecimal tenderedAmount;
    }

    // Checkout API DTOs (for atomic bill creation)
    public static class CheckoutApiRequest {
        public String storeType; // "PHYSICAL" or "ONLINE"
        public String transactionType; // "CASH" or "CREDIT"
        public Integer customerId; // Required for ONLINE
        public String cashierId; // Optional
        public List<CheckoutItemRequest> items;
        public BigDecimal discount; // Optional discount amount
        public BigDecimal cashTendered; // Required for CASH transactions
    }

    public static class CheckoutItemRequest {
        public String productCode;
        public int quantity;
    }

    public record BillSummaryResponse(
            Integer billId,
            String serialNumber,
            String storeType,
            String transactionType,
            LocalDateTime billDate,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total,
            Integer customerId) {
        public static BillSummaryResponse fromBill(Bill bill) {
            return new BillSummaryResponse(
                    bill.getBillId(),
                    bill.getSerialNumberString(),
                    bill.getStoreType() != null ? bill.getStoreType().name() : null,
                    bill.getTransactionType() != null ? bill.getTransactionType().name() : null,
                    bill.getBillDate(),
                    bill.getSubtotal() != null ? bill.getSubtotal().getAmount() : BigDecimal.ZERO,
                    bill.getDiscountAmount() != null ? bill.getDiscountAmount().getAmount() : BigDecimal.ZERO,
                    bill.getTaxAmount() != null ? bill.getTaxAmount().getAmount() : BigDecimal.ZERO,
                    bill.getTotalAmount() != null ? bill.getTotalAmount().getAmount() : BigDecimal.ZERO,
                    bill.getCustomerId());
        }
    }

    public record BillDetailResponse(
            Integer billId,
            String serialNumber,
            String storeType,
            String transactionType,
            LocalDateTime billDate,
            Integer customerId,
            String cashierId,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total,
            BigDecimal tenderedAmount,
            BigDecimal changeAmount,
            List<BillItemResponse> items,
            int itemCount) {
        public static BillDetailResponse fromBill(Bill bill, List<BillItem> items) {
            List<BillItemResponse> itemResponses = items.stream()
                    .map(BillItemResponse::fromItem)
                    .toList();

            return new BillDetailResponse(
                    bill.getBillId(),
                    bill.getSerialNumberString(),
                    bill.getStoreType() != null ? bill.getStoreType().name() : null,
                    bill.getTransactionType() != null ? bill.getTransactionType().name() : null,
                    bill.getBillDate(),
                    bill.getCustomerId(),
                    bill.getCashierId(),
                    bill.getSubtotal() != null ? bill.getSubtotal().getAmount() : BigDecimal.ZERO,
                    bill.getDiscountAmount() != null ? bill.getDiscountAmount().getAmount() : BigDecimal.ZERO,
                    bill.getTaxAmount() != null ? bill.getTaxAmount().getAmount() : BigDecimal.ZERO,
                    bill.getTotalAmount() != null ? bill.getTotalAmount().getAmount() : BigDecimal.ZERO,
                    bill.getTenderedAmount() != null ? bill.getTenderedAmount().getAmount() : null,
                    bill.getChangeAmount() != null ? bill.getChangeAmount().getAmount() : null,
                    itemResponses,
                    itemResponses.size());
        }
    }

    public record BillItemResponse(
            Integer billItemId,
            String productCode,
            String productName,
            Integer batchId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
        public static BillItemResponse fromItem(BillItem item) {
            return new BillItemResponse(
                    item.getBillItemId(),
                    item.getProductCodeString(),
                    item.getProductName(),
                    item.getMainInventoryId(),
                    item.getQuantity(),
                    item.getUnitPrice() != null ? item.getUnitPrice().getAmount() : null,
                    item.getLineTotal() != null ? item.getLineTotal().getAmount() : null);
        }
    }
}
