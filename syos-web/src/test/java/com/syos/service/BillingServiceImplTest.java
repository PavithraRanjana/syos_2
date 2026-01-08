package com.syos.service;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.BillNotFoundException;
import com.syos.exception.InsufficientStockException;
import com.syos.exception.InvalidPaymentException;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.BillItemRepository;
import com.syos.repository.interfaces.BillRepository;
import com.syos.repository.interfaces.InventoryTransactionRepository;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.impl.BillingServiceImpl;
import com.syos.service.interfaces.BillingService.ValidationResult;
import com.syos.service.interfaces.StoreInventoryService;
import com.syos.service.interfaces.StoreInventoryService.BatchAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreInventoryService storeInventoryService;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingServiceImpl(
            billRepository,
            billItemRepository,
            productRepository,
            storeInventoryService,
            transactionRepository
        );
    }

    private Bill createTestBill(Integer billId, StoreType storeType, TransactionType transactionType) {
        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setSerialNumber(new BillSerialNumber("PH-00001"));
        bill.setStoreType(storeType);
        bill.setTransactionType(transactionType);
        bill.setCustomerId(1);
        bill.setCashierId("CASHIER-1");
        return bill;
    }

    private Product createTestProduct(String code, String name, BigDecimal price) {
        Product product = new Product(
            new ProductCode(code),
            name,
            1, 1, 1,
            new Money(price)
        );
        product.setActive(true);
        return product;
    }

    private BillItem createTestBillItem(Integer itemId, Integer billId, String productCode, int qty, BigDecimal price) {
        BillItem item = new BillItem();
        item.setBillItemId(itemId);
        item.setBillId(billId);
        item.setProductCode(new ProductCode(productCode));
        item.setProductName("Test Product");
        item.setMainInventoryId(1);
        item.setQuantity(qty);
        item.setUnitPrice(new Money(price));
        item.recalculateTotal();
        return item;
    }

    @Nested
    @DisplayName("createBill tests")
    class CreateBillTests {

        @Test
        @DisplayName("Should create physical store bill successfully")
        void shouldCreatePhysicalStoreBillSuccessfully() {
            // Arrange
            when(billRepository.generateNextSerialNumber(StoreType.PHYSICAL)).thenReturn("PH-00001");
            when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
                Bill bill = invocation.getArgument(0);
                bill.setBillId(1);
                return bill;
            });

            // Act
            Bill result = billingService.createBill(StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getBillId());
            assertEquals(StoreType.PHYSICAL, result.getStoreType());
            assertEquals(TransactionType.CASH, result.getTransactionType());
            verify(billRepository).save(any(Bill.class));
        }

        @Test
        @DisplayName("Should create online store bill with customer")
        void shouldCreateOnlineStoreBillWithCustomer() {
            // Arrange
            when(billRepository.generateNextSerialNumber(StoreType.ONLINE)).thenReturn("ON-00001");
            when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
                Bill bill = invocation.getArgument(0);
                bill.setBillId(1);
                return bill;
            });

            // Act
            Bill result = billingService.createBill(StoreType.ONLINE, TransactionType.ONLINE, 1, null);

            // Assert
            assertNotNull(result);
            assertEquals(StoreType.ONLINE, result.getStoreType());
            assertEquals(1, result.getCustomerId());
        }

        @Test
        @DisplayName("Should throw exception for online bill without customer")
        void shouldThrowForOnlineBillWithoutCustomer() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.createBill(StoreType.ONLINE, TransactionType.ONLINE, null, null));
        }

        @Test
        @DisplayName("Should throw exception for null store type")
        void shouldThrowForNullStoreType() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.createBill(null, TransactionType.CASH, null, "CASHIER-1"));
        }

        @Test
        @DisplayName("Should throw exception for null transaction type")
        void shouldThrowForNullTransactionType() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.createBill(StoreType.PHYSICAL, null, null, "CASHIER-1"));
        }
    }

    @Nested
    @DisplayName("addItem tests")
    class AddItemTests {

        @Test
        @DisplayName("Should add item to bill successfully")
        void shouldAddItemToBillSuccessfully() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));

            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 5)).thenReturn(true);
            when(storeInventoryService.allocateStockForSale("TEST-001", StoreType.PHYSICAL, 5))
                .thenReturn(List.of(new BatchAllocation(1, "TEST-001", 5, LocalDate.now().plusMonths(6))));
            when(billItemRepository.save(any(BillItem.class))).thenAnswer(invocation -> {
                BillItem item = invocation.getArgument(0);
                item.setBillItemId(1);
                return item;
            });
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            BillItem result = billingService.addItem(1, "TEST-001", 5);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getQuantity());
            verify(billItemRepository).save(any(BillItem.class));
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void shouldThrowForZeroQuantity() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.addItem(1, "TEST-001", 0));
        }

        @Test
        @DisplayName("Should throw exception for non-existent product")
        void shouldThrowForNonExistentProduct() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(productRepository.findByProductCode("NONEXISTENT")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                () -> billingService.addItem(1, "NONEXISTENT", 5));
        }

        @Test
        @DisplayName("Should throw exception for inactive product")
        void shouldThrowForInactiveProduct() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            product.setActive(false);

            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.addItem(1, "TEST-001", 5));
        }

        @Test
        @DisplayName("Should throw exception for insufficient stock")
        void shouldThrowForInsufficientStock() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));

            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.hasAvailableStock("TEST-001", StoreType.PHYSICAL, 100)).thenReturn(false);
            when(storeInventoryService.getAvailableQuantity("TEST-001", StoreType.PHYSICAL)).thenReturn(10);

            // Act & Assert
            assertThrows(InsufficientStockException.class,
                () -> billingService.addItem(1, "TEST-001", 100));
        }
    }

    @Nested
    @DisplayName("removeItem tests")
    class RemoveItemTests {

        @Test
        @DisplayName("Should remove item successfully")
        void shouldRemoveItemSuccessfully() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            BillItem item = createTestBillItem(1, 1, "TEST-001", 5, BigDecimal.valueOf(100.00));

            when(billItemRepository.findById(1)).thenReturn(Optional.of(item));
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(billItemRepository.findByBillId(1)).thenReturn(List.of());
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            boolean result = billingService.removeItem(1);

            // Assert
            assertTrue(result);
            verify(billItemRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw exception for non-existent item")
        void shouldThrowForNonExistentItem() {
            // Arrange
            when(billItemRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.removeItem(999));
        }
    }

    @Nested
    @DisplayName("applyDiscount tests")
    class ApplyDiscountTests {

        @Test
        @DisplayName("Should apply discount successfully")
        void shouldApplyDiscountSuccessfully() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            // Add an item to the bill so calculateTotals computes a non-zero subtotal
            BillItem item = createTestBillItem(1, 1, "TEST-001", 2, BigDecimal.valueOf(100.00));
            bill.addItem(item); // This will set subtotal to 200.00
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            Bill result = billingService.applyDiscount(1, BigDecimal.valueOf(50.00));

            // Assert
            assertNotNull(result);
            verify(billRepository).save(any(Bill.class));
        }

        @Test
        @DisplayName("Should throw exception for negative discount")
        void shouldThrowForNegativeDiscount() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.applyDiscount(1, BigDecimal.valueOf(-10.00)));
        }

        @Test
        @DisplayName("Should throw exception for null discount")
        void shouldThrowForNullDiscount() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> billingService.applyDiscount(1, null));
        }
    }

    @Nested
    @DisplayName("processCashPayment tests")
    class ProcessCashPaymentTests {

        @Test
        @DisplayName("Should process cash payment successfully")
        void shouldProcessCashPaymentSuccessfully() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            bill.setTotalAmount(new Money(100.00));
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            Bill result = billingService.processCashPayment(1, BigDecimal.valueOf(150.00));

            // Assert
            assertNotNull(result);
            verify(billRepository).save(any(Bill.class));
        }

        @Test
        @DisplayName("Should throw exception for insufficient payment")
        void shouldThrowForInsufficientPayment() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            bill.setTotalAmount(new Money(100.00));
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                () -> billingService.processCashPayment(1, BigDecimal.valueOf(50.00)));
        }

        @Test
        @DisplayName("Should throw exception for online store cash payment")
        void shouldThrowForOnlineStoreCashPayment() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.ONLINE, TransactionType.ONLINE);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                () -> billingService.processCashPayment(1, BigDecimal.valueOf(100.00)));
        }
    }

    @Nested
    @DisplayName("processOnlinePayment tests")
    class ProcessOnlinePaymentTests {

        @Test
        @DisplayName("Should process online payment successfully")
        void shouldProcessOnlinePaymentSuccessfully() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.ONLINE, TransactionType.ONLINE);
            bill.setTotalAmount(new Money(100.00));
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            Bill result = billingService.processOnlinePayment(1);

            // Assert
            assertNotNull(result);
            assertEquals(bill.getTotalAmount(), result.getTenderedAmount());
        }

        @Test
        @DisplayName("Should throw exception for physical store online payment")
        void shouldThrowForPhysicalStoreOnlinePayment() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                () -> billingService.processOnlinePayment(1));
        }
    }

    @Nested
    @DisplayName("cancelBill tests")
    class CancelBillTests {

        @Test
        @DisplayName("Should cancel bill successfully")
        void shouldCancelBillSuccessfully() {
            // Arrange - First create a bill to put it in progress
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.generateNextSerialNumber(StoreType.PHYSICAL)).thenReturn("PH-00001");
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            billingService.createBill(StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1");

            // Act
            boolean result = billingService.cancelBill(1);

            // Assert
            assertTrue(result);
            verify(billItemRepository).deleteByBillId(1);
            verify(billRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("findBill tests")
    class FindBillTests {

        @Test
        @DisplayName("Should find bill by ID")
        void shouldFindBillById() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act
            Optional<Bill> result = billingService.findBillById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getBillId());
        }

        @Test
        @DisplayName("Should find bill by serial number")
        void shouldFindBillBySerialNumber() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findBySerialNumber("PH-00001")).thenReturn(Optional.of(bill));

            // Act
            Optional<Bill> result = billingService.findBillBySerialNumber("PH-00001");

            // Assert
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should find bills by date")
        void shouldFindBillsByDate() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findByDate(LocalDate.now())).thenReturn(List.of(bill));

            // Act
            List<Bill> result = billingService.findBillsByDate(LocalDate.now());

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find bills by date range")
        void shouldFindBillsByDateRange() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            when(billRepository.findByDateRange(startDate, endDate)).thenReturn(List.of(bill));

            // Act
            List<Bill> result = billingService.findBillsByDateRange(startDate, endDate);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find bills by customer")
        void shouldFindBillsByCustomer() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.ONLINE, TransactionType.ONLINE);
            when(billRepository.findByCustomerId(1)).thenReturn(List.of(bill));

            // Act
            List<Bill> result = billingService.findBillsByCustomer(1);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find recent bills")
        void shouldFindRecentBills() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findRecent(10)).thenReturn(List.of(bill));

            // Act
            List<Bill> result = billingService.findRecentBills(10);

            // Assert
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getBillItems tests")
    class GetBillItemsTests {

        @Test
        @DisplayName("Should get bill items")
        void shouldGetBillItems() {
            // Arrange
            BillItem item = createTestBillItem(1, 1, "TEST-001", 5, BigDecimal.valueOf(100.00));
            when(billItemRepository.findByBillId(1)).thenReturn(List.of(item));

            // Act
            List<BillItem> result = billingService.getBillItems(1);

            // Assert
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("sales summary tests")
    class SalesSummaryTests {

        @Test
        @DisplayName("Should get today's sales")
        void shouldGetTodaysSales() {
            // Arrange
            when(billRepository.getTotalSalesForDate(LocalDate.now())).thenReturn(BigDecimal.valueOf(5000.00));

            // Act
            BigDecimal result = billingService.getTodaysSales();

            // Assert
            assertEquals(BigDecimal.valueOf(5000.00), result);
        }

        @Test
        @DisplayName("Should get today's bill count")
        void shouldGetTodaysBillCount() {
            // Arrange
            when(billRepository.getBillCountForDate(LocalDate.now())).thenReturn(25);

            // Act
            int result = billingService.getTodaysBillCount();

            // Assert
            assertEquals(25, result);
        }
    }

    @Nested
    @DisplayName("generateSerialNumber tests")
    class GenerateSerialNumberTests {

        @Test
        @DisplayName("Should generate physical store serial number")
        void shouldGeneratePhysicalStoreSerialNumber() {
            // Arrange
            when(billRepository.generateNextSerialNumber(StoreType.PHYSICAL)).thenReturn("PH-00001");

            // Act
            String result = billingService.generateSerialNumber(StoreType.PHYSICAL);

            // Assert
            assertEquals("PH-00001", result);
        }

        @Test
        @DisplayName("Should generate online store serial number")
        void shouldGenerateOnlineStoreSerialNumber() {
            // Arrange
            when(billRepository.generateNextSerialNumber(StoreType.ONLINE)).thenReturn("ON-00001");

            // Act
            String result = billingService.generateSerialNumber(StoreType.ONLINE);

            // Assert
            assertEquals("ON-00001", result);
        }
    }
}
