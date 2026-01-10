package com.syos.web.servlet.view;

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
import java.util.List;

/**
 * Servlet for report views.
 */
@WebServlet(urlPatterns = { "/reports", "/reports/*" })
public class ReportViewServlet extends BaseViewServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        super.init();
        reportService = ServiceRegistry.get(ReportService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showReportsHome(request, response);
            } else if (pathInfo.equals("/sales")) {
                showSalesReport(request, response);
            } else if (pathInfo.equals("/top-products")) {
                showTopProductsReport(request, response);
            } else if (pathInfo.equals("/reshelve")) {
                showReshelveReport(request, response);
            } else if (pathInfo.equals("/reorder-level")) {
                showReorderLevelReport(request, response);
            } else if (pathInfo.equals("/batch-stock")) {
                showBatchStockReport(request, response);
            } else if (pathInfo.equals("/bills")) {
                showBillReport(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            showReportsHome(request, response);
        }
    }

    private void showBillReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateStr = getStringParameter(request, "date", "");
        String storeTypeStr = getStringParameter(request, "storeType", "PHYSICAL");

        LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
        StoreType storeType = StoreType.valueOf(storeTypeStr);

        BillReport billReport = reportService.getBillReport(date, storeType);

        request.setAttribute("billReport", billReport);
        request.setAttribute("selectedDate", date);
        request.setAttribute("selectedStoreType", storeType);

        setActiveNav(request, "reports");
        render(request, response, "reports/bills.jsp");
    }

    private void showReportsHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Dashboard summary for quick overview
        DashboardSummary summary = reportService.getDashboardSummary();
        request.setAttribute("summary", summary);

        setActiveNav(request, "reports");
        render(request, response, "reports/index.jsp");
    }

    private void showSalesReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateStr = getStringParameter(request, "date", "");
        String storeTypeStr = getStringParameter(request, "storeType", "ALL");

        // Default to today if no date provided
        LocalDate selectedDate = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

        // Determine store type filter
        StoreType storeTypeFilter = null;
        if (!"ALL".equals(storeTypeStr)) {
            storeTypeFilter = StoreType.valueOf(storeTypeStr);
        }

        // Sales summary for the selected date
        SalesSummary salesSummary;
        List<ProductSalesReport> productSales;

        if (storeTypeFilter != null) {
            // Filtered by store type
            salesSummary = reportService.getSalesSummaryByStoreType(selectedDate, storeTypeFilter);
            productSales = reportService.getTopSellingProductsByStoreType(selectedDate, selectedDate, 100,
                    storeTypeFilter);
        } else {
            // All stores combined
            salesSummary = reportService.getSalesSummary(selectedDate);
            productSales = reportService.getTopSellingProducts(selectedDate, selectedDate, 100);
        }

        request.setAttribute("salesSummary", salesSummary);
        request.setAttribute("productSales", productSales);

        // Sales by store type for the selected date (always show both for comparison)
        List<StoreTypeSalesReport> storeTypeReport = reportService.getSalesByStoreType(selectedDate, selectedDate);
        request.setAttribute("storeTypeReport", storeTypeReport);

        // Top 5 selling products
        List<ProductSalesReport> topProducts = productSales.size() > 5 ? productSales.subList(0, 5) : productSales;
        request.setAttribute("topProducts", topProducts);

        // Calculate totals
        int totalQuantitySold = productSales.stream().mapToInt(ProductSalesReport::totalQuantitySold).sum();
        request.setAttribute("totalQuantitySold", totalQuantitySold);

        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedStoreType", storeTypeStr);

        setActiveNav(request, "reports");
        render(request, response, "reports/sales.jsp");
    }

    private void showTopProductsReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String startDateStr = getStringParameter(request, "startDate", "");
        String endDateStr = getStringParameter(request, "endDate", "");
        int limit = getIntParameter(request, "limit", 20);

        LocalDate endDate = endDateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(endDateStr);
        LocalDate startDate = startDateStr.isEmpty() ? endDate.minusDays(30) : LocalDate.parse(startDateStr);

        List<ProductSalesReport> topProducts = reportService.getTopSellingProducts(startDate, endDate, limit);
        request.setAttribute("topProducts", topProducts);

        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("limit", limit);

        setActiveNav(request, "reports");
        render(request, response, "reports/top-products.jsp");
    }

    private void showReshelveReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String storeTypeStr = getStringParameter(request, "storeType", "PHYSICAL");
        StoreType storeType = StoreType.valueOf(storeTypeStr);

        List<ReshelveReport> reshelveItems = reportService.getReshelveReport(storeType);
        request.setAttribute("reshelveItems", reshelveItems);

        // Calculate totals
        int totalItems = reshelveItems.size();
        int totalQuantityToReshelve = reshelveItems.stream()
                .mapToInt(ReshelveReport::quantityToReshelve)
                .sum();
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalQuantityToReshelve", totalQuantityToReshelve);

        request.setAttribute("storeType", storeType);

        setActiveNav(request, "reports");
        render(request, response, "reports/reshelve.jsp");
    }

    private void showReorderLevelReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int threshold = getIntParameter(request, "threshold", 70);

        List<ReorderLevelReport> reorderItems = reportService.getReorderLevelReport(threshold);
        request.setAttribute("reorderItems", reorderItems);

        // Calculate totals
        int totalItems = reorderItems.size();
        int totalQuantityToReorder = reorderItems.stream()
                .mapToInt(ReorderLevelReport::quantityToReorder)
                .sum();
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalQuantityToReorder", totalQuantityToReorder);
        request.setAttribute("threshold", threshold);

        setActiveNav(request, "reports");
        render(request, response, "reports/reorder-level.jsp");
    }

    private void showBatchStockReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<BatchStockReport> batchItems = reportService.getBatchStockReport();
        request.setAttribute("batchItems", batchItems);

        // Calculate totals
        int totalBatches = batchItems.size();
        int totalOriginal = batchItems.stream().mapToInt(BatchStockReport::originalQuantity).sum();
        int totalRemaining = batchItems.stream().mapToInt(BatchStockReport::remainingInMain).sum();
        int totalPhysical = batchItems.stream().mapToInt(BatchStockReport::quantityInPhysical).sum();
        int totalOnline = batchItems.stream().mapToInt(BatchStockReport::quantityInOnline).sum();

        request.setAttribute("totalBatches", totalBatches);
        request.setAttribute("totalOriginal", totalOriginal);
        request.setAttribute("totalRemaining", totalRemaining);
        request.setAttribute("totalPhysical", totalPhysical);
        request.setAttribute("totalOnline", totalOnline);

        setActiveNav(request, "reports");
        render(request, response, "reports/batch-stock.jsp");
    }
}
