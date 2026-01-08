package com.syos.web.servlet.api;

import com.syos.config.ServiceRegistry;
import com.syos.domain.models.Product;
import com.syos.service.interfaces.ProductService;
import com.syos.web.dto.response.ProductResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API servlet for Product operations.
 *
 * Endpoints:
 * GET  /api/products              - List all products (with optional filters)
 * GET  /api/products/{code}       - Get product by code
 * GET  /api/products/search       - Search products
 * POST /api/products              - Create new product
 * PUT  /api/products/{code}       - Update product
 * PUT  /api/products/{code}/price - Update product price
 * PUT  /api/products/{code}/activate   - Activate product
 * PUT  /api/products/{code}/deactivate - Deactivate product
 */
@WebServlet(urlPatterns = {"/api/products", "/api/products/*"})
public class ProductApiServlet extends BaseApiServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        productService = ServiceRegistry.get(ProductService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/products - List all or filter
                handleListProducts(request, response);
            } else if (pathInfo.equals("/search")) {
                // GET /api/products/search?q=...
                handleSearchProducts(request, response);
            } else {
                // GET /api/products/{code}
                String productCode = getPathPart(request, 0);
                handleGetProduct(productCode, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // POST /api/products - Create product
            handleCreateProduct(request, response);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Product code required");
                return;
            }

            String[] parts = pathInfo.substring(1).split("/");
            String productCode = parts[0];

            if (parts.length == 1) {
                // PUT /api/products/{code} - Update product
                handleUpdateProduct(productCode, request, response);
            } else if (parts.length == 2) {
                switch (parts[1]) {
                    case "price" -> handleUpdatePrice(productCode, request, response);
                    case "activate" -> handleActivate(productCode, response);
                    case "deactivate" -> handleDeactivate(productCode, response);
                    default -> sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
                }
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleListProducts(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String categoryId = request.getParameter("categoryId");
        String subcategoryId = request.getParameter("subcategoryId");
        String brandId = request.getParameter("brandId");
        String activeOnly = request.getParameter("active");
        int page = getIntParameter(request, "page", 0);
        int size = getIntParameter(request, "size", 50);

        List<Product> products;

        if (categoryId != null) {
            products = productService.findByCategory(Integer.parseInt(categoryId));
        } else if (subcategoryId != null) {
            products = productService.findBySubcategory(Integer.parseInt(subcategoryId));
        } else if (brandId != null) {
            products = productService.findByBrand(Integer.parseInt(brandId));
        } else if ("true".equalsIgnoreCase(activeOnly)) {
            products = productService.findAllActive();
        } else {
            products = productService.findAll(page, size);
        }

        List<ProductResponse> responses = products.stream()
            .map(ProductResponse::fromProduct)
            .toList();

        sendSuccess(response, Map.of(
            "products", responses,
            "count", responses.size(),
            "page", page,
            "size", size
        ));
    }

    private void handleSearchProducts(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String query = getOptionalParameter(request, "q", "");
        if (query.isEmpty()) {
            sendSuccess(response, Map.of("products", List.of(), "count", 0));
            return;
        }

        List<Product> products = productService.searchByName(query);
        List<ProductResponse> responses = products.stream()
            .map(ProductResponse::fromProduct)
            .toList();

        sendSuccess(response, Map.of(
            "products", responses,
            "count", responses.size(),
            "query", query
        ));
    }

    private void handleGetProduct(String productCode, HttpServletResponse response)
            throws IOException {
        Optional<Product> product = productService.findByProductCode(productCode);
        if (product.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "Product not found: " + productCode);
            return;
        }
        sendSuccess(response, ProductResponse.fromProduct(product.get()));
    }

    private void handleCreateProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ProductRequest productRequest = parseRequestBody(request, ProductRequest.class);
        if (productRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        Product product = productRequest.toProduct();
        Product created = productService.createProduct(product);

        response.setStatus(HttpServletResponse.SC_CREATED);
        sendSuccess(response, ProductResponse.fromProduct(created), "Product created successfully");
    }

    private void handleUpdateProduct(String productCode, HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        ProductRequest productRequest = parseRequestBody(request, ProductRequest.class);
        if (productRequest == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        Optional<Product> existing = productService.findByProductCode(productCode);
        if (existing.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "Product not found: " + productCode);
            return;
        }

        Product product = existing.get();
        productRequest.updateProduct(product);
        Product updated = productService.updateProduct(product);

        sendSuccess(response, ProductResponse.fromProduct(updated), "Product updated successfully");
    }

    private void handleUpdatePrice(String productCode, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        PriceUpdateRequest priceRequest = parseRequestBody(request, PriceUpdateRequest.class);
        if (priceRequest == null || priceRequest.price == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Price is required");
            return;
        }

        Product updated = productService.updatePrice(productCode, priceRequest.price);
        sendSuccess(response, ProductResponse.fromProduct(updated), "Price updated successfully");
    }

    private void handleActivate(String productCode, HttpServletResponse response)
            throws IOException {
        boolean activated = productService.activateProduct(productCode);
        if (activated) {
            sendSuccess(response, Map.of("productCode", productCode, "active", true),
                "Product activated successfully");
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
    }

    private void handleDeactivate(String productCode, HttpServletResponse response)
            throws IOException {
        boolean deactivated = productService.deactivateProduct(productCode);
        if (deactivated) {
            sendSuccess(response, Map.of("productCode", productCode, "active", false),
                "Product deactivated successfully");
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
    }

    // ==================== Request DTOs ====================

    public static class ProductRequest {
        public String productCode;
        public String productName;
        public Integer categoryId;
        public Integer subcategoryId;
        public Integer brandId;
        public BigDecimal unitPrice;
        public String description;
        public String unitOfMeasure;

        public Product toProduct() {
            Product product = new Product();
            if (productCode != null) {
                product.setProductCode(new com.syos.domain.valueobjects.ProductCode(productCode));
            }
            product.setProductName(productName);
            product.setCategoryId(categoryId);
            product.setSubcategoryId(subcategoryId);
            product.setBrandId(brandId);
            if (unitPrice != null) {
                product.setUnitPrice(new com.syos.domain.valueobjects.Money(unitPrice));
            }
            product.setDescription(description);
            if (unitOfMeasure != null) {
                product.setUnitOfMeasure(
                    com.syos.domain.enums.UnitOfMeasure.fromString(unitOfMeasure));
            }
            return product;
        }

        public void updateProduct(Product product) {
            if (productName != null) product.setProductName(productName);
            if (categoryId != null) product.setCategoryId(categoryId);
            if (subcategoryId != null) product.setSubcategoryId(subcategoryId);
            if (brandId != null) product.setBrandId(brandId);
            if (unitPrice != null) {
                product.setUnitPrice(new com.syos.domain.valueobjects.Money(unitPrice));
            }
            if (description != null) product.setDescription(description);
            if (unitOfMeasure != null) {
                product.setUnitOfMeasure(
                    com.syos.domain.enums.UnitOfMeasure.fromString(unitOfMeasure));
            }
        }
    }

    public static class PriceUpdateRequest {
        public BigDecimal price;
    }
}
