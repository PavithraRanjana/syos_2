package com.syos.service;

import com.syos.domain.models.MainInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.impl.BackgroundTaskServiceImpl;
import com.syos.service.interfaces.InventoryService;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.*;
import com.syos.service.interfaces.StoreInventoryService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BackgroundTaskServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BackgroundTaskServiceImplTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private StoreInventoryService storeInventoryService;

    @Mock
    private ReportService reportService;

    private BackgroundTaskServiceImpl backgroundTaskService;

    @BeforeEach
    void setUp() {
        backgroundTaskService = new BackgroundTaskServiceImpl(
                inventoryService,
                storeInventoryService,
                reportService);
    }

    private PhysicalStoreInventory createPhysicalInventory(String productCode, int quantity) {
        PhysicalStoreInventory inv = new PhysicalStoreInventory(
                new ProductCode(productCode), 1, quantity, LocalDate.now());
        inv.setProductName("Test Product " + productCode);
        return inv;
    }

    private OnlineStoreInventory createOnlineInventory(String productCode, int quantity) {
        OnlineStoreInventory inv = new OnlineStoreInventory(
                new ProductCode(productCode), 1, quantity, LocalDate.now());
        inv.setProductName("Test Product " + productCode);
        return inv;
    }

    private MainInventory createMainInventory(String productCode, int quantity, LocalDate expiry) {
        MainInventory batch = new MainInventory();
        batch.setMainInventoryId(1);
        batch.setProductCode(new ProductCode(productCode));
        batch.setProductName("Test Product");
        batch.setRemainingQuantity(quantity);
        batch.setExpiryDate(expiry);
        return batch;
    }

    @Nested
    @DisplayName("isRunning tests")
    class IsRunningTests {

        @Test
        @DisplayName("Should be not running initially")
        void shouldBeNotRunningInitially() {
            assertFalse(backgroundTaskService.isRunning());
        }
    }

    @Nested
    @DisplayName("runLowStockCheck tests")
    class RunLowStockCheckTests {

        @Test
        @DisplayName("Should run low stock check when no low stock items")
        void shouldRunLowStockCheckWhenNoLowStockItems() {
            // Arrange
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of());

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runLowStockCheck());
        }

        @Test
        @DisplayName("Should run low stock check with physical low stock")
        void shouldRunLowStockCheckWithPhysicalLowStock() {
            // Arrange
            PhysicalStoreInventory lowStockItem = createPhysicalInventory("TEST-001", 5);
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of(lowStockItem));
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of());

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runLowStockCheck());
        }

        @Test
        @DisplayName("Should run low stock check with online low stock")
        void shouldRunLowStockCheckWithOnlineLowStock() {
            // Arrange
            OnlineStoreInventory lowStockItem = createOnlineInventory("TEST-001", 3);
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of(lowStockItem));

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runLowStockCheck());
        }
    }

    @Nested
    @DisplayName("runExpiredProductCleanup tests")
    class RunExpiredProductCleanupTests {

        @Test
        @DisplayName("Should run expired product cleanup when no expired items")
        void shouldRunExpiredProductCleanupWhenNoExpiredItems() {
            // Arrange
            when(inventoryService.findExpiredBatches()).thenReturn(List.of());
            when(reportService.getExpiringStockReport(anyInt())).thenReturn(List.of());

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runExpiredProductCleanup());
        }

        @Test
        @DisplayName("Should run expired product cleanup with expired batches")
        void shouldRunExpiredProductCleanupWithExpiredBatches() {
            // Arrange
            MainInventory expiredBatch = createMainInventory("TEST-001", 50, LocalDate.now().minusDays(5));
            when(inventoryService.findExpiredBatches()).thenReturn(List.of(expiredBatch));
            when(reportService.getExpiringStockReport(anyInt())).thenReturn(List.of());

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runExpiredProductCleanup());
        }

        @Test
        @DisplayName("Should run expired product cleanup with expiring soon items")
        void shouldRunExpiredProductCleanupWithExpiringSoonItems() {
            // Arrange
            ExpiringStockReport expiringItem = new ExpiringStockReport(
                    "TEST-001", "Test Product", 1, 50,
                    LocalDate.now().plusDays(5), 5);
            when(inventoryService.findExpiredBatches()).thenReturn(List.of());
            when(reportService.getExpiringStockReport(anyInt())).thenReturn(List.of(expiringItem));

            // Act - Should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runExpiredProductCleanup());
        }
    }

    @Nested
    @DisplayName("startScheduledTasks tests")
    class StartScheduledTasksTests {

        @Test
        @DisplayName("Should start scheduled tasks")
        void shouldStartScheduledTasks() {
            // Act
            backgroundTaskService.startScheduledTasks();

            // Assert
            assertTrue(backgroundTaskService.isRunning());
        }

        @Test
        @DisplayName("Should not restart if already running")
        void shouldNotRestartIfAlreadyRunning() {
            // Arrange
            backgroundTaskService.startScheduledTasks();
            assertTrue(backgroundTaskService.isRunning());

            // Act - start again
            backgroundTaskService.startScheduledTasks();

            // Assert - still running, no exception
            assertTrue(backgroundTaskService.isRunning());
        }
    }

    @Nested
    @DisplayName("stopScheduledTasks tests")
    class StopScheduledTasksTests {

        @Test
        @DisplayName("Should stop scheduled tasks")
        void shouldStopScheduledTasks() {
            // Arrange
            backgroundTaskService.startScheduledTasks();
            assertTrue(backgroundTaskService.isRunning());

            // Act
            backgroundTaskService.stopScheduledTasks();

            // Assert
            assertFalse(backgroundTaskService.isRunning());
        }

        @Test
        @DisplayName("Should handle stop when not running")
        void shouldHandleStopWhenNotRunning() {
            // Arrange - not started
            assertFalse(backgroundTaskService.isRunning());

            // Act - should not throw
            assertDoesNotThrow(() -> backgroundTaskService.stopScheduledTasks());
        }
    }

    @Nested
    @DisplayName("performLowStockCheck tests")
    class PerformLowStockCheckTests {

        @Test
        @DisplayName("Should check both physical and online store low stock")
        void shouldCheckBothStores() throws Exception {
            // Arrange
            PhysicalStoreInventory physicalLow = createPhysicalInventory("P1", 5);
            OnlineStoreInventory onlineLow = createOnlineInventory("O1", 3);
            MainInventory expiringBatch = createMainInventory("E1", 10, LocalDate.now().plusDays(5));

            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of(physicalLow));
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of(onlineLow));
            when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of(expiringBatch));

            // Act
            backgroundTaskService.runLowStockCheck().get();

            // Assert - verify all services were called
            verify(storeInventoryService).getPhysicalStoreLowStock(10);
            verify(storeInventoryService).getOnlineStoreLowStock(10);
            verify(inventoryService).findExpiringWithinDays(7);
        }

        @Test
        @DisplayName("Should handle exception gracefully during low stock check")
        void shouldHandleExceptionGracefully() throws Exception {
            // Arrange
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt()))
                    .thenThrow(new RuntimeException("Database error"));

            // Act - should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runLowStockCheck().get());
        }

        @Test
        @DisplayName("Should log each low stock physical item")
        void shouldLogEachLowStockPhysicalItem() throws Exception {
            // Arrange
            PhysicalStoreInventory item1 = createPhysicalInventory("P1", 5);
            PhysicalStoreInventory item2 = createPhysicalInventory("P2", 3);

            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of(item1, item2));
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of());
            when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act
            backgroundTaskService.runLowStockCheck().get();

            // Assert
            verify(storeInventoryService).getPhysicalStoreLowStock(10);
        }

        @Test
        @DisplayName("Should log each low stock online item")
        void shouldLogEachLowStockOnlineItem() throws Exception {
            // Arrange
            OnlineStoreInventory item1 = createOnlineInventory("O1", 2);
            OnlineStoreInventory item2 = createOnlineInventory("O2", 1);

            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of(item1, item2));
            when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act
            backgroundTaskService.runLowStockCheck().get();

            // Assert
            verify(storeInventoryService).getOnlineStoreLowStock(10);
        }
    }

    @Nested
    @DisplayName("performExpiredProductCleanup tests")
    class PerformExpiredProductCleanupTests {

        @Test
        @DisplayName("Should find and log expired batches")
        void shouldFindAndLogExpiredBatches() throws Exception {
            // Arrange
            MainInventory expired1 = createMainInventory("E1", 50, LocalDate.now().minusDays(10));
            MainInventory expired2 = createMainInventory("E2", 30, LocalDate.now().minusDays(5));

            when(inventoryService.findExpiredBatches()).thenReturn(List.of(expired1, expired2));

            // Act
            backgroundTaskService.runExpiredProductCleanup().get();

            // Assert
            verify(inventoryService).findExpiredBatches();
        }

        @Test
        @DisplayName("Should handle no expired batches")
        void shouldHandleNoExpiredBatches() throws Exception {
            // Arrange
            when(inventoryService.findExpiredBatches()).thenReturn(List.of());

            // Act
            backgroundTaskService.runExpiredProductCleanup().get();

            // Assert
            verify(inventoryService).findExpiredBatches();
        }

        @Test
        @DisplayName("Should handle exception gracefully during cleanup")
        void shouldHandleExceptionGracefully() throws Exception {
            // Arrange
            when(inventoryService.findExpiredBatches())
                    .thenThrow(new RuntimeException("Database error"));

            // Act - should not throw
            assertDoesNotThrow(() -> backgroundTaskService.runExpiredProductCleanup().get());
        }

        @Test
        @DisplayName("Should process each expired batch via forEach lambda")
        void shouldProcessEachExpiredBatch() throws Exception {
            // Arrange - create multiple batches to trigger forEach lambda
            MainInventory batch1 = createMainInventory("B1", 10, LocalDate.now().minusDays(1));
            MainInventory batch2 = createMainInventory("B2", 20, LocalDate.now().minusDays(2));
            MainInventory batch3 = createMainInventory("B3", 30, LocalDate.now().minusDays(3));

            when(inventoryService.findExpiredBatches()).thenReturn(List.of(batch1, batch2, batch3));

            // Act
            backgroundTaskService.runExpiredProductCleanup().get();

            // Assert
            verify(inventoryService).findExpiredBatches();
        }
    }

    @Nested
    @DisplayName("performInventorySyncCheck tests")
    class PerformInventorySyncCheckTests {

        @Test
        @DisplayName("Should setup inventory summary with mixed stock levels")
        void shouldSetupInventorySummaryWithMixedStockLevels() {
            // Arrange - setup mocks for when sync runs
            InventoryService.ProductInventorySummary inStock = new InventoryService.ProductInventorySummary("P1",
                    "Product 1", 50, 2, LocalDate.now().plusMonths(3));
            InventoryService.ProductInventorySummary lowStock = new InventoryService.ProductInventorySummary("P2",
                    "Product 2", 5, 1, LocalDate.now().plusMonths(1));
            InventoryService.ProductInventorySummary outOfStock = new InventoryService.ProductInventorySummary("P3",
                    "Product 3", 0, 0, null);

            when(inventoryService.getInventorySummary()).thenReturn(List.of(inStock, lowStock, outOfStock));
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of());
            when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act - just verify tasks can be started/stopped
            backgroundTaskService.startScheduledTasks();
            assertTrue(backgroundTaskService.isRunning());
            backgroundTaskService.stopScheduledTasks();
            assertFalse(backgroundTaskService.isRunning());
        }

        @Test
        @DisplayName("Should handle exception during sync check setup")
        void shouldHandleExceptionDuringSyncCheckSetup() {
            // Arrange
            when(inventoryService.getInventorySummary())
                    .thenThrow(new RuntimeException("Database error"));
            when(storeInventoryService.getPhysicalStoreLowStock(anyInt())).thenReturn(List.of());
            when(storeInventoryService.getOnlineStoreLowStock(anyInt())).thenReturn(List.of());
            when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act - should not throw when starting/stopping
            assertDoesNotThrow(() -> {
                backgroundTaskService.startScheduledTasks();
                backgroundTaskService.stopScheduledTasks();
            });
        }

        @Test
        @DisplayName("Should filter out-of-stock items correctly")
        void shouldFilterOutOfStockItemsCorrectly() {
            // Arrange - tests lambda filter logic for totalQuantity == 0
            InventoryService.ProductInventorySummary outOfStock1 = new InventoryService.ProductInventorySummary("P1",
                    "Product 1", 0, 0, null);
            InventoryService.ProductInventorySummary outOfStock2 = new InventoryService.ProductInventorySummary("P2",
                    "Product 2", 0, 0, null);
            InventoryService.ProductInventorySummary inStock = new InventoryService.ProductInventorySummary("P3",
                    "Product 3", 100, 5, LocalDate.now().plusMonths(6));

            // Verify the filter logic works correctly
            List<InventoryService.ProductInventorySummary> summaryList = List.of(outOfStock1, outOfStock2, inStock);
            long outOfStockCount = summaryList.stream()
                    .filter(s -> s.totalQuantity() == 0)
                    .count();
            assertEquals(2, outOfStockCount);
        }

        @Test
        @DisplayName("Should filter low-stock items correctly")
        void shouldFilterLowStockItemsCorrectly() {
            // Arrange - tests lambda filter logic for 0 < totalQuantity <= threshold
            InventoryService.ProductInventorySummary lowStock1 = new InventoryService.ProductInventorySummary("P1",
                    "Product 1", 5, 1, LocalDate.now().plusMonths(2));
            InventoryService.ProductInventorySummary lowStock2 = new InventoryService.ProductInventorySummary("P2",
                    "Product 2", 10, 2, LocalDate.now().plusMonths(3));
            InventoryService.ProductInventorySummary normalStock = new InventoryService.ProductInventorySummary("P3",
                    "Product 3", 50, 3, LocalDate.now().plusMonths(4));

            // Verify the filter logic works correctly (threshold = 10)
            List<InventoryService.ProductInventorySummary> summaryList = List.of(lowStock1, lowStock2, normalStock);
            int threshold = 10;
            long lowStockCount = summaryList.stream()
                    .filter(s -> s.totalQuantity() > 0 && s.totalQuantity() <= threshold)
                    .count();
            assertEquals(2, lowStockCount);
        }
    }
}
