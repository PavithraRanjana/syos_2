package com.syos.service.impl;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.MainInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.models.Product;
import com.syos.repository.interfaces.BillItemRepository;
import com.syos.repository.interfaces.BillRepository;
import com.syos.repository.interfaces.MainInventoryRepository;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository;
import com.syos.repository.interfaces.PhysicalStoreInventoryRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.interfaces.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ReportService.
 */
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int RESTOCK_SAFETY_DAYS = 7;

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final MainInventoryRepository mainInventoryRepository;
    private final PhysicalStoreInventoryRepository physicalStoreRepository;
    private final OnlineStoreInventoryRepository onlineStoreRepository;
    private final ProductRepository productRepository;

    public ReportServiceImpl(
            BillRepository billRepository,
            BillItemRepository billItemRepository,
            MainInventoryRepository mainInventoryRepository,
            PhysicalStoreInventoryRepository physicalStoreRepository,
            OnlineStoreInventoryRepository onlineStoreRepository,
            ProductRepository productRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.mainInventoryRepository = mainInventoryRepository;
        this.physicalStoreRepository = physicalStoreRepository;
        this.onlineStoreRepository = onlineStoreRepository;
        this.productRepository = productRepository;
    }

    // ==================== Sales Reports ====================

    @Override
    public List<DailySalesReport> getDailySalesReport(LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating daily sales report: {} to {}", startDate, endDate);

        List<BillRepository.DailySalesSummary> summaries = billRepository.getDailySalesSummary(startDate, endDate);

        return summaries.stream()
            .map(s -> new DailySalesReport(
                s.date(),
                s.billCount(),
                s.totalAmount(),
                s.cashAmount(),
                s.onlineAmount()
            ))
            .toList();
    }

    @Override
    public List<StoreTypeSalesReport> getSalesByStoreType(LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating sales by store type report: {} to {}", startDate, endDate);

        List<BillRepository.StoreTypeSalesSummary> summaries = billRepository.getSalesByStoreType(startDate, endDate);

        return summaries.stream()
            .map(s -> new StoreTypeSalesReport(
                s.storeType(),
                s.billCount(),
                s.totalAmount()
            ))
            .toList();
    }

    @Override
    public List<ProductSalesReport> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        logger.debug("Generating top selling products report: {} to {}, limit {}", startDate, endDate, limit);

        List<BillItemRepository.ProductSalesSummary> summaries =
            billItemRepository.getTopSellingProducts(startDate, endDate, limit);

        return summaries.stream()
            .map(s -> new ProductSalesReport(
                s.productCode(),
                s.productName(),
                s.totalQuantity(),
                s.totalRevenue()
            ))
            .toList();
    }

    @Override
    public SalesSummary getSalesSummary(LocalDate date) {
        return getSalesSummaryForRange(date, date);
    }

    @Override
    public SalesSummary getSalesSummaryForRange(LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating sales summary: {} to {}", startDate, endDate);

        BigDecimal totalSales = billRepository.getTotalSalesForDateRange(startDate, endDate);
        int totalBills = 0;

        List<BillRepository.DailySalesSummary> dailySummaries = billRepository.getDailySalesSummary(startDate, endDate);
        for (BillRepository.DailySalesSummary summary : dailySummaries) {
            totalBills += summary.billCount();
        }

        BigDecimal averageBillValue = totalBills > 0 ?
            totalSales.divide(BigDecimal.valueOf(totalBills), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // Get total items sold
        List<BillItemRepository.ProductSalesSummary> productSummaries =
            billItemRepository.getProductSalesSummary(startDate, endDate);
        int totalItemsSold = productSummaries.stream()
            .mapToInt(BillItemRepository.ProductSalesSummary::totalQuantity)
            .sum();

        return new SalesSummary(
            startDate,
            endDate,
            totalBills,
            totalSales,
            averageBillValue,
            totalItemsSold
        );
    }

    // ==================== Inventory Reports ====================

    @Override
    public List<StockLevelReport> getCurrentStockLevels(StoreType storeType) {
        logger.debug("Generating stock level report for store type: {}", storeType);

        List<StockLevelReport> reports = new ArrayList<>();

        if (storeType == StoreType.PHYSICAL) {
            List<PhysicalStoreInventoryRepository.ProductStockSummary> summaries =
                physicalStoreRepository.getStockSummary();

            for (PhysicalStoreInventoryRepository.ProductStockSummary summary : summaries) {
                // Get earliest expiry for this product
                List<PhysicalStoreInventory> batches =
                    physicalStoreRepository.findAvailableByProductCode(summary.productCode());
                LocalDate earliestExpiry = batches.stream()
                    .filter(b -> b.getExpiryDate() != null)
                    .map(PhysicalStoreInventory::getExpiryDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

                reports.add(new StockLevelReport(
                    summary.productCode(),
                    summary.productName(),
                    summary.totalQuantity(),
                    summary.batchCount(),
                    earliestExpiry
                ));
            }
        } else {
            List<OnlineStoreInventoryRepository.ProductStockSummary> summaries =
                onlineStoreRepository.getStockSummary();

            for (OnlineStoreInventoryRepository.ProductStockSummary summary : summaries) {
                // Get earliest expiry for this product
                List<OnlineStoreInventory> batches =
                    onlineStoreRepository.findAvailableByProductCode(summary.productCode());
                LocalDate earliestExpiry = batches.stream()
                    .filter(b -> b.getExpiryDate() != null)
                    .map(OnlineStoreInventory::getExpiryDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

                reports.add(new StockLevelReport(
                    summary.productCode(),
                    summary.productName(),
                    summary.totalQuantity(),
                    summary.batchCount(),
                    earliestExpiry
                ));
            }
        }

        return reports;
    }

    @Override
    public List<LowStockReport> getLowStockReport(StoreType storeType, int threshold) {
        logger.debug("Generating low stock report for store type: {}, threshold: {}", storeType, threshold);

        List<LowStockReport> reports = new ArrayList<>();

        if (storeType == StoreType.PHYSICAL) {
            List<PhysicalStoreInventory> lowStock = physicalStoreRepository.findLowStock(threshold);
            for (PhysicalStoreInventory inv : lowStock) {
                reports.add(new LowStockReport(
                    inv.getProductCodeString(),
                    inv.getProductName(),
                    inv.getQuantityOnShelf(),
                    calculateRecommendedRestock(inv.getProductCodeString(), threshold)
                ));
            }
        } else {
            List<OnlineStoreInventory> lowStock = onlineStoreRepository.findLowStock(threshold);
            for (OnlineStoreInventory inv : lowStock) {
                reports.add(new LowStockReport(
                    inv.getProductCodeString(),
                    inv.getProductName(),
                    inv.getQuantityAvailable(),
                    calculateRecommendedRestock(inv.getProductCodeString(), threshold)
                ));
            }
        }

        return reports;
    }

    @Override
    public List<ExpiringStockReport> getExpiringStockReport(int days) {
        logger.debug("Generating expiring stock report for {} days", days);

        List<MainInventory> expiringBatches = mainInventoryRepository.findExpiringWithinDays(days);
        LocalDate today = LocalDate.now();

        return expiringBatches.stream()
            .map(batch -> new ExpiringStockReport(
                batch.getProductCodeString(),
                batch.getProductName(),
                batch.getMainInventoryId(),
                batch.getRemainingQuantity(),
                batch.getExpiryDate(),
                (int) ChronoUnit.DAYS.between(today, batch.getExpiryDate())
            ))
            .toList();
    }

    @Override
    public List<ExpiringStockReport> getExpiredStockReport() {
        logger.debug("Generating expired stock report");

        List<MainInventory> expiredBatches = mainInventoryRepository.findExpiredBatches();
        LocalDate today = LocalDate.now();

        return expiredBatches.stream()
            .map(batch -> new ExpiringStockReport(
                batch.getProductCodeString(),
                batch.getProductName(),
                batch.getMainInventoryId(),
                batch.getRemainingQuantity(),
                batch.getExpiryDate(),
                (int) ChronoUnit.DAYS.between(today, batch.getExpiryDate())
            ))
            .toList();
    }

    @Override
    public List<RestockRecommendation> getRestockRecommendations(StoreType storeType, int daysOfSalesData) {
        logger.debug("Generating restock recommendations for store type: {}", storeType);

        List<RestockRecommendation> recommendations = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysOfSalesData);

        // Get sales data
        List<BillItemRepository.ProductSalesSummary> salesData =
            billItemRepository.getProductSalesSummary(startDate, endDate);

        // Get current stock levels
        List<StockLevelReport> stockLevels = getCurrentStockLevels(storeType);

        for (StockLevelReport stockLevel : stockLevels) {
            // Find matching sales data
            int totalSold = salesData.stream()
                .filter(s -> s.productCode().equals(stockLevel.productCode()))
                .mapToInt(BillItemRepository.ProductSalesSummary::totalQuantity)
                .sum();

            int avgDailySales = totalSold / Math.max(daysOfSalesData, 1);
            int daysOfStock = avgDailySales > 0 ?
                stockLevel.currentStock() / avgDailySales : Integer.MAX_VALUE;

            // Recommend restock if less than safety days of stock
            if (daysOfStock < RESTOCK_SAFETY_DAYS) {
                int recommendedRestock = (RESTOCK_SAFETY_DAYS * 2 * avgDailySales) - stockLevel.currentStock();
                recommendedRestock = Math.max(recommendedRestock, 0);

                recommendations.add(new RestockRecommendation(
                    stockLevel.productCode(),
                    stockLevel.productName(),
                    stockLevel.currentStock(),
                    avgDailySales,
                    daysOfStock,
                    recommendedRestock
                ));
            }
        }

        // Sort by days of stock remaining (ascending)
        recommendations.sort((a, b) -> Integer.compare(a.daysOfStockRemaining(), b.daysOfStockRemaining()));

        return recommendations;
    }

    @Override
    public List<ReshelveReport> getReshelveReport(StoreType storeType) {
        logger.debug("Generating reshelve report for store type: {}", storeType);

        List<ReshelveReport> reports = new ArrayList<>();

        if (storeType == StoreType.PHYSICAL) {
            // Get stock summary for physical store
            List<PhysicalStoreInventoryRepository.ProductStockSummary> stockSummaries =
                physicalStoreRepository.getStockSummary();

            for (PhysicalStoreInventoryRepository.ProductStockSummary summary : stockSummaries) {
                // Get the product to find its minimum stock level
                Product product = productRepository.findByProductCode(summary.productCode()).orElse(null);
                if (product == null) continue;

                int minStock = product.getMinPhysicalStock();
                int currentStock = summary.totalQuantity();

                if (currentStock < minStock) {
                    int quantityToReshelve = minStock - currentStock;
                    reports.add(new ReshelveReport(
                        summary.productCode(),
                        summary.productName(),
                        currentStock,
                        minStock,
                        quantityToReshelve
                    ));
                }
            }

            // Also check for products with zero stock that have a minimum > 0
            List<Product> allProducts = productRepository.findAllActive();
            for (Product product : allProducts) {
                if (product.getMinPhysicalStock() > 0) {
                    boolean hasStock = stockSummaries.stream()
                        .anyMatch(s -> s.productCode().equals(product.getProductCodeString()));
                    if (!hasStock) {
                        reports.add(new ReshelveReport(
                            product.getProductCodeString(),
                            product.getProductName(),
                            0,
                            product.getMinPhysicalStock(),
                            product.getMinPhysicalStock()
                        ));
                    }
                }
            }
        } else {
            // Get stock summary for online store
            List<OnlineStoreInventoryRepository.ProductStockSummary> stockSummaries =
                onlineStoreRepository.getStockSummary();

            for (OnlineStoreInventoryRepository.ProductStockSummary summary : stockSummaries) {
                // Get the product to find its minimum stock level
                Product product = productRepository.findByProductCode(summary.productCode()).orElse(null);
                if (product == null) continue;

                int minStock = product.getMinOnlineStock();
                int currentStock = summary.totalQuantity();

                if (currentStock < minStock) {
                    int quantityToReshelve = minStock - currentStock;
                    reports.add(new ReshelveReport(
                        summary.productCode(),
                        summary.productName(),
                        currentStock,
                        minStock,
                        quantityToReshelve
                    ));
                }
            }

            // Also check for products with zero stock that have a minimum > 0
            List<Product> allProducts = productRepository.findAllActive();
            for (Product product : allProducts) {
                if (product.getMinOnlineStock() > 0) {
                    boolean hasStock = stockSummaries.stream()
                        .anyMatch(s -> s.productCode().equals(product.getProductCodeString()));
                    if (!hasStock) {
                        reports.add(new ReshelveReport(
                            product.getProductCodeString(),
                            product.getProductName(),
                            0,
                            product.getMinOnlineStock(),
                            product.getMinOnlineStock()
                        ));
                    }
                }
            }
        }

        // Sort by quantity to reshelve (descending - most urgent first)
        reports.sort((a, b) -> Integer.compare(b.quantityToReshelve(), a.quantityToReshelve()));

        return reports;
    }

    @Override
    public List<ReorderLevelReport> getReorderLevelReport(int threshold) {
        logger.debug("Generating reorder level report with threshold: {}", threshold);

        List<ReorderLevelReport> reports = new ArrayList<>();

        // Get all active products
        List<Product> allProducts = productRepository.findAllActive();

        for (Product product : allProducts) {
            // Get total remaining quantity in main inventory for this product
            List<MainInventory> batches = mainInventoryRepository.findByProductCode(product.getProductCodeString());

            int totalRemaining = batches.stream()
                .mapToInt(MainInventory::getRemainingQuantity)
                .sum();

            if (totalRemaining < threshold) {
                int quantityToReorder = threshold - totalRemaining;
                reports.add(new ReorderLevelReport(
                    product.getProductCodeString(),
                    product.getProductName(),
                    totalRemaining,
                    threshold,
                    quantityToReorder
                ));
            }
        }

        // Sort by total remaining quantity (ascending - lowest stock first)
        reports.sort((a, b) -> Integer.compare(a.totalRemainingQuantity(), b.totalRemainingQuantity()));

        return reports;
    }

    @Override
    public List<BatchStockReport> getBatchStockReport() {
        logger.debug("Generating batch-wise stock report");

        List<BatchStockReport> reports = new ArrayList<>();

        // Get all batches from main inventory
        List<MainInventory> allBatches = mainInventoryRepository.findAll();

        for (MainInventory batch : allBatches) {
            String productCode = batch.getProductCodeString();
            int batchId = batch.getMainInventoryId();

            // Get quantity in physical store for this batch
            int physicalQty = physicalStoreRepository.findByProductCodeAndBatchId(productCode, batchId)
                .map(inv -> inv.getQuantityOnShelf())
                .orElse(0);

            // Get quantity in online store for this batch
            int onlineQty = onlineStoreRepository.findByProductCodeAndBatchId(productCode, batchId)
                .map(inv -> inv.getQuantityAvailable())
                .orElse(0);

            reports.add(new BatchStockReport(
                productCode,
                batch.getProductName(),
                batchId,
                batch.getPurchaseDate(),
                batch.getExpiryDate(),
                batch.getQuantityReceived(),
                batch.getRemainingQuantity(),
                physicalQty,
                onlineQty
            ));
        }

        // Sort by product code, then by batch number
        reports.sort((a, b) -> {
            int codeCompare = a.productCode().compareTo(b.productCode());
            if (codeCompare != 0) return codeCompare;
            return Integer.compare(a.batchNumber(), b.batchNumber());
        });

        return reports;
    }

    // ==================== Dashboard Reports ====================

    @Override
    public DashboardSummary getDashboardSummary() {
        logger.debug("Generating dashboard summary");

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        BigDecimal todaySales = billRepository.getTotalSalesForDate(today);
        int todayBillCount = billRepository.getBillCountForDate(today);

        // Low stock count (physical store by default)
        int lowStockCount = physicalStoreRepository.findLowStock(DEFAULT_LOW_STOCK_THRESHOLD).size();

        // Expiring within 7 days
        int expiringCount = mainInventoryRepository.findExpiringWithinDays(7).size();

        BigDecimal weekSales = billRepository.getTotalSalesForDateRange(weekAgo, today);
        BigDecimal monthSales = billRepository.getTotalSalesForDateRange(monthAgo, today);

        List<ProductSalesReport> topProducts = getTopSellingProducts(weekAgo, today, 5);

        return new DashboardSummary(
            todaySales,
            todayBillCount,
            lowStockCount,
            expiringCount,
            weekSales,
            monthSales,
            topProducts
        );
    }

    // ==================== Helper Methods ====================

    private int calculateRecommendedRestock(String productCode, int threshold) {
        // Simple recommendation: restock to 2x threshold
        return threshold * 2;
    }
}
