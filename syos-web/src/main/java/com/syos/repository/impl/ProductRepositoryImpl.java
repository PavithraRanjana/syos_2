package com.syos.repository.impl;

import com.syos.domain.enums.UnitOfMeasure;
import com.syos.domain.models.Product;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.ProductRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductRepository using JDBC.
 */
public class ProductRepositoryImpl extends BaseRepository implements ProductRepository {

    public ProductRepositoryImpl() {
        super();
    }

    public ProductRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Product save(Product product) {
        if (existsById(product.getProductCodeString())) {
            return update(product);
        } else {
            return insert(product);
        }
    }

    private Product insert(Product product) {
        String sql = """
            INSERT INTO product (product_code, product_name, category_id, subcategory_id,
                brand_id, unit_price, description, unit_of_measure, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        executeUpdate(sql,
            product.getProductCodeString(),
            product.getProductName(),
            product.getCategoryId(),
            product.getSubcategoryId(),
            product.getBrandId(),
            product.getUnitPrice().getAmount(),
            product.getDescription(),
            product.getUnitOfMeasure().getSymbol(),
            product.isActive()
        );

        return findByProductCode(product.getProductCodeString()).orElse(product);
    }

    private Product update(Product product) {
        String sql = """
            UPDATE product SET product_name = ?, category_id = ?, subcategory_id = ?,
                brand_id = ?, unit_price = ?, description = ?, unit_of_measure = ?, is_active = ?
            WHERE product_code = ?
            """;

        executeUpdate(sql,
            product.getProductName(),
            product.getCategoryId(),
            product.getSubcategoryId(),
            product.getBrandId(),
            product.getUnitPrice().getAmount(),
            product.getDescription(),
            product.getUnitOfMeasure().getSymbol(),
            product.isActive(),
            product.getProductCodeString()
        );

        return findByProductCode(product.getProductCodeString()).orElse(product);
    }

    @Override
    public Optional<Product> findById(String productCode) {
        return findByProductCode(productCode);
    }

    @Override
    public Optional<Product> findByProductCode(String productCode) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.product_code = ?
            """;

        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), productCode);
    }

    @Override
    public List<Product> findAll() {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            ORDER BY p.product_code
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Product> findAll(int offset, int limit) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            ORDER BY p.product_code
            LIMIT ? OFFSET ?
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public List<Product> findAllWithCatalogInfo() {
        return findAll();
    }

    @Override
    public List<Product> findAllActive() {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.is_active = TRUE
            ORDER BY p.product_code
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Product> findByCategoryId(Integer categoryId) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.category_id = ? AND p.is_active = TRUE
            ORDER BY p.product_code
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), categoryId);
    }

    @Override
    public List<Product> findBySubcategoryId(Integer subcategoryId) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.subcategory_id = ? AND p.is_active = TRUE
            ORDER BY p.product_code
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), subcategoryId);
    }

    @Override
    public List<Product> findByBrandId(Integer brandId) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.brand_id = ? AND p.is_active = TRUE
            ORDER BY p.product_code
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), brandId);
    }

    @Override
    public List<Product> search(String searchTerm) {
        return search(searchTerm, 0, 100);
    }

    @Override
    public List<Product> search(String searchTerm, int offset, int limit) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.is_active = TRUE AND (
                p.product_code LIKE ? OR
                p.product_name LIKE ? OR
                c.category_name LIKE ? OR
                sc.subcategory_name LIKE ? OR
                b.brand_name LIKE ?
            )
            ORDER BY p.product_code
            LIMIT ? OFFSET ?
            """;

        String pattern = "%" + searchTerm + "%";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow),
            pattern, pattern, pattern, pattern, pattern, limit, offset);
    }

    @Override
    public String generateProductCode(Integer categoryId, Integer subcategoryId, Integer brandId) {
        String sql = "{CALL GenerateProductCode(?, ?, ?, @product_code)}";
        String selectSql = "SELECT @product_code";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, subcategoryId);
            stmt.setInt(3, brandId);
            stmt.execute();

            try (var selectStmt = conn.prepareStatement(selectSql);
                 var rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to generate product code: {}", e.getMessage());
            // Fallback: generate manually
            return generateProductCodeManually(categoryId, subcategoryId, brandId);
        }
        return null;
    }

    private String generateProductCodeManually(Integer categoryId, Integer subcategoryId, Integer brandId) {
        // Get codes from respective tables
        String codeSql = """
            SELECT c.category_code, sc.subcategory_code, b.brand_code
            FROM category c, subcategory sc, brand b
            WHERE c.category_id = ? AND sc.subcategory_id = ? AND b.brand_id = ?
            """;

        return executeQuery(codeSql, rs -> {
            if (rs.next()) {
                String baseCode = rs.getString("category_code") +
                                  rs.getString("subcategory_code") +
                                  rs.getString("brand_code");

                // Find next sequence
                String seqSql = """
                    SELECT COALESCE(MAX(CAST(RIGHT(product_code, 3) AS UNSIGNED)), 0) + 1 as next_seq
                    FROM product WHERE product_code LIKE ?
                    """;

                Integer nextSeq = executeQuery(seqSql, rs2 -> {
                    if (rs2.next()) return rs2.getInt("next_seq");
                    return 1;
                }, baseCode + "%");

                return baseCode + String.format("%03d", nextSeq);
            }
            return null;
        }, categoryId, subcategoryId, brandId);
    }

    @Override
    public boolean updatePrice(String productCode, BigDecimal newPrice) {
        String sql = "UPDATE product SET unit_price = ? WHERE product_code = ?";
        return executeUpdate(sql, newPrice, productCode) > 0;
    }

    @Override
    public boolean activate(String productCode) {
        String sql = "UPDATE product SET is_active = TRUE WHERE product_code = ?";
        return executeUpdate(sql, productCode) > 0;
    }

    @Override
    public boolean deactivate(String productCode) {
        String sql = "UPDATE product SET is_active = FALSE WHERE product_code = ?";
        return executeUpdate(sql, productCode) > 0;
    }

    @Override
    public boolean deleteById(String productCode) {
        // Soft delete by deactivating
        return deactivate(productCode);
    }

    @Override
    public boolean existsById(String productCode) {
        String sql = "SELECT COUNT(*) FROM product WHERE product_code = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }, productCode);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM product";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getLong(1);
            return 0L;
        });
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return existsById(productCode);
    }

    @Override
    public List<Product> searchByName(String searchTerm) {
        String sql = """
            SELECT p.*, c.category_name, sc.subcategory_name, b.brand_name
            FROM product p
            JOIN category c ON p.category_id = c.category_id
            JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
            JOIN brand b ON p.brand_id = b.brand_id
            WHERE p.is_active = TRUE AND p.product_name LIKE ?
            ORDER BY p.product_name
            """;

        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), "%" + searchTerm + "%");
    }

    /**
     * Maps a ResultSet row to a Product entity.
     */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductCode(new ProductCode(rs.getString("product_code")));
        product.setProductName(rs.getString("product_name"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setSubcategoryId(rs.getInt("subcategory_id"));
        product.setBrandId(rs.getInt("brand_id"));
        product.setUnitPrice(new Money(rs.getBigDecimal("unit_price")));
        product.setDescription(rs.getString("description"));
        product.setUnitOfMeasure(UnitOfMeasure.fromString(rs.getString("unit_of_measure")));
        product.setActive(rs.getBoolean("is_active"));
        product.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        product.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        // Catalog info from joins (if available)
        try {
            product.setCategoryName(rs.getString("category_name"));
            product.setSubcategoryName(rs.getString("subcategory_name"));
            product.setBrandName(rs.getString("brand_name"));
        } catch (SQLException e) {
            // Columns not in result set, ignore
        }

        return product;
    }
}
