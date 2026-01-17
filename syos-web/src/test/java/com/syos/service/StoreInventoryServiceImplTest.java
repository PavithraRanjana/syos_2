package com.syos.service;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.MainInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.*;
import com.syos.service.impl.StoreInventoryServiceImpl;
import com.syos.service.interfaces.StoreInventoryService.BatchAllocation;
import com.syos.service.interfaces.StoreInventoryService.RestockResult;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreInventoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoreInventoryServiceImplTest {

        @Mock
        private PhysicalStoreInventoryRepository physicalStoreRepository;

        @Mock
        private OnlineStoreInventoryRepository onlineStoreRepository;

        @Mock
        private MainInventoryRepository mainInventoryRepository;

        @Mock
        private InventoryTransactionRepository transactionRepository;

        @Mock
        private ProductRepository productRepository;

        private StoreInventoryServiceImpl storeInventoryService;

        @BeforeEach
        void setUp() {
                storeInventoryService = new StoreInventoryServiceImpl(
                                physicalStoreRepository,
                                onlineStoreRepository,
                                mainInventoryRepository,
                                transactionRepository,
                                productRepository);
        }

        private MainInventory createTestBatch(Integer batchId, String productCode, int quantity, LocalDate expiry) {
                MainInventory batch = new MainInventory();
                batch.setMainInventoryId(batchId);
                batch.setProductCode(new ProductCode(productCode));
                batch.setProductName("Test Product");
                batch.setQuantityReceived(100);
                batch.setRemainingQuantity(quantity);
                batch.setPurchaseDate(LocalDate.now());
                batch.setExpiryDate(expiry);
                return batch;
        }

        private PhysicalStoreInventory createPhysicalInventory(String productCode, Integer batchId, int quantity) {
                PhysicalStoreInventory inv = new PhysicalStoreInventory(
                                new ProductCode(productCode),
                                batchId,
                                quantity,
                                LocalDate.now());
                inv.setProductName("Test Product");
                inv.setExpiryDate(LocalDate.now().plusMonths(6));
                return inv;
        }

        private OnlineStoreInventory createOnlineInventory(String productCode, Integer batchId, int quantity) {
                OnlineStoreInventory inv = new OnlineStoreInventory(
                                new ProductCode(productCode),
                                batchId,
                                quantity,
                                LocalDate.now());
                inv.setProductName("Test Product");
                inv.setExpiryDate(LocalDate.now().plusMonths(6));
                return inv;
        }

        @Nested
        @DisplayName("Physical Store Restock tests")
        class PhysicalStoreRestockTests {

                @Test
                @DisplayName("Should restock physical store successfully")
                void shouldRestockPhysicalStoreSuccessfully() {
                        // Arrange
                        String productCode = "TEST-001";
                        MainInventory batch = createTestBatch(1, productCode, 100, LocalDate.now().plusMonths(6));
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));

                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(20))).thenReturn(true);
                        when(physicalStoreRepository.addQuantity(eq(productCode), eq(1), eq(20))).thenReturn(true);

                        // Act
                        RestockResult result = storeInventoryService.restockPhysicalStore(productCode, 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(20, result.quantityRestocked());
                        assertEquals(1, result.batchesUsed());

                        verify(mainInventoryRepository).reduceQuantity(1, 20);
                        verify(physicalStoreRepository).addQuantity(productCode, 1, 20);
                        verify(transactionRepository).save(any());
                }

                @Test
                @DisplayName("Should restock physical store from specific batch successfully")
                void shouldRestockPhysicalStoreFromBatchSuccessfully() {
                        // Arrange
                        String productCode = "TEST-001";
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(20))).thenReturn(true);
                        when(physicalStoreRepository.addQuantity(eq(productCode), eq(1), eq(20))).thenReturn(true);

                        // Act
                        boolean result = storeInventoryService.restockPhysicalStoreFromBatch(productCode, 1, 20);

                        // Assert
                        assertTrue(result);
                        verify(mainInventoryRepository).reduceQuantity(1, 20);
                        verify(physicalStoreRepository).addQuantity(productCode, 1, 20);
                }

                @Test
                @DisplayName("Should throw for non-existent product")
                void shouldThrowForNonExistentProduct() {
                        when(productRepository.existsByProductCode("NONEXISTENT")).thenReturn(false);
                        assertThrows(ProductNotFoundException.class,
                                        () -> storeInventoryService.restockPhysicalStore("NONEXISTENT", 20));
                }

                @Test
                @DisplayName("Should return failure when no batches available")
                void shouldReturnFailureWhenNoBatchesAvailable() {
                        String productCode = "TEST-001";
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of());

                        RestockResult result = storeInventoryService.restockPhysicalStore(productCode, 20);

                        assertFalse(result.success());
                        assertEquals(0, result.quantityRestocked());
                }

                @Test
                @DisplayName("Should handle partial restock when main inventory insufficient")
                void shouldHandlePartialRestock() {
                        // Arrange
                        String productCode = "TEST-001";
                        MainInventory batch = createTestBatch(1, productCode, 10, LocalDate.now().plusMonths(6)); // Only
                                                                                                                  // 10
                                                                                                                  // available
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));

                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(10))).thenReturn(true);
                        when(physicalStoreRepository.addQuantity(eq(productCode), eq(1), eq(10))).thenReturn(true);

                        // Act - request 20
                        RestockResult result = storeInventoryService.restockPhysicalStore(productCode, 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(10, result.quantityRestocked());
                        assertTrue(result.message().contains("Partial restock"));
                }
        }

        @Nested
        @DisplayName("Online Store Restock tests")
        class OnlineStoreRestockTests {

                @Test
                @DisplayName("Should restock online store successfully")
                void shouldRestockOnlineStoreSuccessfully() {
                        // Arrange
                        String productCode = "TEST-001";
                        MainInventory batch = createTestBatch(1, productCode, 100, LocalDate.now().plusMonths(6));
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));

                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(20))).thenReturn(true);
                        when(onlineStoreRepository.addQuantity(eq(productCode), eq(1), eq(20))).thenReturn(true);

                        // Act
                        RestockResult result = storeInventoryService.restockOnlineStore(productCode, 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(20, result.quantityRestocked());

                        verify(mainInventoryRepository).reduceQuantity(1, 20);
                        verify(onlineStoreRepository).addQuantity(productCode, 1, 20);
                }

                @Test
                @DisplayName("Should restock online store from specific batch successfully")
                void shouldRestockOnlineStoreFromBatchSuccessfully() {
                        // Arrange
                        String productCode = "TEST-001";
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(20))).thenReturn(true);
                        when(onlineStoreRepository.addQuantity(eq(productCode), eq(1), eq(20))).thenReturn(true);

                        // Act
                        boolean result = storeInventoryService.restockOnlineStoreFromBatch(productCode, 1, 20);

                        // Assert
                        assertTrue(result);
                        verify(mainInventoryRepository).reduceQuantity(1, 20);
                        verify(onlineStoreRepository).addQuantity(productCode, 1, 20);
                }

                @Test
                @DisplayName("Should throw for non-existent product")
                void shouldThrowForNonExistentProduct() {
                        when(productRepository.existsByProductCode("NONEXISTENT")).thenReturn(false);
                        assertThrows(ProductNotFoundException.class,
                                        () -> storeInventoryService.restockOnlineStore("NONEXISTENT", 20));
                }

                @Test
                @DisplayName("Should return failure when no batches available")
                void shouldReturnFailureWhenNoBatchesAvailable() {
                        String productCode = "TEST-001";
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of());

                        RestockResult result = storeInventoryService.restockOnlineStore(productCode, 20);

                        assertFalse(result.success());
                        assertEquals(0, result.quantityRestocked());
                }

                @Test
                @DisplayName("Should handle partial restock when main inventory insufficient")
                void shouldHandlePartialRestock() {
                        // Arrange
                        String productCode = "TEST-001";
                        MainInventory batch = createTestBatch(1, productCode, 10, LocalDate.now().plusMonths(6));
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));

                        when(mainInventoryRepository.reduceQuantity(eq(1), eq(10))).thenReturn(true);
                        when(onlineStoreRepository.addQuantity(eq(productCode), eq(1), eq(10))).thenReturn(true);

                        // Act - request 20
                        RestockResult result = storeInventoryService.restockOnlineStore(productCode, 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(10, result.quantityRestocked());
                        assertTrue(result.message().contains("Partial restock"));
                }
        }

        @Nested
        @DisplayName("Stock Query tests")
        class StockQueryTests {

                @Test
                @DisplayName("Should get physical store stock")
                void shouldGetPhysicalStoreStock() {
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));

                        List<PhysicalStoreInventory> result = storeInventoryService.getPhysicalStoreStock("TEST-001");

                        assertEquals(1, result.size());
                        assertEquals(50, result.get(0).getQuantityOnShelf());
                }

                @Test
                @DisplayName("Should get online store stock")
                void shouldGetOnlineStoreStock() {
                        OnlineStoreInventory inv = createOnlineInventory("TEST-001", 1, 50);
                        when(onlineStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));

                        List<OnlineStoreInventory> result = storeInventoryService.getOnlineStoreStock("TEST-001");

                        assertEquals(1, result.size());
                        assertEquals(50, result.get(0).getQuantityAvailable());
                }

                @Test
                @DisplayName("Should get physical store low stock")
                void shouldGetPhysicalStoreLowStock() {
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 5);
                        when(physicalStoreRepository.findLowStock(10)).thenReturn(List.of(inv));

                        List<PhysicalStoreInventory> result = storeInventoryService.getPhysicalStoreLowStock(10);

                        assertEquals(1, result.size());
                }

                @Test
                @DisplayName("Should get online store low stock")
                void shouldGetOnlineStoreLowStock() {
                        OnlineStoreInventory inv = createOnlineInventory("TEST-001", 1, 5);
                        when(onlineStoreRepository.findLowStock(10)).thenReturn(List.of(inv));

                        List<OnlineStoreInventory> result = storeInventoryService.getOnlineStoreLowStock(10);

                        assertEquals(1, result.size());
                }

                @Test
                @DisplayName("Should get stock summaries")
                void shouldGetStockSummaries() {
                        when(physicalStoreRepository.getStockSummary()).thenReturn(List.of(
                                        new PhysicalStoreInventoryRepository.ProductStockSummary("P1", "Product 1", 100,
                                                        2)));
                        when(onlineStoreRepository.getStockSummary()).thenReturn(List.of(
                                        new OnlineStoreInventoryRepository.ProductStockSummary("P2", "Product 2", 50,
                                                        1)));

                        var physical = storeInventoryService.getPhysicalStoreStockSummary();
                        var online = storeInventoryService.getOnlineStoreStockSummary();

                        assertEquals(1, physical.size());
                        assertEquals("P1", physical.get(0).productCode());
                        assertEquals(1, online.size());
                        assertEquals("P2", online.get(0).productCode());
                }
        }

        @Nested
        @DisplayName("Stock Availability Tests")
        class StockAvailabilityTests {

                @Test
                @DisplayName("Should check availability correctly")
                void shouldCheckAvailability() {
                        when(physicalStoreRepository.getTotalQuantityOnShelf("P1")).thenReturn(50);
                        when(onlineStoreRepository.getTotalQuantityAvailable("P1")).thenReturn(30);

                        assertTrue(storeInventoryService.hasAvailableStock("P1", StoreType.PHYSICAL, 50));
                        assertFalse(storeInventoryService.hasAvailableStock("P1", StoreType.PHYSICAL, 51));

                        assertTrue(storeInventoryService.hasAvailableStock("P1", StoreType.ONLINE, 30));
                        assertFalse(storeInventoryService.hasAvailableStock("P1", StoreType.ONLINE, 31));
                }

                @Test
                @DisplayName("Get available quantity returns correct store quantity")
                void shouldGetAvailableQuantity() {
                        when(physicalStoreRepository.getTotalQuantityOnShelf("P1")).thenReturn(50);
                        when(onlineStoreRepository.getTotalQuantityAvailable("P1")).thenReturn(30);

                        assertEquals(50, storeInventoryService.getAvailableQuantity("P1", StoreType.PHYSICAL));
                        assertEquals(30, storeInventoryService.getAvailableQuantity("P1", StoreType.ONLINE));
                }
        }

        @Nested
        @DisplayName("Allocation Tests")
        class AllocationTests {

                @Test
                @DisplayName("Should allocate stock from physical store")
                void shouldAllocateStockFromPhysicalStore() {
                        PhysicalStoreInventory inv = createPhysicalInventory("P1", 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode("P1")).thenReturn(List.of(inv));

                        List<BatchAllocation> result = storeInventoryService.allocateStockForSale("P1",
                                        StoreType.PHYSICAL, 20);

                        assertEquals(1, result.size());
                        assertEquals(20, result.get(0).quantity());
                }

                @Test
                @DisplayName("Should allocate stock from multiple batches FIFO")
                void shouldAllocateFromMultipleBatches() {
                        PhysicalStoreInventory inv1 = createPhysicalInventory("P1", 1, 10);
                        PhysicalStoreInventory inv2 = createPhysicalInventory("P1", 2, 20);
                        when(physicalStoreRepository.findAvailableByProductCode("P1")).thenReturn(List.of(inv1, inv2));

                        List<BatchAllocation> result = storeInventoryService.allocateStockForSale("P1",
                                        StoreType.PHYSICAL, 25);

                        assertEquals(2, result.size());
                        assertEquals(10, result.get(0).quantity()); // First batch fully used
                        assertEquals(15, result.get(1).quantity()); // Second batch partially used
                }

                @Test
                @DisplayName("Should throw if insufficient stock for allocation")
                void shouldThrowIfInsufficientStock() {
                        PhysicalStoreInventory inv = createPhysicalInventory("P1", 1, 10);
                        when(physicalStoreRepository.findAvailableByProductCode("P1")).thenReturn(List.of(inv));

                        assertThrows(InsufficientStockException.class,
                                        () -> storeInventoryService.allocateStockForSale("P1", StoreType.PHYSICAL, 20));
                }

                @Test
                @DisplayName("Should get next batch for sale")
                void shouldGetNextBatchForSale() {
                        PhysicalStoreInventory inv = createPhysicalInventory("P1", 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode("P1")).thenReturn(List.of(inv));

                        Optional<BatchAllocation> result = storeInventoryService.getNextBatchForSale("P1",
                                        StoreType.PHYSICAL, 10);

                        assertTrue(result.isPresent());
                        assertEquals(10, result.get().quantity());
                }
        }

        @Nested
        @DisplayName("Async Method Tests")
        class AsyncMethodTests {

                @Test
                @DisplayName("Should restock physical store asynchronously")
                void shouldRestockPhysicalStoreAsync() throws Exception {
                        String productCode = "TEST-ASYNC";
                        MainInventory batch = createTestBatch(1, productCode, 100, LocalDate.now());
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));
                        when(mainInventoryRepository.reduceQuantity(anyInt(), anyInt())).thenReturn(true);
                        when(physicalStoreRepository.addQuantity(anyString(), anyInt(), anyInt())).thenReturn(true);

                        CompletableFuture<RestockResult> future = storeInventoryService
                                        .restockPhysicalStoreAsync(productCode, 10);
                        RestockResult result = future.get();

                        assertTrue(result.success());
                        verify(physicalStoreRepository).addQuantity(eq(productCode), eq(1), eq(10));
                }

                @Test
                @DisplayName("Should restock online store asynchronously")
                void shouldRestockOnlineStoreAsync() throws Exception {
                        String productCode = "TEST-ASYNC-ON";
                        MainInventory batch = createTestBatch(2, productCode, 100, LocalDate.now());
                        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode(productCode))
                                        .thenReturn(List.of(batch));
                        when(mainInventoryRepository.reduceQuantity(anyInt(), anyInt())).thenReturn(true);
                        when(onlineStoreRepository.addQuantity(anyString(), anyInt(), anyInt())).thenReturn(true);

                        CompletableFuture<RestockResult> future = storeInventoryService
                                        .restockOnlineStoreAsync(productCode, 10);
                        RestockResult result = future.get();

                        assertTrue(result.success());
                        verify(onlineStoreRepository).addQuantity(eq(productCode), eq(2), eq(10));
                }

                @Test
                @DisplayName("Should allocate stock asynchronously")
                void shouldAllocateStockAsync() throws Exception {
                        String productCode = "P-ASYNC";
                        PhysicalStoreInventory inv = createPhysicalInventory(productCode, 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode(productCode)).thenReturn(List.of(inv));

                        CompletableFuture<List<BatchAllocation>> future = storeInventoryService
                                        .allocateStockForSaleAsync(productCode, StoreType.PHYSICAL, 5);
                        List<BatchAllocation> result = future.get();

                        assertEquals(1, result.size());
                        assertEquals(5, result.get(0).quantity());
                }

                @Test
                @DisplayName("Should check stock availability asynchronously")
                void shouldCheckStockAsync() throws Exception {
                        when(physicalStoreRepository.getTotalQuantityOnShelf("P-ASYNC")).thenReturn(100);

                        CompletableFuture<Boolean> future = storeInventoryService.hasAvailableStockAsync("P-ASYNC",
                                        StoreType.PHYSICAL, 50);
                        assertTrue(future.get());
                }
        }
}
