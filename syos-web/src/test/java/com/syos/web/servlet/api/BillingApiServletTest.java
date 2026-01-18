package com.syos.web.servlet.api;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.BillingService.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private BillingService billingService;

    private BillingApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BillingApiServlet();
        // Inject mock BillingService via reflection
        java.lang.reflect.Field field = BillingApiServlet.class.getDeclaredField("billingService");
        field.setAccessible(true);
        field.set(servlet, billingService);

        // Setup response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    private Bill createTestBill(Integer billId, StoreType storeType, TransactionType transactionType) {
        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setStoreType(storeType);
        bill.setTransactionType(transactionType);
        bill.setBillDate(LocalDateTime.now());
        bill.setSerialNumber(new BillSerialNumber(billId));
        bill.setSubtotal(new Money(BigDecimal.valueOf(100.00)));
        bill.setDiscountAmount(new Money(BigDecimal.ZERO));
        bill.setTaxAmount(new Money(BigDecimal.ZERO));
        bill.setTotalAmount(new Money(BigDecimal.valueOf(100.00)));
        return bill;
    }

    private BillItem createTestBillItem(Integer itemId, String productCode, int quantity) {
        BillItem item = new BillItem();
        item.setBillItemId(itemId);
        item.setProductCode(new ProductCode(productCode));
        item.setProductName("Test Product " + productCode);
        item.setQuantity(quantity);
        item.setUnitPrice(new Money(BigDecimal.valueOf(25.00)));
        item.setLineTotal(new Money(BigDecimal.valueOf(25.00 * quantity)));
        return item;
    }

    @Nested
    @DisplayName("doGet tests")
    class DoGetTests {

        @Test
        @DisplayName("Should return 400 when path is null")
        void shouldReturn400WhenPathIsNull() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when path is root")
        void shouldReturn400WhenPathIsRoot() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should get bill by ID")
        void shouldGetBillById() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/123");

            Bill bill = createTestBill(123, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.findBillById(123)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(123)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillById(123);
            verify(billingService).getBillItems(123);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("billId");
        }

        @Test
        @DisplayName("Should return 404 when bill not found")
        void shouldReturn404WhenBillNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/999");
            when(billingService.findBillById(999)).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 for invalid bill ID")
        void shouldReturn400ForInvalidBillId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should get today's summary")
        void shouldGetTodaySummary() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/today");
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.valueOf(1000.00));
            when(billingService.getTodaysBillCount()).thenReturn(5);
            when(billingService.findBillsByDate(LocalDate.now())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).getTodaysSales();
            verify(billingService).getTodaysBillCount();
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("totalSales");
        }

        @Test
        @DisplayName("Should get recent bills")
        void shouldGetRecentBills() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/recent");
            when(request.getParameter("limit")).thenReturn("10");

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.findRecentBills(10)).thenReturn(List.of(bill));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findRecentBills(10);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("bills");
        }

        @Test
        @DisplayName("Should get recent bills with default limit")
        void shouldGetRecentBillsWithDefaultLimit() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/recent");
            when(request.getParameter("limit")).thenReturn(null);

            when(billingService.findRecentBills(20)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findRecentBills(20);
        }

        @Test
        @DisplayName("Should get bills by date")
        void shouldGetBillsByDate() throws Exception {
            // Arrange
            String dateStr = "2026-01-19";
            when(request.getPathInfo()).thenReturn("/date/" + dateStr);

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.findBillsByDate(LocalDate.parse(dateStr))).thenReturn(List.of(bill));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillsByDate(LocalDate.parse(dateStr));
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("date");
        }

        @Test
        @DisplayName("Should return 400 for invalid date format")
        void shouldReturn400ForInvalidDateFormat() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/date/invalid-date");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should get bill by serial number")
        void shouldGetBillBySerialNumber() throws Exception {
            // Arrange
            String serialNumber = "PH-20260119-001";
            when(request.getPathInfo()).thenReturn("/serial/" + serialNumber);

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.findBillBySerialNumber(serialNumber)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillBySerialNumber(serialNumber);
        }

        @Test
        @DisplayName("Should return 404 when serial number not found")
        void shouldReturn404WhenSerialNumberNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/serial/nonexistent");
            when(billingService.findBillBySerialNumber("nonexistent")).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should get bills by customer")
        void shouldGetBillsByCustomer() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/customer/5");

            Bill bill = createTestBill(1, StoreType.ONLINE, TransactionType.ONLINE);
            bill.setCustomerId(5);
            when(billingService.findBillsByCustomer(5)).thenReturn(List.of(bill));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillsByCustomer(5);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("customerId");
        }

        @Test
        @DisplayName("Should return 400 for invalid customer ID")
        void shouldReturn400ForInvalidCustomerId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/customer/abc");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should check stock availability")
        void shouldCheckStockAvailability() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/stock/P001");
            when(request.getParameter("quantity")).thenReturn("5");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            StockCheckResult result = StockCheckResult.available("P001", "Test Product",
                    BigDecimal.valueOf(25.00), 5, 10);
            when(billingService.checkStock("P001", 5, StoreType.PHYSICAL)).thenReturn(result);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).checkStock("P001", 5, StoreType.PHYSICAL);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("available");
        }

        @Test
        @DisplayName("Should return stock unavailable message")
        void shouldReturnStockUnavailableMessage() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/stock/P001");
            when(request.getParameter("quantity")).thenReturn("20");
            when(request.getParameter("storeType")).thenReturn("PHYSICAL");

            StockCheckResult result = StockCheckResult.unavailable("P001", 20, 5);
            when(billingService.checkStock("P001", 20, StoreType.PHYSICAL)).thenReturn(result);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).checkStock("P001", 20, StoreType.PHYSICAL);
        }

        @Test
        @DisplayName("Should return 400 for invalid store type in stock check")
        void shouldReturn400ForInvalidStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/stock/P001");
            when(request.getParameter("quantity")).thenReturn("5");
            when(request.getParameter("storeType")).thenReturn("INVALID");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPost tests - Create Bill")
    class DoPostCreateBillTests {

        @Test
        @DisplayName("Should create bill successfully")
        void shouldCreateBillSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            String jsonBody = "{\"storeType\":\"PHYSICAL\",\"transactionType\":\"CASH\",\"customerId\":null,\"cashierId\":\"C001\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.createBill(StoreType.PHYSICAL, TransactionType.CASH, null, "C001"))
                    .thenReturn(bill);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).createBill(StoreType.PHYSICAL, TransactionType.CASH, null, "C001");
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 500 for invalid request body")
        void shouldReturn500ForInvalidRequestBody() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("invalid json")));

            // Act
            servlet.doPost(request, response);

            // Assert - Invalid JSON causes exception which returns 500
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should return 400 for invalid store type")
        void shouldReturn400ForInvalidStoreType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            String jsonBody = "{\"storeType\":\"INVALID\",\"transactionType\":\"CASH\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid transaction type")
        void shouldReturn400ForInvalidTransactionType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            String jsonBody = "{\"storeType\":\"PHYSICAL\",\"transactionType\":\"INVALID\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPost tests - Checkout")
    class DoPostCheckoutTests {

        @Test
        @DisplayName("Should checkout successfully")
        void shouldCheckoutSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/checkout");
            String jsonBody = """
                    {
                        "storeType": "PHYSICAL",
                        "transactionType": "CASH",
                        "customerId": null,
                        "cashierId": "C001",
                        "items": [{"productCode": "P001", "quantity": 2}],
                        "discount": 0,
                        "cashTendered": 100.00
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            CheckoutResult result = CheckoutResult.success(
                    1, "PH-001", BigDecimal.valueOf(50.00), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.valueOf(50.00), BigDecimal.valueOf(100.00), BigDecimal.valueOf(50.00),
                    LocalDateTime.now(), List.of(new ItemDetail("Test Product", 2,
                            BigDecimal.valueOf(25.00), BigDecimal.valueOf(50.00))));
            when(billingService.checkout(any(CheckoutRequest.class))).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).checkout(any(CheckoutRequest.class));
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 when checkout fails")
        void shouldReturn400WhenCheckoutFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/checkout");
            String jsonBody = """
                    {
                        "storeType": "PHYSICAL",
                        "transactionType": "CASH",
                        "items": [{"productCode": "P001", "quantity": 100}],
                        "cashTendered": 100.00
                    }
                    """;
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            CheckoutResult result = CheckoutResult.failure("Insufficient stock for P001");
            when(billingService.checkout(any(CheckoutRequest.class))).thenReturn(result);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 500 for invalid checkout request body")
        void shouldReturn500ForInvalidCheckoutBody() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/checkout");
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("not json")));

            // Act
            servlet.doPost(request, response);

            // Assert - Invalid JSON causes exception which returns 500
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("doPost tests - Bill Operations")
    class DoPostBillOperationsTests {

        @Test
        @DisplayName("Should add item to bill")
        void shouldAddItemToBill() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items");
            String jsonBody = "{\"productCode\":\"P001\",\"quantity\":2}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            BillItem item = createTestBillItem(1, "P001", 2);
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.addItem(1, "P001", 2)).thenReturn(item);
            when(billingService.findBillById(1)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(1)).thenReturn(List.of(item));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).addItem(1, "P001", 2);
        }

        @Test
        @DisplayName("Should return 400 when product code missing")
        void shouldReturn400WhenProductCodeMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items");
            String jsonBody = "{\"quantity\":2}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should apply discount")
        void shouldApplyDiscount() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/discount");
            String jsonBody = "{\"amount\":10.00}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            bill.setDiscountAmount(new Money(BigDecimal.valueOf(10.00)));
            when(billingService.applyDiscount(eq(1), any(BigDecimal.class))).thenReturn(bill);
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).applyDiscount(eq(1), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Should return 400 when discount amount missing")
        void shouldReturn400WhenDiscountAmountMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/discount");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should process cash payment")
        void shouldProcessCashPayment() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/payment/cash");
            String jsonBody = "{\"tenderedAmount\":200.00}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            bill.setTenderedAmount(new Money(BigDecimal.valueOf(200.00)));
            bill.setChangeAmount(new Money(BigDecimal.valueOf(100.00)));
            when(billingService.processCashPayment(eq(1), any(BigDecimal.class))).thenReturn(bill);
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).processCashPayment(eq(1), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Should return 400 when tendered amount missing")
        void shouldReturn400WhenTenderedAmountMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/payment/cash");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should process online payment")
        void shouldProcessOnlinePayment() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/payment/online");

            Bill bill = createTestBill(1, StoreType.ONLINE, TransactionType.ONLINE);
            when(billingService.processOnlinePayment(1)).thenReturn(bill);
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).processOnlinePayment(1);
        }

        @Test
        @DisplayName("Should return 400 for unknown payment type")
        void shouldReturn400ForUnknownPaymentType() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/payment/bitcoin");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when payment type not specified")
        void shouldReturn400WhenPaymentTypeNotSpecified() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/payment");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should finalize bill successfully")
        void shouldFinalizeBillSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/finalize");

            ValidationResult validResult = ValidationResult.valid();
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.validateBillForFinalization(1)).thenReturn(validResult);
            when(billingService.finalizeBill(1)).thenReturn(bill);
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).validateBillForFinalization(1);
            verify(billingService).finalizeBill(1);
        }

        @Test
        @DisplayName("Should return 400 when validation fails for finalize")
        void shouldReturn400WhenValidationFailsForFinalize() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/finalize");

            ValidationResult invalidResult = ValidationResult.invalid("No items in bill");
            when(billingService.validateBillForFinalization(1)).thenReturn(invalidResult);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            verify(billingService, never()).finalizeBill(anyInt());
        }

        @Test
        @DisplayName("Should cancel bill successfully")
        void shouldCancelBillSuccessfully() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/cancel");
            when(billingService.cancelBill(1)).thenReturn(true);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(billingService).cancelBill(1);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("cancelled");
        }

        @Test
        @DisplayName("Should return 400 when cancel fails")
        void shouldReturn400WhenCancelFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/cancel");
            when(billingService.cancelBill(1)).thenReturn(false);

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid bill ID in post")
        void shouldReturn400ForInvalidBillIdInPost() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc/items");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/unknown");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 when only bill ID in path")
        void shouldReturn400WhenOnlyBillIdInPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1");

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doPut tests")
    class DoPutTests {

        @Test
        @DisplayName("Should update item quantity")
        void shouldUpdateItemQuantity() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/10");
            String jsonBody = "{\"quantity\":5}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            BillItem item = createTestBillItem(10, "P001", 5);
            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.updateItemQuantity(10, 5)).thenReturn(item);
            when(billingService.findBillById(1)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(1)).thenReturn(List.of(item));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(billingService).updateItemQuantity(10, 5);
        }

        @Test
        @DisplayName("Should return 400 when path is null")
        void shouldReturn400WhenPathIsNull() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid path format")
        void shouldReturn400ForInvalidPathFormat() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/invalid");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid bill ID")
        void shouldReturn400ForInvalidBillId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc/items/10");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid item ID")
        void shouldReturn400ForInvalidItemId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/xyz");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when quantity missing")
        void shouldReturn400WhenQuantityMissing() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/10");
            String jsonBody = "{}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("doDelete tests")
    class DoDeleteTests {

        @Test
        @DisplayName("Should return 400 when path is null")
        void shouldReturn400WhenPathIsNull() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid path format")
        void shouldReturn400ForInvalidPathFormat() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/invalid");

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid bill ID")
        void shouldReturn400ForInvalidBillId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/abc/items");

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should clear all items")
        void shouldClearAllItems() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items");

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.findBillById(1)).thenReturn(Optional.of(bill));

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(billingService).clearItems(1);
        }

        @Test
        @DisplayName("Should remove single item")
        void shouldRemoveSingleItem() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/10");

            Bill bill = createTestBill(1, StoreType.PHYSICAL, TransactionType.CASH);
            when(billingService.removeItem(10)).thenReturn(true);
            when(billingService.findBillById(1)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(1)).thenReturn(List.of());

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(billingService).removeItem(10);
        }

        @Test
        @DisplayName("Should return 400 when remove item fails")
        void shouldReturn400WhenRemoveItemFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/10");
            when(billingService.removeItem(10)).thenReturn(false);

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for invalid item ID")
        void shouldReturn400ForInvalidItemId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/1/items/xyz");

            // Act
            servlet.doDelete(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
