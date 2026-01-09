package com.syos.repository.impl;

import com.syos.domain.models.PhysicalStoreInventory;
import com.syos.domain.valueobjects.ProductCode;
import com.syos.repository.interfaces.PhysicalStoreInventoryRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of PhysicalStoreInventoryRepository using JDBC.
 */
public class PhysicalStoreInventoryRepositoryImpl extends BaseRepository implements PhysicalStoreInventoryRepository {

    public PhysicalStoreInventoryRepositoryImpl() {
        super();
    }

    public PhysicalStoreInventoryRepositoryImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public PhysicalStoreInventory save(PhysicalStoreInventory inventory) {
        if (inventory.getPhysicalInventoryId() != null && existsById(inventory.getPhysicalInventoryId())) {
            return update(inventory);
        } else {
            return insert(inventory);
        }
    }

    private PhysicalStoreInventory insert(PhysicalStoreInventory inventory) {
        String sql = """
            INSERT INTO physical_store_inventory (product_code, main_inventory_id, quantity_on_shelf, restocked_date)
            VALUES (?, ?, ?, ?)
            """;

        Integer id = executeInsertAndGetId(sql,
            inventory.getProductCodeString(),
            inventory.getMainInventoryId(),
            inventory.getQuantityOnShelf(),
            inventory.getRestockedDate() != null ? inventory.getRestockedDate() : LocalDate.now()
        );
        inventory.setPhysicalInventoryId(id);
        return inventory;
    }

    private PhysicalStoreInventory update(PhysicalStoreInventory inventory) {
        String sql = """
            UPDATE physical_store_inventory SET quantity_on_shelf = ?, restocked_date = ?
            WHERE physical_store_inventory_id = ?
            """;

        executeUpdate(sql,
            inventory.getQuantityOnShelf(),
            inventory.getRestockedDate(),
            inventory.getPhysicalInventoryId()
        );
        return inventory;
    }

    @Override
    public Optional<PhysicalStoreInventory> findById(Integer id) {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            WHERE psi.physical_store_inventory_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), id);
    }

    @Override
    public List<PhysicalStoreInventory> findByProductCode(String productCode) {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            WHERE psi.product_code = ?
            ORDER BY mi.expiry_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public Optional<PhysicalStoreInventory> findByProductCodeAndBatchId(String productCode, Integer batchId) {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            WHERE psi.product_code = ? AND psi.main_inventory_id = ?
            """;
        return executeQuery(sql, rs -> mapToOptional(rs, this::mapRow), productCode, batchId);
    }

    @Override
    public List<PhysicalStoreInventory> findAvailableByProductCode(String productCode) {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            WHERE psi.product_code = ? AND psi.quantity_on_shelf > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), productCode);
    }

    @Override
    public int getTotalQuantityOnShelf(String productCode) {
        String sql = "SELECT COALESCE(SUM(quantity_on_shelf), 0) FROM physical_store_inventory WHERE product_code = ?";
        return executeQuery(sql, rs -> {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }, productCode);
    }

    @Override
    public boolean reduceQuantity(String productCode, Integer batchId, int amount) {
        String sql = """
            UPDATE physical_store_inventory
            SET quantity_on_shelf = quantity_on_shelf - ?
            WHERE product_code = ? AND main_inventory_id = ? AND quantity_on_shelf >= ?
            """;
        return executeUpdate(sql, amount, productCode, batchId, amount) > 0;
    }

    @Override
    public boolean addQuantity(String productCode, Integer batchId, int amount) {
        // First check if record exists
        Optional<PhysicalStoreInventory> existing = findByProductCodeAndBatchId(productCode, batchId);

        if (existing.isPresent()) {
            String sql = """
                UPDATE physical_store_inventory
                SET quantity_on_shelf = quantity_on_shelf + ?, restocked_date = CURDATE()
                WHERE product_code = ? AND main_inventory_id = ?
                """;
            return executeUpdate(sql, amount, productCode, batchId) > 0;
        } else {
            // Insert new record
            String sql = """
                INSERT INTO physical_store_inventory (product_code, main_inventory_id, quantity_on_shelf, restocked_date)
                VALUES (?, ?, ?, CURDATE())
                """;
            return executeUpdate(sql, productCode, batchId, amount) > 0;
        }
    }

    @Override
    public List<PhysicalStoreInventory> findLowStock(int threshold) {
        String sql = """
            SELECT psi.product_code, p.product_name, SUM(psi.quantity_on_shelf) as total_qty
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            WHERE p.is_active = TRUE
            GROUP BY psi.product_code, p.product_name
            HAVING total_qty < ?
            ORDER BY total_qty ASC
            """;

        return executeQuery(sql, rs -> {
            List<PhysicalStoreInventory> results = new ArrayList<>();
            while (rs.next()) {
                PhysicalStoreInventory inv = new PhysicalStoreInventory();
                inv.setProductCode(new ProductCode(rs.getString("product_code")));
                inv.setProductName(rs.getString("product_name"));
                inv.setQuantityOnShelf(rs.getInt("total_qty"));
                results.add(inv);
            }
            return results;
        }, threshold);
    }

    @Override
    public List<ProductStockSummary> getStockSummary() {
        String sql = """
            SELECT p.product_code, p.product_name,
                   COALESCE(SUM(psi.quantity_on_shelf), 0) as total_qty,
                   COUNT(DISTINCT psi.main_inventory_id) as batch_count
            FROM product p
            LEFT JOIN physical_store_inventory psi ON p.product_code = psi.product_code
            WHERE p.is_active = TRUE
            GROUP BY p.product_code, p.product_name
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
    public List<PhysicalStoreInventory> findAll() {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            ORDER BY psi.product_code, mi.expiry_date
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow));
    }

    @Override
    public List<PhysicalStoreInventory> findAll(int offset, int limit) {
        String sql = """
            SELECT psi.*, p.product_name, mi.expiry_date
            FROM physical_store_inventory psi
            JOIN product p ON psi.product_code = p.product_code
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            ORDER BY psi.product_code, mi.expiry_date
            LIMIT ? OFFSET ?
            """;
        return executeQuery(sql, rs -> mapToList(rs, this::mapRow), limit, offset);
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM physical_store_inventory WHERE physical_store_inventory_id = ?";
        return executeUpdate(sql, id) > 0;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM physical_store_inventory WHERE physical_store_inventory_id = ?";
        return executeQuery(sql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM physical_store_inventory";
        return executeQuery(sql, rs -> rs.next() ? rs.getLong(1) : 0L);
    }

    private PhysicalStoreInventory mapRow(ResultSet rs) throws SQLException {
        PhysicalStoreInventory inventory = new PhysicalStoreInventory();
        inventory.setPhysicalInventoryId(rs.getInt("physical_store_inventory_id"));
        inventory.setProductCode(new ProductCode(rs.getString("product_code")));
        inventory.setMainInventoryId(rs.getInt("main_inventory_id"));
        inventory.setQuantityOnShelf(rs.getInt("quantity_on_shelf"));
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
