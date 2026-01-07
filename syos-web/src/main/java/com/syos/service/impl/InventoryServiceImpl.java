package com.syos.service.impl;

import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.MainInventoryRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.interfaces.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryService for main inventory management.
 */
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final MainInventoryRepository mainInventoryRepository;
    private final ProductRepository productRepository;

    public InventoryServiceImpl(MainInventoryRepository mainInventoryRepository,
                                 ProductRepository productRepository) {
        this.mainInventoryRepository = mainInventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public MainInventory addBatch(String productCode, int quantity, BigDecimal purchasePrice,
                                   LocalDate purchaseDate, LocalDate expiryDate, String supplierName) {
        logger.debug("Adding batch for product: {}, quantity: {}", productCode, quantity);

        // Validate product exists
        if (!productRepository.existsByProductCode(productCode)) {
            throw new ProductNotFoundException(productCode);
        }

        // Validate inputs
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Purchase price must be non-negative");
        }
        if (purchaseDate == null) {
            purchaseDate = LocalDate.now();
        }
        if (expiryDate != null && expiryDate.isBefore(purchaseDate)) {
            throw new ValidationException("Expiry date cannot be before purchase date");
        }

        MainInventory batch = new MainInventory();
        batch.setProductCode(new ProductCode(productCode));
        batch.setQuantityReceived(quantity);
        batch.setRemainingQuantity(quantity);
        batch.setPurchasePrice(new Money(purchasePrice));
        batch.setPurchaseDate(purchaseDate);
        batch.setExpiryDate(expiryDate);
        batch.setSupplierName(supplierName);

        MainInventory saved = mainInventoryRepository.save(batch);
        logger.info("Batch added for product: {}, batch ID: {}, quantity: {}",
            productCode, saved.getMainInventoryId(), quantity);
        return saved;
    }

    @Override
    public Optional<MainInventory> findBatchById(Integer batchId) {
        return mainInventoryRepository.findById(batchId);
    }

    @Override
    public List<MainInventory> findBatchesByProductCode(String productCode) {
        return mainInventoryRepository.findByProductCode(productCode);
    }

    @Override
    public List<MainInventory> findAvailableBatches(String productCode) {
        return mainInventoryRepository.findAvailableBatchesByProductCode(productCode);
    }

    @Override
    public Optional<MainInventory> getNextBatchForSale(String productCode, int requiredQuantity) {
        return mainInventoryRepository.findNextBatchForSale(productCode, requiredQuantity);
    }

    @Override
    public int getTotalRemainingQuantity(String productCode) {
        return mainInventoryRepository.getTotalRemainingQuantity(productCode);
    }

    @Override
    public boolean reduceQuantity(Integer batchId, int amount) {
        logger.debug("Reducing quantity for batch: {} by {}", batchId, amount);

        if (amount <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        boolean success = mainInventoryRepository.reduceQuantity(batchId, amount);
        if (success) {
            logger.info("Reduced quantity for batch: {} by {}", batchId, amount);
        } else {
            logger.warn("Failed to reduce quantity for batch: {} by {} - insufficient stock", batchId, amount);
        }
        return success;
    }

    @Override
    public boolean increaseQuantity(Integer batchId, int amount) {
        logger.debug("Increasing quantity for batch: {} by {}", batchId, amount);

        if (amount <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        boolean success = mainInventoryRepository.increaseQuantity(batchId, amount);
        if (success) {
            logger.info("Increased quantity for batch: {} by {}", batchId, amount);
        }
        return success;
    }

    @Override
    public List<MainInventory> findExpiringWithinDays(int days) {
        if (days < 0) {
            throw new ValidationException("Days must be non-negative");
        }
        return mainInventoryRepository.findExpiringWithinDays(days);
    }

    @Override
    public List<MainInventory> findExpiredBatches() {
        return mainInventoryRepository.findExpiredBatches();
    }

    @Override
    public List<MainInventory> findBySupplier(String supplierName) {
        return mainInventoryRepository.findBySupplier(supplierName);
    }

    @Override
    public List<MainInventory> findByPurchaseDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be before start date");
        }
        return mainInventoryRepository.findByPurchaseDateRange(startDate, endDate);
    }

    @Override
    public List<MainInventory> findAll(int page, int size) {
        int offset = page * size;
        return mainInventoryRepository.findAll(offset, size);
    }

    @Override
    public long getBatchCount() {
        return mainInventoryRepository.count();
    }

    @Override
    public boolean hasAvailableStock(String productCode, int requiredQuantity) {
        int available = getTotalRemainingQuantity(productCode);
        return available >= requiredQuantity;
    }

    @Override
    public List<ProductInventorySummary> getInventorySummary() {
        List<MainInventory> allBatches = mainInventoryRepository.findAll();

        // Group by product code
        Map<String, List<MainInventory>> byProduct = allBatches.stream()
            .collect(Collectors.groupingBy(MainInventory::getProductCodeString));

        List<ProductInventorySummary> summaries = new ArrayList<>();

        for (Map.Entry<String, List<MainInventory>> entry : byProduct.entrySet()) {
            String productCode = entry.getKey();
            List<MainInventory> batches = entry.getValue();

            int totalQuantity = batches.stream()
                .mapToInt(MainInventory::getRemainingQuantity)
                .sum();

            LocalDate earliestExpiry = batches.stream()
                .filter(b -> b.getExpiryDate() != null && b.getRemainingQuantity() > 0)
                .map(MainInventory::getExpiryDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

            String productName = batches.isEmpty() ? productCode :
                (batches.get(0).getProductName() != null ? batches.get(0).getProductName() : productCode);

            summaries.add(new ProductInventorySummary(
                productCode,
                productName,
                totalQuantity,
                batches.size(),
                earliestExpiry
            ));
        }

        return summaries;
    }
}
