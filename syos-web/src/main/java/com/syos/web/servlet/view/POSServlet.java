package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Bill;
import com.syos.domain.models.BillItem;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.BillingService;
import com.syos.service.interfaces.ProductService;
import com.syos.service.interfaces.StoreInventoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for Point of Sale (POS) / Billing views.
 */
@WebServlet(urlPatterns = { "/pos", "/pos/*" })
public class POSServlet extends BaseViewServlet {

    private BillingService billingService;
    private ProductService productService;
    private StoreInventoryService storeInventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        billingService = ServiceRegistry.get(BillingService.class);
        productService = ServiceRegistry.get(ProductService.class);
        storeInventoryService = ServiceRegistry.get(StoreInventoryService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showPOS(request, response);
            } else if (pathInfo.equals("/new")) {
                showNewBillForm(request, response);
            } else if (pathInfo.equals("/stock")) {
                showStock(request, response);
            } else if (pathInfo.equals("/history")) {
                showBillHistory(request, response);
            } else if (pathInfo.startsWith("/bill/")) {
                String billId = pathInfo.substring("/bill/".length());
                viewBill(billId, request, response);
            } else if (pathInfo.startsWith("/receipt/")) {
                String billId = pathInfo.substring("/receipt/".length());
                showReceipt(billId, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            showPOS(request, response);
        }
    }

    private void showStock(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Physical store stock summary
        List<ProductStockSummary> stockSummary = storeInventoryService.getPhysicalStoreStockSummary();
        request.setAttribute("stockSummary", stockSummary);

        // Low stock count
        int lowStockCount = storeInventoryService.getPhysicalStoreLowStock(10).size();
        request.setAttribute("lowStockCount", lowStockCount);

        setActiveNav(request, "pos-stock");
        render(request, response, "pos/stock.jsp");
    }

    private void showPOS(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get today's summary
        request.setAttribute("todaySales", billingService.getTodaysSales());
        request.setAttribute("todayBillCount", billingService.getTodaysBillCount());

        // Get recent bills
        List<Bill> recentBills = billingService.findRecentBills(5);
        request.setAttribute("recentBills", recentBills);

        setActiveNav(request, "pos");
        render(request, response, "pos/index.jsp");
    }

    private void showNewBillForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Products for quick add
        request.setAttribute("products", productService.findAllActive());
        setActiveNav(request, "pos");
        render(request, response, "pos/new-bill.jsp");
    }

    private void showBillHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateStr = getStringParameter(request, "date", "");
        LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

        List<Bill> bills = billingService.findBillsByDate(date);
        request.setAttribute("bills", bills);
        request.setAttribute("selectedDate", date);

        setActiveNav(request, "pos");
        render(request, response, "pos/history.jsp");
    }

    private void viewBill(String billIdStr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer billId = Integer.parseInt(billIdStr);
            Optional<Bill> bill = billingService.findBillById(billId);

            if (bill.isEmpty()) {
                setErrorMessage(request, "Bill not found: " + billId);
                showPOS(request, response);
                return;
            }

            List<BillItem> items = billingService.getBillItems(billId);
            request.setAttribute("bill", bill.get());
            request.setAttribute("items", items);

            setActiveNav(request, "pos");
            render(request, response, "pos/view-bill.jsp");
        } catch (NumberFormatException e) {
            setErrorMessage(request, "Invalid bill ID");
            showPOS(request, response);
        }
    }

    private void showReceipt(String billIdStr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer billId = Integer.parseInt(billIdStr);
            Optional<Bill> bill = billingService.findBillById(billId);

            if (bill.isEmpty()) {
                setErrorMessage(request, "Bill not found: " + billId);
                showPOS(request, response);
                return;
            }

            List<BillItem> items = billingService.getBillItems(billId);
            request.setAttribute("bill", bill.get());
            request.setAttribute("items", items);

            // Receipt uses a minimal layout
            render(request, response, "pos/receipt.jsp");
        } catch (NumberFormatException e) {
            setErrorMessage(request, "Invalid bill ID");
            showPOS(request, response);
        }
    }
}
