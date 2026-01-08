package com.syos.service;

import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.MainInventoryRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.impl.InventoryServiceImpl;
import com.syos.service.interfaces.InventoryService.ProductInventorySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private MainInventoryRepository mainInventoryRepository;

    @Mock
    private ProductRepository productRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(mainInventoryRepository, productRepository);
    }

    private MainInventory createTestBatch(String productCode, int quantity, LocalDate expiryDate) {
        MainInventory batch = new MainInventory();
        batch.setMainInventoryId(1);
        batch.setProductCode(new ProductCode(productCode));
        batch.setQuantityReceived(quantity);
        batch.setRemainingQuantity(quantity);
        batch.setPurchasePrice(new Money(100.00));
        batch.setPurchaseDate(LocalDate.now());
        batch.setExpiryDate(expiryDate);
        batch.setSupplierName("Test Supplier");
        return batch;
    }

    @Nested
    @DisplayName("addBatch tests")
    class AddBatchTests {

        @Test
        @DisplayName("Should add batch successfully")
        void shouldAddBatchSuccessfully() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
            when(mainInventoryRepository.save(any(MainInventory.class))).thenAnswer(invocation -> {
                MainInventory batch = invocation.getArgument(0);
                batch.setMainInventoryId(1);
                return batch;
            });

            // Act
            MainInventory result = inventoryService.addBatch(
                "TEST-001", 100, BigDecimal.valueOf(50.00),
                LocalDate.now(), LocalDate.now().plusMonths(6), "Test Supplier"
            );

            // Assert
            assertNotNull(result);
            assertEquals(100, result.getQuantityReceived());
            assertEquals(100, result.getRemainingQuantity());
            verify(mainInventoryRepository).save(any(MainInventory.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent product")
        void shouldThrowForNonExistentProduct() {
            // Arrange
            when(productRepository.existsByProductCode("NONEXISTENT")).thenReturn(false);

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                () -> inventoryService.addBatch(
                    "NONEXISTENT", 100, BigDecimal.valueOf(50.00),
                    LocalDate.now(), LocalDate.now().plusMonths(6), "Test Supplier"
                ));
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void shouldThrowForZeroQuantity() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.addBatch(
                    "TEST-001", 0, BigDecimal.valueOf(50.00),
                    LocalDate.now(), LocalDate.now().plusMonths(6), "Test Supplier"
                ));
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowForNegativeQuantity() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.addBatch(
                    "TEST-001", -10, BigDecimal.valueOf(50.00),
                    LocalDate.now(), LocalDate.now().plusMonths(6), "Test Supplier"
                ));
        }

        @Test
        @DisplayName("Should throw exception for negative purchase price")
        void shouldThrowForNegativePurchasePrice() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.addBatch(
                    "TEST-001", 100, BigDecimal.valueOf(-10.00),
                    LocalDate.now(), LocalDate.now().plusMonths(6), "Test Supplier"
                ));
        }

        @Test
        @DisplayName("Should throw exception when expiry date is before purchase date")
        void shouldThrowWhenExpiryBeforePurchase() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.addBatch(
                    "TEST-001", 100, BigDecimal.valueOf(50.00),
                    LocalDate.now(), LocalDate.now().minusDays(1), "Test Supplier"
                ));
        }

        @Test
        @DisplayName("Should use current date as purchase date when null")
        void shouldUseCurrentDateWhenPurchaseDateNull() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
            when(mainInventoryRepository.save(any(MainInventory.class))).thenAnswer(invocation -> {
                MainInventory batch = invocation.getArgument(0);
                batch.setMainInventoryId(1);
                return batch;
            });

            // Act
            MainInventory result = inventoryService.addBatch(
                "TEST-001", 100, BigDecimal.valueOf(50.00),
                null, LocalDate.now().plusMonths(6), "Test Supplier"
            );

            // Assert
            assertNotNull(result.getPurchaseDate());
            assertEquals(LocalDate.now(), result.getPurchaseDate());
        }
    }

    @Nested
    @DisplayName("findBatch tests")
    class FindBatchTests {

        @Test
        @DisplayName("Should find batch by ID")
        void shouldFindBatchById() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6));
            when(mainInventoryRepository.findById(1)).thenReturn(Optional.of(batch));

            // Act
            Optional<MainInventory> result = inventoryService.findBatchById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getMainInventoryId());
        }

        @Test
        @DisplayName("Should return empty when batch not found")
        void shouldReturnEmptyWhenBatchNotFound() {
            // Arrange
            when(mainInventoryRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<MainInventory> result = inventoryService.findBatchById(999);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should find batches by product code")
        void shouldFindBatchesByProductCode() {
            // Arrange
            List<MainInventory> batches = List.of(
                createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6)),
                createTestBatch("TEST-001", 50, LocalDate.now().plusMonths(3))
            );
            when(mainInventoryRepository.findByProductCode("TEST-001")).thenReturn(batches);

            // Act
            List<MainInventory> result = inventoryService.findBatchesByProductCode("TEST-001");

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should find available batches for product")
        void shouldFindAvailableBatches() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6));
            when(mainInventoryRepository.findAvailableBatchesByProductCode("TEST-001"))
                .thenReturn(List.of(batch));

            // Act
            List<MainInventory> result = inventoryService.findAvailableBatches("TEST-001");

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).getRemainingQuantity() > 0);
        }
    }

    @Nested
    @DisplayName("quantity management tests")
    class QuantityManagementTests {

        @Test
        @DisplayName("Should reduce quantity successfully")
        void shouldReduceQuantitySuccessfully() {
            // Arrange
            when(mainInventoryRepository.reduceQuantity(1, 10)).thenReturn(true);

            // Act
            boolean result = inventoryService.reduceQuantity(1, 10);

            // Assert
            assertTrue(result);
            verify(mainInventoryRepository).reduceQuantity(1, 10);
        }

        @Test
        @DisplayName("Should return false when insufficient stock")
        void shouldReturnFalseWhenInsufficientStock() {
            // Arrange
            when(mainInventoryRepository.reduceQuantity(1, 100)).thenReturn(false);

            // Act
            boolean result = inventoryService.reduceQuantity(1, 100);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw exception for zero reduction amount")
        void shouldThrowForZeroReductionAmount() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.reduceQuantity(1, 0));
        }

        @Test
        @DisplayName("Should throw exception for negative reduction amount")
        void shouldThrowForNegativeReductionAmount() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.reduceQuantity(1, -10));
        }

        @Test
        @DisplayName("Should increase quantity successfully")
        void shouldIncreaseQuantitySuccessfully() {
            // Arrange
            when(mainInventoryRepository.increaseQuantity(1, 10)).thenReturn(true);

            // Act
            boolean result = inventoryService.increaseQuantity(1, 10);

            // Assert
            assertTrue(result);
            verify(mainInventoryRepository).increaseQuantity(1, 10);
        }

        @Test
        @DisplayName("Should throw exception for zero increase amount")
        void shouldThrowForZeroIncreaseAmount() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.increaseQuantity(1, 0));
        }
    }

    @Nested
    @DisplayName("stock availability tests")
    class StockAvailabilityTests {

        @Test
        @DisplayName("Should return true when sufficient stock available")
        void shouldReturnTrueWhenSufficientStock() {
            // Arrange
            when(mainInventoryRepository.getTotalRemainingQuantity("TEST-001")).thenReturn(100);

            // Act
            boolean result = inventoryService.hasAvailableStock("TEST-001", 50);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when insufficient stock")
        void shouldReturnFalseWhenInsufficientStock() {
            // Arrange
            when(mainInventoryRepository.getTotalRemainingQuantity("TEST-001")).thenReturn(30);

            // Act
            boolean result = inventoryService.hasAvailableStock("TEST-001", 50);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return total remaining quantity")
        void shouldReturnTotalRemainingQuantity() {
            // Arrange
            when(mainInventoryRepository.getTotalRemainingQuantity("TEST-001")).thenReturn(150);

            // Act
            int result = inventoryService.getTotalRemainingQuantity("TEST-001");

            // Assert
            assertEquals(150, result);
        }

        @Test
        @DisplayName("Should get next batch for sale using FIFO")
        void shouldGetNextBatchForSale() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusDays(30));
            when(mainInventoryRepository.findNextBatchForSale("TEST-001", 10))
                .thenReturn(Optional.of(batch));

            // Act
            Optional<MainInventory> result = inventoryService.getNextBatchForSale("TEST-001", 10);

            // Assert
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("expiry management tests")
    class ExpiryManagementTests {

        @Test
        @DisplayName("Should find batches expiring within days")
        void shouldFindBatchesExpiringWithinDays() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusDays(5));
            when(mainInventoryRepository.findExpiringWithinDays(7)).thenReturn(List.of(batch));

            // Act
            List<MainInventory> result = inventoryService.findExpiringWithinDays(7);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception for negative days")
        void shouldThrowForNegativeDays() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.findExpiringWithinDays(-1));
        }

        @Test
        @DisplayName("Should find expired batches")
        void shouldFindExpiredBatches() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().minusDays(1));
            when(mainInventoryRepository.findExpiredBatches()).thenReturn(List.of(batch));

            // Act
            List<MainInventory> result = inventoryService.findExpiredBatches();

            // Assert
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("search tests")
    class SearchTests {

        @Test
        @DisplayName("Should find batches by supplier")
        void shouldFindBatchesBySupplier() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6));
            when(mainInventoryRepository.findBySupplier("Test Supplier")).thenReturn(List.of(batch));

            // Act
            List<MainInventory> result = inventoryService.findBySupplier("Test Supplier");

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find batches by purchase date range")
        void shouldFindBatchesByPurchaseDateRange() {
            // Arrange
            MainInventory batch = createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6));
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            when(mainInventoryRepository.findByPurchaseDateRange(startDate, endDate))
                .thenReturn(List.of(batch));

            // Act
            List<MainInventory> result = inventoryService.findByPurchaseDateRange(startDate, endDate);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when start date is null")
        void shouldThrowWhenStartDateNull() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.findByPurchaseDateRange(null, LocalDate.now()));
        }

        @Test
        @DisplayName("Should throw exception when end date is null")
        void shouldThrowWhenEndDateNull() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.findByPurchaseDateRange(LocalDate.now(), null));
        }

        @Test
        @DisplayName("Should throw exception when end date before start date")
        void shouldThrowWhenEndDateBeforeStartDate() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> inventoryService.findByPurchaseDateRange(
                    LocalDate.now(), LocalDate.now().minusDays(1)));
        }
    }

    @Nested
    @DisplayName("pagination tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated batches")
        void shouldReturnPaginatedBatches() {
            // Arrange
            List<MainInventory> batches = List.of(
                createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6))
            );
            when(mainInventoryRepository.findAll(0, 10)).thenReturn(batches);

            // Act
            List<MainInventory> result = inventoryService.findAll(0, 10);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should calculate correct offset")
        void shouldCalculateCorrectOffset() {
            // Arrange
            when(mainInventoryRepository.findAll(20, 10)).thenReturn(List.of());

            // Act
            inventoryService.findAll(2, 10);

            // Assert
            verify(mainInventoryRepository).findAll(20, 10);
        }
    }

    @Nested
    @DisplayName("count tests")
    class CountTests {

        @Test
        @DisplayName("Should return batch count")
        void shouldReturnBatchCount() {
            // Arrange
            when(mainInventoryRepository.count()).thenReturn(100L);

            // Act
            long result = inventoryService.getBatchCount();

            // Assert
            assertEquals(100L, result);
        }
    }

    @Nested
    @DisplayName("inventory summary tests")
    class InventorySummaryTests {

        @Test
        @DisplayName("Should return inventory summary")
        void shouldReturnInventorySummary() {
            // Arrange
            MainInventory batch1 = createTestBatch("TEST-001", 100, LocalDate.now().plusMonths(6));
            batch1.setProductName("Test Product 1");
            MainInventory batch2 = createTestBatch("TEST-001", 50, LocalDate.now().plusMonths(3));
            batch2.setProductName("Test Product 1");
            MainInventory batch3 = createTestBatch("TEST-002", 75, LocalDate.now().plusMonths(4));
            batch3.setProductName("Test Product 2");

            when(mainInventoryRepository.findAll()).thenReturn(List.of(batch1, batch2, batch3));

            // Act
            List<ProductInventorySummary> result = inventoryService.getInventorySummary();

            // Assert
            assertEquals(2, result.size());

            ProductInventorySummary product1Summary = result.stream()
                .filter(s -> s.productCode().equals("TEST-001"))
                .findFirst()
                .orElse(null);
            assertNotNull(product1Summary);
            assertEquals(150, product1Summary.totalQuantity());
            assertEquals(2, product1Summary.batchCount());
        }

        @Test
        @DisplayName("Should return empty summary for empty inventory")
        void shouldReturnEmptySummary() {
            // Arrange
            when(mainInventoryRepository.findAll()).thenReturn(List.of());

            // Act
            List<ProductInventorySummary> result = inventoryService.getInventorySummary();

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}
