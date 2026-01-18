package com.syos.web.servlet.view;

import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.DashboardSummary;
import com.syos.service.interfaces.ReportService.ProductSalesReport;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
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
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private RequestDispatcher defaultDispatcher;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ReportService reportService;

    private DashboardServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = DashboardServlet.class.getDeclaredField("reportService");
        field.setAccessible(true);
        field.set(servlet, reportService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getNamedDispatcher("default")).thenReturn(defaultDispatcher);
    }

    private DashboardSummary createTestDashboardSummary() {
        ProductSalesReport topProduct = new ProductSalesReport("P001", "Product 1", 100, BigDecimal.valueOf(1000));
        return new DashboardSummary(
                BigDecimal.valueOf(5000.00),
                50,
                5,
                3,
                BigDecimal.valueOf(25000.00),
                BigDecimal.valueOf(100000.00),
                List.of(topProduct));
    }

    @Nested
    @DisplayName("doGet tests - Dashboard")
    class DoGetDashboardTests {

        @Test
        @DisplayName("Should show dashboard for root path")
        void shouldShowDashboardForRootPath() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/");
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDashboardSummary();
            verify(request).setAttribute("todaySales", BigDecimal.valueOf(5000.00));
            verify(request).setAttribute("todayBillCount", 50);
            verify(request).setAttribute("lowStockCount", 5);
            verify(request).setAttribute("expiringCount", 3);
            verify(request).getRequestDispatcher("/WEB-INF/views/dashboard/index.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should show dashboard for /dashboard path")
        void shouldShowDashboardForDashboardPath() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/dashboard");
            when(reportService.getDashboardSummary()).thenReturn(createTestDashboardSummary());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getDashboardSummary();
            verify(request).getRequestDispatcher("/WEB-INF/views/dashboard/index.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Static Resources")
    class DoGetStaticResourcesTests {

        @Test
        @DisplayName("Should forward non-dashboard paths to default servlet")
        void shouldForwardNonDashboardPathsToDefaultServlet() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/static/images/logo.png");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(servletContext).getNamedDispatcher("default");
            verify(defaultDispatcher).forward(request, response);
        }
    }

    @Nested
    @DisplayName("doGet tests - Error Handling")
    class DoGetErrorHandlingTests {

        @Test
        @DisplayName("Should handle exception and show error message")
        void shouldHandleExceptionAndShowErrorMessage() throws Exception {
            // Arrange
            when(request.getServletPath()).thenReturn("/");
            when(reportService.getDashboardSummary()).thenThrow(new RuntimeException("Database error"));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), contains("Database error"));
            verify(request).getRequestDispatcher("/WEB-INF/views/dashboard/index.jsp");
        }
    }
}
