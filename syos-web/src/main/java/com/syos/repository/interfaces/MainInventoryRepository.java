package com.syos.repository.interfaces;

import com.syos.domain.models.MainInventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MainInventory (batch) entity operations.
 */
public interface MainInventoryRepository extends Repository<MainInventory, Integer> {

    /**
     * Finds all batches for a product code.
     */
    List<MainInventory> findByProductCode(String productCode);

    /**
     * Finds all batches with remaining quantity for a product code.
     * Ordered by expiry date (FIFO - first expiring first).
     */
    List<MainInventory> findAvailableBatchesByProductCode(String productCode);

    /**
     * Finds the next batch to use for a sale (FIFO by expiry date).
     */
    Optional<MainInventory> findNextBatchForSale(String productCode, int requiredQuantity);

    /**
     * Finds all batches expiring within the given number of days.
     */
    List<MainInventory> findExpiringWithinDays(int days);

    /**
     * Finds all expired batches with remaining quantity.
     */
    List<MainInventory> findExpiredBatches();

    /**
     * Finds batches by supplier name.
     */
    List<MainInventory> findBySupplier(String supplierName);

    /**
     * Gets total remaining quantity for a product across all batches.
     */
    int getTotalRemainingQuantity(String productCode);

    /**
     * Reduces the remaining quantity of a batch.
     * @return true if successful, false if insufficient quantity
     */
    boolean reduceQuantity(Integer batchId, int amount);

    /**
     * Increases the remaining quantity of a batch (for undo operations).
     */
    boolean increaseQuantity(Integer batchId, int amount);

    /**
     * Finds batches purchased within a date range.
     */
    List<MainInventory> findByPurchaseDateRange(LocalDate startDate, LocalDate endDate);
}
