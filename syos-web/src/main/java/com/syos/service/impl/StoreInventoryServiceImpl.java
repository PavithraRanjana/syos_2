package com.syos.service.impl;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.InventoryTransaction;
import com.syos.domain.models.MainInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.InventoryTransactionRepository;
import com.syos.repository.interfaces.MainInventoryRepository;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.repository.interfaces.PhysicalStoreInventoryRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.interfaces.StoreInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StoreInventoryService for managing physical and online store inventories.
 */
public class StoreInventoryServiceImpl implements StoreInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(StoreInventoryServiceImpl.class);

    private final PhysicalStoreInventoryRepository physicalStoreRepository;
    private final OnlineStoreInventoryRepository onlineStoreRepository;
    private final MainInventoryRepository mainInventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    public StoreInventoryServiceImpl(
            PhysicalStoreInventoryRepository physicalStoreRepository,
            OnlineStoreInventoryRepository onlineStoreRepository,
            MainInventoryRepository mainInventoryRepository,
            InventoryTransactionRepository transactionRepository,
            ProductRepository productRepository) {
        this.physicalStoreRepository = physicalStoreRepository;
        this.onlineStoreRepository = onlineStoreRepository;
        this.mainInventoryRepository = mainInventoryRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    // ==================== Physical Store Operations ====================

    @Override
    public RestockResult restockPhysicalStore(String productCode, int quantity) {
        logger.debug("Restocking physical store: {} quantity: {}", productCode, quantity);

        validateProductExists(productCode);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        List<MainInventory> availableBatches = mainInventoryRepository.findAvailableBatchesByProductCode(productCode);
        if (availableBatches.isEmpty()) {
            return RestockResult.failure("No available batches in main inventory");
        }

        int totalRestocked = 0;
        int batchesUsed = 0;
        int remainingQuantity = quantity;

        // FIFO - process batches in order (already sorted by expiry date)
        for (MainInventory batch : availableBatches) {
            if (remainingQuantity <= 0) break;

            int available = batch.getRemainingQuantity();
            int toRestock = Math.min(available, remainingQuantity);

            if (toRestock > 0) {
                // Reduce from main inventory
                boolean reduced = mainInventoryRepository.reduceQuantity(batch.getMainInventoryId(), toRestock);
                if (reduced) {
                    // Add to physical store
                    physicalStoreRepository.addQuantity(productCode, batch.getMainInventoryId(), toRestock);

                    // Log transaction
                    logTransaction(productCode, batch.getMainInventoryId(), InventoryTransactionType.RESTOCK,
                        StoreType.PHYSICAL, toRestock, "Restocked to physical store shelves");

                    totalRestocked += toRestock;
                    remainingQuantity -= toRestock;
                    batchesUsed++;
                }
            }
        }

        if (totalRestocked == 0) {
            return RestockResult.failure("Failed to restock - no stock available");
        }

        if (totalRestocked < quantity) {
            logger.warn("Partial restock for {}: requested {}, restocked {}", productCode, quantity, totalRestocked);
            return RestockResult.partial(totalRestocked, batchesUsed,
                "Partial restock: only " + totalRestocked + " of " + quantity + " units available");
        }

        logger.info("Restocked physical store: {} quantity: {} from {} batches", productCode, totalRestocked, batchesUsed);
        return RestockResult.success(totalRestocked, batchesUsed);
    }

    @Override
    public boolean restockPhysicalStoreFromBatch(String productCode, Integer batchId, int quantity) {
        logger.debug("Restocking physical store from batch {}: {} quantity: {}", batchId, productCode, quantity);

        validateProductExists(productCode);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        // Reduce from main inventory
        boolean reduced = mainInventoryRepository.reduceQuantity(batchId, quantity);
        if (!reduced) {
            throw new InsufficientStockException(productCode, 0, quantity);
        }

        // Add to physical store
        physicalStoreRepository.addQuantity(productCode, batchId, quantity);

        // Log transaction
        logTransaction(productCode, batchId, InventoryTransactionType.RESTOCK,
            StoreType.PHYSICAL, quantity, "Restocked to physical store from batch " + batchId);

        logger.info("Restocked physical store from batch {}: {} quantity: {}", batchId, productCode, quantity);
        return true;
    }

    @Override
    public List<PhysicalStoreInventory> getPhysicalStoreStock(String productCode) {
        return physicalStoreRepository.findAvailableByProductCode(productCode);
    }

    @Override
    public int getPhysicalStoreQuantity(String productCode) {
        return physicalStoreRepository.getTotalQuantityOnShelf(productCode);
    }

    @Override
    public boolean reducePhysicalStoreStock(String productCode, Integer batchId, int quantity) {
        return physicalStoreRepository.reduceQuantity(productCode, batchId, quantity);
    }

    @Override
    public List<ProductStockSummary> getPhysicalStoreStockSummary() {
        // Convert from PhysicalStoreInventoryRepository.ProductStockSummary to common type
        return physicalStoreRepository.getStockSummary().stream()
            .map(s -> new ProductStockSummary(s.productCode(), s.productName(), s.totalQuantity(), s.batchCount()))
            .toList();
    }

    @Override
    public List<PhysicalStoreInventory> getPhysicalStoreLowStock(int threshold) {
        return physicalStoreRepository.findLowStock(threshold);
    }

    // ==================== Online Store Operations ====================

    @Override
    public RestockResult restockOnlineStore(String productCode, int quantity) {
        logger.debug("Restocking online store: {} quantity: {}", productCode, quantity);

        validateProductExists(productCode);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        List<MainInventory> availableBatches = mainInventoryRepository.findAvailableBatchesByProductCode(productCode);
        if (availableBatches.isEmpty()) {
            return RestockResult.failure("No available batches in main inventory");
        }

        int totalRestocked = 0;
        int batchesUsed = 0;
        int remainingQuantity = quantity;

        // FIFO - process batches in order (already sorted by expiry date)
        for (MainInventory batch : availableBatches) {
            if (remainingQuantity <= 0) break;

            int available = batch.getRemainingQuantity();
            int toRestock = Math.min(available, remainingQuantity);

            if (toRestock > 0) {
                // Reduce from main inventory
                boolean reduced = mainInventoryRepository.reduceQuantity(batch.getMainInventoryId(), toRestock);
                if (reduced) {
                    // Add to online store
                    onlineStoreRepository.addQuantity(productCode, batch.getMainInventoryId(), toRestock);

                    // Log transaction
                    logTransaction(productCode, batch.getMainInventoryId(), InventoryTransactionType.RESTOCK,
                        StoreType.ONLINE, toRestock, "Restocked to online store");

                    totalRestocked += toRestock;
                    remainingQuantity -= toRestock;
                    batchesUsed++;
                }
            }
        }

        if (totalRestocked == 0) {
            return RestockResult.failure("Failed to restock - no stock available");
        }

        if (totalRestocked < quantity) {
            logger.warn("Partial restock for {}: requested {}, restocked {}", productCode, quantity, totalRestocked);
            return RestockResult.partial(totalRestocked, batchesUsed,
                "Partial restock: only " + totalRestocked + " of " + quantity + " units available");
        }

        logger.info("Restocked online store: {} quantity: {} from {} batches", productCode, totalRestocked, batchesUsed);
        return RestockResult.success(totalRestocked, batchesUsed);
    }

    @Override
    public boolean restockOnlineStoreFromBatch(String productCode, Integer batchId, int quantity) {
        logger.debug("Restocking online store from batch {}: {} quantity: {}", batchId, productCode, quantity);

        validateProductExists(productCode);
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        // Reduce from main inventory
        boolean reduced = mainInventoryRepository.reduceQuantity(batchId, quantity);
        if (!reduced) {
            throw new InsufficientStockException(productCode, 0, quantity);
        }

        // Add to online store
        onlineStoreRepository.addQuantity(productCode, batchId, quantity);

        // Log transaction
        logTransaction(productCode, batchId, InventoryTransactionType.RESTOCK,
            StoreType.ONLINE, quantity, "Restocked to online store from batch " + batchId);

        logger.info("Restocked online store from batch {}: {} quantity: {}", batchId, productCode, quantity);
        return true;
    }

    @Override
    public List<OnlineStoreInventory> getOnlineStoreStock(String productCode) {
        return onlineStoreRepository.findAvailableByProductCode(productCode);
    }

    @Override
    public int getOnlineStoreQuantity(String productCode) {
        return onlineStoreRepository.getTotalQuantityAvailable(productCode);
    }

    @Override
    public boolean reduceOnlineStoreStock(String productCode, Integer batchId, int quantity) {
        return onlineStoreRepository.reduceQuantity(productCode, batchId, quantity);
    }

    @Override
    public List<ProductStockSummary> getOnlineStoreStockSummary() {
        return onlineStoreRepository.getStockSummary();
    }

    @Override
    public List<OnlineStoreInventory> getOnlineStoreLowStock(int threshold) {
        return onlineStoreRepository.findLowStock(threshold);
    }

    // ==================== Common Operations ====================

    @Override
    public int getAvailableQuantity(String productCode, StoreType storeType) {
        return switch (storeType) {
            case PHYSICAL -> getPhysicalStoreQuantity(productCode);
            case ONLINE -> getOnlineStoreQuantity(productCode);
        };
    }

    @Override
    public boolean hasAvailableStock(String productCode, StoreType storeType, int requiredQuantity) {
        int available = getAvailableQuantity(productCode, storeType);
        return available >= requiredQuantity;
    }

    @Override
    public Optional<BatchAllocation> getNextBatchForSale(String productCode, StoreType storeType, int requiredQuantity) {
        List<BatchAllocation> allocations = allocateStockForSale(productCode, storeType, requiredQuantity);
        return allocations.isEmpty() ? Optional.empty() : Optional.of(allocations.get(0));
    }

    @Override
    public List<BatchAllocation> allocateStockForSale(String productCode, StoreType storeType, int quantity) {
        logger.debug("Allocating stock for sale: {} store: {} quantity: {}", productCode, storeType, quantity);

        List<BatchAllocation> allocations = new ArrayList<>();
        int remainingQuantity = quantity;

        if (storeType == StoreType.PHYSICAL) {
            List<PhysicalStoreInventory> available = physicalStoreRepository.findAvailableByProductCode(productCode);
            for (PhysicalStoreInventory inv : available) {
                if (remainingQuantity <= 0) break;

                int toAllocate = Math.min(inv.getQuantityOnShelf(), remainingQuantity);
                if (toAllocate > 0) {
                    allocations.add(new BatchAllocation(
                        inv.getMainInventoryId(),
                        productCode,
                        toAllocate,
                        inv.getExpiryDate()
                    ));
                    remainingQuantity -= toAllocate;
                }
            }
        } else {
            List<OnlineStoreInventory> available = onlineStoreRepository.findAvailableByProductCode(productCode);
            for (OnlineStoreInventory inv : available) {
                if (remainingQuantity <= 0) break;

                int toAllocate = Math.min(inv.getQuantityAvailable(), remainingQuantity);
                if (toAllocate > 0) {
                    allocations.add(new BatchAllocation(
                        inv.getMainInventoryId(),
                        productCode,
                        toAllocate,
                        inv.getExpiryDate()
                    ));
                    remainingQuantity -= toAllocate;
                }
            }
        }

        if (remainingQuantity > 0) {
            int available = quantity - remainingQuantity;
            throw new InsufficientStockException(productCode, available, quantity);
        }

        return allocations;
    }

    // ==================== Helper Methods ====================

    private void validateProductExists(String productCode) {
        if (!productRepository.existsByProductCode(productCode)) {
            throw new ProductNotFoundException(productCode);
        }
    }

    private void logTransaction(String productCode, Integer batchId, InventoryTransactionType type,
                                 StoreType storeType, int quantity, String remarks) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductCode(new ProductCode(productCode));
        transaction.setMainInventoryId(batchId);
        transaction.setTransactionType(type);
        transaction.setStoreType(storeType);
        transaction.setQuantityChanged(type == InventoryTransactionType.SALE ? -quantity : quantity);
        transaction.setRemarks(remarks);
        transactionRepository.save(transaction);
    }
}
