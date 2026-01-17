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
}
