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
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.BillingService.CheckoutRequest;
import com.syos.service.interfaces.BillingService.CheckoutResult;
import com.syos.service.interfaces.BillingService.ItemDetail;
import com.syos.service.interfaces.BillingService.ItemRequest;
import com.syos.service.interfaces.BillingService.StockCheckResult;
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
                transactionRepository);
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
                new Money(price));
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

    @Nested
    @DisplayName("updateItemQuantity tests")
    class UpdateItemQuantityTests {

        @Test
        @DisplayName("Should throw for non-positive quantity")
        void shouldThrowForNonPositiveQuantity() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> billingService.updateItemQuantity(1, 0));
        }

        @Test
        @DisplayName("Should throw for negative quantity")
        void shouldThrowForNegativeQuantity() {
            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> billingService.updateItemQuantity(1, -5));
        }

        @Test
        @DisplayName("Should throw for non-existent item")
        void shouldThrowForNonExistentItem() {
            // Arrange
            when(billItemRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> billingService.updateItemQuantity(999, 5));
        }
    }

    @Nested
    @DisplayName("clearItems tests")
    class ClearItemsTests {

        @Test
        @DisplayName("Should clear all items from bill")
        void shouldClearAllItemsFromBill() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            // Act
            billingService.clearItems(1);

            // Assert
            verify(billItemRepository).deleteByBillId(1);
            verify(billRepository).save(any(Bill.class));
        }
    }

    @Nested
    @DisplayName("checkStock tests")
    class CheckStockTests {

        @Test
        @DisplayName("Should return not found for non-existent product")
        void shouldReturnNotFoundForNonExistentProduct() {
            // Arrange
            when(productRepository.findByProductCode("UNKNOWN")).thenReturn(Optional.empty());

            // Act
            StockCheckResult result = billingService.checkStock("UNKNOWN", 5, StoreType.PHYSICAL);

            // Assert
            assertFalse(result.available());
        }

        @Test
        @DisplayName("Should return not found for inactive product")
        void shouldReturnNotFoundForInactiveProduct() {
            // Arrange
            Product product = createTestProduct("INACTIVE", "Inactive Product", BigDecimal.valueOf(50));
            product.setActive(false);
            when(productRepository.findByProductCode("INACTIVE")).thenReturn(Optional.of(product));

            // Act
            StockCheckResult result = billingService.checkStock("INACTIVE", 5, StoreType.PHYSICAL);

            // Assert
            assertFalse(result.available());
        }

        @Test
        @DisplayName("Should return unavailable when insufficient stock")
        void shouldReturnUnavailableWhenInsufficientStock() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity("TEST-001", StoreType.PHYSICAL)).thenReturn(3);

            // Act
            StockCheckResult result = billingService.checkStock("TEST-001", 10, StoreType.PHYSICAL);

            // Assert
            assertFalse(result.available());
        }

        @Test
        @DisplayName("Should return available when sufficient stock")
        void shouldReturnAvailableWhenSufficientStock() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity("TEST-001", StoreType.PHYSICAL)).thenReturn(50);

            // Act
            StockCheckResult result = billingService.checkStock("TEST-001", 5, StoreType.PHYSICAL);

            // Assert
            assertTrue(result.available());
            assertEquals("TEST-001", result.productCode());
            assertEquals("Test Product", result.productName());
        }
    }

    @Nested
    @DisplayName("checkout tests")
    class CheckoutTests {

        @Test
        @DisplayName("Should fail checkout with null store type")
        void shouldFailCheckoutWithNullStoreType() {
            // Arrange
            CheckoutRequest request = new CheckoutRequest(
                    null, TransactionType.CASH, null, "CASHIER-1",
                    List.of(new ItemRequest("TEST-001", 5)),
                    BigDecimal.ZERO, BigDecimal.valueOf(500));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Store type")));
        }

        @Test
        @DisplayName("Should fail checkout with null transaction type")
        void shouldFailCheckoutWithNullTransactionType() {
            // Arrange
            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, null, null, "CASHIER-1",
                    List.of(new ItemRequest("TEST-001", 5)),
                    BigDecimal.ZERO, BigDecimal.valueOf(500));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Transaction type")));
        }

        @Test
        @DisplayName("Should fail checkout with empty cart")
        void shouldFailCheckoutWithEmptyCart() {
            // Arrange
            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1",
                    List.of(),
                    BigDecimal.ZERO, BigDecimal.valueOf(500));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Cart is empty")));
        }

        @Test
        @DisplayName("Should fail checkout for online order without customer")
        void shouldFailCheckoutForOnlineOrderWithoutCustomer() {
            // Arrange
            CheckoutRequest request = new CheckoutRequest(
                    StoreType.ONLINE, TransactionType.ONLINE, null, null,
                    List.of(new ItemRequest("TEST-001", 5)),
                    BigDecimal.ZERO, null);

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Customer ID")));
        }

        @Test
        @DisplayName("Should checkout successfully for physical store")
        void shouldCheckoutSuccessfullyForPhysicalStore() {
            // Arrange
            String productCode = "TEST-001";
            Product product = createTestProduct(productCode, "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity(productCode, StoreType.PHYSICAL)).thenReturn(50);

            BatchAllocation allocation = new BatchAllocation(1, productCode, 5, LocalDate.now().plusDays(10));
            when(storeInventoryService.allocateStockForSale(productCode, StoreType.PHYSICAL, 5))
                    .thenReturn(List.of(allocation));

            when(billRepository.generateNextSerialNumber(StoreType.PHYSICAL)).thenReturn("PH-001");
            when(billRepository.save(any(Bill.class))).thenAnswer(i -> {
                Bill b = i.getArgument(0);
                if (b.getBillId() == null)
                    b.setBillId(1);
                return b;
            });
            when(billItemRepository.save(any(BillItem.class))).thenAnswer(i -> i.getArgument(0));
            when(storeInventoryService.reducePhysicalStoreStock(productCode, 1, 5)).thenReturn(true);

            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1",
                    List.of(new ItemRequest(productCode, 5)),
                    BigDecimal.ZERO, BigDecimal.valueOf(1000.00));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertTrue(result.success());
            assertEquals(new BigDecimal("500.00"), result.total());
            assertEquals(new BigDecimal("500.00"), result.change());
            assertEquals(1, result.items().size());
            assertEquals("Test Product", result.items().get(0).productName());

            verify(storeInventoryService).reducePhysicalStoreStock(productCode, 1, 5);
        }

        @Test
        @DisplayName("Should checkout successfully for online store")
        void shouldCheckoutSuccessfullyForOnlineStore() {
            // Arrange
            String productCode = "TEST-002";
            Product product = createTestProduct(productCode, "Online Product", BigDecimal.valueOf(200.00));
            when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity(productCode, StoreType.ONLINE)).thenReturn(20);

            BatchAllocation allocation = new BatchAllocation(2, productCode, 2, LocalDate.now().plusDays(10));
            when(storeInventoryService.allocateStockForSale(productCode, StoreType.ONLINE, 2))
                    .thenReturn(List.of(allocation));

            when(billRepository.generateNextSerialNumber(StoreType.ONLINE)).thenReturn("ON-001");
            when(billRepository.save(any(Bill.class))).thenAnswer(i -> {
                Bill b = i.getArgument(0);
                if (b.getBillId() == null)
                    b.setBillId(2);
                return b;
            });
            when(billItemRepository.save(any(BillItem.class))).thenAnswer(i -> i.getArgument(0));

            CheckoutRequest request = new CheckoutRequest(
                    StoreType.ONLINE, TransactionType.ONLINE, 1001, null,
                    List.of(new ItemRequest(productCode, 2)),
                    BigDecimal.valueOf(50.00), null);

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertTrue(result.success());
            assertEquals(new BigDecimal("350.00"), result.total());
            assertEquals(new BigDecimal("350.00"), result.cashTendered());
            assertEquals(new BigDecimal("0.00"), result.change());

            verify(storeInventoryService).reduceOnlineStoreStock(productCode, 2, 2);
        }

        @Test
        @DisplayName("Should fail checkout when stock unavailable")
        void shouldFailCheckoutWhenStockUnavailable() {
            // Arrange
            String productCode = "TEST-001";
            Product product = createTestProduct(productCode, "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));

            // Available 2, requested 5
            when(storeInventoryService.getAvailableQuantity(productCode, StoreType.PHYSICAL)).thenReturn(2);

            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1",
                    List.of(new ItemRequest(productCode, 5)),
                    BigDecimal.ZERO, BigDecimal.valueOf(1000.00));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Insufficient stock")));
        }

        @Test
        @DisplayName("Should fail checkout when discount exceeds subtotal")
        void shouldFailCheckoutWhenDiscountExceedsSubtotal() {
            // Arrange
            String productCode = "TEST-001";
            Product product = createTestProduct(productCode, "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity(productCode, StoreType.PHYSICAL)).thenReturn(50);

            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1",
                    List.of(new ItemRequest(productCode, 5)),
                    BigDecimal.valueOf(600.00),
                    BigDecimal.valueOf(1000.00));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Discount cannot exceed")));
        }

        @Test
        @DisplayName("Should fail checkout when insufficient cash tendered")
        void shouldFailCheckoutWhenInsufficientCashTendered() {
            // Arrange
            String productCode = "TEST-001";
            Product product = createTestProduct(productCode, "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(product));
            when(storeInventoryService.getAvailableQuantity(productCode, StoreType.PHYSICAL)).thenReturn(50);

            CheckoutRequest request = new CheckoutRequest(
                    StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1",
                    List.of(new ItemRequest(productCode, 5)),
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(400.00));

            // Act
            CheckoutResult result = billingService.checkout(request);

            // Assert
            assertFalse(result.success());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Insufficient cash")));
        }
    }

    @Nested
    @DisplayName("validateBillForFinalization tests")
    class ValidateBillForFinalizationTests {

        @Test
        @DisplayName("Should return invalid for bill not in progress")
        void shouldReturnInvalidForBillNotInProgress() {
            // Act
            ValidationResult result = billingService.validateBillForFinalization(999);

            // Assert
            assertFalse(result.isValid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("not found")));
        }
    }

    @Nested
    @DisplayName("processCashPayment edge cases")
    class ProcessCashPaymentEdgeCases {

        @Test
        @DisplayName("Should throw for null tendered amount")
        void shouldThrowForNullTenderedAmount() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                    () -> billingService.processCashPayment(1, null));
        }

        @Test
        @DisplayName("Should throw for zero tendered amount")
        void shouldThrowForZeroTenderedAmount() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                    () -> billingService.processCashPayment(1, BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should throw for negative tendered amount")
        void shouldThrowForNegativeTenderedAmount() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));

            // Act & Assert
            assertThrows(InvalidPaymentException.class,
                    () -> billingService.processCashPayment(1, BigDecimal.valueOf(-100)));
        }
    }

    @Nested
    @DisplayName("cancelBill edge cases")
    class CancelBillEdgeCases {

        @Test
        @DisplayName("Should throw for non-existent bill")
        void shouldThrowForNonExistentBill() {
            // Arrange
            when(billRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(BillNotFoundException.class,
                    () -> billingService.cancelBill(999));
        }

        @Test
        @DisplayName("Should throw for already finalized bill")
        void shouldThrowForAlreadyFinalizedBill() {
            // Arrange
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.findById(1)).thenReturn(Optional.of(bill));
            // Bill is not in billsInProgress map, so cancelling will try to check
            // repository

            // Act & Assert
            assertThrows(ValidationException.class,
                    () -> billingService.cancelBill(1));
        }
    }

    @Nested
    @DisplayName("findBillById edge cases")
    class FindBillByIdEdgeCases {

        @Test
        @DisplayName("Should find in-progress bill first")
        void shouldFindInProgressBillFirst() {
            // Arrange - First create a bill to put it in progress
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billRepository.generateNextSerialNumber(StoreType.PHYSICAL)).thenReturn("PH-00001");
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            billingService.createBill(StoreType.PHYSICAL, TransactionType.CASH, null, "CASHIER-1");

            // Act
            Optional<Bill> result = billingService.findBillById(1);

            // Assert
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty for non-existent bill")
        void shouldReturnEmptyForNonExistentBill() {
            // Arrange
            when(billRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<Bill> result = billingService.findBillById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}
