package com.syos.service.interfaces;

import com.syos.domain.enums.StoreType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for generating reports.
 */
public interface ReportService {

        // ==================== Sales Reports ====================

        /**
         * Gets daily sales summary for a date range.
         */
        List<DailySalesReport> getDailySalesReport(LocalDate startDate, LocalDate endDate);

        /**
         * Gets sales summary by store type for a date range.
         */
        List<StoreTypeSalesReport> getSalesByStoreType(LocalDate startDate, LocalDate endDate);

        /**
         * Gets top selling products for a date range.
         */
        List<ProductSalesReport> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit);

        /**
         * Gets top selling products for a date range filtered by store type.
         */
        List<ProductSalesReport> getTopSellingProductsByStoreType(LocalDate startDate, LocalDate endDate, int limit,
                        StoreType storeType);

        /**
         * Gets sales summary for a specific date.
         */
        SalesSummary getSalesSummary(LocalDate date);

        /**
         * Gets sales summary for a specific date filtered by store type.
         */
        SalesSummary getSalesSummaryByStoreType(LocalDate date, StoreType storeType);

        /**
         * Gets sales summary for a date range.
         */
        SalesSummary getSalesSummaryForRange(LocalDate startDate, LocalDate endDate);

        // ==================== Inventory Reports ====================

        /**
         * Gets current stock levels for all products.
         */
        List<StockLevelReport> getCurrentStockLevels(StoreType storeType);

        /**
         * Gets products with low stock.
         */
        List<LowStockReport> getLowStockReport(StoreType storeType, int threshold);

        /**
         * Gets products expiring within specified days.
         */
        List<ExpiringStockReport> getExpiringStockReport(int days);

        /**
         * Gets expired stock that needs attention.
         */
        List<ExpiringStockReport> getExpiredStockReport();

        /**
         * Gets restock recommendations based on sales velocity and current stock.
         */
        List<RestockRecommendation> getRestockRecommendations(StoreType storeType, int daysOfSalesData);

        /**
         * Gets items that need to be reshelved (stock below minimum threshold).
         * This report shows products where current stock is below the configured
         * minimum.
         */
        List<ReshelveReport> getReshelveReport(StoreType storeType);

        /**
         * Gets products that need to be reordered based on main inventory levels.
         * Products with total remaining quantity below the threshold will appear.
         */
        List<ReorderLevelReport> getReorderLevelReport(int threshold);

        /**
         * Gets batch-wise stock report showing details of each batch in main inventory.
         */
        List<BatchStockReport> getBatchStockReport();

        // ==================== Dashboard Reports ====================

        /**
         * Gets dashboard summary for today.
         */
        /**
         * Gets dashboard summary for today.
         */
        DashboardSummary getDashboardSummary();

        /**
         * Gets bill report for a specific date and store type, including all line
         * items.
         * Uses multithreading to fetch bill items concurrently.
         */
        BillReport getBillReport(LocalDate date, StoreType storeType);

        // ==================== Async Operations (using ApiThreadPool)
        // ====================

        /**
         * Async version of getDashboardSummary.
         * Executes on the ApiThreadPool for non-blocking dashboard loading.
         */
        CompletableFuture<DashboardSummary> getDashboardSummaryAsync();

        /**
         * Async version of getDailySalesReport.
         * Executes on the ApiThreadPool.
         */
        CompletableFuture<List<DailySalesReport>> getDailySalesReportAsync(LocalDate startDate, LocalDate endDate);

        /**
         * Async version of getCurrentStockLevels.
         * Executes on the ApiThreadPool.
         */
        CompletableFuture<List<StockLevelReport>> getCurrentStockLevelsAsync(StoreType storeType);

        /**
         * Async version of getBillReport.
         * Executes on the ApiThreadPool.
         */
        CompletableFuture<BillReport> getBillReportAsync(LocalDate date, StoreType storeType);

        // ==================== Report DTOs ====================

        record DailySalesReport(
                        LocalDate date,
                        int billCount,
                        BigDecimal totalSales,
                        BigDecimal cashSales,
                        BigDecimal onlineSales) {
        }

        record StoreTypeSalesReport(
                        StoreType storeType,
                        int billCount,
                        BigDecimal totalSales) {
        }

        record ProductSalesReport(
                        String productCode,
                        String productName,
                        int totalQuantitySold,
                        BigDecimal totalRevenue) {
        }

        record SalesSummary(
                        LocalDate startDate,
                        LocalDate endDate,
                        int totalBills,
                        BigDecimal totalSales,
                        BigDecimal averageBillValue,
                        int totalItemsSold) {
        }

        record StockLevelReport(
                        String productCode,
                        String productName,
                        int currentStock,
                        int batchCount,
                        LocalDate earliestExpiry) {
        }

        record LowStockReport(
                        String productCode,
                        String productName,
                        int currentStock,
                        int recommendedRestock) {
        }

        record ExpiringStockReport(
                        String productCode,
                        String productName,
                        Integer batchId,
                        int quantity,
                        LocalDate expiryDate,
                        int daysUntilExpiry) {
        }

        record RestockRecommendation(
                        String productCode,
                        String productName,
                        int currentStock,
                        int averageDailySales,
                        int daysOfStockRemaining,
                        int recommendedRestock) {
        }

        record ReshelveReport(
                        String productCode,
                        String productName,
                        int currentStock,
                        int minimumStock,
                        int quantityToReshelve) {
        }

        record ReorderLevelReport(
                        String productCode,
                        String productName,
                        int totalRemainingQuantity,
                        int reorderThreshold,
                        int quantityToReorder) {
        }

        record BatchStockReport(
                        String productCode,
                        String productName,
                        int batchNumber,
                        LocalDate purchaseDate,
                        LocalDate expiryDate,
                        int originalQuantity,
                        int remainingInMain,
                        int quantityInPhysical,
                        int quantityInOnline) {
        }

        record DashboardSummary(
                        BigDecimal todaySales,
                        int todayBillCount,
                        int lowStockProductCount,
                        int expiringProductCount,
                        BigDecimal weekSales,
                        BigDecimal monthSales,
                        List<ProductSalesReport> topProducts) {
        }

        record BillReport(
                        LocalDate date,
                        StoreType storeType,
                        int totalBills,
                        BigDecimal totalRevenue,
                        List<com.syos.domain.models.Bill> bills) {
        }
}
