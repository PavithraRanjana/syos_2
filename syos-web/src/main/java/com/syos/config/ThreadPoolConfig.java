package com.syos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Configuration for thread pools used in the application.
 */
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    private static volatile ExecutorService apiThreadPool;
    private static volatile ScheduledExecutorService backgroundTaskExecutor;
    private static volatile ExecutorService inventoryThreadPool;
    private static final Object lock = new Object();

    private ThreadPoolConfig() {
        // Prevent instantiation
    }

    /**
     * Gets the thread pool for API request handling.
     */
    public static ExecutorService getApiThreadPool() {
        if (apiThreadPool == null) {
            synchronized (lock) {
                if (apiThreadPool == null) {
                    apiThreadPool = createApiThreadPool();
                }
            }
        }
        return apiThreadPool;
    }

    /**
     * Gets the scheduled executor for background tasks.
     */
    public static ScheduledExecutorService getBackgroundTaskExecutor() {
        if (backgroundTaskExecutor == null) {
            synchronized (lock) {
                if (backgroundTaskExecutor == null) {
                    backgroundTaskExecutor = createBackgroundTaskExecutor();
                }
            }
        }
        return backgroundTaskExecutor;
    }

    /**
     * Gets the thread pool for inventory operations (higher priority).
     */
    public static ExecutorService getInventoryThreadPool() {
        if (inventoryThreadPool == null) {
            synchronized (lock) {
                if (inventoryThreadPool == null) {
                    inventoryThreadPool = createInventoryThreadPool();
                }
            }
        }
        return inventoryThreadPool;
    }

    /**
     * Creates the API thread pool.
     */
    private static ExecutorService createApiThreadPool() {
        logger.info("Creating API thread pool");
        return new ThreadPoolExecutor(
            10,  // Core pool size
            50,  // Maximum pool size
            60L, TimeUnit.SECONDS, // Keep alive time
            new LinkedBlockingQueue<>(100), // Queue capacity
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "syos-api-" + counter++);
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }

    /**
     * Creates the background task executor.
     */
    private static ScheduledExecutorService createBackgroundTaskExecutor() {
        logger.info("Creating background task executor");
        return Executors.newScheduledThreadPool(5, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "syos-background-" + counter++);
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Creates the inventory thread pool.
     */
    private static ExecutorService createInventoryThreadPool() {
        logger.info("Creating inventory thread pool");
        return new ThreadPoolExecutor(
            5,
            20,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "syos-inventory-" + counter++);
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * Shuts down all thread pools gracefully.
     */
    public static void shutdownAll() {
        logger.info("Shutting down all thread pools");

        shutdownExecutor(apiThreadPool, "API");
        shutdownExecutor(backgroundTaskExecutor, "Background");
        shutdownExecutor(inventoryThreadPool, "Inventory");

        apiThreadPool = null;
        backgroundTaskExecutor = null;
        inventoryThreadPool = null;
    }

    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("{} thread pool did not terminate in time, forcing shutdown", name);
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("{} thread pool shutdown interrupted", name);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
