package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.MainInventory;
import com.syos.service.interfaces.InventoryService;
import com.syos.service.interfaces.InventoryService.ProductInventorySummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for main inventory (batch) management views.
 */
@WebServlet(urlPatterns = {"/inventory", "/inventory/*"})
public class InventoryViewServlet extends BaseViewServlet {

    private InventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        inventoryService = ServiceRegistry.get(InventoryService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                listBatches(request, response);
            } else if (pathInfo.equals("/add")) {
                showAddForm(request, response);
            } else if (pathInfo.equals("/summary")) {
                showSummary(request, response);
            } else if (pathInfo.equals("/expiring")) {
                showExpiring(request, response);
            } else if (pathInfo.equals("/expired")) {
                showExpired(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                String batchId = pathInfo.substring("/view/".length());
                viewBatch(batchId, request, response);
            } else if (pathInfo.startsWith("/product/")) {
                String productCode = pathInfo.substring("/product/".length());
                showProductBatches(productCode, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            listBatches(request, response);
        }
    }

    private void listBatches(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 20);
        String filter = getStringParameter(request, "filter", "");

        List<MainInventory> batches;
        if ("expiring".equals(filter)) {
            int days = getIntParameter(request, "days", 7);
            batches = inventoryService.findExpiringWithinDays(days);
            request.setAttribute("filterLabel", "Expiring within " + days + " days");
        } else if ("expired".equals(filter)) {
            batches = inventoryService.findExpiredBatches();
            request.setAttribute("filterLabel", "Expired Batches");
        } else {
            batches = inventoryService.findAll(page, size);
        }

        long totalBatches = inventoryService.getBatchCount();
        int totalPages = (int) Math.ceil((double) totalBatches / size);

        request.setAttribute("batches", batches);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalBatches", totalBatches);
        request.setAttribute("filter", filter);

        setActiveNav(request, "inventory");
        render(request, response, "inventory/list.jsp");
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setActiveNav(request, "inventory");
        render(request, response, "inventory/add.jsp");
    }

    private void showSummary(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProductInventorySummary> summaries = inventoryService.getInventorySummary();
        request.setAttribute("summaries", summaries);
        setActiveNav(request, "inventory");
        render(request, response, "inventory/summary.jsp");
    }

    private void showExpiring(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int days = getIntParameter(request, "days", 7);
        List<MainInventory> batches = inventoryService.findExpiringWithinDays(days);
        request.setAttribute("batches", batches);
        request.setAttribute("days", days);
        setActiveNav(request, "inventory");
        render(request, response, "inventory/expiring.jsp");
    }

    private void showExpired(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<MainInventory> batches = inventoryService.findExpiredBatches();
        request.setAttribute("batches", batches);
        setActiveNav(request, "inventory");
        render(request, response, "inventory/expired.jsp");
    }

    private void viewBatch(String batchIdStr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer batchId = Integer.parseInt(batchIdStr);
            Optional<MainInventory> batch = inventoryService.findBatchById(batchId);

            if (batch.isEmpty()) {
                setErrorMessage(request, "Batch not found: " + batchId);
                listBatches(request, response);
                return;
            }

            request.setAttribute("batch", batch.get());
            setActiveNav(request, "inventory");
            render(request, response, "inventory/view.jsp");
        } catch (NumberFormatException e) {
            setErrorMessage(request, "Invalid batch ID");
            listBatches(request, response);
        }
    }

    private void showProductBatches(String productCode, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<MainInventory> batches = inventoryService.findBatchesByProductCode(productCode);
        int totalQuantity = inventoryService.getTotalRemainingQuantity(productCode);

        request.setAttribute("batches", batches);
        request.setAttribute("productCode", productCode);
        request.setAttribute("totalQuantity", totalQuantity);
        setActiveNav(request, "inventory");
        render(request, response, "inventory/product-batches.jsp");
    }
}
