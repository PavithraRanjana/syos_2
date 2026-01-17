package com.syos.service.interfaces;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for Store Inventory management (Physical and Online
 * stores).
 * Handles restocking from main inventory and stock queries for sales.
 */
public interface StoreInventoryService {

    // ==================== Physical Store Operations ====================

    /**
     * Restocks a product on physical store shelves from main inventory.
     * Uses FIFO - takes from the batch with earliest expiry.
     */
    RestockResult restockPhysicalStore(String productCode, int quantity);

    /**
     * Restocks a specific batch to physical store shelves.
     */
    boolean restockPhysicalStoreFromBatch(String productCode, Integer batchId, int quantity);

    /**
     * Gets available stock for a product in the physical store (all batches).
     */
    List<PhysicalStoreInventory> getPhysicalStoreStock(String productCode);

    /**
     * Gets total quantity available on physical store shelves for a product.
     */
    int getPhysicalStoreQuantity(String productCode);

    /**
     * Reduces physical store stock for a specific batch (used during sales).
     */
    boolean reducePhysicalStoreStock(String productCode, Integer batchId, int quantity);

    /**
     * Gets physical store stock summary for all products.
     */
    List<ProductStockSummary> getPhysicalStoreStockSummary();

    /**
     * Gets products with low stock in physical store.
     */
    List<PhysicalStoreInventory> getPhysicalStoreLowStock(int threshold);

    // ==================== Online Store Operations ====================

    /**
     * Restocks a product for online store from main inventory.
     * Uses FIFO - takes from the batch with earliest expiry.
     */
    RestockResult restockOnlineStore(String productCode, int quantity);

    /**
     * Restocks a specific batch to online store.
     */
    boolean restockOnlineStoreFromBatch(String productCode, Integer batchId, int quantity);

    /**
     * Gets available stock for a product in the online store (all batches).
     */
    List<OnlineStoreInventory> getOnlineStoreStock(String productCode);

    /**
     * Gets total quantity available for online sales for a product.
     */
    int getOnlineStoreQuantity(String productCode);

    /**
     * Reduces online store stock for a specific batch (used during sales).
     */
    boolean reduceOnlineStoreStock(String productCode, Integer batchId, int quantity);

    /**
     * Gets online store stock summary for all products.
     */
    List<ProductStockSummary> getOnlineStoreStockSummary();

    /**
     * Gets products with low stock in online store.
     */
    List<OnlineStoreInventory> getOnlineStoreLowStock(int threshold);

    // ==================== Common Operations ====================

    /**
     * Gets available stock quantity for a product by store type.
     */
    int getAvailableQuantity(String productCode, StoreType storeType);

    /**
     * Checks if sufficient stock is available for a sale.
     */
    boolean hasAvailableStock(String productCode, StoreType storeType, int requiredQuantity);

    /**
     * Gets the next batch to use for a sale (FIFO based on expiry date).
     */
    Optional<BatchAllocation> getNextBatchForSale(String productCode, StoreType storeType, int requiredQuantity);

    /**
     * Allocates stock for a sale across multiple batches if needed (FIFO).
     * Returns the list of batch allocations needed to fulfill the quantity.
     */
    List<BatchAllocation> allocateStockForSale(String productCode, StoreType storeType, int quantity);

    // ==================== Async Operations (using InventoryThreadPool)
    // ====================

    /**
     * Async version of restockPhysicalStore.
     * Executes on the InventoryThreadPool.
     */
    CompletableFuture<RestockResult> restockPhysicalStoreAsync(String productCode, int quantity);

    /**
     * Async version of restockOnlineStore.
     * Executes on the InventoryThreadPool.
     */
    CompletableFuture<RestockResult> restockOnlineStoreAsync(String productCode, int quantity);

    /**
     * Async version of allocateStockForSale.
     * Executes on the InventoryThreadPool.
     */
    CompletableFuture<List<BatchAllocation>> allocateStockForSaleAsync(String productCode, StoreType storeType,
            int quantity);

    /**
     * Async check for stock availability.
     * Executes on the InventoryThreadPool.
     */
    CompletableFuture<Boolean> hasAvailableStockAsync(String productCode, StoreType storeType, int requiredQuantity);

    /**
     * Batch allocation result for sales.
     */
    record BatchAllocation(
            Integer batchId,
            String productCode,
            int quantity,
            java.time.LocalDate expiryDate) {
    }

    /**
     * Result of a restock operation.
     */
    record RestockResult(
            boolean success,
            int quantityRestocked,
            int batchesUsed,
            String message) {
        public static RestockResult success(int quantity, int batches) {
            return new RestockResult(true, quantity, batches, "Restock successful");
        }

        public static RestockResult partial(int quantity, int batches, String message) {
            return new RestockResult(true, quantity, batches, message);
        }

        public static RestockResult failure(String message) {
            return new RestockResult(false, 0, 0, message);
        }
    }
}
