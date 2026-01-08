package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.service.interfaces.ReportService;
import com.syos.service.interfaces.ReportService.DashboardSummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet for the main dashboard view.
 */
@WebServlet(urlPatterns = {"/dashboard", "/"})
public class DashboardServlet extends BaseViewServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        super.init();
        reportService = ServiceRegistry.get(ReportService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Get dashboard data
            DashboardSummary summary = reportService.getDashboardSummary();

            // Set attributes for the view
            request.setAttribute("todaySales", summary.todaySales());
            request.setAttribute("todayBillCount", summary.todayBillCount());
            request.setAttribute("lowStockCount", summary.lowStockProductCount());
            request.setAttribute("expiringCount", summary.expiringProductCount());
            request.setAttribute("weekSales", summary.weekSales());
            request.setAttribute("monthSales", summary.monthSales());
            request.setAttribute("topProducts", summary.topProducts());

            setActiveNav(request, "dashboard");
            render(request, response, "dashboard/index.jsp");

        } catch (Exception e) {
            setErrorMessage(request, "Error loading dashboard: " + e.getMessage());
            setActiveNav(request, "dashboard");
            render(request, response, "dashboard/index.jsp");
        }
    }
}
