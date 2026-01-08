package com.syos.repository.impl;

import com.syos.domain.enums.UserRole;
import com.syos.domain.models.Customer;
import com.syos.repository.interfaces.CustomerRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CustomerRepository using JDBC.
 */
public class CustomerRepositoryImpl extends BaseRepository implements CustomerRepository {

    public CustomerRepositoryImpl() {
        super();
    }

    public CustomerRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Customer save(Customer customer) {
        if (customer.getCustomerId() != null && existsById(customer.getCustomerId())) {
            return update(customer);
        } else {
            return insert(customer);
        }
    }

    private Customer insert(Customer customer) {
        String sql = """
            INSERT INTO customer (customer_name, email, phone, address, password_hash, role, registration_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            customer.getCustomerName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getPasswordHash(),
            customer.getRole().name(),
            customer.getRegistrationDate() != null ? customer.getRegistrationDate() : LocalDate.now(),
            customer.isActive()
        );
        customer.setCustomerId(id);
        return customer;
    }

    private Customer update(Customer customer) {
        String sql = """
            UPDATE customer SET customer_name = ?, email = ?, phone = ?, address = ?, role = ?, is_active = ?
            WHERE customer_id = ?
            """;

        executeUpdate(sql,
            customer.getCustomerName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getRole().name(),
            customer.isActive(),
            customer.getCustomerId()
        );
        return customer;
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customer WHERE email = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), email);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM customer WHERE phone = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM customer WHERE phone = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, phone);
    }

    @Override
    public List<Customer> findAllActive() {
        String sql = "SELECT * FROM customer WHERE is_active = TRUE ORDER BY customer_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Customer> findByNameContaining(String name) {
        String sql = "SELECT * FROM customer WHERE customer_name LIKE ? ORDER BY customer_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), "%" + name + "%");
    }

    @Override
    public List<Customer> findByRegistrationDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT * FROM customer
            WHERE registration_date BETWEEN ? AND ?
            ORDER BY registration_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), startDate, endDate);
    }

    @Override
    public List<Customer> findCustomersWithRecentOrders(int days) {
        String sql = """
            SELECT DISTINCT c.*
            FROM customer c
            JOIN bill b ON c.customer_id = b.customer_id
            WHERE DATE(b.bill_date) >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            ORDER BY c.customer_name
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), days);
    }

    @Override
    public boolean deactivate(Integer customerId) {
        String sql = "UPDATE customer SET is_active = FALSE WHERE customer_id = ?";
        return executeUpdate(sql, customerId) > 0;
    }

    @Override
    public boolean activate(Integer customerId) {
        String sql = "UPDATE customer SET is_active = TRUE WHERE customer_id = ?";
        return executeUpdate(sql, customerId) > 0;
    }

    @Override
    public boolean updatePassword(Integer customerId, String passwordHash) {
        String sql = "UPDATE customer SET password_hash = ? WHERE customer_id = ?";
        return executeUpdate(sql, passwordHash, customerId) > 0;
    }

    @Override
    public CustomerStatistics getStatistics() {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active,
                SUM(CASE WHEN registration_date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) THEN 1 ELSE 0 END) as new_this_month
            FROM customer
            """;

        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return new CustomerStatistics(
                    rs.getLong("total"),
                    rs.getLong("active"),
                    rs.getLong("new_this_month")
                );
            }
            return new CustomerStatistics(0, 0, 0);
        });
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customer ORDER BY customer_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Customer> findAll(int offset, int limit) {
        String sql = "SELECT * FROM customer ORDER BY customer_name LIMIT ? OFFSET ?";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM customer WHERE customer_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM customer";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    /**
     * Find all users with a specific role.
     */
    public List<Customer> findByRole(UserRole role) {
        String sql = "SELECT * FROM customer WHERE role = ? ORDER BY customer_name";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), role.name());
    }

    /**
     * Update a user's role.
     */
    public boolean updateRole(Integer customerId, UserRole role) {
        String sql = "UPDATE customer SET role = ? WHERE customer_id = ?";
        return executeUpdate(sql, role.name(), customerId) > 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setCustomerName(rs.getString("customer_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setPasswordHash(rs.getString("password_hash"));
        customer.setRole(UserRole.fromString(rs.getString("role")));
        customer.setRegistrationDate(toLocalDate(rs.getDate("registration_date")));
        customer.setActive(rs.getBoolean("is_active"));
        customer.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        customer.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return customer;
    }
}
