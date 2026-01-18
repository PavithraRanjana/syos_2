package com.syos.web.servlet.view;

import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.ProductService;
import com.syos.service.interfaces.StoreInventoryService;
import jakarta.servlet.RequestDispatcher;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for POSServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class POSServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private BillingService billingService;

    @Mock
    private ProductService productService;

    @Mock
    private StoreInventoryService storeInventoryService;

    private POSServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new POSServlet();

        // Inject mock services via reflection
        java.lang.reflect.Field billingField = POSServlet.class.getDeclaredField("billingService");
        billingField.setAccessible(true);
        billingField.set(servlet, billingService);

        java.lang.reflect.Field productField = POSServlet.class.getDeclaredField("productService");
        productField.setAccessible(true);
        productField.set(servlet, productService);

        java.lang.reflect.Field storeField = POSServlet.class.getDeclaredField("storeInventoryService");
        storeField.setAccessible(true);
        storeField.set(servlet, storeInventoryService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Nested
    @DisplayName("doGet tests - POS Home")
    class DoGetPOSHomeTests {

        @Test
        @DisplayName("Should show POS home with null path")
        void shouldShowPOSHomeWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.valueOf(1000));
            when(billingService.getTodaysBillCount()).thenReturn(10);
            when(billingService.findRecentBills(5)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).getTodaysSales();
            verify(billingService).getTodaysBillCount();
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/index.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should show POS home with root path")
        void shouldShowPOSHomeWithRootPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.valueOf(500));
            when(billingService.getTodaysBillCount()).thenReturn(5);
            when(billingService.findRecentBills(5)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/index.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - New Bill")
    class DoGetNewBillTests {

        @Test
        @DisplayName("Should show new bill form")
        void shouldShowNewBillForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/new");
            when(productService.findAllActive()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findAllActive();
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/new-bill.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Stock")
    class DoGetStockTests {

        @Test
        @DisplayName("Should show stock page")
        void shouldShowStockPage() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/stock");

            ProductStockSummary summary = new ProductStockSummary("P001", "Product 1", 100, 2);
            when(storeInventoryService.getPhysicalStoreStockSummary()).thenReturn(List.of(summary));
            when(storeInventoryService.getPhysicalStoreLowStock(10)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(storeInventoryService).getPhysicalStoreStockSummary();
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/stock.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Bill History")
    class DoGetBillHistoryTests {

        @Test
        @DisplayName("Should show bill history for today")
        void shouldShowBillHistoryForToday() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/history");
            when(request.getParameter("date")).thenReturn(null);
            when(billingService.findBillsByDate(any())).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillsByDate(any());
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/history.jsp");
        }

        @Test
        @DisplayName("Should show bill history for specific date")
        void shouldShowBillHistoryForSpecificDate() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/history");
            when(request.getParameter("date")).thenReturn("2026-01-15");

            LocalDate date = LocalDate.parse("2026-01-15");
            when(billingService.findBillsByDate(date)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillsByDate(date);
            verify(request).setAttribute("selectedDate", date);
        }
    }

    @Nested
    @DisplayName("doGet tests - View Bill")
    class DoGetViewBillTests {

        @Test
        @DisplayName("Should view bill details")
        void shouldViewBillDetails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/bill/123");

            Bill bill = new Bill();
            bill.setBillId(123);
            when(billingService.findBillById(123)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(123)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(billingService).findBillById(123);
            verify(billingService).getBillItems(123);
            verify(request).setAttribute("bill", bill);
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/view-bill.jsp");
        }

        @Test
        @DisplayName("Should handle bill not found")
        void shouldHandleBillNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/bill/999");
            when(billingService.findBillById(999)).thenReturn(Optional.empty());
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.ZERO);
            when(billingService.getTodaysBillCount()).thenReturn(0);
            when(billingService.findRecentBills(5)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
        }

        @Test
        @DisplayName("Should handle invalid bill ID")
        void shouldHandleInvalidBillId() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/bill/abc");
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.ZERO);
            when(billingService.getTodaysBillCount()).thenReturn(0);
            when(billingService.findRecentBills(5)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), eq("Invalid bill ID"));
        }
    }

    @Nested
    @DisplayName("doGet tests - Receipt")
    class DoGetReceiptTests {

        @Test
        @DisplayName("Should show receipt")
        void shouldShowReceipt() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/receipt/123");

            Bill bill = new Bill();
            bill.setBillId(123);
            when(billingService.findBillById(123)).thenReturn(Optional.of(bill));
            when(billingService.getBillItems(123)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).getRequestDispatcher("/WEB-INF/views/pos/receipt.jsp");
        }

        @Test
        @DisplayName("Should handle receipt bill not found")
        void shouldHandleReceiptBillNotFound() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/receipt/999");
            when(billingService.findBillById(999)).thenReturn(Optional.empty());
            when(billingService.getTodaysSales()).thenReturn(BigDecimal.ZERO);
            when(billingService.getTodaysBillCount()).thenReturn(0);
            when(billingService.findRecentBills(5)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
        }
    }

    @Nested
    @DisplayName("doGet tests - Error Handling")
    class DoGetErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for unknown path")
        void shouldReturn404ForUnknownPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/unknown");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
