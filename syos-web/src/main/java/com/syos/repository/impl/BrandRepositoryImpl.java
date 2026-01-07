package com.syos.repository.impl;

import com.syos.domain.models.Brand;
import com.syos.repository.interfaces.BrandRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BrandRepository using JDBC.
 */
public class BrandRepositoryImpl extends BaseRepository implements BrandRepository {

    public BrandRepositoryImpl() {
        super();
    }

    public BrandRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Brand save(Brand brand) {
        if (brand.getBrandId() != null && existsById(brand.getBrandId())) {
            return update(brand);
        } else {
            return insert(brand);
        }
    }

    private Brand insert(Brand brand) {
        String sql = "INSERT INTO brand (brand_name, brand_code) VALUES (?, ?)";
        Integer id = executeInsertAndGetId(sql, brand.getBrandName(), brand.getBrandCode());
        brand.setBrandId(id);
        return brand;
    }

    private Brand update(Brand brand) {
        String sql = "UPDATE brand SET brand_name = ?, brand_code = ? WHERE brand_id = ?";
        executeUpdate(sql, brand.getBrandName(), brand.getBrandCode(), brand.getBrandId());
        return brand;
    }

    @Override
    public Optional<Brand> findById(Integer id) {
        String sql = "SELECT * FROM brand WHERE brand_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public Optional<Brand> findByCode(String brandCode) {
        String sql = "SELECT * FROM brand WHERE brand_code = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), brandCode);
    }

    @Override
    public Optional<Brand> findByName(String brandName) {
        String sql = "SELECT * FROM brand WHERE brand_name = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), brandName);
    }

    @Override
    public List<Brand> findAll() {
        String sql = "SELECT * FROM brand ORDER BY brand_id";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Brand> findAll(int offset, int limit) {
        String sql = "SELECT * FROM brand ORDER BY brand_id LIMIT ? OFFSET ?";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public List<Brand> findAllOrderByName() {
        String sql = "SELECT * FROM brand ORDER BY brand_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Brand> searchByName(String namePart) {
        String sql = "SELECT * FROM brand WHERE brand_name LIKE ? ORDER BY brand_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), "%" + namePart + "%");
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM brand WHERE brand_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM brand WHERE brand_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM brand";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private Brand mapRow(ResultSet rs) throws SQLException {
        Brand brand = new Brand();
        brand.setBrandId(rs.getInt("brand_id"));
        brand.setBrandName(rs.getString("brand_name"));
        brand.setBrandCode(rs.getString("brand_code"));
        brand.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        brand.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return brand;
    }
}
