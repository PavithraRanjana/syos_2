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
            } else if (pathInfo.equals("/inventory")) {
                showInventoryReport(request, response);
            } else if (pathInfo.equals("/top-products")) {
                showTopProductsReport(request, response);
            } else if (pathInfo.equals("/restock")) {
                showRestockRecommendations(request, response);
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

    private void showInventoryReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String storeTypeStr = getStringParameter(request, "storeType", "PHYSICAL");
        StoreType storeType = StoreType.valueOf(storeTypeStr);

        // Current stock levels
        List<StockLevelReport> stockLevels = reportService.getCurrentStockLevels(storeType);
        request.setAttribute("stockLevels", stockLevels);

        // Low stock
        int threshold = getIntParameter(request, "threshold", 10);
        List<LowStockReport> lowStock = reportService.getLowStockReport(storeType, threshold);
        request.setAttribute("lowStock", lowStock);

        // Expiring soon
        int days = getIntParameter(request, "days", 7);
        List<ExpiringStockReport> expiring = reportService.getExpiringStockReport(days);
        request.setAttribute("expiring", expiring);

        // Expired
        List<ExpiringStockReport> expired = reportService.getExpiredStockReport();
        request.setAttribute("expired", expired);

        request.setAttribute("storeType", storeType);
        request.setAttribute("threshold", threshold);
        request.setAttribute("days", days);

        setActiveNav(request, "reports");
        render(request, response, "reports/inventory.jsp");
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

    private void showRestockRecommendations(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String storeTypeStr = getStringParameter(request, "storeType", "PHYSICAL");
        StoreType storeType = StoreType.valueOf(storeTypeStr);
        int salesDays = getIntParameter(request, "salesDays", 30);

        List<RestockRecommendation> recommendations = reportService.getRestockRecommendations(storeType, salesDays);
        request.setAttribute("recommendations", recommendations);

        request.setAttribute("storeType", storeType);
        request.setAttribute("salesDays", salesDays);

        setActiveNav(request, "reports");
        render(request, response, "reports/restock.jsp");
    }
}
