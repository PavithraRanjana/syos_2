package com.syos.repository.impl;

import com.syos.domain.models.Category;
import com.syos.exception.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private CategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new CategoryRepositoryImpl(dataSource);
    }

    private void mockCategoryResultSet(int id, String name, String code) throws SQLException {
        when(resultSet.getInt("category_id")).thenReturn(id);
        when(resultSet.getString("category_name")).thenReturn(name);
        when(resultSet.getString("category_code")).thenReturn(code);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new category")
        void shouldInsertNewCategory() throws Exception {
            // Arrange
            Category category = new Category();
            category.setCategoryName("Electronics");
            category.setCategoryCode("ELEC");

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            // Act
            Category saved = repository.save(category);

            // Assert
            assertEquals(1, saved.getCategoryId());
            verify(preparedStatement).setString(1, "Electronics");
            verify(preparedStatement).setString(2, "ELEC");
        }

        @Test
        @DisplayName("Should update existing category")
        void shouldUpdateExistingCategory() throws Exception {
            // Arrange
            Category category = new Category();
            category.setCategoryId(1);
            category.setCategoryName("Electronics Updated");
            category.setCategoryCode("ELEC");

            // Mock existsById check
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Act
            Category updated = repository.save(category);

            // Assert
            assertEquals(1, updated.getCategoryId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find category by ID")
        void shouldFindCategoryById() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCategoryResultSet(1, "Electronics", "ELEC");

            // Act
            Optional<Category> result = repository.findById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Electronics", result.get().getCategoryName());
            verify(preparedStatement).setInt(1, 1);
        }

        @Test
        @DisplayName("Should return empty when category not found")
        void shouldReturnEmptyWhenNotFound() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(false);

            // Act
            Optional<Category> result = repository.findById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByCode tests")
    class FindByCodeTests {

        @Test
        @DisplayName("Should find category by code")
        void shouldFindCategoryByCode() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCategoryResultSet(1, "Electronics", "ELEC");

            // Act
            Optional<Category> result = repository.findByCode("ELEC");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("ELEC", result.get().getCategoryCode());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all categories")
        void shouldFindAllCategories() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(resultSet.getInt("category_id")).thenReturn(1, 2);
            when(resultSet.getString("category_name")).thenReturn("Electronics", "Clothing");
            when(resultSet.getString("category_code")).thenReturn("ELEC", "CLTH");
            when(resultSet.getTimestamp("created_at")).thenReturn(null);
            when(resultSet.getTimestamp("updated_at")).thenReturn(null);

            // Act
            List<Category> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should find all categories with pagination")
        void shouldFindAllCategoriesWithPagination() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockCategoryResultSet(1, "Electronics", "ELEC");

            // Act
            List<Category> result = repository.findAll(0, 10);

            // Assert
            assertEquals(1, result.size());
            verify(preparedStatement).setInt(1, 10); // limit
            verify(preparedStatement).setInt(2, 0); // offset
        }
    }

    @Nested
    @DisplayName("deleteById tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete category by ID")
        void shouldDeleteCategoryById() throws Exception {
            // Arrange
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Act
            boolean result = repository.deleteById(1);

            // Assert
            assertTrue(result);
            verify(preparedStatement).setInt(1, 1);
        }

        @Test
        @DisplayName("Should return false when delete fails")
        void shouldReturnFalseWhenDeleteFails() throws Exception {
            // Arrange
            when(preparedStatement.executeUpdate()).thenReturn(0);

            // Act
            boolean result = repository.deleteById(999);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("count tests")
    class CountTests {

        @Test
        @DisplayName("Should return category count")
        void shouldReturnCategoryCount() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(5L);

            // Act
            long count = repository.count();

            // Assert
            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RepositoryException on SQL error")
        void shouldThrowRepositoryExceptionOnSqlError() throws Exception {
            // Arrange
            when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

            // Act & Assert
            assertThrows(RepositoryException.class, () -> repository.findAll());
        }
    }
}
