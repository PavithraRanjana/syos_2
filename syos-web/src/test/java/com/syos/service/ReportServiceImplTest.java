package com.syos.service;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.MainInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.*;
import com.syos.service.impl.ReportServiceImpl;
import com.syos.service.interfaces.ReportService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private MainInventoryRepository mainInventoryRepository;

    @Mock
    private PhysicalStoreInventoryRepository physicalStoreRepository;

    @Mock
    private OnlineStoreInventoryRepository onlineStoreRepository;

    @Mock
    private ProductRepository productRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                billRepository,
                billItemRepository,
                mainInventoryRepository,
                physicalStoreRepository,
                onlineStoreRepository,
                productRepository);
    }

    private Bill createTestBill(Integer billId, StoreType storeType, LocalDate date) {
        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setStoreType(storeType);
        bill.setBillDate(date.atStartOfDay());
        bill.setTotalAmount(new Money(BigDecimal.valueOf(1000.00)));
        bill.setSerialNumber(new BillSerialNumber(billId));
        return bill;
    }

    private PhysicalStoreInventory createPhysicalInventory(String productCode, int quantity) {
        PhysicalStoreInventory inv = new PhysicalStoreInventory(
                new ProductCode(productCode), 1, quantity, LocalDate.now());
        inv.setProductName("Test Product " + productCode);
        return inv;
    }

    private OnlineStoreInventory createOnlineInventory(String productCode, int quantity) {
        OnlineStoreInventory inv = new OnlineStoreInventory(
                new ProductCode(productCode), 1, quantity, LocalDate.now());
        inv.setProductName("Test Product " + productCode);
        return inv;
    }

    private MainInventory createMainInventory(String productCode, int quantity, LocalDate expiry) {
        MainInventory batch = new MainInventory();
        batch.setMainInventoryId(1);
        batch.setProductCode(new ProductCode(productCode));
        batch.setProductName("Test Product");
        batch.setRemainingQuantity(quantity);
        batch.setExpiryDate(expiry);
        batch.setPurchaseDate(LocalDate.now().minusDays(30));
        return batch;
    }

    @Nested
    @DisplayName("getDailySalesReport tests")
    class GetDailySalesReportTests {

        @Test
        @DisplayName("Should return empty list when no sales data")
        void shouldReturnEmptyListWhenNoSales() {
            // Arrange
            when(billRepository.findByDateRange(any(), any())).thenReturn(List.of());

            // Act
            List<DailySalesReport> result = reportService.getDailySalesReport(
                    LocalDate.now().minusDays(7), LocalDate.now());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return sales data for date range")
        void shouldReturnSalesDataForDateRange() {
            // Arrange
            LocalDate today = LocalDate.now();
            Bill bill1 = createTestBill(1, StoreType.PHYSICAL, today);
            Bill bill2 = createTestBill(2, StoreType.ONLINE, today);
            when(billRepository.findByDateRange(any(), any())).thenReturn(List.of(bill1, bill2));

            // Act
            List<DailySalesReport> result = reportService.getDailySalesReport(
                    today.minusDays(1), today);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getSalesByStoreType tests")
    class GetSalesByStoreTypeTests {

        @Test
        @DisplayName("Should return sales grouped by store type")
        void shouldReturnSalesGroupedByStoreType() {
            // Arrange
            LocalDate today = LocalDate.now();
            Bill bill1 = createTestBill(1, StoreType.PHYSICAL, today);
            Bill bill2 = createTestBill(2, StoreType.ONLINE, today);
            when(billRepository.findByDateRange(any(), any())).thenReturn(List.of(bill1, bill2));

            // Act
            List<StoreTypeSalesReport> result = reportService.getSalesByStoreType(
                    today.minusDays(1), today);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getSalesSummary tests")
    class GetSalesSummaryTests {

        @Test
        @DisplayName("Should return sales summary for date")
        void shouldReturnSalesSummaryForDate() {
            // Arrange
            LocalDate today = LocalDate.now();
            Bill bill1 = createTestBill(1, StoreType.PHYSICAL, today);
            Bill bill2 = createTestBill(2, StoreType.PHYSICAL, today);
            when(billRepository.findByDateRange(eq(today), eq(today))).thenReturn(List.of(bill1, bill2));

            // Act
            SalesSummary result = reportService.getSalesSummary(today);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getCurrentStockLevels tests")
    class GetCurrentStockLevelsTests {

        @Test
        @DisplayName("Should return physical store stock levels")
        void shouldReturnPhysicalStoreStockLevels() {
            // Arrange
            PhysicalStoreInventoryRepository.ProductStockSummary summary = new PhysicalStoreInventoryRepository.ProductStockSummary(
                    "TEST-001", "Test", 50, 2);
            when(physicalStoreRepository.getStockSummary()).thenReturn(List.of(summary));

            // Act
            List<StockLevelReport> result = reportService.getCurrentStockLevels(StoreType.PHYSICAL);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TEST-001", result.get(0).productCode());
            assertEquals(50, result.get(0).currentStock());
        }

        @Test
        @DisplayName("Should return online store stock levels")
        void shouldReturnOnlineStoreStockLevels() {
            // Arrange
            OnlineStoreInventoryRepository.ProductStockSummary summary = new OnlineStoreInventoryRepository.ProductStockSummary(
                    "TEST-001", "Test", 30, 1);
            when(onlineStoreRepository.getStockSummary()).thenReturn(List.of(summary));

            // Act
            List<StockLevelReport> result = reportService.getCurrentStockLevels(StoreType.ONLINE);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(30, result.get(0).currentStock());
        }
    }

    @Nested
    @DisplayName("getLowStockReport tests")
    class GetLowStockReportTests {

        @Test
        @DisplayName("Should return low stock physical items")
        void shouldReturnLowStockPhysicalItems() {
            // Arrange
            PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 5);
            when(physicalStoreRepository.findLowStock(10)).thenReturn(List.of(inv));

            // Act
            List<LowStockReport> result = reportService.getLowStockReport(StoreType.PHYSICAL, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TEST-001", result.get(0).productCode());
        }

        @Test
        @DisplayName("Should return low stock online items")
        void shouldReturnLowStockOnlineItems() {
            // Arrange
            OnlineStoreInventory inv = createOnlineInventory("TEST-001", 3);
            when(onlineStoreRepository.findLowStock(10)).thenReturn(List.of(inv));

            // Act
            List<LowStockReport> result = reportService.getLowStockReport(StoreType.ONLINE, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getExpiringStockReport tests")
    class GetExpiringStockReportTests {

        @Test
        @DisplayName("Should return expiring stock within days")
        void shouldReturnExpiringStockWithinDays() {
            // Arrange
            MainInventory batch = createMainInventory("TEST-001", 50, LocalDate.now().plusDays(5));
            when(mainInventoryRepository.findExpiringWithinDays(7)).thenReturn(List.of(batch));

            // Act
            List<ExpiringStockReport> result = reportService.getExpiringStockReport(7);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty when no expiring stock")
        void shouldReturnEmptyWhenNoExpiringStock() {
            // Arrange
            when(mainInventoryRepository.findExpiringWithinDays(7)).thenReturn(List.of());

            // Act
            List<ExpiringStockReport> result = reportService.getExpiringStockReport(7);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getBatchStockReport tests")
    class GetBatchStockReportTests {

        @Test
        @DisplayName("Should return batch stock report")
        void shouldReturnBatchStockReport() {
            // Arrange
            MainInventory batch = createMainInventory("TEST-001", 100, LocalDate.now().plusMonths(6));
            when(mainInventoryRepository.findAll()).thenReturn(List.of(batch));

            // Act
            List<BatchStockReport> result = reportService.getBatchStockReport();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getDashboardSummary tests")
    class GetDashboardSummaryTests {

        @Test
        @DisplayName("Should return dashboard summary")
        void shouldReturnDashboardSummary() {
            // Arrange
            LocalDate today = LocalDate.now();
            Bill bill = createTestBill(1, StoreType.PHYSICAL, today);
            when(billRepository.findByDateRange(today, today)).thenReturn(List.of(bill));
            when(physicalStoreRepository.findLowStock(anyInt())).thenReturn(List.of());
            when(onlineStoreRepository.findLowStock(anyInt())).thenReturn(List.of());
            when(mainInventoryRepository.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act
            DashboardSummary result = reportService.getDashboardSummary();

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Async method tests")
    class AsyncMethodTests {

        @Test
        @DisplayName("getDashboardSummaryAsync should return CompletableFuture")
        void getDashboardSummaryAsyncShouldReturnFuture() throws Exception {
            // Arrange
            when(billRepository.findByDateRange(any(), any())).thenReturn(List.of());
            when(physicalStoreRepository.findLowStock(anyInt())).thenReturn(List.of());
            when(onlineStoreRepository.findLowStock(anyInt())).thenReturn(List.of());
            when(mainInventoryRepository.findExpiringWithinDays(anyInt())).thenReturn(List.of());

            // Act
            var future = reportService.getDashboardSummaryAsync();

            // Assert
            assertNotNull(future);
            var result = future.get();
            assertNotNull(result);
        }

        @Test
        @DisplayName("getDailySalesReportAsync should return CompletableFuture")
        void getDailySalesReportAsyncShouldReturnFuture() throws Exception {
            // Arrange
            when(billRepository.findByDateRange(any(), any())).thenReturn(List.of());

            // Act
            var future = reportService.getDailySalesReportAsync(
                    LocalDate.now().minusDays(7), LocalDate.now());

            // Assert
            assertNotNull(future);
            var result = future.get();
            assertNotNull(result);
        }

        @Test
        @DisplayName("getCurrentStockLevelsAsync should return CompletableFuture")
        void getCurrentStockLevelsAsyncShouldReturnFuture() throws Exception {
            // Arrange
            when(physicalStoreRepository.getStockSummary()).thenReturn(List.of());

            // Act
            var future = reportService.getCurrentStockLevelsAsync(StoreType.PHYSICAL);

            // Assert
            assertNotNull(future);
            var result = future.get();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getReorderLevelReport tests")
    class GetReorderLevelReportTests {

        @Test
        @DisplayName("Should return reorder level report")
        void shouldReturnReorderLevelReport() {
            // Arrange
            MainInventory batch = createMainInventory("TEST-001", 5, LocalDate.now().plusMonths(6));
            when(mainInventoryRepository.findAll()).thenReturn(List.of(batch));

            // Act
            List<ReorderLevelReport> result = reportService.getReorderLevelReport(10);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getTopSellingProducts tests")
    class GetTopSellingProductsTests {

        @Test
        @DisplayName("Should return top selling products")
        void shouldReturnTopSellingProducts() {
            // Arrange
            BillItemRepository.ProductSalesSummary summary = new BillItemRepository.ProductSalesSummary(
                    "TEST-001", "Test Product", 100, BigDecimal.valueOf(5000));
            when(billItemRepository.getProductSalesSummary(any(), any())).thenReturn(List.of(summary));

            // Act
            List<ProductSalesReport> result = reportService.getTopSellingProducts(
                    LocalDate.now().minusDays(30), LocalDate.now(), 10);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getTopSellingProductsByStoreType tests")
    class GetTopSellingProductsByStoreTypeTests {

        @Test
        @DisplayName("Should return top selling products by store type")
        void shouldReturnTopSellingProductsByStoreType() {
            // Arrange
            BillItemRepository.ProductSalesSummary summary = new BillItemRepository.ProductSalesSummary(
                    "TEST-001", "Test Product", 50, BigDecimal.valueOf(2500));
            when(billItemRepository.getProductSalesSummaryByStoreType(any(), any(), eq(StoreType.PHYSICAL)))
                    .thenReturn(List.of(summary));

            // Act
            List<ProductSalesReport> result = reportService.getTopSellingProductsByStoreType(
                    LocalDate.now().minusDays(30), LocalDate.now(), 10, StoreType.PHYSICAL);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getExpiredStockReport tests")
    class GetExpiredStockReportTests {

        @Test
        @DisplayName("Should return expired stock")
        void shouldReturnExpiredStock() {
            // Arrange
            MainInventory batch = createMainInventory("TEST-001", 50, LocalDate.now().minusDays(10));
            when(mainInventoryRepository.findExpiredBatches()).thenReturn(List.of(batch));

            // Act
            List<ExpiringStockReport> result = reportService.getExpiredStockReport();

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getReshelveReport tests")
    class GetReshelveReportTests {

        @Test
        @DisplayName("Should return reshelve report for physical store")
        void shouldReturnReshelveReportForPhysicalStore() {
            // Arrange
            PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 5);
            when(physicalStoreRepository.findLowStock(anyInt())).thenReturn(List.of(inv));
            when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001")).thenReturn(List.of());

            // Act
            List<ReshelveReport> result = reportService.getReshelveReport(StoreType.PHYSICAL);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should return reshelve report for online store")
        void shouldReturnReshelveReportForOnlineStore() {
            // Arrange
            OnlineStoreInventory inv = createOnlineInventory("TEST-001", 3);
            when(onlineStoreRepository.findLowStock(anyInt())).thenReturn(List.of(inv));
            when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001")).thenReturn(List.of());

            // Act
            List<ReshelveReport> result = reportService.getReshelveReport(StoreType.ONLINE);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getRestockRecommendations tests")
    class GetRestockRecommendationsTests {

        @Test
        @DisplayName("Should return restock recommendations for physical store")
        void shouldReturnRestockRecommendationsForPhysicalStore() {
            // Arrange
            PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 5);
            when(physicalStoreRepository.findLowStock(anyInt())).thenReturn(List.of(inv));
            when(billItemRepository.getProductSalesSummary(any(), any())).thenReturn(List.of());

            // Act
            List<RestockRecommendation> result = reportService.getRestockRecommendations(
                    StoreType.PHYSICAL, 30);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should return restock recommendations for online store")
        void shouldReturnRestockRecommendationsForOnlineStore() {
            // Arrange
            OnlineStoreInventory inv = createOnlineInventory("TEST-001", 3);
            when(onlineStoreRepository.findLowStock(anyInt())).thenReturn(List.of(inv));
            when(billItemRepository.getProductSalesSummary(any(), any())).thenReturn(List.of());

            // Act
            List<RestockRecommendation> result = reportService.getRestockRecommendations(
                    StoreType.ONLINE, 30);

            // Assert
            assertNotNull(result);
        }
    }
}
