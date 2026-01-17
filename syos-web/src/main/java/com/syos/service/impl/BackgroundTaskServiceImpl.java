package com.syos.service.impl;

import com.syos.config.ThreadPoolConfig;
import com.syos.domain.models.MainInventory;
import com.syos.service.interfaces.BackgroundTaskService;
import com.syos.service.interfaces.InventoryService;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.StoreInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of BackgroundTaskService.
 * Manages scheduled background tasks using the BackgroundTaskExecutor thread
 * pool.
 */
public class BackgroundTaskServiceImpl implements BackgroundTaskService {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundTaskServiceImpl.class);

    private static final int LOW_STOCK_CHECK_INTERVAL_MINUTES = 60; // Every hour
    private static final int EXPIRED_CLEANUP_INTERVAL_HOURS = 24; // Daily
    private static final int INVENTORY_SYNC_INTERVAL_MINUTES = 30; // Every 30 minutes
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPIRING_SOON_DAYS = 7;

    private final InventoryService inventoryService;
    private final StoreInventoryService storeInventoryService;
    private final ReportService reportService;

    private ScheduledFuture<?> lowStockCheckTask;
    private ScheduledFuture<?> expiredCleanupTask;
    private ScheduledFuture<?> inventorySyncTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public BackgroundTaskServiceImpl(InventoryService inventoryService,
            StoreInventoryService storeInventoryService,
            ReportService reportService) {
        this.inventoryService = inventoryService;
        this.storeInventoryService = storeInventoryService;
        this.reportService = reportService;
    }

    @Override
    public void startScheduledTasks() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting background scheduled tasks");
            ScheduledExecutorService executor = ThreadPoolConfig.getBackgroundTaskExecutor();

            // Schedule low stock check - every hour
            lowStockCheckTask = executor.scheduleAtFixedRate(
                    this::performLowStockCheck,
                    1, // Initial delay - 1 minute to let app start
                    LOW_STOCK_CHECK_INTERVAL_MINUTES,
                    TimeUnit.MINUTES);
            logger.info("Low stock check scheduled every {} minutes", LOW_STOCK_CHECK_INTERVAL_MINUTES);

            // Schedule expired product cleanup - daily
            expiredCleanupTask = executor.scheduleAtFixedRate(
                    this::performExpiredProductCleanup,
                    5, // Initial delay - 5 minutes
                    EXPIRED_CLEANUP_INTERVAL_HOURS * 60,
                    TimeUnit.MINUTES);
            logger.info("Expired product cleanup scheduled every {} hours", EXPIRED_CLEANUP_INTERVAL_HOURS);

            // Schedule inventory sync check - every 30 minutes
            inventorySyncTask = executor.scheduleAtFixedRate(
                    this::performInventorySyncCheck,
                    2, // Initial delay - 2 minutes
                    INVENTORY_SYNC_INTERVAL_MINUTES,
                    TimeUnit.MINUTES);
            logger.info("Inventory sync check scheduled every {} minutes", INVENTORY_SYNC_INTERVAL_MINUTES);

            logger.info("All background tasks started successfully");
        } else {
            logger.warn("Background tasks are already running");
        }
    }

    @Override
    public void stopScheduledTasks() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping background scheduled tasks");

            if (lowStockCheckTask != null) {
                lowStockCheckTask.cancel(false);
                logger.info("Low stock check task stopped");
            }
            if (expiredCleanupTask != null) {
                expiredCleanupTask.cancel(false);
                logger.info("Expired cleanup task stopped");
            }
            if (inventorySyncTask != null) {
                inventorySyncTask.cancel(false);
                logger.info("Inventory sync task stopped");
            }

            logger.info("All background tasks stopped");
        } else {
            logger.warn("Background tasks are not running");
        }
    }

    @Override
    public CompletableFuture<Void> runLowStockCheck() {
        return CompletableFuture.runAsync(
                this::performLowStockCheck,
                ThreadPoolConfig.getBackgroundTaskExecutor());
    }

    @Override
    public CompletableFuture<Void> runExpiredProductCleanup() {
        return CompletableFuture.runAsync(
                this::performExpiredProductCleanup,
                ThreadPoolConfig.getBackgroundTaskExecutor());
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    // ==================== Private Task Methods ====================

    private void performLowStockCheck() {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Running low stock check...", threadName);

        try {
            // Check physical store low stock
            var physicalLowStock = storeInventoryService.getPhysicalStoreLowStock(LOW_STOCK_THRESHOLD);
            if (!physicalLowStock.isEmpty()) {
                logger.warn("[{}] Found {} products with low physical store stock (threshold: {})",
                        threadName, physicalLowStock.size(), LOW_STOCK_THRESHOLD);
                physicalLowStock.forEach(item -> logger.warn("[{}] Low stock alert - Physical Store: {} ({}), Qty: {}",
                        threadName, item.getProductName(), item.getProductCodeString(), item.getQuantityOnShelf()));
            }

            // Check online store low stock
            var onlineLowStock = storeInventoryService.getOnlineStoreLowStock(LOW_STOCK_THRESHOLD);
            if (!onlineLowStock.isEmpty()) {
                logger.warn("[{}] Found {} products with low online store stock (threshold: {})",
                        threadName, onlineLowStock.size(), LOW_STOCK_THRESHOLD);
                onlineLowStock.forEach(item -> logger.warn("[{}] Low stock alert - Online Store: {} ({}), Qty: {}",
                        threadName, item.getProductName(), item.getProductCodeString(), item.getQuantityAvailable()));
            }

            // Check main inventory for expiring soon items
            List<MainInventory> expiringSoon = inventoryService.findExpiringWithinDays(EXPIRING_SOON_DAYS);
            if (!expiringSoon.isEmpty()) {
                logger.warn("[{}] Found {} batches expiring within {} days",
                        threadName, expiringSoon.size(), EXPIRING_SOON_DAYS);
            }

            logger.info("[{}] Low stock check completed", threadName);

        } catch (Exception e) {
            logger.error("[{}] Error during low stock check: {}", threadName, e.getMessage(), e);
        }
    }

    private void performExpiredProductCleanup() {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Running expired product cleanup...", threadName);

        try {
            List<MainInventory> expiredBatches = inventoryService.findExpiredBatches();

            if (expiredBatches.isEmpty()) {
                logger.info("[{}] No expired batches found", threadName);
            } else {
                logger.warn("[{}] Found {} expired batches that need attention", threadName, expiredBatches.size());

                // Log details for each expired batch
                expiredBatches.forEach(batch -> logger.warn("[{}] Expired batch: ID={}, Product={}, Qty={}, Expiry={}",
                        threadName,
                        batch.getMainInventoryId(),
                        batch.getProductCodeString(),
                        batch.getRemainingQuantity(),
                        batch.getExpiryDate()));

                // Note: Actual cleanup (zeroing out quantities) would require business decision
                // For now, we just log alerts. Implement actual cleanup if needed:
                // expiredBatches.forEach(batch -> {
                // if (batch.getRemainingQuantity() > 0) {
                // inventoryService.reduceQuantity(batch.getMainInventoryId(),
                // batch.getRemainingQuantity());
                // }
                // });
            }

            logger.info("[{}] Expired product cleanup completed", threadName);

        } catch (Exception e) {
            logger.error("[{}] Error during expired product cleanup: {}", threadName, e.getMessage(), e);
        }
    }

    private void performInventorySyncCheck() {
        String threadName = Thread.currentThread().getName();
        logger.debug("[{}] Running inventory sync check...", threadName);

        try {
            // Get inventory summary for monitoring
            var summary = inventoryService.getInventorySummary();

            int totalProducts = summary.size();
            int outOfStock = (int) summary.stream()
                    .filter(s -> s.totalQuantity() == 0)
                    .count();
            int lowStock = (int) summary.stream()
                    .filter(s -> s.totalQuantity() > 0 && s.totalQuantity() <= LOW_STOCK_THRESHOLD)
                    .count();

            logger.info("[{}] Inventory sync: {} products, {} in stock, {} low stock, {} out of stock",
                    threadName, totalProducts, totalProducts - outOfStock - lowStock, lowStock, outOfStock);

        } catch (Exception e) {
            logger.error("[{}] Error during inventory sync check: {}", threadName, e.getMessage(), e);
        }
    }
}
