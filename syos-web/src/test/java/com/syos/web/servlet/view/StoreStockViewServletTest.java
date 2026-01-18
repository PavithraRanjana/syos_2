package com.syos.web.servlet.view;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.StoreInventoryService;
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

import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreStockViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoreStockViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private StoreInventoryService storeInventoryService;

    private StoreStockViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new StoreStockViewServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = StoreStockViewServlet.class.getDeclaredField("storeInventoryService");
        field.setAccessible(true);
        field.set(servlet, storeInventoryService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private PhysicalStoreInventory createPhysicalInventory(String productCode, int quantity) {
        PhysicalStoreInventory inv = new PhysicalStoreInventory();
        inv.setMainInventoryId(1);
        inv.setProductCode(new ProductCode(productCode));
        inv.setQuantityOnShelf(quantity);
        inv.setExpiryDate(LocalDate.now().plusDays(30));
        return inv;
    }

    private OnlineStoreInventory createOnlineInventory(String productCode, int quantity) {
        OnlineStoreInventory inv = new OnlineStoreInventory();
        inv.setMainInventoryId(1);
        inv.setProductCode(new ProductCode(productCode));
        inv.setQuantityAvailable(quantity);
        inv.setExpiryDate(LocalDate.now().plusDays(30));
        return inv;
    }

    @Nested
    @DisplayName("doGet tests - Stock Overview")
    class DoGetStockOverviewTests {

        @Test
        @DisplayName("Should show stock overview with null path")
        void shouldShowStockOverviewWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("filter")).thenReturn(null);

            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreStockSummary()).thenReturn(List.of());
            when(storeInventoryService.getPhysicalStoreLowStock(10)).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreStockSummary();
            verify(storeInventoryService).getOnlineStoreStockSummary();
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/index.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should show stock overview with root path")
        void shouldShowStockOverviewWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("filter")).thenReturn("");

            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreStockSummary()).thenReturn(List.of());
            when(storeInventoryService.getPhysicalStoreLowStock(10)).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/index.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Physical Stock")
    class DoGetPhysicalStockTests {

        @Test
        @DisplayName("Should show physical stock summary")
        void shouldShowPhysicalStockSummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical");
            when(request.getParameter("product")).thenReturn(null);

            ProductStockSummary summary = new ProductStockSummary("P001", "Product 1", 100, 2);
            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of(summary));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreStockSummary();
            verify(request).setAttribute("storeType", "PHYSICAL");
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/physical.jsp");
        }

        @Test
        @DisplayName("Should show physical stock for specific product")
        void shouldShowPhysicalStockForSpecificProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical");
            when(request.getParameter("product")).thenReturn("P001");

            PhysicalStoreInventory inv = createPhysicalInventory("P001", 50);
            when(storeInventoryService.getPhysicalStoreStock("P001")).thenReturn(List.of(inv));
            when(storeInventoryService.getAvailableQuantity("P001", StoreType.PHYSICAL)).thenReturn(50);
            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreStock("P001");
            verify(request).setAttribute("productCode", "P001");
            verify(request).setAttribute("totalQuantity", 50);
        }
    }

    @Nested
    @DisplayName("doGet tests - Online Stock")
    class DoGetOnlineStockTests {

        @Test
        @DisplayName("Should show online stock summary")
        void shouldShowOnlineStockSummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online");
            when(request.getParameter("product")).thenReturn(null);

            ProductStockSummary summary = new ProductStockSummary("P001", "Product 1", 50, 1);
            when(storeInventoryService.getOnlineStoreStockSummary()).thenReturn(List.of(summary));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getOnlineStoreStockSummary();
            verify(request).setAttribute("storeType", "ONLINE");
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/online.jsp");
        }

        @Test
        @DisplayName("Should show online stock for specific product")
        void shouldShowOnlineStockForSpecificProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online");
            when(request.getParameter("product")).thenReturn("P001");

            OnlineStoreInventory inv = createOnlineInventory("P001", 30);
            when(storeInventoryService.getOnlineStoreStock("P001")).thenReturn(List.of(inv));
            when(storeInventoryService.getAvailableQuantity("P001", StoreType.ONLINE)).thenReturn(30);
            when(storeInventoryService.getOnlineStoreStockSummary()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getOnlineStoreStock("P001");
            verify(request).setAttribute("totalQuantity", 30);
        }
    }

    @Nested
    @DisplayName("doGet tests - Restock Form")
    class DoGetRestockFormTests {

        @Test
        @DisplayName("Should show restock form")
        void shouldShowRestockForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/restock");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/restock.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Low Stock")
    class DoGetLowStockTests {

        @Test
        @DisplayName("Should show low stock with default threshold")
        void shouldShowLowStockWithDefaultThreshold() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/low-stock");
            when(request.getParameter("threshold")).thenReturn(null);

            when(storeInventoryService.getPhysicalStoreLowStock(10)).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreLowStock(10);
            verify(storeInventoryService).getOnlineStoreLowStock(10);
            verify(request).setAttribute("threshold", 10);
            verify(request).getRequestDispatcher("/WEB-INF/views/store-stock/low-stock.jsp");
        }

        @Test
        @DisplayName("Should show low stock with custom threshold")
        void shouldShowLowStockWithCustomThreshold() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/low-stock");
            when(request.getParameter("threshold")).thenReturn("20");

            when(storeInventoryService.getPhysicalStoreLowStock(20)).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(20)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreLowStock(20);
            verify(storeInventoryService).getOnlineStoreLowStock(20);
            verify(request).setAttribute("threshold", 20);
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
