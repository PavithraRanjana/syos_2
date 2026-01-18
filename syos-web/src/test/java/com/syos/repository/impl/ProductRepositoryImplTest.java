package com.syos.repository.impl;

import com.syos.domain.enums.UnitOfMeasure;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
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
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductRepositoryImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductRepositoryImplTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private ProductRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        repository = new ProductRepositoryImpl(dataSource);
    }

    private void mockProductResultSet(String code, String name, int categoryId, int subcategoryId, int brandId)
            throws SQLException {
        when(resultSet.getString("product_code")).thenReturn(code);
        when(resultSet.getString("product_name")).thenReturn(name);
        when(resultSet.getInt("category_id")).thenReturn(categoryId);
        when(resultSet.getInt("subcategory_id")).thenReturn(subcategoryId);
        when(resultSet.getInt("brand_id")).thenReturn(brandId);
        when(resultSet.getBigDecimal("unit_price")).thenReturn(BigDecimal.TEN);
        when(resultSet.getString("description")).thenReturn("Desc");
        when(resultSet.getString("unit_of_measure")).thenReturn("PCS");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getInt("min_physical_stock")).thenReturn(10);
        when(resultSet.getInt("min_online_stock")).thenReturn(5);
        when(resultSet.getString("category_name")).thenReturn("Cat");
        when(resultSet.getString("subcategory_name")).thenReturn("Sub");
        when(resultSet.getString("brand_name")).thenReturn("Brand");
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("Should insert new product")
        void shouldInsertNewProduct() throws Exception {
            Product product = new Product();
            product.setProductCode(new ProductCode("P001"));
            product.setProductName("Test Product");
            product.setCategoryId(1);
            product.setSubcategoryId(1);
            product.setBrandId(1);
            product.setUnitPrice(new Money(BigDecimal.TEN));
            product.setUnitOfMeasure(UnitOfMeasure.PCS);

            // Mock exists check
            PreparedStatement existsStmt = mock(PreparedStatement.class);
            ResultSet existsRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(existsStmt);
            when(existsStmt.executeQuery()).thenReturn(existsRs);
            when(existsRs.next()).thenReturn(true);
            when(existsRs.getInt(1)).thenReturn(0); // Not exists

            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Mock find after insert
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test Product", 1, 1, 1);

            Product saved = repository.save(product);

            assertEquals("P001", saved.getProductCodeString());
        }

        @Test
        @DisplayName("Should update existing product")
        void shouldUpdateProduct() throws Exception {
            Product product = new Product();
            product.setProductCode(new ProductCode("P001"));
            product.setProductName("Test Product");
            product.setCategoryId(1);
            product.setSubcategoryId(1);
            product.setBrandId(1);
            product.setUnitPrice(new Money(BigDecimal.TEN));
            product.setUnitOfMeasure(UnitOfMeasure.PCS);

            // Mock exists check
            PreparedStatement existsStmt = mock(PreparedStatement.class);
            ResultSet existsRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(existsStmt);
            when(existsStmt.executeQuery()).thenReturn(existsRs);
            when(existsRs.next()).thenReturn(true);
            when(existsRs.getInt(1)).thenReturn(1); // Exists

            when(connection.prepareStatement(contains("UPDATE product"))).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Mock find after update
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Updated", 1, 1, 1);

            Product saved = repository.save(product);
            assertEquals("P001", saved.getProductCodeString());
        }
    }

    @Nested
    @DisplayName("find tests")
    class FindTests {
        @Test
        void shouldFindById() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertTrue(repository.findById("P001").isPresent());
        }

        @Test
        void shouldFindByProductCode() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertTrue(repository.findByProductCode("P001").isPresent());
        }

        @Test
        void shouldFindAll() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findAll().size());
        }

        @Test
        void shouldFindAllPaged() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findAll(0, 10).size());
        }

        @Test
        void shouldFindAllWithCatalogInfo() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findAllWithCatalogInfo().size());
        }

        @Test
        void shouldFindAllActive() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findAllActive().size());
        }

        @Test
        void shouldFindByCategoryId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findByCategoryId(1).size());
        }

        @Test
        void shouldFindBySubcategoryId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findBySubcategoryId(1).size());
        }

        @Test
        void shouldFindByBrandId() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.findByBrandId(1).size());
        }

        @Test
        void shouldSearch() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.search("Test").size());
        }

        @Test
        void shouldSearchByName() throws Exception {
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            mockProductResultSet("P001", "Test", 1, 1, 1);
            assertEquals(1, repository.searchByName("Test").size());
        }
    }

    @Nested
    @DisplayName("generateCode tests")
    class GenerateCodeTests {
        @Test
        void shouldGenerateProductCode() throws Exception {
            // Mock stored procedure
            CallableStatement cs = mock(CallableStatement.class);
            when(connection.prepareCall(anyString())).thenReturn(cs); // Incorrect, code uses prepareStatement for CALL
            // The code uses "stmt.execute()" on a PreparedStatement for the CALL

            // Let's mock the code flow:
            PreparedStatement callStmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(contains("CALL GenerateProductCode"))).thenReturn(callStmt);

            PreparedStatement selectStmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT @product_code"))).thenReturn(selectStmt);
            when(selectStmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString(1)).thenReturn("C01S01B01001");

            String code = repository.generateProductCode(1, 1, 1);
            assertEquals("C01S01B01001", code);
        }

        @Test
        void shouldGenerateProductCodeManually() throws Exception {
            // Force exception in SP call to trigger fallback
            when(connection.prepareStatement(contains("CALL GenerateProductCode"))).thenThrow(new SQLException());

            // Mock fallback queries
            PreparedStatement metaStmt = mock(PreparedStatement.class);
            ResultSet metaRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("FROM category c"))).thenReturn(metaStmt);
            when(metaStmt.executeQuery()).thenReturn(metaRs);
            when(metaRs.next()).thenReturn(true);
            when(metaRs.getString("category_code")).thenReturn("C01");
            when(metaRs.getString("subcategory_code")).thenReturn("S01");
            when(metaRs.getString("brand_code")).thenReturn("B01");

            PreparedStatement maxStmt = mock(PreparedStatement.class);
            ResultSet maxRs = mock(ResultSet.class);
            when(connection.prepareStatement(contains("SELECT COALESCE"))).thenReturn(maxStmt);
            when(maxStmt.executeQuery()).thenReturn(maxRs);
            when(maxRs.next()).thenReturn(true);
            when(maxRs.getInt("next_seq")).thenReturn(5);

            String code = repository.generateProductCode(1, 1, 1);
            assertEquals("C01S01B01005", code);
        }
    }

    @Nested
    @DisplayName("update/delete tests")
    class UpdateDeleteTests {
        @Test
        void shouldUpdatePrice() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            assertTrue(repository.updatePrice("P001", BigDecimal.TEN));
        }

        @Test
        void shouldActivate() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            assertTrue(repository.activate("P001"));
        }

        @Test
        void shouldDeactivate() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            assertTrue(repository.deactivate("P001"));
        }

        @Test
        void shouldDelete() throws Exception {
            when(preparedStatement.executeUpdate()).thenReturn(1);
            assertTrue(repository.deleteById("P001")); // calls deactivate
        }

        @Test
        void shouldExist() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            assertTrue(repository.existsById("P001"));
        }

        @Test
        void shouldCount() throws Exception {
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong(1)).thenReturn(100L);
            assertEquals(100L, repository.count());
        }
    }

    @Nested
    @DisplayName("error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RepositoryException on SQL error")
        void shouldThrowRepositoryExceptionOnSqlError() throws Exception {
            when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

            assertThrows(RepositoryException.class, () -> repository.findAll());
        }
    }
}
