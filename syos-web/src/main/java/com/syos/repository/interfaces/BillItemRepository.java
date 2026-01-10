package com.syos.repository.interfaces;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.BillItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BillItem entity operations.
 */
public interface BillItemRepository extends Repository<BillItem, Integer> {

    /**
     * Finds all items for a specific bill.
     */
    List<BillItem> findByBillId(Integer billId);

    /**
     * Finds all bill items for a specific product.
     */
    List<BillItem> findByProductCode(String productCode);

    /**
     * Finds bill items for a product within a date range.
     */
    List<BillItem> findByProductCodeAndDateRange(String productCode, LocalDate startDate, LocalDate endDate);

    /**
     * Gets total quantity sold for a product on a specific date.
     */
    int getTotalQuantitySoldForDate(String productCode, LocalDate date);

    /**
     * Gets total quantity sold for a product within a date range.
     */
    int getTotalQuantitySoldForDateRange(String productCode, LocalDate startDate, LocalDate endDate);

    /**
     * Gets total revenue for a product within a date range.
     */
    BigDecimal getTotalRevenueForProduct(String productCode, LocalDate startDate, LocalDate endDate);

    /**
     * Gets top selling products by quantity within a date range.
     */
    List<ProductSalesSummary> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Gets top selling products by quantity within a date range for a specific store type.
     */
    List<ProductSalesSummary> getTopSellingProductsByStoreType(LocalDate startDate, LocalDate endDate, int limit, StoreType storeType);

    /**
     * Gets sales summary by product for a date range.
     */
    List<ProductSalesSummary> getProductSalesSummary(LocalDate startDate, LocalDate endDate);

    /**
     * Gets sales summary by product for a date range for a specific store type.
     */
    List<ProductSalesSummary> getProductSalesSummaryByStoreType(LocalDate startDate, LocalDate endDate, StoreType storeType);

    /**
     * Deletes all items for a specific bill.
     */
    int deleteByBillId(Integer billId);

    /**
     * Saves all items for a bill in batch.
     */
    List<BillItem> saveAll(List<BillItem> items);

    /**
     * Product sales summary DTO.
     */
    record ProductSalesSummary(
        String productCode,
        String productName,
        int totalQuantity,
        BigDecimal totalRevenue
    ) {}
}
