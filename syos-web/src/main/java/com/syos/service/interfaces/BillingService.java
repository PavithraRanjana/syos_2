package com.syos.service.interfaces;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Billing operations.
 * Handles bill creation, item management, payment processing, and stock
 * deduction.
 */
public interface BillingService {

    /**
     * Creates a new bill/transaction.
     */
    Bill createBill(StoreType storeType, TransactionType transactionType, Integer customerId, String cashierId);

    /**
     * Adds an item to a bill with automatic FIFO batch allocation.
     */
    BillItem addItem(Integer billId, String productCode, int quantity);

    /**
     * Updates the quantity of an existing bill item.
     */
    BillItem updateItemQuantity(Integer billItemId, int newQuantity);

    /**
     * Removes an item from a bill.
     */
    boolean removeItem(Integer billItemId);

    /**
     * Clears all items from a bill.
     */
    void clearItems(Integer billId);

    /**
     * Applies a discount to a bill.
     */
    Bill applyDiscount(Integer billId, BigDecimal discountAmount);

    /**
     * Processes payment for a physical store cash transaction.
     */
    Bill processCashPayment(Integer billId, BigDecimal tenderedAmount);

    /**
     * Processes payment for an online transaction.
     */
    Bill processOnlinePayment(Integer billId);

    /**
     * Finalizes and saves a completed bill.
     * Deducts stock from inventory based on FIFO allocation.
     */
    Bill finalizeBill(Integer billId);

    /**
     * Cancels a bill before finalization.
     */
    boolean cancelBill(Integer billId);

    /**
     * Finds a bill by its ID.
     */
    Optional<Bill> findBillById(Integer billId);

    /**
     * Finds a bill by its serial number.
     */
    Optional<Bill> findBillBySerialNumber(String serialNumber);

    /**
     * Finds bills for a specific date.
     */
    List<Bill> findBillsByDate(LocalDate date);

    /**
     * Finds bills within a date range.
     */
    List<Bill> findBillsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds bills for a specific customer.
     */
    List<Bill> findBillsByCustomer(Integer customerId);

    /**
     * Finds recent bills with limit.
     */
    List<Bill> findRecentBills(int limit);

    /**
     * Gets the items for a bill.
     */
    List<BillItem> getBillItems(Integer billId);

    /**
     * Generates a new serial number for a bill.
     */
    String generateSerialNumber(StoreType storeType);

    /**
     * Gets the total sales amount for today.
     */
    BigDecimal getTodaysSales();

    /**
     * Gets the bill count for today.
     */
    int getTodaysBillCount();

    /**
     * Validates a bill can be finalized (has items, sufficient stock, etc.).
     */
    ValidationResult validateBillForFinalization(Integer billId);

    /**
     * Bill validation result.
     */
    record ValidationResult(
            boolean isValid,
            List<String> errors) {
        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, List.of(error));
        }
    }

    // ==================== POS Checkout (Single Transaction) ====================

    /**
     * Creates and finalizes a complete bill in a single atomic transaction.
     * This is the primary method for POS checkout - defers bill creation to payment
     * time.
     * Uses multithreading for concurrent stock validation across items.
     */
    CheckoutResult checkout(CheckoutRequest request);

    /**
     * Validates if a product has sufficient stock for the requested quantity.
     * Used by POS to validate before adding to cart.
     */
    StockCheckResult checkStock(String productCode, int quantity, StoreType storeType);

    /**
     * Request DTO for POS checkout.
     */
    record CheckoutRequest(
            StoreType storeType,
            TransactionType transactionType,
            Integer customerId,
            String cashierId,
            List<ItemRequest> items,
            BigDecimal discount,
            BigDecimal cashTendered) {
    }

    /**
     * Item request within checkout.
     */
    record ItemRequest(String productCode, int quantity) {
    }

    /**
     * Result DTO for checkout operation.
     */
    record CheckoutResult(
            boolean success,
            Integer billId,
            String serialNumber,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total,
            BigDecimal cashTendered,
            BigDecimal change,
            java.time.LocalDateTime billDate,
            List<ItemDetail> items,
            List<String> errors) {
        public static CheckoutResult success(
                Integer billId, String serialNumber, BigDecimal subtotal,
                BigDecimal discount, BigDecimal tax, BigDecimal total,
                BigDecimal cashTendered, BigDecimal change,
                java.time.LocalDateTime billDate, List<ItemDetail> items) {
            return new CheckoutResult(true, billId, serialNumber, subtotal, discount,
                    tax, total, cashTendered, change, billDate, items, List.of());
        }

        public static CheckoutResult failure(List<String> errors) {
            return new CheckoutResult(false, null, null, null, null, null, null,
                    null, null, null, null, errors);
        }

        public static CheckoutResult failure(String error) {
            return failure(List.of(error));
        }
    }

    /**
     * Item detail for receipt display.
     */
    record ItemDetail(
            String productName, // Name only, no code per requirement
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }

    /**
     * Stock check result for POS item validation.
     */
    record StockCheckResult(
            boolean available,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            int requestedQuantity,
            int availableQuantity,
            String message) {
        public static StockCheckResult available(String productCode, String productName,
                BigDecimal unitPrice, int requested, int available) {
            return new StockCheckResult(true, productCode, productName, unitPrice,
                    requested, available, "Stock available");
        }

        public static StockCheckResult unavailable(String productCode, int requested, int available) {
            String msg = available == 0
                    ? "Product is out of stock"
                    : "Insufficient stock. Only " + available + " available, requested " + requested;
            return new StockCheckResult(false, productCode, null, null,
                    requested, available, msg);
        }

        public static StockCheckResult notFound(String productCode) {
            return new StockCheckResult(false, productCode, null, null,
                    0, 0, "Product not found: " + productCode);
        }
    }
}
