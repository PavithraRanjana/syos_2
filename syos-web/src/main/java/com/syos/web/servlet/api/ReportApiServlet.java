package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.StoreType;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for Reports.
 *
 * Endpoints:
 * GET /api/reports/dashboard                  - Dashboard summary
 * GET /api/reports/sales/daily                - Daily sales report
 * GET /api/reports/sales/by-store-type        - Sales by store type
 * GET /api/reports/sales/top-products         - Top selling products
 * GET /api/reports/sales/summary              - Sales summary for date/range
 *
 * GET /api/reports/inventory/stock-levels     - Current stock levels
 * GET /api/reports/inventory/low-stock        - Low stock products
 * GET /api/reports/inventory/expiring         - Expiring products
 * GET /api/reports/inventory/expired          - Expired products
 * GET /api/reports/inventory/restock          - Restock recommendations
 */
@WebServlet(urlPatterns = {"/api/reports/*"})
public class ReportApiServlet extends BaseApiServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        super.init();
        reportService = ServiceRegistry.get(ReportService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Specify a report type: /dashboard, /sales/*, /inventory/*");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");

            if (parts[0].equals("dashboard")) {
                handleDashboard(response);
            } else if (parts[0].equals("sales")) {
                handleSalesReport(parts, request, response);
            } else if (parts[0].equals("inventory")) {
                handleInventoryReport(parts, request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "Unknown report type: " + parts[0]);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    // ==================== Dashboard ====================

    private void handleDashboard(HttpServletResponse response) throws IOException {
        DashboardSummary summary = reportService.getDashboardSummary();

        sendSuccess(response, Map.of(
            "todaySales", summary.todaySales(),
            "todayBillCount", summary.todayBillCount(),
            "lowStockProductCount", summary.lowStockProductCount(),
            "expiringProductCount", summary.expiringProductCount(),
            "weekSales", summary.weekSales(),
            "monthSales", summary.monthSales(),
            "topProducts", summary.topProducts()
        ));
    }

    // ==================== Sales Reports ====================

    private void handleSalesReport(String[] parts, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        if (parts.length < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Specify report: /sales/daily, /sales/by-store-type, /sales/top-products, /sales/summary");
            return;
        }

        LocalDate startDate = parseDateParam(request, "startDate", LocalDate.now().minusDays(30));
        LocalDate endDate = parseDateParam(request, "endDate", LocalDate.now());

        switch (parts[1]) {
            case "daily" -> handleDailySales(startDate, endDate, response);
            case "by-store-type" -> handleSalesByStoreType(startDate, endDate, response);
            case "top-products" -> handleTopProducts(startDate, endDate, request, response);
            case "summary" -> handleSalesSummary(startDate, endDate, request, response);
            default -> sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "Unknown sales report: " + parts[1]);
        }
    }

    private void handleDailySales(LocalDate startDate, LocalDate endDate,
                                   HttpServletResponse response) throws IOException {
        List<DailySalesReport> report = reportService.getDailySalesReport(startDate, endDate);

        sendSuccess(response, Map.of(
            "startDate", startDate.toString(),
            "endDate", endDate.toString(),
            "reports", report,
            "count", report.size()
        ));
    }

    private void handleSalesByStoreType(LocalDate startDate, LocalDate endDate,
                                         HttpServletResponse response) throws IOException {
        List<StoreTypeSalesReport> report = reportService.getSalesByStoreType(startDate, endDate);

        sendSuccess(response, Map.of(
            "startDate", startDate.toString(),
            "endDate", endDate.toString(),
            "reports", report
        ));
    }

    private void handleTopProducts(LocalDate startDate, LocalDate endDate,
                                    HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int limit = getIntParameter(request, "limit", 10);
        List<ProductSalesReport> report = reportService.getTopSellingProducts(startDate, endDate, limit);

        sendSuccess(response, Map.of(
            "startDate", startDate.toString(),
            "endDate", endDate.toString(),
            "limit", limit,
            "products", report,
            "count", report.size()
        ));
    }

    private void handleSalesSummary(LocalDate startDate, LocalDate endDate,
                                     HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String dateParam = request.getParameter("date");

        SalesSummary summary;
        if (dateParam != null) {
            // Single date summary
            LocalDate date = LocalDate.parse(dateParam);
            summary = reportService.getSalesSummary(date);
        } else {
            // Date range summary
            summary = reportService.getSalesSummaryForRange(startDate, endDate);
        }

        sendSuccess(response, Map.of(
            "startDate", summary.startDate().toString(),
            "endDate", summary.endDate().toString(),
            "totalBills", summary.totalBills(),
            "totalSales", summary.totalSales(),
            "averageBillValue", summary.averageBillValue(),
            "totalItemsSold", summary.totalItemsSold()
        ));
    }

    // ==================== Inventory Reports ====================

    private void handleInventoryReport(String[] parts, HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        if (parts.length < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Specify report: /inventory/stock-levels, /inventory/low-stock, " +
                "/inventory/expiring, /inventory/expired, /inventory/restock");
            return;
        }

        switch (parts[1]) {
            case "stock-levels" -> handleStockLevels(request, response);
            case "low-stock" -> handleLowStock(request, response);
            case "expiring" -> handleExpiringStock(request, response);
            case "expired" -> handleExpiredStock(response);
            case "restock" -> handleRestockRecommendations(request, response);
            default -> sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "Unknown inventory report: " + parts[1]);
        }
    }

    private void handleStockLevels(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        StoreType storeType = parseStoreType(request);

        List<StockLevelReport> report = reportService.getCurrentStockLevels(storeType);

        sendSuccess(response, Map.of(
            "storeType", storeType != null ? storeType.name() : "ALL",
            "products", report,
            "count", report.size()
        ));
    }

    private void handleLowStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        StoreType storeType = parseStoreType(request);
        int threshold = getIntParameter(request, "threshold", 10);

        List<LowStockReport> report = reportService.getLowStockReport(storeType, threshold);

        sendSuccess(response, Map.of(
            "storeType", storeType != null ? storeType.name() : "ALL",
            "threshold", threshold,
            "products", report,
            "count", report.size()
        ));
    }

    private void handleExpiringStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int days = getIntParameter(request, "days", 7);

        List<ExpiringStockReport> report = reportService.getExpiringStockReport(days);

        sendSuccess(response, Map.of(
            "daysUntilExpiry", days,
            "products", report,
            "count", report.size()
        ));
    }

    private void handleExpiredStock(HttpServletResponse response) throws IOException {
        List<ExpiringStockReport> report = reportService.getExpiredStockReport();

        sendSuccess(response, Map.of(
            "products", report,
            "count", report.size()
        ));
    }

    private void handleRestockRecommendations(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        StoreType storeType = parseStoreType(request);
        int daysOfSalesData = getIntParameter(request, "salesDays", 30);

        List<RestockRecommendation> report =
            reportService.getRestockRecommendations(storeType, daysOfSalesData);

        sendSuccess(response, Map.of(
            "storeType", storeType != null ? storeType.name() : "ALL",
            "salesDaysAnalyzed", daysOfSalesData,
            "recommendations", report,
            "count", report.size()
        ));
    }

    // ==================== Helper Methods ====================

    private LocalDate parseDateParam(HttpServletRequest request, String paramName, LocalDate defaultValue) {
        String dateStr = request.getParameter(paramName);
        if (dateStr == null || dateStr.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }

    private StoreType parseStoreType(HttpServletRequest request) {
        String storeTypeStr = request.getParameter("storeType");
        if (storeTypeStr == null || storeTypeStr.isEmpty()) {
            return null; // All stores
        }
        try {
            return StoreType.valueOf(storeTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
