package com.syos.service.interfaces;

import com.syos.domain.models.MainInventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Main Inventory (batch) management operations.
 * Handles incoming stock batches with purchase price, expiry dates, and FIFO tracking.
 */
public interface InventoryService {

    /**
     * Adds a new batch of stock to the main inventory.
     */
    MainInventory addBatch(String productCode, int quantity, BigDecimal purchasePrice,
                           LocalDate purchaseDate, LocalDate expiryDate, String supplierName);

    /**
     * Finds a batch by its ID.
     */
    Optional<MainInventory> findBatchById(Integer batchId);

    /**
     * Finds all batches for a product.
     */
    List<MainInventory> findBatchesByProductCode(String productCode);

    /**
     * Finds available batches (with remaining quantity) for a product, ordered by FIFO.
     */
    List<MainInventory> findAvailableBatches(String productCode);

    /**
     * Gets the next batch to use for a sale based on FIFO (earliest expiry first).
     */
    Optional<MainInventory> getNextBatchForSale(String productCode, int requiredQuantity);

    /**
     * Gets the total remaining quantity across all batches for a product.
     */
    int getTotalRemainingQuantity(String productCode);

    /**
     * Reduces the quantity of a specific batch.
     * Used when stock is moved to store inventory or sold.
     */
    boolean reduceQuantity(Integer batchId, int amount);

    /**
     * Increases the quantity of a specific batch.
     * Used for returns or corrections.
     */
    boolean increaseQuantity(Integer batchId, int amount);

    /**
     * Finds all batches expiring within the specified number of days.
     */
    List<MainInventory> findExpiringWithinDays(int days);

    /**
     * Finds all expired batches with remaining stock.
     */
    List<MainInventory> findExpiredBatches();

    /**
     * Finds batches by supplier name.
     */
    List<MainInventory> findBySupplier(String supplierName);

    /**
     * Finds batches purchased within a date range.
     */
    List<MainInventory> findByPurchaseDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Gets all batches with pagination.
     */
    List<MainInventory> findAll(int page, int size);

    /**
     * Gets the count of all batches.
     */
    long getBatchCount();

    /**
     * Checks if there is sufficient stock available for a product.
     */
    boolean hasAvailableStock(String productCode, int requiredQuantity);

    /**
     * Gets inventory summary for all products.
     */
    List<ProductInventorySummary> getInventorySummary();

    /**
     * Product inventory summary DTO.
     */
    record ProductInventorySummary(
        String productCode,
        String productName,
        int totalQuantity,
        int batchCount,
        LocalDate earliestExpiry
    ) {}
}
