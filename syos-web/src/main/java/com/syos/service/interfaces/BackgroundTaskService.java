package com.syos.service.interfaces;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing background tasks.
 * Uses ScheduledExecutorService for periodic task execution.
 */
public interface BackgroundTaskService {

    /**
     * Starts all scheduled background tasks.
     */
    void startScheduledTasks();

    /**
     * Stops all scheduled background tasks.
     */
    void stopScheduledTasks();

    /**
     * Manually triggers a low stock check.
     * 
     * @return CompletableFuture that completes when check is done
     */
    CompletableFuture<Void> runLowStockCheck();

    /**
     * Manually triggers expired product cleanup.
     * 
     * @return CompletableFuture that completes when cleanup is done
     */
    CompletableFuture<Void> runExpiredProductCleanup();

    /**
     * Checks if background tasks are running.
     */
    boolean isRunning();
}
