package com.syos.repository.interfaces;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bill entity operations.
 */
public interface BillRepository extends Repository<Bill, Integer> {

    /**
     * Finds a bill by its serial number.
     */
    Optional<Bill> findBySerialNumber(String serialNumber);

    /**
     * Finds all bills for a specific date.
     */
    List<Bill> findByDate(LocalDate date);

    /**
     * Finds all bills within a date range.
     */
    List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds all bills for a specific store type.
     */
    List<Bill> findByStoreType(StoreType storeType);

    /**
     * Finds all bills by transaction type.
     */
    List<Bill> findByTransactionType(TransactionType transactionType);

    /**
     * Finds all bills for a specific customer.
     */
    List<Bill> findByCustomerId(Integer customerId);

    /**
     * Finds all bills created by a specific cashier.
     */
    List<Bill> findByCashierId(String cashierId);

    /**
     * Finds recent bills with pagination.
     */
    List<Bill> findRecent(int limit);

    /**
     * Gets the total sales amount for a specific date.
     */
    BigDecimal getTotalSalesForDate(LocalDate date);

    /**
     * Gets the total sales amount for a date range.
     */
    BigDecimal getTotalSalesForDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Gets daily sales summary.
     */
    List<DailySalesSummary> getDailySalesSummary(LocalDate startDate, LocalDate endDate);

    /**
     * Gets sales by store type for a date range.
     */
    List<StoreTypeSalesSummary> getSalesByStoreType(LocalDate startDate, LocalDate endDate);

    /**
     * Gets the count of bills for a specific date.
     */
    int getBillCountForDate(LocalDate date);

    /**
     * Generates the next bill serial number for a store type.
     */
    String generateNextSerialNumber(StoreType storeType);

    /**
     * Finds bills by store type and date range.
     */
    List<Bill> findByStoreTypeAndDateRange(StoreType storeType, LocalDate startDate, LocalDate endDate);

    /**
     * Daily sales summary DTO.
     */
    record DailySalesSummary(
        LocalDate date,
        int billCount,
        BigDecimal totalAmount,
        BigDecimal cashAmount,
        BigDecimal onlineAmount
    ) {}

    /**
     * Store type sales summary DTO.
     */
    record StoreTypeSalesSummary(
        StoreType storeType,
        int billCount,
        BigDecimal totalAmount
    ) {}
}
