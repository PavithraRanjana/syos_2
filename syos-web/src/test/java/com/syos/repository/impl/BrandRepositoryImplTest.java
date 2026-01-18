package com.syos.repository.impl;

import com.syos.domain.models.Brand;
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
 * Unit tests for BrandRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BrandRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private BrandRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new BrandRepositoryImpl(dataSource);
    }

    private void mockBrandResultSet(int id, String name, String code) throws SQLException {
        when(resultSet.getInt("brand_id")).thenReturn(id);
        when(resultSet.getString("brand_name")).thenReturn(name);
        when(resultSet.getString("brand_code")).thenReturn(code);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new brand")
        void shouldInsertNewBrand() throws Exception {
            // Arrange
            Brand brand = new Brand();
            brand.setBrandName("Apple");
            brand.setBrandCode("APPL");

            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(1);

            // Act
            Brand saved = repository.save(brand);

            // Assert
            assertEquals(1, saved.getBrandId());
            verify(preparedStatement).setString(1, "Apple");
            verify(preparedStatement).setString(2, "APPL");
        }

        @Test
        @DisplayName("Should update existing brand")
        void shouldUpdateExistingBrand() throws Exception {
            // Arrange
            Brand brand = new Brand();
            brand.setBrandId(1);
            brand.setBrandName("Apple Inc");
            brand.setBrandCode("APPL");

            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Act
            Brand updated = repository.save(brand);

            // Assert
            assertEquals(1, updated.getBrandId());
        }
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find brand by ID")
        void shouldFindBrandById() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBrandResultSet(1, "Apple", "APPL");

            // Act
            Optional<Brand> result = repository.findById(1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Apple", result.get().getBrandName());
        }

        @Test
        @DisplayName("Should return empty when brand not found")
        void shouldReturnEmptyWhenNotFound() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(false);

            // Act
            Optional<Brand> result = repository.findById(999);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByCode tests")
    class FindByCodeTests {

        @Test
        @DisplayName("Should find brand by code")
        void shouldFindBrandByCode() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBrandResultSet(1, "Apple", "APPL");

            // Act
            Optional<Brand> result = repository.findByCode("APPL");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("APPL", result.get().getBrandCode());
        }
    }

    @Nested
    @DisplayName("findByName tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should find brand by name")
        void shouldFindBrandByName() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBrandResultSet(1, "Apple", "APPL");

            // Act
            Optional<Brand> result = repository.findByName("Apple");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Apple", result.get().getBrandName());
        }
    }

    @Nested
    @DisplayName("findAll tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all brands")
        void shouldFindAllBrands() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(resultSet.getInt("brand_id")).thenReturn(1, 2);
            when(resultSet.getString("brand_name")).thenReturn("Apple", "Samsung");
            when(resultSet.getString("brand_code")).thenReturn("APPL", "SAMS");
            when(resultSet.getTimestamp("created_at")).thenReturn(null);
            when(resultSet.getTimestamp("updated_at")).thenReturn(null);

            // Act
            List<Brand> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should find all brands with pagination")
        void shouldFindAllBrandsWithPagination() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBrandResultSet(1, "Apple", "APPL");

            // Act
            List<Brand> result = repository.findAll(0, 10);

            // Assert
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("searchByName tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Should search brands by name part")
        void shouldSearchBrandsByNamePart() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockBrandResultSet(1, "Apple", "APPL");

            // Act
            List<Brand> result = repository.searchByName("App");

            // Assert
            assertEquals(1, result.size());
            verify(preparedStatement).setString(1, "%App%");
        }
    }

    @Nested
    @DisplayName("deleteById tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete brand by ID")
        void shouldDeleteBrandById() throws Exception {
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
        @DisplayName("Should return brand count")
        void shouldReturnBrandCount() throws Exception {
            // Arrange
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(10L);

            // Act
            long count = repository.count();

            // Assert
            assertEquals(10L, count);
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
