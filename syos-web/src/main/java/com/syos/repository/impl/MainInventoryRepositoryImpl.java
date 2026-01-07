package com.syos.repository.impl;

import com.syos.domain.models.MainInventory;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.MainInventoryRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of MainInventoryRepository using JDBC.
 */
public class MainInventoryRepositoryImpl extends BaseRepository implements MainInventoryRepository {

    public MainInventoryRepositoryImpl() {
        super();
    }

    public MainInventoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public MainInventory save(MainInventory inventory) {
        if (inventory.getMainInventoryId() != null && existsById(inventory.getMainInventoryId())) {
            return update(inventory);
        } else {
            return insert(inventory);
        }
    }

    private MainInventory insert(MainInventory inventory) {
        String sql = """
            INSERT INTO main_inventory (product_code, quantity_received, purchase_price,
                purchase_date, expiry_date, supplier_name, remaining_quantity)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            inventory.getProductCodeString(),
            inventory.getQuantityReceived(),
            inventory.getPurchasePrice().getAmount(),
            inventory.getPurchaseDate(),
            inventory.getExpiryDate(),
            inventory.getSupplierName(),
            inventory.getRemainingQuantity()
        );
        inventory.setMainInventoryId(id);
        return inventory;
    }

    private MainInventory update(MainInventory inventory) {
        String sql = """
            UPDATE main_inventory SET quantity_received = ?, purchase_price = ?,
                purchase_date = ?, expiry_date = ?, supplier_name = ?, remaining_quantity = ?
            WHERE main_inventory_id = ?
            """;

        executeUpdate(sql,
            inventory.getQuantityReceived(),
            inventory.getPurchasePrice().getAmount(),
            inventory.getPurchaseDate(),
            inventory.getExpiryDate(),
            inventory.getSupplierName(),
            inventory.getRemainingQuantity(),
            inventory.getMainInventoryId()
        );
        return inventory;
    }

    @Override
    public Optional<MainInventory> findById(Integer id) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.main_inventory_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<MainInventory> findByProductCode(String productCode) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.product_code = ?
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public List<MainInventory> findAvailableBatchesByProductCode(String productCode) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.product_code = ? AND mi.remaining_quantity > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public Optional<MainInventory> findNextBatchForSale(String productCode, int requiredQuantity) {
        // First try to find a single batch that can fulfill the entire quantity
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.product_code = ? AND mi.remaining_quantity >= ?
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            LIMIT 1
            """;

        Optional<MainInventory> result = executeQuery(sql, rs -> mapToOptional(rs, this::mapRow),
            productCode, requiredQuantity);

        if (result.isPresent()) {
            return result;
        }

        // If no single batch can fulfill, return the batch with earliest expiry
        String fallbackSql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.product_code = ? AND mi.remaining_quantity > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            LIMIT 1
            """;

        return executeQuery(fallbackSql, rs -> mapToOptional(rs, this::mapRow), productCode);
    }

    @Override
    public List<MainInventory> findExpiringWithinDays(int days) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.remaining_quantity > 0
                AND mi.expiry_date IS NOT NULL
                AND mi.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
                AND mi.expiry_date >= CURDATE()
            ORDER BY mi.expiry_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), days);
    }

    @Override
    public List<MainInventory> findExpiredBatches() {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.remaining_quantity > 0
                AND mi.expiry_date IS NOT NULL
                AND mi.expiry_date < CURDATE()
            ORDER BY mi.expiry_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<MainInventory> findBySupplier(String supplierName) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.supplier_name LIKE ?
            ORDER BY mi.purchase_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), "%" + supplierName + "%");
    }

    @Override
    public int getTotalRemainingQuantity(String productCode) {
        String sql = "SELECT COALESCE(SUM(remaining_quantity), 0) FROM main_inventory WHERE product_code = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode);
    }

    @Override
    public boolean reduceQuantity(Integer batchId, int amount) {
        String sql = """
            UPDATE main_inventory
            SET remaining_quantity = remaining_quantity - ?
            WHERE main_inventory_id = ? AND remaining_quantity >= ?
            """;
        return executeUpdate(sql, amount, batchId, amount) > 0;
    }

    @Override
    public boolean increaseQuantity(Integer batchId, int amount) {
        String sql = """
            UPDATE main_inventory
            SET remaining_quantity = remaining_quantity + ?
            WHERE main_inventory_id = ?
            """;
        return executeUpdate(sql, amount, batchId) > 0;
    }

    @Override
    public List<MainInventory> findByPurchaseDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            WHERE mi.purchase_date BETWEEN ? AND ?
            ORDER BY mi.purchase_date DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), startDate, endDate);
    }

    @Override
    public List<MainInventory> findAll() {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            ORDER BY mi.main_inventory_id DESC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<MainInventory> findAll(int offset, int limit) {
        String sql = """
            SELECT mi.*, p.product_name
            FROM main_inventory mi
            JOIN product p ON mi.product_code = p.product_code
            ORDER BY mi.main_inventory_id DESC
            LIMIT ? OFFSET ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM main_inventory WHERE main_inventory_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM main_inventory WHERE main_inventory_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM main_inventory";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private MainInventory mapRow(ResultSet rs) throws SQLException {
        MainInventory inventory = new MainInventory();
        inventory.setMainInventoryId(rs.getInt("main_inventory_id"));
        inventory.setProductCode(new ProductCode(rs.getString("product_code")));
        inventory.setQuantityReceived(rs.getInt("quantity_received"));
        inventory.setPurchasePrice(new Money(rs.getBigDecimal("purchase_price")));
        inventory.setPurchaseDate(toLocalDate(rs.getDate("purchase_date")));
        inventory.setExpiryDate(toLocalDate(rs.getDate("expiry_date")));
        inventory.setSupplierName(rs.getString("supplier_name"));
        inventory.setRemainingQuantity(rs.getInt("remaining_quantity"));
        inventory.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        inventory.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        try {
            inventory.setProductName(rs.getString("product_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        return inventory;
    }
}
