package com.syos.domain;

import com.syos.domain.models.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Category model.
 */
class CategoryTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create category with all parameters")
        void shouldCreateCategoryWithAllParameters() {
            // Arrange & Act
            Category category = new Category(1, "Electronics", "ELEC");

            // Assert
            assertEquals(1, category.getCategoryId());
            assertEquals("Electronics", category.getCategoryName());
            assertEquals("ELEC", category.getCategoryCode());
        }

        @Test
        @DisplayName("Should create category with default constructor")
        void shouldCreateCategoryWithDefaultConstructor() {
            // Arrange & Act
            Category category = new Category();

            // Assert
            assertNull(category.getCategoryId());
            assertNull(category.getCategoryName());
            assertNull(category.getCategoryCode());
        }
    }

    @Nested
    @DisplayName("Getter and Setter tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get categoryId")
        void shouldSetAndGetCategoryId() {
            // Arrange
            Category category = new Category();

            // Act
            category.setCategoryId(5);

            // Assert
            assertEquals(5, category.getCategoryId());
        }

        @Test
        @DisplayName("Should set and get categoryName")
        void shouldSetAndGetCategoryName() {
            // Arrange
            Category category = new Category();

            // Act
            category.setCategoryName("Clothing");

            // Assert
            assertEquals("Clothing", category.getCategoryName());
        }

        @Test
        @DisplayName("Should set and get categoryCode")
        void shouldSetAndGetCategoryCode() {
            // Arrange
            Category category = new Category();

            // Act
            category.setCategoryCode("CLO");

            // Assert
            assertEquals("CLO", category.getCategoryCode());
        }

        @Test
        @DisplayName("Should set and get createdAt")
        void shouldSetAndGetCreatedAt() {
            // Arrange
            Category category = new Category();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 18, 10, 30);

            // Act
            category.setCreatedAt(createdAt);

            // Assert
            assertEquals(createdAt, category.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updatedAt")
        void shouldSetAndGetUpdatedAt() {
            // Arrange
            Category category = new Category();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 18, 15, 45);

            // Act
            category.setUpdatedAt(updatedAt);

            // Assert
            assertEquals(updatedAt, category.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string with all fields")
        void shouldReturnFormattedStringWithAllFields() {
            // Arrange
            Category category = new Category(1, "Electronics", "ELEC");

            // Act
            String result = category.toString();

            // Assert
            assertEquals("Category{categoryId=1, categoryName='Electronics', categoryCode='ELEC'}", result);
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            // Arrange
            Category category = new Category();

            // Act
            String result = category.toString();

            // Assert
            assertTrue(result.contains("categoryId=null"));
            assertTrue(result.contains("categoryName='null'"));
            assertTrue(result.contains("categoryCode='null'"));
        }
    }
}
