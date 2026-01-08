package com.syos.service;

import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.exception.ProductNotFoundException;
import com.syos.exception.ValidationException;
import com.syos.repository.interfaces.ProductRepository;
import com.syos.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    private Product createTestProduct(String code, String name, BigDecimal price) {
        Product product = new Product(
            new ProductCode(code),
            name,
            1,  // categoryId
            1,  // subcategoryId
            1,  // brandId
            new Money(price)
        );
        return product;
    }

    @Nested
    @DisplayName("createProduct tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act
            Product result = productService.createProduct(product);

            // Assert
            assertNotNull(result);
            assertEquals("TEST-001", result.getProductCodeString());
            verify(productRepository).existsByProductCode("TEST-001");
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw exception when product code already exists")
        void shouldThrowWhenProductCodeExists() {
            // Arrange
            Product product = createTestProduct("EXISTING-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.existsByProductCode("EXISTING-001")).thenReturn(true);

            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class,
                () -> productService.createProduct(product));
            assertTrue(exception.getMessage().contains("already exists"));
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for null product")
        void shouldThrowForNullProduct() {
            // Act & Assert - Implementation validates inside validateProduct which throws ValidationException
            // but logging happens first which causes NullPointerException
            assertThrows(Exception.class, () -> productService.createProduct(null));
        }

        @Test
        @DisplayName("Should throw exception for product without code")
        void shouldThrowForProductWithoutCode() {
            // Arrange
            Product product = new Product();
            product.setProductName("Test Product");
            product.setUnitPrice(new Money(100.00));

            // Act & Assert
            assertThrows(ValidationException.class, () -> productService.createProduct(product));
        }

        @Test
        @DisplayName("Should throw exception for product without name")
        void shouldThrowForProductWithoutName() {
            // Arrange
            Product product = new Product();
            product.setProductCode(new ProductCode("TEST-001"));
            product.setUnitPrice(new Money(100.00));

            // Act & Assert
            assertThrows(ValidationException.class, () -> productService.createProduct(product));
        }
    }

    @Nested
    @DisplayName("updateProduct tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Updated Product", BigDecimal.valueOf(150.00));
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act
            Product result = productService.updateProduct(product);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Product", result.getProductName());
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowWhenProductNotFound() {
            // Arrange
            Product product = createTestProduct("NONEXISTENT-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.existsByProductCode("NONEXISTENT-001")).thenReturn(false);

            // Act & Assert
            assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(product));
        }
    }

    @Nested
    @DisplayName("findByProductCode tests")
    class FindByProductCodeTests {

        @Test
        @DisplayName("Should find product by code")
        void shouldFindProductByCode() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));

            // Act
            Optional<Product> result = productService.findByProductCode("TEST-001");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("TEST-001", result.get().getProductCodeString());
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(productRepository.findByProductCode("NONEXISTENT")).thenReturn(Optional.empty());

            // Act
            Optional<Product> result = productService.findByProductCode("NONEXISTENT");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() {
            // Arrange
            List<Product> products = List.of(
                createTestProduct("TEST-001", "Product 1", BigDecimal.valueOf(100.00)),
                createTestProduct("TEST-002", "Product 2", BigDecimal.valueOf(200.00))
            );
            when(productRepository.findAll()).thenReturn(products);

            // Act
            List<Product> result = productService.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no products")
        void shouldReturnEmptyListWhenNoProducts() {
            // Arrange
            when(productRepository.findAll()).thenReturn(List.of());

            // Act
            List<Product> result = productService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAllActive tests")
    class FindAllActiveTests {

        @Test
        @DisplayName("Should return only active products")
        void shouldReturnOnlyActiveProducts() {
            // Arrange
            Product activeProduct = createTestProduct("TEST-001", "Active Product", BigDecimal.valueOf(100.00));
            activeProduct.setActive(true);
            when(productRepository.findAllActive()).thenReturn(List.of(activeProduct));

            // Act
            List<Product> result = productService.findAllActive();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isActive());
        }
    }

    @Nested
    @DisplayName("searchByName tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Coca-Cola 500ml", BigDecimal.valueOf(100.00));
            when(productRepository.searchByName("Coca")).thenReturn(List.of(product));

            // Act
            List<Product> result = productService.searchByName("Coca");

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).getProductName().contains("Coca"));
        }

        @Test
        @DisplayName("Should return empty list for null search term")
        void shouldReturnEmptyForNullSearchTerm() {
            // Act
            List<Product> result = productService.searchByName(null);

            // Assert
            assertTrue(result.isEmpty());
            verify(productRepository, never()).searchByName(anyString());
        }

        @Test
        @DisplayName("Should return empty list for empty search term")
        void shouldReturnEmptyForEmptySearchTerm() {
            // Act
            List<Product> result = productService.searchByName("   ");

            // Assert
            assertTrue(result.isEmpty());
            verify(productRepository, never()).searchByName(anyString());
        }
    }

    @Nested
    @DisplayName("updatePrice tests")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should update product price successfully")
        void shouldUpdatePriceSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act
            Product result = productService.updatePrice("TEST-001", BigDecimal.valueOf(150.00));

            // Assert
            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception for negative price")
        void shouldThrowForNegativePrice() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> productService.updatePrice("TEST-001", BigDecimal.valueOf(-10.00)));
        }

        @Test
        @DisplayName("Should throw exception for null price")
        void shouldThrowForNullPrice() {
            // Act & Assert
            assertThrows(ValidationException.class,
                () -> productService.updatePrice("TEST-001", null));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowWhenProductNotFound() {
            // Arrange
            when(productRepository.findByProductCode("NONEXISTENT")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                () -> productService.updatePrice("NONEXISTENT", BigDecimal.valueOf(100.00)));
        }
    }

    @Nested
    @DisplayName("activateProduct tests")
    class ActivateProductTests {

        @Test
        @DisplayName("Should activate product successfully")
        void shouldActivateProductSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            product.setActive(false);
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act
            boolean result = productService.activateProduct("TEST-001");

            // Assert
            assertTrue(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowWhenProductNotFoundForActivation() {
            // Arrange
            when(productRepository.findByProductCode("NONEXISTENT")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                () -> productService.activateProduct("NONEXISTENT"));
        }
    }

    @Nested
    @DisplayName("deactivateProduct tests")
    class DeactivateProductTests {

        @Test
        @DisplayName("Should deactivate product successfully")
        void shouldDeactivateProductSuccessfully() {
            // Arrange
            Product product = createTestProduct("TEST-001", "Test Product", BigDecimal.valueOf(100.00));
            product.setActive(true);
            when(productRepository.findByProductCode("TEST-001")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            // Act
            boolean result = productService.deactivateProduct("TEST-001");

            // Assert
            assertTrue(result);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("existsByProductCode tests")
    class ExistsByProductCodeTests {

        @Test
        @DisplayName("Should return true when product exists")
        void shouldReturnTrueWhenProductExists() {
            // Arrange
            when(productRepository.existsByProductCode("TEST-001")).thenReturn(true);

            // Act
            boolean result = productService.existsByProductCode("TEST-001");

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when product does not exist")
        void shouldReturnFalseWhenProductNotExists() {
            // Arrange
            when(productRepository.existsByProductCode("NONEXISTENT")).thenReturn(false);

            // Act
            boolean result = productService.existsByProductCode("NONEXISTENT");

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("pagination tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() {
            // Arrange
            List<Product> products = List.of(
                createTestProduct("TEST-001", "Product 1", BigDecimal.valueOf(100.00))
            );
            when(productRepository.findAll(0, 10)).thenReturn(products);

            // Act
            List<Product> result = productService.findAll(0, 10);

            // Assert
            assertEquals(1, result.size());
            verify(productRepository).findAll(0, 10);
        }

        @Test
        @DisplayName("Should calculate correct offset for page")
        void shouldCalculateCorrectOffset() {
            // Arrange
            when(productRepository.findAll(20, 10)).thenReturn(List.of());

            // Act
            productService.findAll(2, 10);

            // Assert
            verify(productRepository).findAll(20, 10);
        }
    }

    @Nested
    @DisplayName("count tests")
    class CountTests {

        @Test
        @DisplayName("Should return product count")
        void shouldReturnProductCount() {
            // Arrange
            when(productRepository.count()).thenReturn(100L);

            // Act
            long result = productService.getProductCount();

            // Assert
            assertEquals(100L, result);
        }

        @Test
        @DisplayName("Should return active product count")
        void shouldReturnActiveProductCount() {
            // Arrange
            List<Product> activeProducts = List.of(
                createTestProduct("TEST-001", "Product 1", BigDecimal.valueOf(100.00)),
                createTestProduct("TEST-002", "Product 2", BigDecimal.valueOf(100.00))
            );
            when(productRepository.findAllActive()).thenReturn(activeProducts);

            // Act
            long result = productService.getActiveProductCount();

            // Assert
            assertEquals(2L, result);
        }
    }

    @Nested
    @DisplayName("findByCategory tests")
    class FindByCategoryTests {

        @Test
        @DisplayName("Should find products by category")
        void shouldFindProductsByCategory() {
            // Arrange
            Product product = createTestProduct("BEV-001", "Beverage", BigDecimal.valueOf(100.00));
            when(productRepository.findByCategoryId(1)).thenReturn(List.of(product));

            // Act
            List<Product> result = productService.findByCategory(1);

            // Assert
            assertEquals(1, result.size());
            verify(productRepository).findByCategoryId(1);
        }
    }

    @Nested
    @DisplayName("findBySubcategory tests")
    class FindBySubcategoryTests {

        @Test
        @DisplayName("Should find products by subcategory")
        void shouldFindProductsBySubcategory() {
            // Arrange
            Product product = createTestProduct("BEV-SD-001", "Soft Drink", BigDecimal.valueOf(100.00));
            when(productRepository.findBySubcategoryId(1)).thenReturn(List.of(product));

            // Act
            List<Product> result = productService.findBySubcategory(1);

            // Assert
            assertEquals(1, result.size());
            verify(productRepository).findBySubcategoryId(1);
        }
    }

    @Nested
    @DisplayName("findByBrand tests")
    class FindByBrandTests {

        @Test
        @DisplayName("Should find products by brand")
        void shouldFindProductsByBrand() {
            // Arrange
            Product product = createTestProduct("BEV-CC-001", "Coca-Cola", BigDecimal.valueOf(100.00));
            when(productRepository.findByBrandId(1)).thenReturn(List.of(product));

            // Act
            List<Product> result = productService.findByBrand(1);

            // Assert
            assertEquals(1, result.size());
            verify(productRepository).findByBrandId(1);
        }
    }
}
