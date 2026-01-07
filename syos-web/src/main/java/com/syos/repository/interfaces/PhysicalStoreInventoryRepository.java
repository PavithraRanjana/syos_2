package com.syos.repository.interfaces;

import com.syos.domain.models.PhysicalStoreInventory;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PhysicalStoreInventory entity operations.
 */
public interface PhysicalStoreInventoryRepository extends Repository<PhysicalStoreInventory, Integer> {

    /**
     * Finds all shelf stock for a product code.
     */
    List<PhysicalStoreInventory> findByProductCode(String productCode);

    /**
     * Finds shelf stock for a specific product and batch.
     */
    Optional<PhysicalStoreInventory> findByProductCodeAndBatchId(String productCode, Integer batchId);

    /**
     * Finds all shelf stock with quantity > 0 for a product, ordered by expiry (FIFO).
     */
    List<PhysicalStoreInventory> findAvailableByProductCode(String productCode);

    /**
     * Gets total quantity on shelf for a product.
     */
    int getTotalQuantityOnShelf(String productCode);

    /**
     * Reduces shelf quantity for a specific batch.
     * @return true if successful
     */
    boolean reduceQuantity(String productCode, Integer batchId, int amount);

    /**
     * Adds quantity to shelf for a specific batch (restock operation).
     */
    boolean addQuantity(String productCode, Integer batchId, int amount);

    /**
     * Finds all products with low stock (below threshold).
     */
    List<PhysicalStoreInventory> findLowStock(int threshold);

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
