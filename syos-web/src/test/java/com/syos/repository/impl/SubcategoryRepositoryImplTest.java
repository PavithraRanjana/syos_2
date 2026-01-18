package com.syos.repository.impl;

import com.syos.domain.models.Subcategory;
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
 * Unit tests for SubcategoryRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubcategoryRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private SubcategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new SubcategoryRepositoryImpl(dataSource);
    }

    private void mockSubcategoryResultSet(int id, String name, String code, int categoryId) throws SQLException {
        when(resultSet.getInt("subcategory_id")).thenReturn(id);
        when(resultSet.getString("subcategory_name")).thenReturn(name);
        when(resultSet.getString("subcategory_code")).thenReturn(code);
        when(resultSet.getInt("category_id")).thenReturn(categoryId);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new subcategory")
        void shouldInsertNewSubcategory() throws Exception {
            // Arrange
            Subcategory subcategory = new Subcategory();
            subcategory.setSubcategoryName("Smartphones");
            subcategory.setSubcategoryCode("SMPH");
            subcategory.setCategoryId(1);

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            // Act
            Subcategory saved = repository.save(subcategory);

            // Assert
            assertEquals(1, saved.getSubcategoryId());
            verify(preparedStatement).setString(1, "Smartphones");
            verify(preparedStatement).setString(2, "SMPH");
            verify(preparedStatement).setInt(3, 1);
        }

        @Test
        @DisplayName("Should update existing subcategory")
        void shouldUpdateExistingSubcategory() throws Exception {
            // Arrange
            Subcategory subcategory = new Subcategory();
            subcategory.setSubcategoryId(1);
            subcategory.setSubcategoryName("Smartphones Updated");
            subcategory.setSubcategoryCode("SMPH");
            subcategory.setCategoryId(1);

            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Act
            Subcategory updated = repository.save(subcategory);

            // Assert
            assertEquals(1, updated.getSubcategoryId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find subcategory by ID")
        void shouldFindSubcategoryById() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockSubcategoryResultSet(1, "Smartphones", "SMPH", 1);

            // Act
            Optional<Subcategory> result = repository.findById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Smartphones", result.get().getSubcategoryName());
        }

        @Test
        @DisplayName("Should return empty when subcategory not found")
        void shouldReturnEmptyWhenNotFound() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(false);

            // Act
            Optional<Subcategory> result = repository.findById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByCategoryId tests")
    class FindByCategoryIdTests {

        @Test
        @DisplayName("Should find subcategories by category ID")
        void shouldFindSubcategoriesByCategoryId() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(resultSet.getInt("subcategory_id")).thenReturn(1, 2);
            when(resultSet.getString("subcategory_name")).thenReturn("Smartphones", "Laptops");
            when(resultSet.getString("subcategory_code")).thenReturn("SMPH", "LPTS");
            when(resultSet.getInt("category_id")).thenReturn(1, 1);
            when(resultSet.getTimestamp("created_at")).thenReturn(null);
            when(resultSet.getTimestamp("updated_at")).thenReturn(null);

            // Act
            List<Subcategory> result = repository.findByCategoryId(1);

            // Assert
            assertEquals(2, result.size());
            verify(preparedStatement).setInt(1, 1);
        }
    }

    @Nested
    @DisplayName("findByCodeAndCategoryId tests")
    class FindByCodeAndCategoryIdTests {

        @Test
        @DisplayName("Should find subcategory by code and category ID")
        void shouldFindSubcategoryByCodeAndCategoryId() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockSubcategoryResultSet(1, "Smartphones", "SMPH", 1);

            // Act
            Optional<Subcategory> result = repository.findByCodeAndCategoryId("SMPH", 1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("SMPH", result.get().getSubcategoryCode());
            verify(preparedStatement).setString(1, "SMPH");
            verify(preparedStatement).setInt(2, 1);
        }
    }

    @Nested
    @DisplayName("findByNameAndCategoryId tests")
    class FindByNameAndCategoryIdTests {

        @Test
        @DisplayName("Should find subcategory by name and category ID")
        void shouldFindSubcategoryByNameAndCategoryId() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockSubcategoryResultSet(1, "Smartphones", "SMPH", 1);

            // Act
            Optional<Subcategory> result = repository.findByNameAndCategoryId("Smartphones", 1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Smartphones", result.get().getSubcategoryName());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all subcategories")
        void shouldFindAllSubcategories() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockSubcategoryResultSet(1, "Smartphones", "SMPH", 1);

            // Act
            List<Subcategory> result = repository.findAll();

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should find all subcategories with pagination")
        void shouldFindAllSubcategoriesWithPagination() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockSubcategoryResultSet(1, "Smartphones", "SMPH", 1);

            // Act
            List<Subcategory> result = repository.findAll(0, 10);

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
        @DisplayName("Should delete subcategory by ID")
        void shouldDeleteSubcategoryById() throws Exception {
            // Arrange
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Act
            boolean result = repository.deleteById(1);

            // Assert
            assertTrue(result);
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
        @DisplayName("Should return subcategory count")
        void shouldReturnSubcategoryCount() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(15L);

            // Act
            long count = repository.count();

            // Assert
            assertEquals(15L, count);
        }
    }

    @Nested
    @DisplayName("existsById tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when subcategory exists")
        void shouldReturnTrueWhenExists() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);

            // Act
            boolean result = repository.existsById(1);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when subcategory does not exist")
        void shouldReturnFalseWhenNotExists() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(0);

            // Act
            boolean result = repository.existsById(999);

            // Assert
            assertFalse(result);
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
