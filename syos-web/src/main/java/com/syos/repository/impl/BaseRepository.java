package com.syos.repository.impl;

import com.syos.config.DataSourceConfig;
import com.syos.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Base repository class providing common database operations.
 * Uses HikariCP DataSource for connection pooling.
 */
public abstract class BaseRepository {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DataSource dataSource;

    protected BaseRepository() {
        this.dataSource = DataSourceConfig.getDataSource();
    }

    protected BaseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets a connection from the pool.
     */
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Executes a query and maps results using the provided mapper.
     */
    protected <T> T executeQuery(String sql, ResultSetMapper<T> mapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            return mapper.map(rs);
        } catch (SQLException e) {
            logger.error("Query execution failed: {} - {}", sql, e.getMessage());
            throw new RepositoryException("Database query failed", e);
        }
    }

    /**
     * Executes an update (INSERT, UPDATE, DELETE) and returns affected rows.
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Update execution failed: {} - {}", sql, e.getMessage());
            throw new RepositoryException("Database update failed", e);
        }
    }

    /**
     * Executes an insert and returns the generated key.
     */
    protected <T> T executeInsertWithKey(String sql, ResultSetMapper<T> keyMapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                return keyMapper.map(generatedKeys);
            }
        } catch (SQLException e) {
            logger.error("Insert execution failed: {} - {}", sql, e.getMessage());
            throw new RepositoryException("Database insert failed", e);
        }
    }

    /**
     * Executes an insert and returns the generated integer key.
     */
    protected Integer executeInsertAndGetId(String sql, Object... params) {
        return executeInsertWithKey(sql, rs -> {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        }, params);
    }

    /**
     * Prepares a statement with parameters.
     */
    protected PreparedStatement prepareStatement(Connection conn, String sql, Object... params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt;
    }

    /**
     * Sets parameters on a prepared statement.
     */
    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            setParameter(stmt, i + 1, params[i]);
        }
    }

    /**
     * Sets a single parameter, handling null values and type conversions.
     */
    protected void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            stmt.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof java.math.BigDecimal) {
            stmt.setBigDecimal(index, (java.math.BigDecimal) value);
        } else if (value instanceof java.time.LocalDate) {
            stmt.setDate(index, java.sql.Date.valueOf((java.time.LocalDate) value));
        } else if (value instanceof java.time.LocalDateTime) {
            stmt.setTimestamp(index, java.sql.Timestamp.valueOf((java.time.LocalDateTime) value));
        } else if (value instanceof java.util.Date) {
            stmt.setTimestamp(index, new java.sql.Timestamp(((java.util.Date) value).getTime()));
        } else if (value instanceof Enum) {
            stmt.setString(index, ((Enum<?>) value).name());
        } else {
            stmt.setObject(index, value);
        }
    }

    /**
     * Converts a SQL Date to LocalDate.
     */
    protected java.time.LocalDate toLocalDate(java.sql.Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    /**
     * Converts a SQL Timestamp to LocalDateTime.
     */
    protected java.time.LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Functional interface for mapping ResultSet to objects.
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Functional interface for mapping a single row.
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    /**
     * Maps ResultSet to a list using the provided row mapper.
     */
    protected <T> java.util.List<T> mapToList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        java.util.List<T> results = new java.util.ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    /**
     * Maps ResultSet to an optional single result.
     */
    protected <T> java.util.Optional<T> mapToOptional(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        if (rs.next()) {
            return java.util.Optional.ofNullable(rowMapper.mapRow(rs));
        }
        return java.util.Optional.empty();
    }
}
