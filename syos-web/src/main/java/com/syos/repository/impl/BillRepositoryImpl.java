package com.syos.repository.impl;

import com.syos.domain.enums.StoreType;
import com.syos.domain.enums.TransactionType;
import com.syos.domain.models.Bill;
import com.syos.domain.valueobjects.BillSerialNumber;
import com.syos.domain.valueobjects.Money;
import com.syos.repository.interfaces.BillRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BillRepository using JDBC.
 */
public class BillRepositoryImpl extends BaseRepository implements BillRepository {

    public BillRepositoryImpl() {
        super();
    }

    public BillRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Bill save(Bill bill) {
        if (bill.getBillId() != null && existsById(bill.getBillId())) {
            return update(bill);
        } else {
            return insert(bill);
        }
    }

    private Bill insert(Bill bill) {
        String sql = """
            INSERT INTO bill (serial_number, customer_id, store_type, transaction_type,
                total_amount, discount_amount, tax_amount, tendered_amount, change_amount, cashier_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            bill.getSerialNumberString(),
            bill.getCustomerId(),
            bill.getStoreType().name(),
            bill.getTransactionType().name(),
            bill.getTotalAmount().getAmount(),
            bill.getDiscountAmount() != null ? bill.getDiscountAmount().getAmount() : BigDecimal.ZERO,
            bill.getTaxAmount() != null ? bill.getTaxAmount().getAmount() : BigDecimal.ZERO,
            bill.getTenderedAmount() != null ? bill.getTenderedAmount().getAmount() : null,
            bill.getChangeAmount() != null ? bill.getChangeAmount().getAmount() : null,
            bill.getCashierId()
        );
        bill.setBillId(id);
        return bill;
    }

    private Bill update(Bill bill) {
        String sql = """
            UPDATE bill SET total_amount = ?, discount_amount = ?, tax_amount = ?,
                tendered_amount = ?, change_amount = ?
            WHERE bill_id = ?
            """;

        executeUpdate(sql,
            bill.getTotalAmount().getAmount(),
            bill.getDiscountAmount() != null ? bill.getDiscountAmount().getAmount() : BigDecimal.ZERO,
            bill.getTaxAmount() != null ? bill.getTaxAmount().getAmount() : BigDecimal.ZERO,
            bill.getTenderedAmount() != null ? bill.getTenderedAmount().getAmount() : null,
            bill.getChangeAmount() != null ? bill.getChangeAmount().getAmount() : null,
            bill.getBillId()
        );
        return bill;
    }

    @Override
    public Optional<Bill> findById(Integer id) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.bill_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public Optional<Bill> findBySerialNumber(String serialNumber) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.serial_number = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), serialNumber);
    }

    @Override
    public List<Bill> findByDate(LocalDate date) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE DATE(b.bill_date) = ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), date);
    }

    @Override
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE DATE(b.bill_date) BETWEEN ? AND ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), startDate, endDate);
    }

    @Override
    public List<Bill> findByStoreType(StoreType storeType) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.store_type = ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), storeType.name());
    }

    @Override
    public List<Bill> findByTransactionType(TransactionType transactionType) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.transaction_type = ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), transactionType.name());
    }

    @Override
    public List<Bill> findByCustomerId(Integer customerId) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.customer_id = ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), customerId);
    }

    @Override
    public List<Bill> findByCashierId(String cashierId) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.cashier_id = ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), cashierId);
    }

    @Override
    public List<Bill> findRecent(int limit) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            ORDER BY b.bill_date DESC
            LIMIT ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit);
    }

    @Override
    public BigDecimal getTotalSalesForDate(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bill WHERE DATE(bill_date) = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getBigDecimal(1);
            return BigDecimal.ZERO;
        }, date);
    }

    @Override
    public BigDecimal getTotalSalesForDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bill WHERE DATE(bill_date) BETWEEN ? AND ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getBigDecimal(1);
            return BigDecimal.ZERO;
        }, startDate, endDate);
    }

    @Override
    public List<DailySalesSummary> getDailySalesSummary(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT DATE(bill_date) as sale_date,
                   COUNT(*) as bill_count,
                   SUM(total_amount) as total_amount,
                   SUM(CASE WHEN transaction_type = 'CASH' THEN total_amount ELSE 0 END) as cash_amount,
                   SUM(CASE WHEN transaction_type = 'ONLINE' THEN total_amount ELSE 0 END) as online_amount
            FROM bill
            WHERE DATE(bill_date) BETWEEN ? AND ?
            GROUP BY DATE(bill_date)
            ORDER BY sale_date DESC
            """;

        return executeQuery(sql, rs -> {
            List<DailySalesSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new DailySalesSummary(
                    rs.getDate("sale_date").toLocalDate(),
                    rs.getInt("bill_count"),
                    rs.getBigDecimal("total_amount"),
                    rs.getBigDecimal("cash_amount"),
                    rs.getBigDecimal("online_amount")
                ));
            }
            return results;
        }, startDate, endDate);
    }

    @Override
    public List<StoreTypeSalesSummary> getSalesByStoreType(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT store_type, COUNT(*) as bill_count, COALESCE(SUM(total_amount), 0) as total_amount
            FROM bill
            WHERE DATE(bill_date) BETWEEN ? AND ?
            GROUP BY store_type
            ORDER BY store_type
            """;

        return executeQuery(sql, rs -> {
            List<StoreTypeSalesSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new StoreTypeSalesSummary(
                    StoreType.valueOf(rs.getString("store_type")),
                    rs.getInt("bill_count"),
                    rs.getBigDecimal("total_amount")
                ));
            }
            return results;
        }, startDate, endDate);
    }

    @Override
    public int getBillCountForDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM bill WHERE DATE(bill_date) = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, date);
    }

    @Override
    public String generateNextSerialNumber(StoreType storeType) {
        String prefix = storeType == StoreType.PHYSICAL ? "POS" : "ONL";
        String datePrefix = LocalDate.now().toString().replace("-", "");

        String sql = """
            SELECT serial_number FROM bill
            WHERE serial_number LIKE ?
            ORDER BY serial_number DESC
            LIMIT 1
            """;

        String pattern = prefix + "-" + datePrefix + "-%";

        String lastSerial = executeQuery(sql, rs -> {
            if (rs.next()) return rs.getString(1);
            return null;
        }, pattern);

        int nextNumber = 1;
        if (lastSerial != null) {
            String[] parts = lastSerial.split("-");
            if (parts.length == 3) {
                nextNumber = Integer.parseInt(parts[2]) + 1;
            }
        }

        return String.format("%s-%s-%04d", prefix, datePrefix, nextNumber);
    }

    @Override
    public List<Bill> findByStoreTypeAndDateRange(StoreType storeType, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            WHERE b.store_type = ? AND DATE(b.bill_date) BETWEEN ? AND ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), storeType.name(), startDate, endDate);
    }

    @Override
    public List<Bill> findAll() {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<Bill> findAll(int offset, int limit) {
        String sql = """
            SELECT b.*, c.customer_name, c.email as customer_email
            FROM bill b
            LEFT JOIN customer c ON b.customer_id = c.customer_id
            ORDER BY b.bill_date DESC
            LIMIT ? OFFSET ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM bill WHERE bill_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM bill WHERE bill_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM bill";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setSerialNumber(new BillSerialNumber(rs.getString("serial_number")));

        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            bill.setCustomerId(customerId);
        }

        bill.setStoreType(StoreType.valueOf(rs.getString("store_type")));
        bill.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        bill.setTotalAmount(new Money(rs.getBigDecimal("total_amount")));

        BigDecimal discount = rs.getBigDecimal("discount_amount");
        if (discount != null) {
            bill.setDiscountAmount(new Money(discount));
        }

        BigDecimal tax = rs.getBigDecimal("tax_amount");
        if (tax != null) {
            bill.setTaxAmount(new Money(tax));
        }

        BigDecimal tendered = rs.getBigDecimal("tendered_amount");
        if (tendered != null) {
            bill.setTenderedAmount(new Money(tendered));
        }

        BigDecimal change = rs.getBigDecimal("change_amount");
        if (change != null) {
            bill.setChangeAmount(new Money(change));
        }

        bill.setCashierId(rs.getString("cashier_id"));
        bill.setBillDate(toLocalDateTime(rs.getTimestamp("bill_date")));
        bill.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        bill.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        try {
            bill.setCustomerName(rs.getString("customer_name"));
            bill.setCustomerEmail(rs.getString("customer_email"));
        } catch (SQLException e) {
            // Columns not in result set
        }

        return bill;
    }
}
