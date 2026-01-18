package com.syos.web.servlet.view;

import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.service.interfaces.ProductService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductViewServlet using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductViewServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ProductService productService;

    private ProductViewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProductViewServlet();

        // Inject mock service via reflection
        java.lang.reflect.Field field = ProductViewServlet.class.getDeclaredField("productService");
        field.setAccessible(true);
        field.set(servlet, productService);

        // Setup request dispatcher
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private Product createTestProduct(String code, String name, BigDecimal price) {
        Product product = new Product();
        product.setProductCode(new ProductCode(code));
        product.setProductName(name);
        product.setUnitPrice(new Money(price));
        product.setActive(true);
        return product;
    }

    @Nested
    @DisplayName("doGet tests - List Products")
    class DoGetListProductsTests {

        @Test
        @DisplayName("Should list products with null path")
        void shouldListProductsWithNullPath() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn(null);
            when(request.getParameter("page")).thenReturn(null);
            when(request.getParameter("size")).thenReturn(null);
            when(request.getParameter("search")).thenReturn(null);

            Product product = createTestProduct("P001", "Product 1", BigDecimal.valueOf(10.00));
            when(productService.findAll(0, 20)).thenReturn(List.of(product));
            when(productService.getProductCount()).thenReturn(1L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findAll(0, 20);
            verify(request).setAttribute(eq("products"), any());
            verify(request).getRequestDispatcher("/WEB-INF/views/products/list.jsp");
            verify(requestDispatcher).forward(request, response);
        }

        @Test
        @DisplayName("Should list products with pagination")
        void shouldListProductsWithPagination() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("page")).thenReturn("2");
            when(request.getParameter("size")).thenReturn("10");
            when(request.getParameter("search")).thenReturn(null);

            when(productService.findAll(2, 10)).thenReturn(List.of());
            when(productService.getProductCount()).thenReturn(50L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findAll(2, 10);
            verify(request).setAttribute("currentPage", 2);
            verify(request).setAttribute("pageSize", 10);
        }

        @Test
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/");
            when(request.getParameter("search")).thenReturn("Coffee");

            Product product = createTestProduct("P001", "Coffee Beans", BigDecimal.valueOf(15.00));
            when(productService.searchByName("Coffee")).thenReturn(List.of(product));
            when(productService.getProductCount()).thenReturn(10L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).searchByName("Coffee");
            verify(request).setAttribute("search", "Coffee");
        }
    }

    @Nested
    @DisplayName("doGet tests - Add Form")
    class DoGetAddFormTests {

        @Test
        @DisplayName("Should show add form")
        void shouldShowAddForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/add");

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute("isEdit", false);
            verify(request).getRequestDispatcher("/WEB-INF/views/products/form.jsp");
        }
    }

    @Nested
    @DisplayName("doGet tests - Edit Form")
    class DoGetEditFormTests {

        @Test
        @DisplayName("Should show edit form")
        void shouldShowEditForm() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/edit/P001");

            Product product = createTestProduct("P001", "Product 1", BigDecimal.valueOf(10.00));
            when(productService.findByProductCode("P001")).thenReturn(Optional.of(product));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findByProductCode("P001");
            verify(request).setAttribute("product", product);
            verify(request).setAttribute("isEdit", true);
            verify(request).getRequestDispatcher("/WEB-INF/views/products/form.jsp");
        }

        @Test
        @DisplayName("Should handle product not found for edit")
        void shouldHandleProductNotFoundForEdit() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/edit/NOTFOUND");
            when(productService.findByProductCode("NOTFOUND")).thenReturn(Optional.empty());
            when(productService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(productService.getProductCount()).thenReturn(0L);

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(request).setAttribute(eq("errorMessage"), anyString());
        }
    }

    @Nested
    @DisplayName("doGet tests - View Product")
    class DoGetViewProductTests {

        @Test
        @DisplayName("Should view product details")
        void shouldViewProductDetails() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/P001");

            Product product = createTestProduct("P001", "Product 1", BigDecimal.valueOf(10.00));
            when(productService.findByProductCode("P001")).thenReturn(Optional.of(product));

            // Act
            servlet.doGet(request, response);

            // Assert
            verify(productService).findByProductCode("P001");
            verify(request).setAttribute("product", product);
            verify(request).getRequestDispatcher("/WEB-INF/views/products/view.jsp");
        }

        @Test
        @DisplayName("Should handle product not found for view")
        void shouldHandleProductNotFoundForView() throws Exception {
            // Arrange
            when(request.getPathInfo()).thenReturn("/view/NOTFOUND");
            when(productService.findByProductCode("NOTFOUND")).thenReturn(Optional.empty());
            when(productService.findAll(anyInt(), anyInt())).thenReturn(List.of());
            when(productService.getProductCount()).thenReturn(0L);

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
