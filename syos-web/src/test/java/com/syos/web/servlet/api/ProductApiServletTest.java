package com.syos.web.servlet.api;

import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductApiServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductApiServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ProductService productService;

    private ProductApiServlet servlet;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProductApiServlet();
        // Inject mock via reflection
        java.lang.reflect.Field field = ProductApiServlet.class.getDeclaredField("productService");
        field.setAccessible(true);
        field.set(servlet, productService);

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("doGet tests")
    class DoGetTests {

        @Test
        @DisplayName("Should list all products")
        void shouldListAllProducts() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("categoryId")).thenReturn(null);
            when(request.getParameter("subcategoryId")).thenReturn(null);
            when(request.getParameter("brandId")).thenReturn(null);
            when(request.getParameter("active")).thenReturn(null);
            when(request.getParameter("page")).thenReturn(null);
            when(request.getParameter("size")).thenReturn(null);
            when(productService.findAll(0, 50)).thenReturn(List.of(createTestProduct("P001")));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findAll(0, 50);
            printWriter.flush();
            String output = responseWriter.toString();
            assert output.contains("products");
        }

        @Test
        @DisplayName("Should filter by category")
        void shouldFilterByCategory() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("categoryId")).thenReturn("1");
            when(productService.findByCategory(1)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findByCategory(1);
        }

        @Test
        @DisplayName("Should filter by subcategory")
        void shouldFilterBySubcategory() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("categoryId")).thenReturn(null);
            when(request.getParameter("subcategoryId")).thenReturn("2");
            when(productService.findBySubcategory(2)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findBySubcategory(2);
        }

        @Test
        @DisplayName("Should filter by brand")
        void shouldFilterByBrand() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("categoryId")).thenReturn(null);
            when(request.getParameter("subcategoryId")).thenReturn(null);
            when(request.getParameter("brandId")).thenReturn("3");
            when(productService.findByBrand(3)).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findByBrand(3);
        }

        @Test
        @DisplayName("Should filter active only")
        void shouldFilterActiveOnly() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("categoryId")).thenReturn(null);
            when(request.getParameter("subcategoryId")).thenReturn(null);
            when(request.getParameter("brandId")).thenReturn(null);
            when(request.getParameter("active")).thenReturn("true");
            when(productService.findAllActive()).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findAllActive();
        }

        @Test
        @DisplayName("Should search products")
        void shouldSearchProducts() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/search");
            when(request.getParameter("q")).thenReturn("test");
            when(productService.searchByName("test")).thenReturn(List.of());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).searchByName("test");
        }

        @Test
        @DisplayName("Should return empty for empty search query")
        void shouldReturnEmptyForEmptySearchQuery() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/search");
            when(request.getParameter("q")).thenReturn("");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService, never()).searchByName(anyString());
        }

        @Test
        @DisplayName("Should get product by code")
        void shouldGetProductByCode() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001");
            Product product = createTestProduct("P001");
            when(productService.findByProductCode("P001")).thenReturn(Optional.of(product));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findByProductCode("P001");
        }

        @Test
        @DisplayName("Should return 404 for non-existent product")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P999");
            when(productService.findByProductCode("P999")).thenReturn(Optional.empty());

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("doPost tests")
    class DoPostTests {

        @Test
        @DisplayName("Should create product")
        void shouldCreateProduct() throws Exception {
            // Arrange
            String jsonBody = "{\"productCode\":\"P001\",\"productName\":\"Test Product\",\"unitPrice\":99.99}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(productService.createProduct(any())).thenReturn(createTestProduct("P001"));

            // Act
            servlet.doPost(request, response);

            // Assert
            verify(productService).createProduct(any());
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
        }

        @Test
        @DisplayName("Should return 400 for empty body")
        void shouldReturn400ForEmptyBody() throws Exception {
            // Arrange
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

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
        @DisplayName("Should return 400 when no product code")
        void shouldReturn400WhenNoProductCode() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("Should update product")
        void shouldUpdateProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001");
            String jsonBody = "{\"productName\":\"Updated Name\"}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            Product existing = createTestProduct("P001");
            when(productService.findByProductCode("P001")).thenReturn(Optional.of(existing));
            when(productService.updateProduct(any())).thenReturn(existing);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(productService).updateProduct(any());
        }

        @Test
        @DisplayName("Should update price")
        void shouldUpdatePrice() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001/price");
            String jsonBody = "{\"price\":199.99}";
            when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(productService.updatePrice("P001", new BigDecimal("199.99")))
                    .thenReturn(createTestProduct("P001"));

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(productService).updatePrice(eq("P001"), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Should activate product")
        void shouldActivateProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001/activate");
            when(productService.activateProduct("P001")).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(productService).activateProduct("P001");
        }

        @Test
        @DisplayName("Should deactivate product")
        void shouldDeactivateProduct() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001/deactivate");
            when(productService.deactivateProduct("P001")).thenReturn(true);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(productService).deactivateProduct("P001");
        }

        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001/unknown");

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 404 when activate fails")
        void shouldReturn404WhenActivateFails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/P001/activate");
            when(productService.activateProduct("P001")).thenReturn(false);

            // Act
            servlet.doPut(request, response);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private Product createTestProduct(String code) {
        Product product = new Product();
        product.setProductCode(new ProductCode(code));
        product.setProductName("Test Product " + code);
        product.setUnitPrice(new Money(BigDecimal.valueOf(99.99)));
        product.setActive(true);
        return product;
    }
}
