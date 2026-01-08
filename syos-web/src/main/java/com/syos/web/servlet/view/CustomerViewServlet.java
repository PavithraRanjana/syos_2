package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Customer;
import com.syos.service.interfaces.CustomerService;
import com.syos.service.interfaces.CustomerService.CustomerStatistics;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for customer management views.
 */
@WebServlet(urlPatterns = {"/customers", "/customers/*"})
public class CustomerViewServlet extends BaseViewServlet {

    private CustomerService customerService;

    @Override
    public void init() throws ServletException {
        super.init();
        customerService = ServiceRegistry.get(CustomerService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                listCustomers(request, response);
            } else if (pathInfo.equals("/add")) {
                showAddForm(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                String customerId = pathInfo.substring("/view/".length());
                viewCustomer(customerId, request, response);
            } else if (pathInfo.startsWith("/edit/")) {
                String customerId = pathInfo.substring("/edit/".length());
                showEditForm(customerId, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            listCustomers(request, response);
        }
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 20);
        String search = getStringParameter(request, "search", "");

        List<Customer> customers;
        if (!search.isEmpty()) {
            customers = customerService.searchByName(search);
            request.setAttribute("search", search);
        } else {
            customers = customerService.findAll(page, size);
        }

        // Statistics
        CustomerStatistics stats = customerService.getStatistics();
        request.setAttribute("stats", stats);

        long totalCustomers = customerService.getCustomerCount();
        int totalPages = (int) Math.ceil((double) totalCustomers / size);

        request.setAttribute("customers", customers);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalCustomers", totalCustomers);

        setActiveNav(request, "customers");
        render(request, response, "customers/list.jsp");
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("isEdit", false);
        setActiveNav(request, "customers");
        render(request, response, "customers/form.jsp");
    }

    private void showEditForm(String customerIdStr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = Integer.parseInt(customerIdStr);
            Optional<Customer> customer = customerService.findById(customerId);

            if (customer.isEmpty()) {
                setErrorMessage(request, "Customer not found: " + customerId);
                listCustomers(request, response);
                return;
            }

            request.setAttribute("customer", customer.get());
            request.setAttribute("isEdit", true);
            setActiveNav(request, "customers");
            render(request, response, "customers/form.jsp");
        } catch (NumberFormatException e) {
            setErrorMessage(request, "Invalid customer ID");
            listCustomers(request, response);
        }
    }

    private void viewCustomer(String customerIdStr, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer customerId = Integer.parseInt(customerIdStr);
            Optional<Customer> customer = customerService.findById(customerId);

            if (customer.isEmpty()) {
                setErrorMessage(request, "Customer not found: " + customerId);
                listCustomers(request, response);
                return;
            }

            request.setAttribute("customer", customer.get());
            setActiveNav(request, "customers");
            render(request, response, "customers/view.jsp");
        } catch (NumberFormatException e) {
            setErrorMessage(request, "Invalid customer ID");
            listCustomers(request, response);
        }
    }
}
