package com.syos.repository.impl;

import com.syos.domain.models.Category;
import com.syos.repository.interfaces.CategoryRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CategoryRepository using JDBC.
 */
public class CategoryRepositoryImpl extends BaseRepository implements CategoryRepository {

    public CategoryRepositoryImpl() {
        super();
    }

    public CategoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Category save(Category category) {
        if (category.getCategoryId() != null && existsById(category.getCategoryId())) {
            return update(category);
        } else {
            return insert(category);
        }
    }

    private Category insert(Category category) {
        String sql = "INSERT INTO category (category_name, category_code) VALUES (?, ?)";
        Integer id = executeInsertAndGetId(sql, category.getCategoryName(), category.getCategoryCode());
        category.setCategoryId(id);
        return category;
    }

    private Category update(Category category) {
        String sql = "UPDATE category SET category_name = ?, category_code = ? WHERE category_id = ?";
        executeUpdate(sql, category.getCategoryName(), category.getCategoryCode(), category.getCategoryId());
        return category;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT * FROM category WHERE category_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public Optional<Category> findByCode(String categoryCode) {
        String sql = "SELECT * FROM category WHERE category_code = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), categoryCode);
    }

    @Override
    public Optional<Category> findByName(String categoryName) {
        String sql = "SELECT * FROM category WHERE category_name = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), categoryName);
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM category ORDER BY category_id";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Category> findAll(int offset, int limit) {
        String sql = "SELECT * FROM category ORDER BY category_id LIMIT ? OFFSET ?";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public List<Category> findAllOrderByName() {
        String sql = "SELECT * FROM category ORDER BY category_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM category WHERE category_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM category WHERE category_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM category";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setCategoryCode(rs.getString("category_code"));
        category.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        category.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return category;
    }
}
