package com.syos.repository.interfaces;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.InventoryTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for InventoryTransaction entity operations (audit log).
 */
public interface InventoryTransactionRepository extends Repository<InventoryTransaction, Integer> {

    /**
     * Finds all transactions for a specific product.
     */
    List<InventoryTransaction> findByProductCode(String productCode);

    /**
     * Finds all transactions for a specific batch.
     */
    List<InventoryTransaction> findByMainInventoryId(Integer mainInventoryId);

    /**
     * Finds all transactions by type.
     */
    List<InventoryTransaction> findByTransactionType(InventoryTransactionType transactionType);

    /**
     * Finds all transactions by store type.
     */
    List<InventoryTransaction> findByStoreType(StoreType storeType);

    /**
     * Finds all transactions for a specific bill.
     */
    List<InventoryTransaction> findByBillId(Integer billId);

    /**
     * Finds all transactions within a date range.
     */
    List<InventoryTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds all transactions for a product within a date range.
     */
    List<InventoryTransaction> findByProductCodeAndDateRange(String productCode, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets the total quantity change for a product within a date range.
     */
    int getTotalQuantityChange(String productCode, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets transaction summary by type for a date range.
     */
    List<TransactionTypeSummary> getSummaryByType(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets recent transactions with limit.
     */
    List<InventoryTransaction> findRecent(int limit);

    /**
     * Gets daily transaction summary.
     */
    List<DailyTransactionSummary> getDailySummary(LocalDate startDate, LocalDate endDate);

    /**
     * Transaction type summary DTO.
     */
    record TransactionTypeSummary(
        InventoryTransactionType transactionType,
        int transactionCount,
        int totalQuantity
    ) {}

    /**
     * Daily transaction summary DTO.
     */
    record DailyTransactionSummary(
        LocalDate date,
        int transactionCount,
        int salesQuantity,
        int restockQuantity
    ) {}
}
