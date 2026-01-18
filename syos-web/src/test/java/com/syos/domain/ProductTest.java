package com.syos.domain;

import com.syos.domain.enums.UnitOfMeasure;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product model.
 */
class ProductTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create product with default constructor")
        void shouldCreateProductWithDefaultConstructor() {
            Product product = new Product();

            assertTrue(product.isActive());
            assertEquals(10, product.getMinPhysicalStock());
            assertEquals(10, product.getMinOnlineStock());
        }

        @Test
        @DisplayName("Should create product with all parameters")
        void shouldCreateProductWithAllParameters() {
            ProductCode code = new ProductCode("P001");
            Money price = new Money(BigDecimal.valueOf(99.99));

            Product product = new Product(code, "Test Product", 1, 2, 3, price);

            assertEquals(code, product.getProductCode());
            assertEquals("P001", product.getProductCodeString());
            assertEquals("Test Product", product.getProductName());
            assertEquals(1, product.getCategoryId());
            assertEquals(2, product.getSubcategoryId());
            assertEquals(3, product.getBrandId());
            assertEquals(price, product.getUnitPrice());
            assertTrue(product.isActive());
            assertEquals(UnitOfMeasure.PCS, product.getUnitOfMeasure());
        }
    }

    @Nested
    @DisplayName("updatePrice tests")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should update price with valid value")
        void shouldUpdatePriceWithValidValue() {
            Product product = new Product();
            product.setUnitPrice(new Money(BigDecimal.valueOf(50.00)));

            Money newPrice = new Money(BigDecimal.valueOf(75.00));
            product.updatePrice(newPrice);

            assertEquals(75, product.getUnitPrice().getAmount().intValue());
        }

        @Test
        @DisplayName("Should throw when price is null")
        void shouldThrowWhenPriceIsNull() {
            Product product = new Product();
            assertThrows(IllegalArgumentException.class, () -> product.updatePrice(null));
        }

        @Test
        @DisplayName("Should throw when price is zero")
        void shouldThrowWhenPriceIsZero() {
            Product product = new Product();
            assertThrows(IllegalArgumentException.class,
                    () -> product.updatePrice(new Money(BigDecimal.ZERO)));
        }
    }

    @Nested
    @DisplayName("activate/deactivate tests")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("Should activate product")
        void shouldActivateProduct() {
            Product product = new Product();
            product.setActive(false);

            product.activate();

            assertTrue(product.isActive());
        }

        @Test
        @DisplayName("Should deactivate product")
        void shouldDeactivateProduct() {
            Product product = new Product();

            product.deactivate();

            assertFalse(product.isActive());
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get description")
        void shouldSetAndGetDescription() {
            Product product = new Product();
            product.setDescription("A great product");
            assertEquals("A great product", product.getDescription());
        }

        @Test
        @DisplayName("Should set and get unit of measure")
        void shouldSetAndGetUnitOfMeasure() {
            Product product = new Product();
            product.setUnitOfMeasure(UnitOfMeasure.KILOGRAM);
            assertEquals(UnitOfMeasure.KILOGRAM, product.getUnitOfMeasure());
        }

        @Test
        @DisplayName("Should set and get category/subcategory/brand names")
        void shouldSetAndGetDisplayNames() {
            Product product = new Product();
            product.setCategoryName("Electronics");
            product.setSubcategoryName("Phones");
            product.setBrandName("Apple");

            assertEquals("Electronics", product.getCategoryName());
            assertEquals("Phones", product.getSubcategoryName());
            assertEquals("Apple", product.getBrandName());
        }

        @Test
        @DisplayName("Should set and get timestamps")
        void shouldSetAndGetTimestamps() {
            Product product = new Product();
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);

            assertEquals(now, product.getCreatedAt());
            assertEquals(now, product.getUpdatedAt());
        }

        @Test
        @DisplayName("getProductCodeString should return null when productCode is null")
        void getProductCodeStringShouldReturnNullWhenProductCodeIsNull() {
            Product product = new Product();
            assertNull(product.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            Product product = new Product(new ProductCode("P001"), "Test", 1, 2, 3,
                    new Money(BigDecimal.valueOf(99.99)));

            String result = product.toString();
            assertTrue(result.contains("Product{"));
            assertTrue(result.contains("productName='Test'"));
            assertTrue(result.contains("active=true"));
        }
    }
}
