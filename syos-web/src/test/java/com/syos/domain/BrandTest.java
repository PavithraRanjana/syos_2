package com.syos.domain;

import com.syos.domain.models.Brand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Brand model.
 */
class BrandTest {

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string with all fields")
        void shouldReturnFormattedStringWithAllFields() {
            // Arrange
            Brand brand = new Brand(1, "Nike", "NK");

            // Act
            String result = brand.toString();

            // Assert
            assertEquals("Brand{brandId=1, brandName='Nike', brandCode='NK'}", result);
        }

        @Test
        @DisplayName("Should handle null brandId")
        void shouldHandleNullBrandId() {
            // Arrange
            Brand brand = new Brand(null, "Adidas", "AD");

            // Act
            String result = brand.toString();

            // Assert
            assertTrue(result.contains("brandId=null"));
            assertTrue(result.contains("brandName='Adidas'"));
            assertTrue(result.contains("brandCode='AD'"));
        }

        @Test
        @DisplayName("Should handle null brandName")
        void shouldHandleNullBrandName() {
            // Arrange
            Brand brand = new Brand(2, null, "PU");

            // Act
            String result = brand.toString();

            // Assert
            assertTrue(result.contains("brandId=2"));
            assertTrue(result.contains("brandName='null'"));
            assertTrue(result.contains("brandCode='PU'"));
        }

        @Test
        @DisplayName("Should handle null brandCode")
        void shouldHandleNullBrandCode() {
            // Arrange
            Brand brand = new Brand(3, "Puma", null);

            // Act
            String result = brand.toString();

            // Assert
            assertTrue(result.contains("brandId=3"));
            assertTrue(result.contains("brandName='Puma'"));
            assertTrue(result.contains("brandCode='null'"));
        }

        @Test
        @DisplayName("Should return formatted string using default constructor")
        void shouldReturnFormattedStringUsingDefaultConstructor() {
            // Arrange
            Brand brand = new Brand();
            brand.setBrandId(10);
            brand.setBrandName("Reebok");
            brand.setBrandCode("RB");

            // Act
            String result = brand.toString();

            // Assert
            assertEquals("Brand{brandId=10, brandName='Reebok', brandCode='RB'}", result);
        }
    }
}
