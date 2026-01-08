package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository.ProductStockSummary;
import com.syos.service.interfaces.StoreInventoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for store stock (physical and online) management views.
 */
@WebServlet(urlPatterns = {"/store-stock", "/store-stock/*"})
public class StoreStockViewServlet extends BaseViewServlet {

    private StoreInventoryService storeInventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        storeInventoryService = ServiceRegistry.get(StoreInventoryService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showStockOverview(request, response);
            } else if (pathInfo.equals("/physical")) {
                showPhysicalStock(request, response);
            } else if (pathInfo.equals("/online")) {
                showOnlineStock(request, response);
            } else if (pathInfo.equals("/restock")) {
                showRestockForm(request, response);
            } else if (pathInfo.equals("/low-stock")) {
                showLowStock(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            showStockOverview(request, response);
        }
    }

    private void showStockOverview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = getStringParameter(request, "filter", "");

        // Physical store summary
        List<ProductStockSummary> physicalSummary = storeInventoryService.getPhysicalStoreStockSummary();
        request.setAttribute("physicalSummary", physicalSummary);

        // Online store summary
        List<ProductStockSummary> onlineSummary = storeInventoryService.getOnlineStoreStockSummary();
        request.setAttribute("onlineSummary", onlineSummary);

        // Low stock counts
        int physicalLowStock = storeInventoryService.getPhysicalStoreLowStock(10).size();
        int onlineLowStock = storeInventoryService.getOnlineStoreLowStock(10).size();
        request.setAttribute("physicalLowStockCount", physicalLowStock);
        request.setAttribute("onlineLowStockCount", onlineLowStock);

        request.setAttribute("filter", filter);
        setActiveNav(request, "store-stock");
        render(request, response, "store-stock/index.jsp");
    }

    private void showPhysicalStock(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productCode = getStringParameter(request, "product", "");

        if (!productCode.isEmpty()) {
            List<PhysicalStoreInventory> stock = storeInventoryService.getPhysicalStoreStock(productCode);
            int totalQty = storeInventoryService.getAvailableQuantity(productCode, StoreType.PHYSICAL);
            request.setAttribute("stock", stock);
            request.setAttribute("productCode", productCode);
            request.setAttribute("totalQuantity", totalQty);
        }

        List<ProductStockSummary> summary = storeInventoryService.getPhysicalStoreStockSummary();
        request.setAttribute("summary", summary);
        request.setAttribute("storeType", "PHYSICAL");

        setActiveNav(request, "store-stock");
        render(request, response, "store-stock/physical.jsp");
    }

    private void showOnlineStock(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productCode = getStringParameter(request, "product", "");

        if (!productCode.isEmpty()) {
            List<OnlineStoreInventory> stock = storeInventoryService.getOnlineStoreStock(productCode);
            int totalQty = storeInventoryService.getAvailableQuantity(productCode, StoreType.ONLINE);
            request.setAttribute("stock", stock);
            request.setAttribute("productCode", productCode);
            request.setAttribute("totalQuantity", totalQty);
        }

        List<ProductStockSummary> summary = storeInventoryService.getOnlineStoreStockSummary();
        request.setAttribute("summary", summary);
        request.setAttribute("storeType", "ONLINE");

        setActiveNav(request, "store-stock");
        render(request, response, "store-stock/online.jsp");
    }

    private void showRestockForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setActiveNav(request, "store-stock");
        render(request, response, "store-stock/restock.jsp");
    }

    private void showLowStock(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int threshold = getIntParameter(request, "threshold", 10);

        List<PhysicalStoreInventory> physicalLowStock = storeInventoryService.getPhysicalStoreLowStock(threshold);
        List<OnlineStoreInventory> onlineLowStock = storeInventoryService.getOnlineStoreLowStock(threshold);

        request.setAttribute("physicalLowStock", physicalLowStock);
        request.setAttribute("onlineLowStock", onlineLowStock);
        request.setAttribute("threshold", threshold);

        setActiveNav(request, "store-stock");
        render(request, response, "store-stock/low-stock.jsp");
    }
}
