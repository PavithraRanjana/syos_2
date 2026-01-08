package com.syos.repository.impl;

import com.syos.domain.enums.InventoryTransactionType;
import com.syos.domain.enums.StoreType;
import com.syos.domain.models.InventoryTransaction;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.InventoryTransactionRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of InventoryTransactionRepository using JDBC.
 */
public class InventoryTransactionRepositoryImpl extends BaseRepository implements InventoryTransactionRepository {

    public InventoryTransactionRepositoryImpl() {
        super();
    }

    public InventoryTransactionRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        if (transaction.getTransactionId() != null && existsById(transaction.getTransactionId())) {
            return update(transaction);
        } else {
            return insert(transaction);
        }
    }

    private InventoryTransaction insert(InventoryTransaction transaction) {
        String sql = """
            INSERT INTO inventory_transaction (product_code, main_inventory_id, transaction_type,
                store_type, quantity_changed, bill_id, remarks, transaction_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            transaction.getProductCodeString(),
            transaction.getMainInventoryId(),
            transaction.getTransactionType().name(),
            transaction.getStoreType() != null ? transaction.getStoreType().name() : null,
            transaction.getQuantityChanged(),
            transaction.getBillId(),
            transaction.getRemarks(),
            transaction.getTransactionDate() != null ? transaction.getTransactionDate() : LocalDateTime.now()
        );
        transaction.setTransactionId(id);
        return transaction;
    }

    private InventoryTransaction update(InventoryTransaction transaction) {
        String sql = """
            UPDATE inventory_transaction SET remarks = ?
            WHERE transaction_id = ?
            """;

        executeUpdate(sql,
            transaction.getRemarks(),
            transaction.getTransactionId()
        );
        return transaction;
    }

    @Override
    public Optional<InventoryTransaction> findById(Integer id) {
        String sql = "SELECT * FROM inventory_transaction WHERE transaction_id = ?";
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<InventoryTransaction> findByProductCode(String productCode) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE product_code = ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public List<InventoryTransaction> findByMainInventoryId(Integer mainInventoryId) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE main_inventory_id = ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), mainInventoryId);
    }

    @Override
    public List<InventoryTransaction> findByTransactionType(InventoryTransactionType transactionType) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE transaction_type = ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), transactionType.name());
    }

    @Override
    public List<InventoryTransaction> findByStoreType(StoreType storeType) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE store_type = ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), storeType.name());
    }

    @Override
    public List<InventoryTransaction> findByBillId(Integer billId) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE bill_id = ?
            ORDER BY transaction_date
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), billId);
    }

    @Override
    public List<InventoryTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE transaction_date BETWEEN ? AND ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), startDate, endDate);
    }

    @Override
    public List<InventoryTransaction> findByProductCodeAndDateRange(String productCode, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT * FROM inventory_transaction
            WHERE product_code = ? AND transaction_date BETWEEN ? AND ?
            ORDER BY transaction_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode, startDate, endDate);
    }

    @Override
    public int getTotalQuantityChange(String productCode, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(quantity_changed), 0)
            FROM inventory_transaction
            WHERE product_code = ? AND transaction_date BETWEEN ? AND ?
            """;
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode, startDate, endDate);
    }

    @Override
    public List<TransactionTypeSummary> getSummaryByType(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT transaction_type,
                   COUNT(*) as transaction_count,
                   COALESCE(SUM(quantity_changed), 0) as total_quantity
            FROM inventory_transaction
            WHERE transaction_date BETWEEN ? AND ?
            GROUP BY transaction_type
            ORDER BY transaction_type
            """;

        return executeQuery(sql, rs -> {
            List<TransactionTypeSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new TransactionTypeSummary(
                    InventoryTransactionType.valueOf(rs.getString("transaction_type")),
                    rs.getInt("transaction_count"),
                    rs.getInt("total_quantity")
                ));
            }
            return results;
        }, startDate, endDate);
    }

    @Override
    public List<InventoryTransaction> findRecent(int limit) {
        String sql = """
            SELECT * FROM inventory_transaction
            ORDER BY transaction_date DESC
            LIMIT ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit);
    }

    @Override
    public List<DailyTransactionSummary> getDailySummary(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT DATE(transaction_date) as trans_date,
                   COUNT(*) as transaction_count,
                   COALESCE(SUM(CASE WHEN transaction_type = 'SALE' THEN ABS(quantity_changed) ELSE 0 END), 0) as sales_qty,
                   COALESCE(SUM(CASE WHEN transaction_type IN ('RESTOCK_PHYSICAL', 'RESTOCK_ONLINE') THEN quantity_changed ELSE 0 END), 0) as restock_qty
            FROM inventory_transaction
            WHERE DATE(transaction_date) BETWEEN ? AND ?
            GROUP BY DATE(transaction_date)
            ORDER BY trans_date DESC
            """;

        return executeQuery(sql, rs -> {
            List<DailyTransactionSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new DailyTransactionSummary(
                    rs.getDate("trans_date").toLocalDate(),
                    rs.getInt("transaction_count"),
                    rs.getInt("sales_qty"),
                    rs.getInt("restock_qty")
                ));
            }
            return results;
        }, startDate, endDate);
    }

    @Override
    public List<InventoryTransaction> findAll() {
        String sql = "SELECT * FROM inventory_transaction ORDER BY transaction_date DESC";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<InventoryTransaction> findAll(int offset, int limit) {
        String sql = "SELECT * FROM inventory_transaction ORDER BY transaction_date DESC LIMIT ? OFFSET ?";
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM inventory_transaction WHERE transaction_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM inventory_transaction WHERE transaction_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM inventory_transaction";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setProductCode(new ProductCode(rs.getString("product_code")));

        int mainInventoryId = rs.getInt("main_inventory_id");
        if (!rs.wasNull()) {
            transaction.setMainInventoryId(mainInventoryId);
        }

        transaction.setTransactionType(InventoryTransactionType.valueOf(rs.getString("transaction_type")));

        String storeTypeStr = rs.getString("store_type");
        if (storeTypeStr != null) {
            transaction.setStoreType(StoreType.valueOf(storeTypeStr));
        }

        transaction.setQuantityChanged(rs.getInt("quantity_changed"));

        int billId = rs.getInt("bill_id");
        if (!rs.wasNull()) {
            transaction.setBillId(billId);
        }

        transaction.setTransactionDate(toLocalDateTime(rs.getTimestamp("transaction_date")));
        transaction.setRemarks(rs.getString("remarks"));

        return transaction;
    }
}
