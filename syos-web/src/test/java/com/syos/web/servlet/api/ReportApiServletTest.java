package com.syos.web.servlet.api;

import com.syos.domain.enums.StoreType;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ReportService reportService;

    private ReportApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReportApiServlet();
        // Inject mock ReportService via reflection
        java.lang.reflect.Field field = ReportApiServlet.class.getDeclaredField("reportService");
        field.setAccessible(true);
        field.set(servlet, reportService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    private DashboardSummary createTestDashboardSummary() {
        return new DashboardSummary(
                BigDecimal.valueOf(1000.00),
                10,
                5,
                2,
                BigDecimal.valueOf(5000.00),
                BigDecimal.valueOf(20000.00),
                List.of());
    }

    @Nested
    @DisplayName("doGet tests - Dashboard")
    class DoGetDashboardTests {

        @Test
        @DisplayName("Should return dashboard summary")
        void shouldReturnDashboardSummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/dashboard");
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDashboardSummary();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("todaySales");
            assert output.contains("lowStockProductCount");
        }

        @Test
        @DisplayName("Should return 400 for null path")
        void shouldReturn400ForNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for root path")
        void shouldReturn400ForRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for unknown report type")
        void shouldReturn404ForUnknownReportType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doGet tests - Sales Reports")
    class DoGetSalesReportsTests {

        @Test
        @DisplayName("Should return 400 when sales sub-report not specified")
        void shouldReturn400WhenSalesSubReportNotSpecified() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return daily sales report")
        void shouldReturnDailySalesReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/daily");
            when(request.getParameter("startDate")).thenReturn("2026-01-01");
            when(request.getParameter("endDate")).thenReturn("2026-01-19");

            List<DailySalesReport> reports = List.of(
                    new DailySalesReport(LocalDate.parse("2026-01-19"), 5, BigDecimal.valueOf(500.00),
                            BigDecimal.valueOf(300.00), BigDecimal.valueOf(200.00)));
            when(reportService.getDailySalesReport(any(), any())).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDailySalesReport(any(), any());
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("reports");
        }

        @Test
        @DisplayName("Should use default dates when not provided")
        void shouldUseDefaultDatesWhenNotProvided() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/daily");
            when(request.getParameter("startDate")).thenReturn(null);
            when(request.getParameter("endDate")).thenReturn(null);

            when(reportService.getDailySalesReport(any(), any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDailySalesReport(any(), any());
        }

        @Test
        @DisplayName("Should return sales by store type")
        void shouldReturnSalesByStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/by-store-type");
            when(request.getParameter("startDate")).thenReturn("2026-01-01");
            when(request.getParameter("endDate")).thenReturn("2026-01-19");

            List<StoreTypeSalesReport> reports = List.of(
                    new StoreTypeSalesReport(StoreType.PHYSICAL, 10, BigDecimal.valueOf(1000.00)));
            when(reportService.getSalesByStoreType(any(), any())).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesByStoreType(any(), any());
        }

        @Test
        @DisplayName("Should return top products")
        void shouldReturnTopProducts() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/top-products");
            when(request.getParameter("startDate")).thenReturn("2026-01-01");
            when(request.getParameter("endDate")).thenReturn("2026-01-19");
            when(request.getParameter("limit")).thenReturn("5");

            List<ProductSalesReport> reports = List.of(
                    new ProductSalesReport("P001", "Product 1", 100, BigDecimal.valueOf(2500.00)));
            when(reportService.getTopSellingProducts(any(), any(), eq(5))).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getTopSellingProducts(any(), any(), eq(5));
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("products");
        }

        @Test
        @DisplayName("Should return sales summary for specific date")
        void shouldReturnSalesSummaryForSpecificDate() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/summary");
            when(request.getParameter("date")).thenReturn("2026-01-19");

            SalesSummary summary = new SalesSummary(
                    LocalDate.parse("2026-01-19"),
                    LocalDate.parse("2026-01-19"),
                    10,
                    BigDecimal.valueOf(1000.00),
                    BigDecimal.valueOf(100.00),
                    50);
            when(reportService.getSalesSummary(any())).thenReturn(summary);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesSummary(any());
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("totalBills");
        }

        @Test
        @DisplayName("Should return sales summary for date range")
        void shouldReturnSalesSummaryForDateRange() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/summary");
            when(request.getParameter("date")).thenReturn(null);
            when(request.getParameter("startDate")).thenReturn("2026-01-01");
            when(request.getParameter("endDate")).thenReturn("2026-01-19");

            SalesSummary summary = new SalesSummary(
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-01-19"),
                    100,
                    BigDecimal.valueOf(10000.00),
                    BigDecimal.valueOf(100.00),
                    500);
            when(reportService.getSalesSummaryForRange(any(), any())).thenReturn(summary);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesSummaryForRange(any(), any());
        }

        @Test
        @DisplayName("Should return 404 for unknown sales report")
        void shouldReturn404ForUnknownSalesReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doGet tests - Inventory Reports")
    class DoGetInventoryReportsTests {

        @Test
        @DisplayName("Should return 400 when inventory sub-report not specified")
        void shouldReturn400WhenInventorySubReportNotSpecified() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return stock levels for all stores")
        void shouldReturnStockLevelsForAllStores() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/stock-levels");
            when(request.getParameter("storeType")).thenReturn(null);

            List<StockLevelReport> reports = List.of(
                    new StockLevelReport("P001", "Product 1", 100, 2, LocalDate.now().plusDays(30)));
            when(reportService.getCurrentStockLevels(null)).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getCurrentStockLevels(null);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("products");
        }

        @Test
        @DisplayName("Should return stock levels for specific store type")
        void shouldReturnStockLevelsForSpecificStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/stock-levels");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            List<StockLevelReport> reports = List.of(
                    new StockLevelReport("P001", "Product 1", 50, 1, LocalDate.now().plusDays(15)));
            when(reportService.getCurrentStockLevels(StoreType.PHYSICAL)).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getCurrentStockLevels(StoreType.PHYSICAL);
        }

        @Test
        @DisplayName("Should return low stock report")
        void shouldReturnLowStockReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/low-stock");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");
            when(request.getParameter("threshold")).thenReturn("20");

            List<LowStockReport> reports = List.of(
                    new LowStockReport("P001", "Product 1", 5, 45));
            when(reportService.getLowStockReport(StoreType.PHYSICAL, 20)).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getLowStockReport(StoreType.PHYSICAL, 20);
        }

        @Test
        @DisplayName("Should use default threshold for low stock")
        void shouldUseDefaultThresholdForLowStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/low-stock");
            when(request.getParameter("storeType")).thenReturn(null);
            when(request.getParameter("threshold")).thenReturn(null);

            when(reportService.getLowStockReport(null, 10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getLowStockReport(null, 10);
        }

        @Test
        @DisplayName("Should return expiring stock report")
        void shouldReturnExpiringStockReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/expiring");
            when(request.getParameter("days")).thenReturn("14");

            List<ExpiringStockReport> reports = List.of(
                    new ExpiringStockReport("P001", "Product 1", 1, 100, LocalDate.now().plusDays(10), 10));
            when(reportService.getExpiringStockReport(14)).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getExpiringStockReport(14);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("daysUntilExpiry");
        }

        @Test
        @DisplayName("Should use default days for expiring stock")
        void shouldUseDefaultDaysForExpiringStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/expiring");
            when(request.getParameter("days")).thenReturn(null);

            when(reportService.getExpiringStockReport(7)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getExpiringStockReport(7);
        }

        @Test
        @DisplayName("Should return expired stock report")
        void shouldReturnExpiredStockReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/expired");

            List<ExpiringStockReport> reports = List.of(
                    new ExpiringStockReport("P001", "Product 1", 1, 50, LocalDate.now().minusDays(5), -5));
            when(reportService.getExpiredStockReport()).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getExpiredStockReport();
        }

        @Test
        @DisplayName("Should return restock recommendations")
        void shouldReturnRestockRecommendations() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/restock");
            when(request.getParameter("storeType")).thenReturn("ONLINE");
            when(request.getParameter("salesDays")).thenReturn("60");

            List<RestockRecommendation> reports = List.of(
                    new RestockRecommendation("P001", "Product 1", 10, 5, 2, 50));
            when(reportService.getRestockRecommendations(StoreType.ONLINE, 60)).thenReturn(reports);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getRestockRecommendations(StoreType.ONLINE, 60);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("recommendations");
        }

        @Test
        @DisplayName("Should use default values for restock recommendations")
        void shouldUseDefaultValuesForRestockRecommendations() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/restock");
            when(request.getParameter("storeType")).thenReturn(null);
            when(request.getParameter("salesDays")).thenReturn(null);

            when(reportService.getRestockRecommendations(null, 30)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getRestockRecommendations(null, 30);
        }

        @Test
        @DisplayName("Should return 404 for unknown inventory report")
        void shouldReturn404ForUnknownInventoryReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should handle invalid store type gracefully")
        void shouldHandleInvalidStoreTypeGracefully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/inventory/stock-levels");
            when(request.getParameter("storeType")).thenReturn("INVALID");

            when(reportService.getCurrentStockLevels(null)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert - Should fall back to null (all stores)
            verify(reportService).getCurrentStockLevels(null);
        }
    }
}
