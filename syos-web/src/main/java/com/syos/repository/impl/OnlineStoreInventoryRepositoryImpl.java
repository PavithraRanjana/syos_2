package com.syos.repository.impl;

import com.syos.domain.models.OnlineStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.OnlineStoreInventoryRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OnlineStoreInventoryRepository using JDBC.
 */
public class OnlineStoreInventoryRepositoryImpl extends BaseRepository implements OnlineStoreInventoryRepository {

    public OnlineStoreInventoryRepositoryImpl() {
        super();
    }

    public OnlineStoreInventoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public OnlineStoreInventory save(OnlineStoreInventory inventory) {
        if (inventory.getOnlineInventoryId() != null && existsById(inventory.getOnlineInventoryId())) {
            return update(inventory);
        } else {
            return insert(inventory);
        }
    }

    private OnlineStoreInventory insert(OnlineStoreInventory inventory) {
        String sql = """
            INSERT INTO online_store_inventory (product_code, main_inventory_id, quantity_available, restocked_date)
            VALUES (?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            inventory.getProductCodeString(),
            inventory.getMainInventoryId(),
            inventory.getQuantityAvailable(),
            inventory.getRestockedDate() != null ? inventory.getRestockedDate() : LocalDate.now()
        );
        inventory.setOnlineInventoryId(id);
        return inventory;
    }

    private OnlineStoreInventory update(OnlineStoreInventory inventory) {
        String sql = """
            UPDATE online_store_inventory SET quantity_available = ?, restocked_date = ?
            WHERE online_inventory_id = ?
            """;

        executeUpdate(sql,
            inventory.getQuantityAvailable(),
            inventory.getRestockedDate(),
            inventory.getOnlineInventoryId()
        );
        return inventory;
    }

    @Override
    public Optional<OnlineStoreInventory> findById(Integer id) {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            WHERE osi.online_inventory_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<OnlineStoreInventory> findByProductCode(String productCode) {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            WHERE osi.product_code = ?
            ORDER BY mi.expiry_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public Optional<OnlineStoreInventory> findByProductCodeAndBatchId(String productCode, Integer batchId) {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            WHERE osi.product_code = ? AND osi.main_inventory_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), productCode, batchId);
    }

    @Override
    public List<OnlineStoreInventory> findAvailableByProductCode(String productCode) {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            WHERE osi.product_code = ? AND osi.quantity_available > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public int getTotalQuantityAvailable(String productCode) {
        String sql = "SELECT COALESCE(SUM(quantity_available), 0) FROM online_store_inventory WHERE product_code = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode);
    }

    @Override
    public boolean reduceQuantity(String productCode, Integer batchId, int amount) {
        String sql = """
            UPDATE online_store_inventory
            SET quantity_available = quantity_available - ?
            WHERE product_code = ? AND main_inventory_id = ? AND quantity_available >= ?
            """;
        return executeUpdate(sql, amount, productCode, batchId, amount) > 0;
    }

    @Override
    public boolean addQuantity(String productCode, Integer batchId, int amount) {
        Optional<OnlineStoreInventory> existing = findByProductCodeAndBatchId(productCode, batchId);

        if (existing.isPresent()) {
            String sql = """
                UPDATE online_store_inventory
                SET quantity_available = quantity_available + ?, restocked_date = CURDATE()
                WHERE product_code = ? AND main_inventory_id = ?
                """;
            return executeUpdate(sql, amount, productCode, batchId) > 0;
        } else {
            String sql = """
                INSERT INTO online_store_inventory (product_code, main_inventory_id, quantity_available, restocked_date)
                VALUES (?, ?, ?, CURDATE())
                """;
            return executeUpdate(sql, productCode, batchId, amount) > 0;
        }
    }

    @Override
    public List<OnlineStoreInventory> findLowStock(int threshold) {
        String sql = """
            SELECT osi.product_code, p.product_name, SUM(osi.quantity_available) as total_qty
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            WHERE p.is_active = TRUE
            GROUP BY osi.product_code, p.product_name
            HAVING total_qty < ?
            ORDER BY total_qty ASC
            """;

        return executeQuery(sql, rs -> {
            List<OnlineStoreInventory> results = new ArrayList<>();
            while (rs.next()) {
                OnlineStoreInventory inv = new OnlineStoreInventory();
                inv.setProductCode(new ProductCode(rs.getString("product_code")));
                inv.setProductName(rs.getString("product_name"));
                inv.setQuantityAvailable(rs.getInt("total_qty"));
                results.add(inv);
            }
            return results;
        }, threshold);
    }

    @Override
    public List<ProductStockSummary> getStockSummary() {
        String sql = """
            SELECT osi.product_code, p.product_name,
                   COALESCE(SUM(osi.quantity_available), 0) as total_qty,
                   COUNT(DISTINCT osi.main_inventory_id) as batch_count
            FROM product p
            LEFT JOIN online_store_inventory osi ON p.product_code = osi.product_code
            WHERE p.is_active = TRUE
            GROUP BY osi.product_code, p.product_name
            ORDER BY p.product_name
            """;

        return executeQuery(sql, rs -> {
            List<ProductStockSummary> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new ProductStockSummary(
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getInt("total_qty"),
                    rs.getInt("batch_count")
                ));
            }
            return results;
        });
    }

    @Override
    public List<OnlineStoreInventory> findAll() {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            ORDER BY osi.product_code, mi.expiry_date
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<OnlineStoreInventory> findAll(int offset, int limit) {
        String sql = """
            SELECT osi.*, p.product_name, mi.expiry_date
            FROM online_store_inventory osi
            JOIN product p ON osi.product_code = p.product_code
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            ORDER BY osi.product_code, mi.expiry_date
            LIMIT ? OFFSET ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM online_store_inventory WHERE online_inventory_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM online_store_inventory WHERE online_inventory_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM online_store_inventory";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private OnlineStoreInventory mapRow(ResultSet rs) throws SQLException {
        OnlineStoreInventory inventory = new OnlineStoreInventory();
        inventory.setOnlineInventoryId(rs.getInt("online_inventory_id"));
        inventory.setProductCode(new ProductCode(rs.getString("product_code")));
        inventory.setMainInventoryId(rs.getInt("main_inventory_id"));
        inventory.setQuantityAvailable(rs.getInt("quantity_available"));
        inventory.setRestockedDate(toLocalDate(rs.getDate("restocked_date")));
        inventory.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        inventory.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));

        try {
            inventory.setProductName(rs.getString("product_name"));
            inventory.setExpiryDate(toLocalDate(rs.getDate("expiry_date")));
        } catch (SQLException e) {
            // Columns not in result set
        }

        return inventory;
    }
}
