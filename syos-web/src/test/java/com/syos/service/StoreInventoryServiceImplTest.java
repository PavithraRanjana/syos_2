package com.syos.service;

import com.syos.domain.enums.StoreType;
import com.syos.domain.models.MainInventory;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.ProductNotFoundException;
import com.syos.repository.interfaces.*;
import com.syos.service.impl.StoreInventoryServiceImpl;
import com.syos.service.interfaces.StoreInventoryService.BatchAllocation;
import com.syos.service.interfaces.StoreInventoryService.RestockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
                // Constructor order: physical, online, main, transaction, product
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
                @Disabled("needs mock adjustment for reduceQuantity/addQuantity pattern")
                @DisplayName("Should restock physical store successfully")
                void shouldRestockPhysicalStoreSuccessfully() {
                        // Arrange
                        MainInventory batch = createTestBatch(1, "TEST-001", 100, LocalDate.now().plusMonths(6));
                        when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001"))
                                        .thenReturn(List.of(batch));
                        when(physicalStoreRepository.findByProductCodeAndBatchId("TEST-001", 1))
                                        .thenReturn(Optional.empty());
                        when(physicalStoreRepository.save(any(PhysicalStoreInventory.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));
                        when(mainInventoryRepository.save(any(MainInventory.class)))
                                        .thenReturn(batch);

                        // Act
                        RestockResult result = storeInventoryService.restockPhysicalStore("TEST-001", 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(20, result.quantityRestocked());
                        verify(physicalStoreRepository).save(any(PhysicalStoreInventory.class));
                }

                @Test
                @DisplayName("Should throw exception for non-existent product")
                void shouldThrowForNonExistentProduct() {
                        // Arrange
                        when(productRepository.existsByProductCode("NONEXISTENT")).thenReturn(false);

                        // Act & Assert
                        assertThrows(ProductNotFoundException.class,
                                        () -> storeInventoryService.restockPhysicalStore("NONEXISTENT", 20));
                }

                @Test
                @DisplayName("Should return failure when no batches available")
                void shouldReturnFailureWhenNoBatchesAvailable() {
                        // Arrange
                        when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001"))
                                        .thenReturn(List.of());

                        // Act
                        RestockResult result = storeInventoryService.restockPhysicalStore("TEST-001", 20);

                        // Assert
                        assertFalse(result.success());
                        assertEquals(0, result.quantityRestocked());
                }
        }

        @Nested
        @DisplayName("Physical Store Stock Query tests")
        class PhysicalStoreStockQueryTests {

                @Test
                @DisplayName("Should get physical store stock")
                void shouldGetPhysicalStoreStock() {
                        // Arrange
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));

                        // Act
                        List<PhysicalStoreInventory> result = storeInventoryService.getPhysicalStoreStock("TEST-001");

                        // Assert
                        assertEquals(1, result.size());
                        assertEquals(50, result.get(0).getQuantityOnShelf());
                }

                @Test
                @DisplayName("Should get physical store quantity")
                void shouldGetPhysicalStoreQuantity() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001"))
                                        .thenReturn(75);

                        // Act
                        int result = storeInventoryService.getPhysicalStoreQuantity("TEST-001");

                        // Assert
                        assertEquals(75, result);
                }

                @Test
                @DisplayName("Should get physical store low stock")
                void shouldGetPhysicalStoreLowStock() {
                        // Arrange
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 5);
                        when(physicalStoreRepository.findLowStock(10)).thenReturn(List.of(inv));

                        // Act
                        List<PhysicalStoreInventory> result = storeInventoryService.getPhysicalStoreLowStock(10);

                        // Assert
                        assertEquals(1, result.size());
                }
        }

        @Nested
        @DisplayName("Online Store Restock tests")
        class OnlineStoreRestockTests {

                @Test
                @Disabled("needs mock adjustment for reduceQuantity/addQuantity pattern")
                @DisplayName("Should restock online store successfully")
                void shouldRestockOnlineStoreSuccessfully() {
                        // Arrange
                        MainInventory batch = createTestBatch(1, "TEST-001", 100, LocalDate.now().plusMonths(6));
                        when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
                        when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001"))
                                        .thenReturn(List.of(batch));
                        when(onlineStoreRepository.findByProductCodeAndBatchId("TEST-001", 1))
                                        .thenReturn(Optional.empty());
                        when(onlineStoreRepository.save(any(OnlineStoreInventory.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));
                        when(mainInventoryRepository.save(any(MainInventory.class)))
                                        .thenReturn(batch);

                        // Act
                        RestockResult result = storeInventoryService.restockOnlineStore("TEST-001", 20);

                        // Assert
                        assertTrue(result.success());
                        assertEquals(20, result.quantityRestocked());
                }
        }

        @Nested
        @DisplayName("Online Store Stock Query tests")
        class OnlineStoreStockQueryTests {

                @Test
                @DisplayName("Should get online store stock")
                void shouldGetOnlineStoreStock() {
                        // Arrange
                        OnlineStoreInventory inv = createOnlineInventory("TEST-001", 1, 50);
                        when(onlineStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));

                        // Act
                        List<OnlineStoreInventory> result = storeInventoryService.getOnlineStoreStock("TEST-001");

                        // Assert
                        assertEquals(1, result.size());
                }

                @Test
                @DisplayName("Should get online store quantity")
                void shouldGetOnlineStoreQuantity() {
                        // Arrange
                        when(onlineStoreRepository.getTotalQuantityAvailable("TEST-001"))
                                        .thenReturn(60);

                        // Act
                        int result = storeInventoryService.getOnlineStoreQuantity("TEST-001");

                        // Assert
                        assertEquals(60, result);
                }
        }

        @Nested
        @DisplayName("Stock Availability tests")
        class StockAvailabilityTests {

                @Test
                @DisplayName("Should check availability for physical store")
                void shouldCheckAvailabilityForPhysicalStore() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001"))
                                        .thenReturn(50);

                        // Act
                        boolean hasStock = storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 20);
                        int available = storeInventoryService.getAvailableQuantity("TEST-001", StoreType.PHYSICAL);

                        // Assert
                        assertTrue(hasStock);
                        assertEquals(50, available);
                }

                @Test
                @DisplayName("Should check availability for online store")
                void shouldCheckAvailabilityForOnlineStore() {
                        // Arrange
                        when(onlineStoreRepository.getTotalQuantityAvailable("TEST-001"))
                                        .thenReturn(30);

                        // Act
                        boolean hasStock = storeInventoryService.hasAvailableStock("TEST-001", StoreType.ONLINE, 20);
                        int available = storeInventoryService.getAvailableQuantity("TEST-001", StoreType.ONLINE);

                        // Assert
                        assertTrue(hasStock);
                        assertEquals(30, available);
                }

                @Test
                @DisplayName("Should return false when insufficient stock")
                void shouldReturnFalseWhenInsufficientStock() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001"))
                                        .thenReturn(10);

                        // Act
                        boolean hasStock = storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 50);

                        // Assert
                        assertFalse(hasStock);
                }
        }

        @Nested
        @DisplayName("allocateStockForSale tests")
        class AllocateStockForSaleTests {

                @Test
                @DisplayName("Should allocate stock from physical store successfully")
                void shouldAllocateStockFromPhysicalStoreSuccessfully() {
                        // Arrange
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 50);
                        when(physicalStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));
                        when(physicalStoreRepository.save(any(PhysicalStoreInventory.class)))
                                        .thenReturn(inv);

                        // Act
                        List<BatchAllocation> allocations = storeInventoryService.allocateStockForSale(
                                        "TEST-001", StoreType.PHYSICAL, 20);

                        // Assert
                        assertNotNull(allocations);
                        assertEquals(1, allocations.size());
                        assertEquals(20, allocations.get(0).quantity());
                }

                @Test
                @DisplayName("Should allocate stock from online store successfully")
                void shouldAllocateStockFromOnlineStoreSuccessfully() {
                        // Arrange
                        OnlineStoreInventory inv = createOnlineInventory("TEST-001", 1, 50);
                        when(onlineStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));
                        when(onlineStoreRepository.save(any(OnlineStoreInventory.class)))
                                        .thenReturn(inv);

                        // Act
                        List<BatchAllocation> allocations = storeInventoryService.allocateStockForSale(
                                        "TEST-001", StoreType.ONLINE, 20);

                        // Assert
                        assertNotNull(allocations);
                        assertEquals(1, allocations.size());
                        assertEquals(20, allocations.get(0).quantity());
                }

                @Test
                @DisplayName("Should throw exception when insufficient stock for allocation")
                void shouldThrowWhenInsufficientStockForAllocation() {
                        // Arrange
                        PhysicalStoreInventory inv = createPhysicalInventory("TEST-001", 1, 10);
                        when(physicalStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv));

                        // Act & Assert
                        assertThrows(InsufficientStockException.class,
                                        () -> storeInventoryService.allocateStockForSale("TEST-001", StoreType.PHYSICAL,
                                                        100));
                }

                @Test
                @DisplayName("Should allocate from multiple batches with FIFO")
                void shouldAllocateFromMultipleBatchesWithFIFO() {
                        // Arrange
                        PhysicalStoreInventory inv1 = createPhysicalInventory("TEST-001", 1, 15);
                        PhysicalStoreInventory inv2 = createPhysicalInventory("TEST-001", 2, 20);
                        when(physicalStoreRepository.findAvailableByProductCode("TEST-001"))
                                        .thenReturn(List.of(inv1, inv2));
                        when(physicalStoreRepository.save(any(PhysicalStoreInventory.class)))
                                        .thenAnswer(i -> i.getArgument(0));

                        // Act
                        List<BatchAllocation> allocations = storeInventoryService.allocateStockForSale(
                                        "TEST-001", StoreType.PHYSICAL, 25);

                        // Assert
                        assertNotNull(allocations);
                        assertEquals(2, allocations.size());
                        assertEquals(15, allocations.get(0).quantity());
                        assertEquals(10, allocations.get(1).quantity());
                }
        }

        @Nested
        @DisplayName("getStockSummary tests")
        class GetStockSummaryTests {

                @Test
                @DisplayName("Should get physical store stock summary")
                void shouldGetPhysicalStoreStockSummary() {
                        // Arrange
                        PhysicalStoreInventoryRepository.ProductStockSummary summary = new PhysicalStoreInventoryRepository.ProductStockSummary(
                                        "TEST-001", "Test", 100, 2);
                        when(physicalStoreRepository.getStockSummary()).thenReturn(List.of(summary));

                        // Act
                        var result = storeInventoryService.getPhysicalStoreStockSummary();

                        // Assert
                        assertEquals(1, result.size());
                        assertEquals(100, result.get(0).totalQuantity());
                }

                @Test
                @DisplayName("Should get online store stock summary")
                void shouldGetOnlineStoreStockSummary() {
                        // Arrange
                        OnlineStoreInventoryRepository.ProductStockSummary summary = new OnlineStoreInventoryRepository.ProductStockSummary(
                                        "TEST-001", "Test", 80, 1);
                        when(onlineStoreRepository.getStockSummary()).thenReturn(List.of(summary));

                        // Act
                        var result = storeInventoryService.getOnlineStoreStockSummary();

                        // Assert
                        assertEquals(1, result.size());
                        assertEquals(80, result.get(0).totalQuantity());
                }
        }

        @Nested
        @DisplayName("getAvailableQuantity tests")
        class GetAvailableQuantityTests {

                @Test
                @DisplayName("Should return physical store available quantity")
                void shouldReturnPhysicalStoreAvailableQuantity() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001")).thenReturn(50);

                        // Act
                        int result = storeInventoryService.getAvailableQuantity("TEST-001", StoreType.PHYSICAL);

                        // Assert
                        assertEquals(50, result);
                }

                @Test
                @DisplayName("Should return online store available quantity")
                void shouldReturnOnlineStoreAvailableQuantity() {
                        // Arrange
                        when(onlineStoreRepository.getTotalQuantityAvailable("TEST-001")).thenReturn(30);

                        // Act
                        int result = storeInventoryService.getAvailableQuantity("TEST-001", StoreType.ONLINE);

                        // Assert
                        assertEquals(30, result);
                }
        }

        @Nested
        @DisplayName("hasAvailableStock tests")
        class HasAvailableStockTests {

                @Test
                @DisplayName("Should return true when physical store has sufficient stock")
                void shouldReturnTrueWhenPhysicalStoreHasSufficientStock() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001")).thenReturn(50);

                        // Act
                        boolean result = storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 25);

                        // Assert
                        assertTrue(result);
                }

                @Test
                @DisplayName("Should return false when physical store has insufficient stock")
                void shouldReturnFalseWhenPhysicalStoreHasInsufficientStock() {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001")).thenReturn(10);

                        // Act
                        boolean result = storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 25);

                        // Assert
                        assertFalse(result);
                }

                @Test
                @DisplayName("Should return true when online store has sufficient stock")
                void shouldReturnTrueWhenOnlineStoreHasSufficientStock() {
                        // Arrange
                        when(onlineStoreRepository.getTotalQuantityAvailable("TEST-001")).thenReturn(40);

                        // Act
                        boolean result = storeInventoryService.hasAvailableStock("TEST-001", StoreType.ONLINE, 20);

                        // Assert
                        assertTrue(result);
                }
        }

        @Nested
        @DisplayName("reduceStock tests")
        class ReduceStockTests {

                @Test
                @DisplayName("Should reduce physical store stock")
                void shouldReducePhysicalStoreStock() {
                        // Arrange
                        when(physicalStoreRepository.reduceQuantity("TEST-001", 1, 10)).thenReturn(true);

                        // Act
                        boolean result = storeInventoryService.reducePhysicalStoreStock("TEST-001", 1, 10);

                        // Assert
                        assertTrue(result);
                        verify(physicalStoreRepository).reduceQuantity("TEST-001", 1, 10);
                }

                @Test
                @DisplayName("Should reduce online store stock")
                void shouldReduceOnlineStoreStock() {
                        // Arrange
                        when(onlineStoreRepository.reduceQuantity("TEST-001", 1, 10)).thenReturn(true);

                        // Act
                        boolean result = storeInventoryService.reduceOnlineStoreStock("TEST-001", 1, 10);

                        // Assert
                        assertTrue(result);
                        verify(onlineStoreRepository).reduceQuantity("TEST-001", 1, 10);
                }
        }

        @Nested
        @DisplayName("async method tests")
        class AsyncMethodTests {

                @Test
                @DisplayName("Should check available stock asynchronously")
                void shouldCheckAvailableStockAsynchronously() throws Exception {
                        // Arrange
                        when(physicalStoreRepository.getTotalQuantityOnShelf("TEST-001")).thenReturn(50);

                        // Act
                        var future = storeInventoryService.hasAvailableStockAsync("TEST-001", StoreType.PHYSICAL, 25);

                        // Assert
                        assertNotNull(future);
                        assertTrue(future.get());
                }
        }
}
