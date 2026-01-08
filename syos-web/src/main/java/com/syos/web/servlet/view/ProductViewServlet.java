package com.syos.web.servlet.view;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Product;
import com.syos.service.interfaces.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for product management views.
 */
@WebServlet(urlPatterns = {"/products", "/products/*"})
public class ProductViewServlet extends BaseViewServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        productService = ServiceRegistry.get(ProductService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                listProducts(request, response);
            } else if (pathInfo.equals("/add")) {
                showAddForm(request, response);
            } else if (pathInfo.startsWith("/edit/")) {
                String code = pathInfo.substring("/edit/".length());
                showEditForm(code, request, response);
            } else if (pathInfo.startsWith("/view/")) {
                String code = pathInfo.substring("/view/".length());
                viewProduct(code, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorMessage(request, "Error: " + e.getMessage());
            listProducts(request, response);
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 20);
        String search = getStringParameter(request, "search", "");

        List<Product> products;
        if (!search.isEmpty()) {
            products = productService.searchByName(search);
            request.setAttribute("search", search);
        } else {
            products = productService.findAll(page, size);
        }

        long totalProducts = productService.getProductCount();
        int totalPages = (int) Math.ceil((double) totalProducts / size);

        request.setAttribute("products", products);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalProducts", totalProducts);

        setActiveNav(request, "products");
        render(request, response, "products/list.jsp");
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("isEdit", false);
        setActiveNav(request, "products");
        render(request, response, "products/form.jsp");
    }

    private void showEditForm(String productCode, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Optional<Product> product = productService.findByProductCode(productCode);
        if (product.isEmpty()) {
            setErrorMessage(request, "Product not found: " + productCode);
            listProducts(request, response);
            return;
        }

        request.setAttribute("product", product.get());
        request.setAttribute("isEdit", true);
        setActiveNav(request, "products");
        render(request, response, "products/form.jsp");
    }

    private void viewProduct(String productCode, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Optional<Product> product = productService.findByProductCode(productCode);
        if (product.isEmpty()) {
            setErrorMessage(request, "Product not found: " + productCode);
            listProducts(request, response);
            return;
        }

        request.setAttribute("product", product.get());
        setActiveNav(request, "products");
        render(request, response, "products/view.jsp");
    }
}
