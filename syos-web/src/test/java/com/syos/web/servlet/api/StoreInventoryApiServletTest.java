package com.syos.web.servlet.api;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.StoreInventoryService.RestockResult;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreInventoryApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoreInventoryApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private StoreInventoryService storeInventoryService;

    private StoreInventoryApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new StoreInventoryApiServlet();
        // Inject mock StoreInventoryService via reflection
        java.lang.reflect.Field field = StoreInventoryApiServlet.class.getDeclaredField("storeInventoryService");
        field.setAccessible(true);
        field.set(servlet, storeInventoryService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    private PhysicalStoreInventory createPhysicalInventory(String productCode, int quantity) {
        PhysicalStoreInventory inv = new PhysicalStoreInventory();
        inv.setMainInventoryId(1);
        inv.setProductCode(new ProductCode(productCode));
        inv.setQuantityOnShelf(quantity);
        inv.setExpiryDate(LocalDate.now().plusDays(30));
        inv.setRestockedDate(LocalDate.now());
        return inv;
    }

    private OnlineStoreInventory createOnlineInventory(String productCode, int quantity) {
        OnlineStoreInventory inv = new OnlineStoreInventory();
        inv.setMainInventoryId(1);
        inv.setProductCode(new ProductCode(productCode));
        inv.setQuantityAvailable(quantity);
        inv.setExpiryDate(LocalDate.now().plusDays(30));
        inv.setRestockedDate(LocalDate.now());
        return inv;
    }

    @Nested
    @DisplayName("doGet tests - Stock Summary")
    class DoGetStockSummaryTests {

        @Test
        @DisplayName("Should return physical store stock summary")
        void shouldReturnPhysicalStoreStockSummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical");

            ProductStockSummary summary = new ProductStockSummary("P001", "Product 1", 100, 2);
            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of(summary));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreStockSummary();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("PHYSICAL");
            assert output.contains("summary");
        }

        @Test
        @DisplayName("Should return online store stock summary")
        void shouldReturnOnlineStoreStockSummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online");

            ProductStockSummary summary = new ProductStockSummary("P001", "Product 1", 50, 1);
            when(storeInventoryService.getOnlineStoreStockSummary()).thenReturn(List.of(summary));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getOnlineStoreStockSummary();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("ONLINE");
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
        @DisplayName("Should return 400 for invalid store type")
        void shouldReturn400ForInvalidStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/invalid");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doGet tests - Product Stock")
    class DoGetProductStockTests {

        @Test
        @DisplayName("Should return physical store product stock")
        void shouldReturnPhysicalStoreProductStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/P001");
            when(storeInventoryService.getAvailableQuantity("P001", StoreType.PHYSICAL)).thenReturn(50);

            PhysicalStoreInventory inv = createPhysicalInventory("P001", 50);
            when(storeInventoryService.getPhysicalStoreStock("P001")).thenReturn(List.of(inv));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getAvailableQuantity("P001", StoreType.PHYSICAL);
            verify(storeInventoryService).getPhysicalStoreStock("P001");
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("totalQuantity");
        }

        @Test
        @DisplayName("Should return online store product stock")
        void shouldReturnOnlineStoreProductStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online/P001");
            when(storeInventoryService.getAvailableQuantity("P001", StoreType.ONLINE)).thenReturn(30);

            OnlineStoreInventory inv = createOnlineInventory("P001", 30);
            when(storeInventoryService.getOnlineStoreStock("P001")).thenReturn(List.of(inv));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getAvailableQuantity("P001", StoreType.ONLINE);
            verify(storeInventoryService).getOnlineStoreStock("P001");
        }
    }

    @Nested
    @DisplayName("doGet tests - Low Stock")
    class DoGetLowStockTests {

        @Test
        @DisplayName("Should return physical store low stock")
        void shouldReturnPhysicalStoreLowStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/low-stock");
            when(request.getParameter("threshold")).thenReturn("15");

            PhysicalStoreInventory inv = createPhysicalInventory("P001", 5);
            when(storeInventoryService.getPhysicalStoreLowStock(15)).thenReturn(List.of(inv));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreLowStock(15);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("products");
        }

        @Test
        @DisplayName("Should return online store low stock")
        void shouldReturnOnlineStoreLowStock() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online/low-stock");
            when(request.getParameter("threshold")).thenReturn("20");

            OnlineStoreInventory inv = createOnlineInventory("P001", 8);
            when(storeInventoryService.getOnlineStoreLowStock(20)).thenReturn(List.of(inv));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getOnlineStoreLowStock(20);
        }

        @Test
        @DisplayName("Should use default threshold")
        void shouldUseDefaultThreshold() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/low-stock");
            when(request.getParameter("threshold")).thenReturn(null);

            when(storeInventoryService.getPhysicalStoreLowStock(10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreLowStock(10);
        }
    }

    @Nested
    @DisplayName("doPost tests - Restock")
    class DoPostRestockTests {

        @Test
        @DisplayName("Should restock physical store using FIFO")
        void shouldRestockPhysicalStoreUsingFifo() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 50}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            RestockResult result = RestockResult.success(50, 2);
            when(storeInventoryService.restockPhysicalStore("P001", 50)).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(storeInventoryService).restockPhysicalStore("P001", 50);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("quantityRestocked");
        }

        @Test
        @DisplayName("Should restock online store using FIFO")
        void shouldRestockOnlineStoreUsingFifo() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 30}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            RestockResult result = RestockResult.success(30, 1);
            when(storeInventoryService.restockOnlineStore("P001", 30)).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(storeInventoryService).restockOnlineStore("P001", 30);
        }

        @Test
        @DisplayName("Should restock from specific batch for physical store")
        void shouldRestockFromSpecificBatchForPhysicalStore() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 25, \"batchId\": 5}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(storeInventoryService.restockPhysicalStoreFromBatch("P001", 5, 25)).thenReturn(true);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(storeInventoryService).restockPhysicalStoreFromBatch("P001", 5, 25);
        }

        @Test
        @DisplayName("Should restock from specific batch for online store")
        void shouldRestockFromSpecificBatchForOnlineStore() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/online/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 20, \"batchId\": 3}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(storeInventoryService.restockOnlineStoreFromBatch("P001", 3, 20)).thenReturn(true);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(storeInventoryService).restockOnlineStoreFromBatch("P001", 3, 20);
        }

        @Test
        @DisplayName("Should return 400 when restock fails")
        void shouldReturn400WhenRestockFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 1000}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            RestockResult result = RestockResult.failure("Insufficient stock in main inventory");
            when(storeInventoryService.restockPhysicalStore("P001", 1000)).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when batch restock fails")
        void shouldReturn400WhenBatchRestockFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"productCode\": \"P001\", \"quantity\": 50, \"batchId\": 999}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(storeInventoryService.restockPhysicalStoreFromBatch("P001", 999, 50)).thenReturn(false);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when productCode is missing")
        void shouldReturn400WhenProductCodeMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"quantity\": 50}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when quantity is missing")
        void shouldReturn400WhenQuantityMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/restock");
            String jsonBody = "{\"productCode\": \"P001\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for null path")
        void shouldReturn400ForNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when not restock endpoint")
        void shouldReturn400WhenNotRestockEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/physical/other");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
