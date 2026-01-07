package com.syos.repository.impl;

import com.syos.domain.models.BillItem;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.BillItemRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BillItemRepository using JDBC.
 */
public class BillItemRepositoryImpl extends BaseRepository implements BillItemRepository {

    public BillItemRepositoryImpl() {
        super();
    }

    public BillItemRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public BillItem save(BillItem item) {
        if (item.getBillItemId() != null && existsById(item.getBillItemId())) {
            return update(item);
        } else {
            return insert(item);
        }
    }

    private BillItem insert(BillItem item) {
        String sql = """
            INSERT INTO bill_item (bill_id, product_code, main_inventory_id, quantity, unit_price, line_total)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            item.getBillId(),
            item.getProductCodeString(),
            item.getMainInventoryId(),
            item.getQuantity(),
            item.getUnitPrice().getAmount(),
            item.getLineTotal().getAmount()
        );
        item.setBillItemId(id);
        return item;
    }

    private BillItem update(BillItem item) {
        String sql = """
            UPDATE bill_item SET quantity = ?, unit_price = ?, line_total = ?
            WHERE bill_item_id = ?
            """;

        executeUpdate(sql,
            item.getQuantity(),
            item.getUnitPrice().getAmount(),
            item.getLineTotal().getAmount(),
            item.getBillItemId()
        );
        return item;
    }

    @Override
    public Optional<BillItem> findById(Integer id) {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            WHERE bi.bill_item_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<BillItem> findByBillId(Integer billId) {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            WHERE bi.bill_id = ?
            ORDER BY bi.bill_item_id
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), billId);
    }

    @Override
    public List<BillItem> findByProductCode(String productCode) {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            WHERE bi.product_code = ?
            ORDER BY bi.created_at DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public List<BillItem> findByProductCodeAndDateRange(String productCode, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE bi.product_code = ? AND DATE(b.bill_date) BETWEEN ? AND ?
            ORDER BY b.bill_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode, startDate, endDate);
    }

    @Override
    public int getTotalQuantitySoldForDate(String productCode, LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(bi.quantity), 0)
            FROM bill_item bi
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE bi.product_code = ? AND DATE(b.bill_date) = ?
            """;
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode, date);
    }

    @Override
    public int getTotalQuantitySoldForDateRange(String productCode, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(bi.quantity), 0)
            FROM bill_item bi
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE bi.product_code = ? AND DATE(b.bill_date) BETWEEN ? AND ?
            """;
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalRevenueForProduct(String productCode, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(bi.line_total), 0)
            FROM bill_item bi
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE bi.product_code = ? AND DATE(b.bill_date) BETWEEN ? AND ?
            """;
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getBigDecimal(1);
            return BigDecimal.ZERO;
        }, productCode, startDate, endDate);
    }

    @Override
    public List<ProductSalesSummary> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
            SELECT bi.product_code, p.product_name,
                   SUM(bi.quantity) as total_quantity,
                   SUM(bi.line_total) as total_revenue
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE DATE(b.bill_date) BETWEEN ? AND ?
            GROUP BY bi.product_code, p.product_name
            ORDER BY total_quantity DESC
            LIMIT ?
            """;

        return executeQuery(sql, rs -> {
            List<ProductSalesSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new ProductSalesSummary(
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getInt("total_quantity"),
                    rs.getBigDecimal("total_revenue")
                ));
            }
            return results;
        }, startDate, endDate, limit);
    }

    @Override
    public List<ProductSalesSummary> getProductSalesSummary(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT bi.product_code, p.product_name,
                   SUM(bi.quantity) as total_quantity,
                   SUM(bi.line_total) as total_revenue
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            JOIN bill b ON bi.bill_id = b.bill_id
            WHERE DATE(b.bill_date) BETWEEN ? AND ?
            GROUP BY bi.product_code, p.product_name
            ORDER BY p.product_name
            """;

        return executeQuery(sql, rs -> {
            List<ProductSalesSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new ProductSalesSummary(
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getInt("total_quantity"),
                    rs.getBigDecimal("total_revenue")
                ));
            }
            return results;
        }, startDate, endDate);
    }

    @Override
    public int deleteByBillId(Integer billId) {
        String sql = "DELETE FROM bill_item WHERE bill_id = ?";
        return executeUpdate(sql, billId);
    }

    @Override
    public List<BillItem> saveAll(List<BillItem> items) {
        List<BillItem> savedItems = new ArrayList<>();
        for (BillItem item : items) {
            savedItems.add(save(item));
        }
        return savedItems;
    }

    @Override
    public List<BillItem> findAll() {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            ORDER BY bi.bill_item_id DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<BillItem> findAll(int offset, int limit) {
        String sql = """
            SELECT bi.*, p.product_name
            FROM bill_item bi
            JOIN product p ON bi.product_code = p.product_code
            ORDER BY bi.bill_item_id DESC
            LIMIT ? OFFSET ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM bill_item WHERE bill_item_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM bill_item WHERE bill_item_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM bill_item";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private BillItem mapRow(ResultSet rs) throws SQLException {
        BillItem item = new BillItem();
        item.setBillItemId(rs.getInt("bill_item_id"));
        item.setBillId(rs.getInt("bill_id"));
        item.setProductCode(new ProductCode(rs.getString("product_code")));

        int mainInventoryId = rs.getInt("main_inventory_id");
        if (!rs.wasNull()) {
            item.setMainInventoryId(mainInventoryId);
        }

        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(new Money(rs.getBigDecimal("unit_price")));
        item.setLineTotal(new Money(rs.getBigDecimal("line_total")));
        item.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));

        try {
            item.setProductName(rs.getString("product_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        return item;
    }
}
