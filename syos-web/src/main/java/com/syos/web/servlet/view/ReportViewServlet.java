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
@WebServlet(urlPatterns = {"/reports", "/reports/*"})
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
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            showReportsHome(request, response);
        }
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
        String startDateStr = getStringParameter(request, "startDate", "");
        String endDateStr = getStringParameter(request, "endDate", "");

        LocalDate endDate = endDateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(endDateStr);
        LocalDate startDate = startDateStr.isEmpty() ? endDate.minusDays(30) : LocalDate.parse(startDateStr);

        // Daily sales report
        List<DailySalesReport> dailyReport = reportService.getDailySalesReport(startDate, endDate);
        request.setAttribute("dailyReport", dailyReport);

        // Sales by store type
        List<StoreTypeSalesReport> storeTypeReport = reportService.getSalesByStoreType(startDate, endDate);
        request.setAttribute("storeTypeReport", storeTypeReport);

        // Summary
        SalesSummary salesSummary = reportService.getSalesSummaryForRange(startDate, endDate);
        request.setAttribute("salesSummary", salesSummary);

        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);

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
