package com.syos.web.servlet.api;

import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.InventoryService;
import com.syos.service.interfaces.InventoryService.ProductInventorySummary;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class InventoryApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private InventoryService inventoryService;

    private InventoryApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new InventoryApiServlet();
        java.lang.reflect.Field field = InventoryApiServlet.class.getDeclaredField("inventoryService");
        field.setAccessible(true);
        field.set(servlet, inventoryService);

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("doGet tests")
    class DoGetTests {

        @Test
        @DisplayName("Should list batches")
        void shouldListBatches() throws Exception {
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("page")).thenReturn(null);
            when(request.getParameter("size")).thenReturn(null);
            when(inventoryService.findAll(0, 50)).thenReturn(List.of());

            servlet.doGet(request, response);

            verify(inventoryService).findAll(0, 50);
        }

        @Test
        @DisplayName("Should get inventory summary")
        void shouldGetInventorySummary() throws Exception {
            when(request.getPathInfo()).thenReturn("/summary");
            ProductInventorySummary summary = new ProductInventorySummary("P001", "Test", 100, 2, LocalDate.now());
            when(inventoryService.getInventorySummary()).thenReturn(List.of(summary));

            servlet.doGet(request, response);

            verify(inventoryService).getInventorySummary();
        }

        @Test
        @DisplayName("Should get expiring inventory")
        void shouldGetExpiringInventory() throws Exception {
            when(request.getPathInfo()).thenReturn("/expiring");
            when(request.getParameter("days")).thenReturn("7");
            when(inventoryService.findExpiringWithinDays(7)).thenReturn(List.of());

            servlet.doGet(request, response);

            verify(inventoryService).findExpiringWithinDays(7);
        }

        @Test
        @DisplayName("Should get expired inventory")
        void shouldGetExpiredInventory() throws Exception {
            when(request.getPathInfo()).thenReturn("/expired");
            when(inventoryService.findExpiredBatches()).thenReturn(List.of());

            servlet.doGet(request, response);

            verify(inventoryService).findExpiredBatches();
        }

        @Test
        @DisplayName("Should get batch by id")
        void shouldGetBatchById() throws Exception {
            when(request.getPathInfo()).thenReturn("/123");
            MainInventory batch = createTestBatch(123, "P001");
            when(inventoryService.findBatchById(123)).thenReturn(Optional.of(batch));

            servlet.doGet(request, response);

            verify(inventoryService).findBatchById(123);
        }

        @Test
        @DisplayName("Should return 404 for non-existent batch")
        void shouldReturn404ForNonExistentBatch() throws Exception {
            when(request.getPathInfo()).thenReturn("/999");
            when(inventoryService.findBatchById(999)).thenReturn(Optional.empty());

            servlet.doGet(request, response);

            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should get batches by product code")
        void shouldGetBatchesByProductCode() throws Exception {
            when(request.getPathInfo()).thenReturn("/product/P001");
            when(request.getParameter("available")).thenReturn(null);
            when(inventoryService.findBatchesByProductCode("P001")).thenReturn(List.of());
            when(inventoryService.getTotalRemainingQuantity("P001")).thenReturn(100);

            servlet.doGet(request, response);

            verify(inventoryService).findBatchesByProductCode("P001");
        }
    }

    @Nested
    @DisplayName("doPost tests")
    class DoPostTests {

        @Test
        @DisplayName("Should add batch")
        void shouldAddBatch() throws Exception {
            String jsonBody = "{\"productCode\":\"P001\",\"quantity\":100,\"purchasePrice\":50.00,\"expiryDate\":\"2027-01-01\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            MainInventory batch = createTestBatch(1, "P001");
            when(inventoryService.addBatch(any(), anyInt(), any(), any(), any(), any())).thenReturn(batch);

            servlet.doPost(request, response);

            verify(inventoryService).addBatch(any(), anyInt(), any(), any(), any(), any());
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

            servlet.doPost(request, response);

            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private MainInventory createTestBatch(int id, String productCode) {
        MainInventory batch = new MainInventory();
        batch.setMainInventoryId(id);
        batch.setProductCode(new ProductCode(productCode));
        batch.setQuantityReceived(100);
        batch.setRemainingQuantity(100);
        batch.setPurchasePrice(new com.syos.domain.valueobjects.Money(new BigDecimal("50.00")));
        batch.setExpiryDate(LocalDate.now().plusYears(1));
        return batch;
    }
}
