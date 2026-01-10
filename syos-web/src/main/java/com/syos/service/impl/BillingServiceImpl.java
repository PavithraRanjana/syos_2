package com.syos.service.impl;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.domain.models.InventoryTransaction;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.BillNotFoundException;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.InvalidPaymentException;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.BillItemRepository;
import com.syos.repository.interfaces.BillRepository;
import com.syos.repository.interfaces.InventoryTransactionRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.StoreInventoryService.BatchAllocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of BillingService with FIFO stock allocation.
 */
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final ProductRepository productRepository;
    private final StoreInventoryService storeInventoryService;
    private final InventoryTransactionRepository transactionRepository;

    // In-memory storage for bills in progress (before finalization)
    private final Map<Integer, Bill> billsInProgress = new ConcurrentHashMap<>();

    public BillingServiceImpl(
            BillRepository billRepository,
            BillItemRepository billItemRepository,
            ProductRepository productRepository,
            StoreInventoryService storeInventoryService,
            InventoryTransactionRepository transactionRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.productRepository = productRepository;
        this.storeInventoryService = storeInventoryService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Bill createBill(StoreType storeType, TransactionType transactionType, Integer customerId, String cashierId) {
        logger.debug("Creating bill: storeType={}, transactionType={}, customerId={}", storeType, transactionType,
                customerId);

        if (storeType == null) {
            throw new ValidationException("Store type is required");
        }
        if (transactionType == null) {
            throw new ValidationException("Transaction type is required");
        }

        // Online orders require a customer
        if (storeType == StoreType.ONLINE && customerId == null) {
            throw new ValidationException("Customer ID is required for online orders");
        }

        String serialNumber = generateSerialNumber(storeType);

        Bill bill = new Bill();
        bill.setSerialNumber(new BillSerialNumber(serialNumber));
        bill.setStoreType(storeType);
        bill.setTransactionType(transactionType);
        bill.setCustomerId(customerId);
        bill.setCashierId(cashierId);
        bill.setBillDate(LocalDateTime.now());

        // Save to get an ID
        Bill saved = billRepository.save(bill);
        billsInProgress.put(saved.getBillId(), saved);

        logger.info("Bill created: {} (ID: {})", serialNumber, saved.getBillId());
        return saved;
    }

    @Override
    public BillItem addItem(Integer billId, String productCode, int quantity) {
        logger.debug("Adding item to bill {}: {} x {}", billId, productCode, quantity);

        Bill bill = getBillInProgress(billId);

        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        // Get product
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));

        if (!product.isActive()) {
            throw new ValidationException("Product is not active: " + productCode);
        }

        // Check stock availability
        if (!storeInventoryService.hasAvailableStock(productCode, bill.getStoreType(), quantity)) {
            int available = storeInventoryService.getAvailableQuantity(productCode, bill.getStoreType());
            throw InsufficientStockException.forProduct(productCode, available, quantity);
        }

        // Get batch allocations (FIFO)
        List<BatchAllocation> allocations = storeInventoryService.allocateStockForSale(
                productCode, bill.getStoreType(), quantity);

        // Create bill items for each batch allocation
        List<BillItem> createdItems = new ArrayList<>();
        for (BatchAllocation allocation : allocations) {
            BillItem item = new BillItem();
            item.setBillId(billId);
            item.setProductCode(new ProductCode(productCode));
            item.setProductName(product.getProductName());
            item.setMainInventoryId(allocation.batchId());
            item.setQuantity(allocation.quantity());
            item.setUnitPrice(product.getUnitPrice());
            item.recalculateTotal();

            BillItem saved = billItemRepository.save(item);
            createdItems.add(saved);
            bill.addItem(saved);
        }

        // Update bill totals
        bill.calculateTotals();
        billRepository.save(bill);

        logger.info("Added {} item(s) to bill {}: {} x {} (from {} batches)",
                createdItems.size(), billId, productCode, quantity, allocations.size());

        // Return the first item (or a combined view if multiple batches)
        return createdItems.isEmpty() ? null : createdItems.get(0);
    }

    @Override
    public BillItem updateItemQuantity(Integer billItemId, int newQuantity) {
        logger.debug("Updating bill item {} quantity to {}", billItemId, newQuantity);

        if (newQuantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }

        BillItem item = billItemRepository.findById(billItemId)
                .orElseThrow(() -> new ValidationException("Bill item not found: " + billItemId));

        Bill bill = getBillInProgress(item.getBillId());

        // Check stock availability for the difference
        int currentQty = item.getQuantity();
        if (newQuantity > currentQty) {
            int additionalNeeded = newQuantity - currentQty;
            String productCode = item.getProductCodeString();
            if (!storeInventoryService.hasAvailableStock(productCode, bill.getStoreType(), additionalNeeded)) {
                int available = storeInventoryService.getAvailableQuantity(productCode, bill.getStoreType());
                throw InsufficientStockException.forProduct(productCode, available + currentQty, newQuantity);
            }
        }

        item.updateQuantity(newQuantity);
        BillItem updated = billItemRepository.save(item);

        // Update bill totals
        refreshBillItems(bill);
        bill.calculateTotals();
        billRepository.save(bill);

        logger.info("Updated bill item {} quantity to {}", billItemId, newQuantity);
        return updated;
    }

    @Override
    public boolean removeItem(Integer billItemId) {
        logger.debug("Removing bill item {}", billItemId);

        BillItem item = billItemRepository.findById(billItemId)
                .orElseThrow(() -> new ValidationException("Bill item not found: " + billItemId));

        Bill bill = getBillInProgress(item.getBillId());

        billItemRepository.deleteById(billItemId);

        // Update bill
        refreshBillItems(bill);
        bill.calculateTotals();
        billRepository.save(bill);

        logger.info("Removed bill item {}", billItemId);
        return true;
    }

    @Override
    public void clearItems(Integer billId) {
        logger.debug("Clearing all items from bill {}", billId);

        Bill bill = getBillInProgress(billId);

        billItemRepository.deleteByBillId(billId);
        bill.clearItems();
        billRepository.save(bill);

        logger.info("Cleared all items from bill {}", billId);
    }

    @Override
    public Bill applyDiscount(Integer billId, BigDecimal discountAmount) {
        logger.debug("Applying discount {} to bill {}", discountAmount, billId);

        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Discount amount must be non-negative");
        }

        Bill bill = getBillInProgress(billId);
        bill.applyDiscount(new Money(discountAmount));
        Bill updated = billRepository.save(bill);

        logger.info("Applied discount {} to bill {}", discountAmount, billId);
        return updated;
    }

    @Override
    public Bill processCashPayment(Integer billId, BigDecimal tenderedAmount) {
        logger.debug("Processing cash payment for bill {}: tendered {}", billId, tenderedAmount);

        Bill bill = getBillInProgress(billId);

        if (bill.getStoreType() != StoreType.PHYSICAL) {
            throw new InvalidPaymentException("Cash payment only allowed for physical store transactions");
        }

        if (tenderedAmount == null || tenderedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Tendered amount must be positive");
        }

        Money tendered = new Money(tenderedAmount);
        if (tendered.isLessThan(bill.getTotalAmount())) {
            throw new InvalidPaymentException(
                    "Insufficient payment. Required: " + bill.getTotalAmount().format() +
                            ", Tendered: " + tendered.format());
        }

        bill.processCashPayment(tendered);
        Bill updated = billRepository.save(bill);

        logger.info("Cash payment processed for bill {}: tendered {}, change {}",
                billId, tenderedAmount, bill.getChangeAmount());
        return updated;
    }

    @Override
    public Bill processOnlinePayment(Integer billId) {
        logger.debug("Processing online payment for bill {}", billId);

        Bill bill = getBillInProgress(billId);

        if (bill.getStoreType() != StoreType.ONLINE) {
            throw new InvalidPaymentException("Online payment only allowed for online store transactions");
        }

        // Online payments are handled externally, just mark as paid
        bill.setTenderedAmount(bill.getTotalAmount());
        bill.setChangeAmount(Money.ZERO);
        Bill updated = billRepository.save(bill);

        logger.info("Online payment processed for bill {}", billId);
        return updated;
    }

    @Override
    public Bill finalizeBill(Integer billId) {
        logger.debug("Finalizing bill {}", billId);

        Bill bill = getBillInProgress(billId);

        // Validate
        ValidationResult validation = validateBillForFinalization(billId);
        if (!validation.isValid()) {
            throw new ValidationException("Bill validation failed: " + String.join(", ", validation.errors()));
        }

        // Deduct stock for each item
        List<BillItem> items = billItemRepository.findByBillId(billId);
        for (BillItem item : items) {
            boolean deducted;
            if (bill.getStoreType() == StoreType.PHYSICAL) {
                deducted = storeInventoryService.reducePhysicalStoreStock(
                        item.getProductCodeString(), item.getMainInventoryId(), item.getQuantity());
            } else {
                deducted = storeInventoryService.reduceOnlineStoreStock(
                        item.getProductCodeString(), item.getMainInventoryId(), item.getQuantity());
            }

            if (!deducted) {
                throw InsufficientStockException.forProduct(item.getProductCodeString(), 0, item.getQuantity());
            }

            // Log transaction
            logSaleTransaction(bill, item);
        }

        // Remove from in-progress
        billsInProgress.remove(billId);

        logger.info("Bill finalized: {} (ID: {})", bill.getSerialNumberString(), billId);
        return bill;
    }

    @Override
    public boolean cancelBill(Integer billId) {
        logger.debug("Cancelling bill {}", billId);

        Bill bill = billsInProgress.get(billId);
        if (bill == null) {
            // Check if it's a finalized bill
            Optional<Bill> existing = billRepository.findById(billId);
            if (existing.isPresent()) {
                throw new ValidationException("Cannot cancel a finalized bill");
            }
            throw new BillNotFoundException(billId);
        }

        // Clear items and delete bill
        billItemRepository.deleteByBillId(billId);
        billRepository.deleteById(billId);
        billsInProgress.remove(billId);

        logger.info("Bill cancelled: {}", billId);
        return true;
    }

    @Override
    public Optional<Bill> findBillById(Integer billId) {
        // Check in-progress first
        Bill inProgress = billsInProgress.get(billId);
        if (inProgress != null) {
            return Optional.of(inProgress);
        }
        return billRepository.findById(billId);
    }

    @Override
    public Optional<Bill> findBillBySerialNumber(String serialNumber) {
        return billRepository.findBySerialNumber(serialNumber);
    }

    @Override
    public List<Bill> findBillsByDate(LocalDate date) {
        return billRepository.findByDate(date);
    }

    @Override
    public List<Bill> findBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Bill> findBillsByCustomer(Integer customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Bill> findRecentBills(int limit) {
        return billRepository.findRecent(limit);
    }

    @Override
    public List<BillItem> getBillItems(Integer billId) {
        return billItemRepository.findByBillId(billId);
    }

    @Override
    public String generateSerialNumber(StoreType storeType) {
        return billRepository.generateNextSerialNumber(storeType);
    }

    @Override
    public BigDecimal getTodaysSales() {
        return billRepository.getTotalSalesForDate(LocalDate.now());
    }

    @Override
    public int getTodaysBillCount() {
        return billRepository.getBillCountForDate(LocalDate.now());
    }

    @Override
    public ValidationResult validateBillForFinalization(Integer billId) {
        List<String> errors = new ArrayList<>();

        Bill bill = billsInProgress.get(billId);
        if (bill == null) {
            errors.add("Bill not found or already finalized");
            return ValidationResult.invalid(errors);
        }

        List<BillItem> items = billItemRepository.findByBillId(billId);
        if (items.isEmpty()) {
            errors.add("Bill has no items");
        }

        // Check payment for cash transactions
        if (bill.getTransactionType() == TransactionType.CASH) {
            if (bill.getTenderedAmount() == null ||
                    bill.getTenderedAmount().isLessThan(bill.getTotalAmount())) {
                errors.add("Cash payment not completed");
            }
        }

        // Verify stock availability for each item
        for (BillItem item : items) {
            boolean hasStock;
            if (bill.getStoreType() == StoreType.PHYSICAL) {
                hasStock = storeInventoryService.hasAvailableStock(
                        item.getProductCodeString(), StoreType.PHYSICAL, item.getQuantity());
            } else {
                hasStock = storeInventoryService.hasAvailableStock(
                        item.getProductCodeString(), StoreType.ONLINE, item.getQuantity());
            }
            if (!hasStock) {
                errors.add("Insufficient stock for " + item.getProductName());
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(errors);
    }

    // ==================== Helper Methods ====================

    private Bill getBillInProgress(Integer billId) {
        Bill bill = billsInProgress.get(billId);
        if (bill == null) {
            // Try to load from database
            bill = billRepository.findById(billId)
                    .orElseThrow(() -> new BillNotFoundException(billId));
            billsInProgress.put(billId, bill);
        }
        return bill;
    }

    private void refreshBillItems(Bill bill) {
        List<BillItem> items = billItemRepository.findByBillId(bill.getBillId());
        bill.setItems(items);
    }

    private void logSaleTransaction(Bill bill, BillItem item) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductCode(item.getProductCode());
        transaction.setMainInventoryId(item.getMainInventoryId());
        transaction.setTransactionType(InventoryTransactionType.SALE);
        transaction.setStoreType(bill.getStoreType());
        transaction.setQuantityChanged(-item.getQuantity()); // Negative for sales
        transaction.setBillId(bill.getBillId());
        transaction.setRemarks("Sale: Bill " + bill.getSerialNumberString());
        transactionRepository.save(transaction);
    }

    // ==================== POS Checkout (Single Atomic Transaction)
    // ====================

    @Override
    public StockCheckResult checkStock(String productCode, int quantity, StoreType storeType) {
        logger.debug("Checking stock for {} x {} in {}", productCode, quantity, storeType);

        // Find product
        Optional<Product> productOpt = productRepository.findByProductCode(productCode);
        if (productOpt.isEmpty()) {
            return StockCheckResult.notFound(productCode);
        }

        Product product = productOpt.get();
        if (!product.isActive()) {
            return StockCheckResult.notFound(productCode + " (inactive)");
        }

        // Check available stock
        int available = storeInventoryService.getAvailableQuantity(productCode, storeType);
        if (available < quantity) {
            return StockCheckResult.unavailable(productCode, quantity, available);
        }

        return StockCheckResult.available(
                productCode,
                product.getProductName(),
                product.getUnitPrice().getAmount(),
                quantity,
                available);
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) {
        logger.info("Processing checkout: {} items, storeType={}, transactionType={}",
                request.items().size(), request.storeType(), request.transactionType());

        List<String> errors = new ArrayList<>();

        // Validate basic request
        if (request.storeType() == null) {
            errors.add("Store type is required");
        }
        if (request.transactionType() == null) {
            errors.add("Transaction type is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            errors.add("Cart is empty - add items before checkout");
        }
        if (request.storeType() == StoreType.ONLINE && request.customerId() == null) {
            errors.add("Customer ID is required for online orders");
        }

        if (!errors.isEmpty()) {
            return CheckoutResult.failure(errors);
        }

        // Use parallel stream for concurrent stock validation
        List<StockCheckResult> stockResults = request.items().parallelStream()
                .map(item -> checkStock(item.productCode(), item.quantity(), request.storeType()))
                .toList();

        // Collect any stock errors
        for (StockCheckResult result : stockResults) {
            if (!result.available()) {
                errors.add(result.message());
            }
        }

        if (!errors.isEmpty()) {
            return CheckoutResult.failure(errors);
        }

        // Calculate subtotal from stock results (which have prices)
        BigDecimal subtotal = stockResults.stream()
                .map(r -> r.unitPrice().multiply(BigDecimal.valueOf(r.requestedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validate discount
        BigDecimal discount = request.discount() != null ? request.discount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) {
            return CheckoutResult.failure("Discount cannot exceed subtotal of " + subtotal);
        }

        BigDecimal tax = BigDecimal.ZERO; // Tax calculation can be added later
        BigDecimal total = subtotal.subtract(discount).add(tax);

        // Validate cash payment
        BigDecimal cashTendered = request.cashTendered() != null ? request.cashTendered() : BigDecimal.ZERO;
        if (request.transactionType() == TransactionType.CASH) {
            if (cashTendered.compareTo(total) < 0) {
                return CheckoutResult
                        .failure("Insufficient cash tendered. Required: " + total + ", Tendered: " + cashTendered);
            }
        } else {
            // For online/credit, tendered equals total
            cashTendered = total;
        }

        BigDecimal change = cashTendered.subtract(total);

        // === All validations passed - Create the bill atomically ===

        String serialNumber = generateSerialNumber(request.storeType());
        LocalDateTime billDate = LocalDateTime.now();

        Bill bill = new Bill();
        bill.setSerialNumber(new BillSerialNumber(serialNumber));
        bill.setStoreType(request.storeType());
        bill.setTransactionType(request.transactionType());
        bill.setCustomerId(request.customerId());
        bill.setCashierId(request.cashierId());
        bill.setBillDate(billDate);
        bill.setTenderedAmount(new Money(cashTendered));
        bill.setChangeAmount(new Money(change));
        bill.setDiscountAmount(new Money(discount));

        Bill savedBill = billRepository.save(bill);
        Integer billId = savedBill.getBillId();

        // Create bill items and deduct stock
        List<ItemDetail> itemDetails = new ArrayList<>();

        for (int i = 0; i < request.items().size(); i++) {
            ItemRequest itemReq = request.items().get(i);
            StockCheckResult stockResult = stockResults.get(i);

            // Get batch allocations (FIFO)
            List<BatchAllocation> allocations = storeInventoryService.allocateStockForSale(
                    itemReq.productCode(), request.storeType(), itemReq.quantity());

            for (BatchAllocation allocation : allocations) {
                BillItem billItem = new BillItem();
                billItem.setBillId(billId);
                billItem.setProductCode(new ProductCode(itemReq.productCode()));
                billItem.setProductName(stockResult.productName());
                billItem.setMainInventoryId(allocation.batchId());
                billItem.setQuantity(allocation.quantity());
                billItem.setUnitPrice(new Money(stockResult.unitPrice()));
                billItem.recalculateTotal();

                BillItem savedItem = billItemRepository.save(billItem);
                savedBill.addItem(savedItem);

                // Deduct stock immediately
                if (request.storeType() == StoreType.PHYSICAL) {
                    storeInventoryService.reducePhysicalStoreStock(
                            itemReq.productCode(), allocation.batchId(), allocation.quantity());
                } else {
                    storeInventoryService.reduceOnlineStoreStock(
                            itemReq.productCode(), allocation.batchId(), allocation.quantity());
                }

                // Log transaction
                logSaleTransaction(savedBill, savedItem);
            }

            // Add to receipt details (aggregate by product for display)
            BigDecimal lineTotal = stockResult.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            itemDetails.add(new ItemDetail(
                    stockResult.productName(), // Name only, no code
                    itemReq.quantity(),
                    stockResult.unitPrice(),
                    lineTotal));
        }

        // Set bill totals explicitly (calculateTotals may not work if items list isn't
        // in memory)
        savedBill.setSubtotal(new Money(subtotal));
        savedBill.setTaxAmount(new Money(tax));
        savedBill.setTotalAmount(new Money(total));
        billRepository.save(savedBill);

        logger.info("Checkout complete: Bill {} (ID: {}), Total: {}, Items: {}",
                serialNumber, billId, total, itemDetails.size());

        return CheckoutResult.success(
                billId,
                serialNumber,
                subtotal,
                discount,
                tax,
                total,
                cashTendered,
                change,
                billDate,
                itemDetails);
    }
}
