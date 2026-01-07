package com.syos.repository.interfaces;

import com.syos.domain.models.OnlineStoreInventory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OnlineStoreInventory entity operations.
 */
public interface OnlineStoreInventoryRepository extends Repository<OnlineStoreInventory, Integer> {

    /**
     * Finds all online stock for a product code.
     */
    List<OnlineStoreInventory> findByProductCode(String productCode);

    /**
     * Finds online stock for a specific product and batch.
     */
    Optional<OnlineStoreInventory> findByProductCodeAndBatchId(String productCode, Integer batchId);

    /**
     * Finds all online stock with quantity > 0 for a product, ordered by expiry (FIFO).
     */
    List<OnlineStoreInventory> findAvailableByProductCode(String productCode);

    /**
     * Gets total quantity available online for a product.
     */
    int getTotalQuantityAvailable(String productCode);

    /**
     * Reduces online quantity for a specific batch.
     * @return true if successful
     */
    boolean reduceQuantity(String productCode, Integer batchId, int amount);

    /**
     * Adds quantity to online stock for a specific batch (restock operation).
     */
    boolean addQuantity(String productCode, Integer batchId, int amount);

    /**
     * Finds all products with low stock (below threshold).
     */
    List<OnlineStoreInventory> findLowStock(int threshold);

    /**
     * Gets stock status for all products (aggregated by product).
     */
    List<ProductStockSummary> getStockSummary();

    /**
     * Stock summary DTO.
     */
    record ProductStockSummary(
        String productCode,
        String productName,
        int totalQuantity,
        int batchCount
    ) {}
}
