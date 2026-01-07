package com.syos.repository.impl;

import com.syos.domain.models.Subcategory;
import com.syos.repository.interfaces.SubcategoryRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SubcategoryRepository using JDBC.
 */
public class SubcategoryRepositoryImpl extends BaseRepository implements SubcategoryRepository {

    public SubcategoryRepositoryImpl() {
        super();
    }

    public SubcategoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Subcategory save(Subcategory subcategory) {
        if (subcategory.getSubcategoryId() != null && existsById(subcategory.getSubcategoryId())) {
            return update(subcategory);
        } else {
            return insert(subcategory);
        }
    }

    private Subcategory insert(Subcategory subcategory) {
        String sql = "INSERT INTO subcategory (subcategory_name, subcategory_code, category_id) VALUES (?, ?, ?)";
        Integer id = executeInsertAndGetId(sql,
            subcategory.getSubcategoryName(),
            subcategory.getSubcategoryCode(),
            subcategory.getCategoryId()
        );
        subcategory.setSubcategoryId(id);
        return subcategory;
    }

    private Subcategory update(Subcategory subcategory) {
        String sql = "UPDATE subcategory SET subcategory_name = ?, subcategory_code = ?, category_id = ? WHERE subcategory_id = ?";
        executeUpdate(sql,
            subcategory.getSubcategoryName(),
            subcategory.getSubcategoryCode(),
            subcategory.getCategoryId(),
            subcategory.getSubcategoryId()
        );
        return subcategory;
    }

    @Override
    public Optional<Subcategory> findById(Integer id) {
        String sql = "SELECT * FROM subcategory WHERE subcategory_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<Subcategory> findByCategoryId(Integer categoryId) {
        String sql = "SELECT * FROM subcategory WHERE category_id = ? ORDER BY subcategory_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), categoryId);
    }

    @Override
    public Optional<Subcategory> findByCodeAndCategoryId(String subcategoryCode, Integer categoryId) {
        String sql = "SELECT * FROM subcategory WHERE subcategory_code = ? AND category_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), subcategoryCode, categoryId);
    }

    @Override
    public Optional<Subcategory> findByNameAndCategoryId(String subcategoryName, Integer categoryId) {
        String sql = "SELECT * FROM subcategory WHERE subcategory_name = ? AND category_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), subcategoryName, categoryId);
    }

    @Override
    public List<Subcategory> findAll() {
        String sql = "SELECT * FROM subcategory ORDER BY category_id, subcategory_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Subcategory> findAll(int offset, int limit) {
        String sql = "SELECT * FROM subcategory ORDER BY category_id, subcategory_name LIMIT ? OFFSET ?";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM subcategory WHERE subcategory_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM subcategory WHERE subcategory_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM subcategory";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private Subcategory mapRow(ResultSet rs) throws SQLException {
        Subcategory subcategory = new Subcategory();
        subcategory.setSubcategoryId(rs.getInt("subcategory_id"));
        subcategory.setSubcategoryName(rs.getString("subcategory_name"));
        subcategory.setSubcategoryCode(rs.getString("subcategory_code"));
        subcategory.setCategoryId(rs.getInt("category_id"));
        subcategory.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        subcategory.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return subcategory;
    }
}
