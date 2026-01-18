package com.syos.web.servlet.view;

import com.syos.domain.enums.StoreType;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.*;
import jakarta.servlet.RequestDispatcher;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ReportService reportService;

    private ReportViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReportViewServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = ReportViewServlet.class.getDeclaredField("reportService");
        field.setAccessible(true);
        field.set(servlet, reportService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
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

    private SalesSummary createTestSalesSummary(LocalDate date) {
        return new SalesSummary(
                date,
                date,
                10,
                BigDecimal.valueOf(1000.00),
                BigDecimal.valueOf(100.00),
                50);
    }

    @Nested
    @DisplayName("doGet tests - Reports Home")
    class DoGetReportsHomeTests {

        @Test
        @DisplayName("Should show reports home with null path")
        void shouldShowReportsHomeWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDashboardSummary();
            verify(request).setAttribute(eq("summary"), any());
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/index.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should show reports home with root path")
        void shouldShowReportsHomeWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDashboardSummary();
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/index.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Sales Report")
    class DoGetSalesReportTests {

        @Test
        @DisplayName("Should show sales report for today")
        void shouldShowSalesReportForToday() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales");
            when(request.getParameter("date")).thenReturn(null);
            when(request.getParameter("storeType")).thenReturn(null);

            LocalDate today = LocalDate.now();
            when(reportService.getSalesSummary(today)).thenReturn(createTestSalesSummary(today));
            when(reportService.getTopSellingProducts(any(), any(), eq(100))).thenReturn(List.of());
            when(reportService.getSalesByStoreType(any(), any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesSummary(today);
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/sales.jsp");
        }

        @Test
        @DisplayName("Should show sales report for specific date")
        void shouldShowSalesReportForSpecificDate() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales");
            when(request.getParameter("date")).thenReturn("2026-01-15");
            when(request.getParameter("storeType")).thenReturn("ALL");

            LocalDate date = LocalDate.parse("2026-01-15");
            when(reportService.getSalesSummary(date)).thenReturn(createTestSalesSummary(date));
            when(reportService.getTopSellingProducts(any(), any(), eq(100))).thenReturn(List.of());
            when(reportService.getSalesByStoreType(any(), any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesSummary(date);
            verify(request).setAttribute("selectedDate", date);
        }

        @Test
        @DisplayName("Should filter sales report by store type")
        void shouldFilterSalesReportByStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales");
            when(request.getParameter("date")).thenReturn("2026-01-15");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            LocalDate date = LocalDate.parse("2026-01-15");
            when(reportService.getSalesSummaryByStoreType(date, StoreType.PHYSICAL))
                    .thenReturn(createTestSalesSummary(date));
            when(reportService.getTopSellingProductsByStoreType(any(), any(), eq(100), eq(StoreType.PHYSICAL)))
                    .thenReturn(List.of());
            when(reportService.getSalesByStoreType(any(), any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getSalesSummaryByStoreType(date, StoreType.PHYSICAL);
        }
    }

    @Nested
    @DisplayName("doGet tests - Bill Report")
    class DoGetBillReportTests {

        @Test
        @DisplayName("Should show bill report for today")
        void shouldShowBillReportForToday() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/bills");
            when(request.getParameter("date")).thenReturn(null);
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            LocalDate today = LocalDate.now();
            BillReport billReport = new BillReport(today, StoreType.PHYSICAL, 5, BigDecimal.valueOf(500), List.of());
            when(reportService.getBillReport(today, StoreType.PHYSICAL)).thenReturn(billReport);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getBillReport(today, StoreType.PHYSICAL);
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/bills.jsp");
        }

        @Test
        @DisplayName("Should show bill report for specific date")
        void shouldShowBillReportForSpecificDate() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/bills");
            when(request.getParameter("date")).thenReturn("2026-01-10");
            when(request.getParameter("storeType")).thenReturn("ONLINE");

            LocalDate date = LocalDate.parse("2026-01-10");
            BillReport billReport = new BillReport(date, StoreType.ONLINE, 3, BigDecimal.valueOf(300), List.of());
            when(reportService.getBillReport(date, StoreType.ONLINE)).thenReturn(billReport);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getBillReport(date, StoreType.ONLINE);
            verify(request).setAttribute("selectedDate", date);
            verify(request).setAttribute("selectedStoreType", StoreType.ONLINE);
        }
    }

    @Nested
    @DisplayName("doGet tests - Reshelve Report")
    class DoGetReshelveReportTests {

        @Test
        @DisplayName("Should show reshelve report for physical store")
        void shouldShowReshelveReportForPhysicalStore() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/reshelve");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            List<ReshelveReport> items = List.of(
                    new ReshelveReport("P001", "Product 1", 5, 20, 15));
            when(reportService.getReshelveReport(StoreType.PHYSICAL)).thenReturn(items);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getReshelveReport(StoreType.PHYSICAL);
            verify(request).setAttribute("reshelveItems", items);
            verify(request).setAttribute("totalItems", 1);
            verify(request).setAttribute("totalQuantityToReshelve", 15);
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/reshelve.jsp");
        }

        @Test
        @DisplayName("Should show reshelve report for online store")
        void shouldShowReshelveReportForOnlineStore() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/reshelve");
            when(request.getParameter("storeType")).thenReturn("ONLINE");

            when(reportService.getReshelveReport(StoreType.ONLINE)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getReshelveReport(StoreType.ONLINE);
        }
    }

    @Nested
    @DisplayName("doGet tests - Reorder Level Report")
    class DoGetReorderLevelReportTests {

        @Test
        @DisplayName("Should show reorder level report with default threshold")
        void shouldShowReorderLevelReportWithDefaultThreshold() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/reorder-level");
            when(request.getParameter("threshold")).thenReturn(null);

            List<ReorderLevelReport> items = List.of(
                    new ReorderLevelReport("P001", "Product 1", 50, 70, 100));
            when(reportService.getReorderLevelReport(70)).thenReturn(items);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getReorderLevelReport(70);
            verify(request).setAttribute("reorderItems", items);
            verify(request).setAttribute("threshold", 70);
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/reorder-level.jsp");
        }

        @Test
        @DisplayName("Should show reorder level report with custom threshold")
        void shouldShowReorderLevelReportWithCustomThreshold() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/reorder-level");
            when(request.getParameter("threshold")).thenReturn("50");

            when(reportService.getReorderLevelReport(50)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getReorderLevelReport(50);
            verify(request).setAttribute("threshold", 50);
        }
    }

    @Nested
    @DisplayName("doGet tests - Batch Stock Report")
    class DoGetBatchStockReportTests {

        @Test
        @DisplayName("Should show batch stock report")
        void shouldShowBatchStockReport() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/batch-stock");

            List<BatchStockReport> items = List.of(
                    new BatchStockReport("P001", "Product 1", 1, LocalDate.now(), LocalDate.now().plusDays(30),
                            100, 80, 15, 5));
            when(reportService.getBatchStockReport()).thenReturn(items);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getBatchStockReport();
            verify(request).setAttribute("batchItems", items);
            verify(request).setAttribute("totalBatches", 1);
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/batch-stock.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Error Handling")
    class DoGetErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for unknown path")
        void shouldReturn404ForUnknownPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should handle exception and show home")
        void shouldHandleExceptionAndShowHome() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/sales");
            when(request.getParameter("date")).thenReturn("invalid-date");

            // This will cause a DateTimeParseException
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
            // Should fall back to home view
            verify(request).getRequestDispatcher("/WEB-INF/views/reports/index.jsp");
        }
    }
}
