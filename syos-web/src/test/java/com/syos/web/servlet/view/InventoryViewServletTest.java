package com.syos.web.servlet.view;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.InventoryService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ReportService reportService;

    private InventoryViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new InventoryViewServlet();

        // Inject mock services via reflection
        java.lang.reflect.Field invField = InventoryViewServlet.class.getDeclaredField("inventoryService");
        invField.setAccessible(true);
        invField.set(servlet, inventoryService);

        java.lang.reflect.Field repField = InventoryViewServlet.class.getDeclaredField("reportService");
        repField.setAccessible(true);
        repField.set(servlet, reportService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private MainInventory createTestBatch(Integer id, String productCode, int quantity) {
        MainInventory batch = new MainInventory();
        batch.setMainInventoryId(id);
        batch.setProductCode(new ProductCode(productCode));
        batch.setQuantityReceived(quantity);
        batch.setRemainingQuantity(quantity);
        batch.setPurchaseDate(LocalDate.now());
        batch.setExpiryDate(LocalDate.now().plusDays(30));
        return batch;
    }

    @Nested
    @DisplayName("doGet tests - List Batches")
    class DoGetListBatchesTests {

        @Test
        @DisplayName("Should list batches with null path")
        void shouldListBatchesWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("page")).thenReturn(null);
            when(request.getParameter("size")).thenReturn(null);
            when(request.getParameter("filter")).thenReturn(null);

            MainInventory batch = createTestBatch(1, "P001", 100);
            when(inventoryService.findAll(0, 20)).thenReturn(List.of(batch));
            when(inventoryService.getBatchCount()).thenReturn(1L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findAll(0, 20);
            verify(request).setAttribute(eq("batches"), any());
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should list batches with root path")
        void shouldListBatchesWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("page")).thenReturn("1");
            when(request.getParameter("size")).thenReturn("10");
            when(request.getParameter("filter")).thenReturn(null);

            when(inventoryService.findAll(1, 10)).thenReturn(List.of());
            when(inventoryService.getBatchCount()).thenReturn(50L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findAll(1, 10);
            verify(request).setAttribute("currentPage", 1);
            verify(request).setAttribute("pageSize", 10);
        }

        @Test
        @DisplayName("Should filter expiring batches")
        void shouldFilterExpiringBatches() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("filter")).thenReturn("expiring");
            when(request.getParameter("days")).thenReturn("14");

            when(inventoryService.findExpiringWithinDays(14)).thenReturn(List.of());
            when(inventoryService.getBatchCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findExpiringWithinDays(14);
        }

        @Test
        @DisplayName("Should filter expired batches")
        void shouldFilterExpiredBatches() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("filter")).thenReturn("expired");

            when(inventoryService.findExpiredBatches()).thenReturn(List.of());
            when(inventoryService.getBatchCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findExpiredBatches();
        }
    }

    @Nested
    @DisplayName("doGet tests - Add Form")
    class DoGetAddFormTests {

        @Test
        @DisplayName("Should show add form")
        void shouldShowAddForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/add");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/add.jsp");
            verify(requestDispatcher).forward(request, response);
        }
    }

    @Nested
    @DisplayName("doGet tests - Expiring and Expired")
    class DoGetExpiringExpiredTests {

        @Test
        @DisplayName("Should show expiring batches page")
        void shouldShowExpiringBatchesPage() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/expiring");
            when(request.getParameter("days")).thenReturn("7");

            MainInventory batch = createTestBatch(1, "P001", 50);
            when(inventoryService.findExpiringWithinDays(7)).thenReturn(List.of(batch));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findExpiringWithinDays(7);
            verify(request).setAttribute("days", 7);
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/expiring.jsp");
        }

        @Test
        @DisplayName("Should show expired batches page")
        void shouldShowExpiredBatchesPage() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/expired");

            when(inventoryService.findExpiredBatches()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findExpiredBatches();
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/expired.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - View Batch")
    class DoGetViewBatchTests {

        @Test
        @DisplayName("Should view batch details")
        void shouldViewBatchDetails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/123");

            MainInventory batch = createTestBatch(123, "P001", 100);
            when(inventoryService.findBatchById(123)).thenReturn(Optional.of(batch));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findBatchById(123);
            verify(request).setAttribute("batch", batch);
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/view.jsp");
        }

        @Test
        @DisplayName("Should handle batch not found")
        void shouldHandleBatchNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/999");
            when(inventoryService.findBatchById(999)).thenReturn(Optional.empty());
            when(inventoryService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(inventoryService.getBatchCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
        }

        @Test
        @DisplayName("Should handle invalid batch ID")
        void shouldHandleInvalidBatchId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/abc");
            when(inventoryService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(inventoryService.getBatchCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), eq("Invalid batch ID"));
        }
    }

    @Nested
    @DisplayName("doGet tests - Product Batches")
    class DoGetProductBatchesTests {

        @Test
        @DisplayName("Should show product batches")
        void shouldShowProductBatches() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/product/P001");

            MainInventory batch = createTestBatch(1, "P001", 100);
            when(inventoryService.findBatchesByProductCode("P001")).thenReturn(List.of(batch));
            when(inventoryService.getTotalRemainingQuantity("P001")).thenReturn(100);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(inventoryService).findBatchesByProductCode("P001");
            verify(inventoryService).getTotalRemainingQuantity("P001");
            verify(request).setAttribute("productCode", "P001");
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/product-batches.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Reports Dashboard")
    class DoGetReportsDashboardTests {

        @Test
        @DisplayName("Should show reports dashboard")
        void shouldShowReportsDashboard() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/reports");

            when(reportService.getReshelveReport(StoreType.PHYSICAL)).thenReturn(List.of());
            when(reportService.getReshelveReport(StoreType.ONLINE)).thenReturn(List.of());
            when(reportService.getReorderLevelReport(70)).thenReturn(List.of());
            when(reportService.getBatchStockReport()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(reportService).getReshelveReport(StoreType.PHYSICAL);
            verify(reportService).getReshelveReport(StoreType.ONLINE);
            verify(reportService).getReorderLevelReport(70);
            verify(request).getRequestDispatcher("/WEB-INF/views/inventory/reports.jsp");
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
    }
}
