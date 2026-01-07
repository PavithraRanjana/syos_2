-- SYOS Grocery Store Database Schema - REVISED VERSION
-- MySQL Database Creation Script with Hierarchical Categories and Simple Product Codes

-- Create Database
CREATE DATABASE IF NOT EXISTS syos_grocery_store;
USE syos_grocery_store;

-- =============================================
-- MASTER DATA TABLES - HIERARCHICAL STRUCTURE
-- =============================================

-- Main Categories Table
CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    category_code VARCHAR(5) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Sub-Categories Table (New hierarchical level)
CREATE TABLE subcategory (
    subcategory_id INT AUTO_INCREMENT PRIMARY KEY,
    subcategory_name VARCHAR(100) NOT NULL,
    subcategory_code VARCHAR(5) NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES category(category_id),
    UNIQUE KEY uk_subcategory_per_category (category_id, subcategory_name),
    UNIQUE KEY uk_subcategory_code_per_category (category_id, subcategory_code),
    INDEX idx_category (category_id)
);

-- Brands Table (Simplified)
CREATE TABLE brand (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE,
    brand_code VARCHAR(5) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Products Table with Simple Product Code
-- New Format: [CATEGORY_CODE][SUBCATEGORY_CODE][BRAND_CODE][SEQUENCE]
-- Example: BVEDRB001 (Beverages-EnergyDrink-RedBull-001)
CREATE TABLE product (
    product_code VARCHAR(15) PRIMARY KEY,  -- Reduced from 50 to 15 characters
    product_name VARCHAR(200) NOT NULL,
    category_id INT NOT NULL,
    subcategory_id INT NOT NULL,
    brand_id INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    description TEXT,
    unit_of_measure VARCHAR(20) DEFAULT 'pcs',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (subcategory_id) REFERENCES subcategory(subcategory_id),
    FOREIGN KEY (brand_id) REFERENCES brand(brand_id),
    INDEX idx_category (category_id),
    INDEX idx_subcategory (subcategory_id),
    INDEX idx_brand (brand_id),
    INDEX idx_active (is_active)
);

-- =============================================
-- INVENTORY MANAGEMENT TABLES
-- =============================================

-- Main Inventory (Central Stock) - Batch Information
CREATE TABLE main_inventory (
    main_inventory_id INT AUTO_INCREMENT PRIMARY KEY,  -- This is the BATCH NUMBER
    product_code VARCHAR(15) NOT NULL,
    quantity_received INT NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    purchase_date DATE NOT NULL,
    expiry_date DATE,
    supplier_name VARCHAR(200),
    remaining_quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (product_code) REFERENCES product(product_code),
    INDEX idx_product (product_code),
    INDEX idx_expiry (expiry_date),
    INDEX idx_purchase_date (purchase_date),
    INDEX idx_remaining_qty (remaining_quantity)
);

-- Physical Store Inventory (Shelf Stock)
CREATE TABLE physical_store_inventory (
    physical_inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(15) NOT NULL,
    main_inventory_id INT NOT NULL,  -- BATCH NUMBER reference
    quantity_on_shelf INT NOT NULL DEFAULT 0,
    restocked_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_product (product_code),
    INDEX idx_main_inventory (main_inventory_id),
    INDEX idx_quantity (quantity_on_shelf)
);

-- Online Store Inventory
CREATE TABLE online_store_inventory (
    online_inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(15) NOT NULL,
    main_inventory_id INT NOT NULL,  -- BATCH NUMBER reference
    quantity_available INT NOT NULL DEFAULT 0,
    restocked_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_product (product_code),
    INDEX idx_main_inventory (main_inventory_id),
    INDEX idx_quantity (quantity_available)
);

-- =============================================
-- CUSTOMER MANAGEMENT
-- =============================================

-- Customer Table (for online customers)
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(200),
    email VARCHAR(200) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    registration_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_active (is_active)
);

-- =============================================
-- BILLING SYSTEM - WITH BATCH TRACKING
-- =============================================

-- Bills Table
CREATE TABLE bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_serial_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id INT NULL, -- NULL for walk-in customers (physical store)
    transaction_type ENUM('CASH', 'ONLINE') NOT NULL,
    store_type ENUM('PHYSICAL', 'ONLINE') NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    cash_tendered DECIMAL(12,2), -- Only for cash transactions
    change_amount DECIMAL(12,2), -- Only for cash transactions
    bill_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    INDEX idx_customer (customer_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_store_type (store_type),
    INDEX idx_bill_date (bill_date),
    INDEX idx_serial_number (bill_serial_number)
);

-- Bill Items Table - NOW INCLUDES BATCH TRACKING
CREATE TABLE bill_item (
    bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    product_code VARCHAR(15) NOT NULL,
    main_inventory_id INT NOT NULL,  -- BATCH NUMBER - which batch was sold
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (bill_id) REFERENCES bill(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_bill (bill_id),
    INDEX idx_product (product_code),
    INDEX idx_batch (main_inventory_id)
);

-- =============================================
-- INVENTORY TRACKING & LOGS
-- =============================================

-- Inventory Transactions Log
CREATE TABLE inventory_transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(15) NOT NULL,
    main_inventory_id INT NOT NULL,  -- BATCH NUMBER
    transaction_type ENUM('SALE', 'RESTOCK', 'ADJUSTMENT', 'EXPIRED') NOT NULL,
    store_type ENUM('PHYSICAL', 'ONLINE', 'MAIN') NOT NULL,
    quantity_changed INT NOT NULL, -- Positive for additions, negative for reductions
    bill_id INT NULL, -- Reference to bill if transaction is due to sale
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks TEXT,

    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    INDEX idx_product (product_code),
    INDEX idx_main_inventory (main_inventory_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_store_type (store_type),
    INDEX idx_transaction_date (transaction_date)
);

-- Restock Log (Track restocking from main to store inventories)
CREATE TABLE restock_log (
    restock_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(15) NOT NULL,
    main_inventory_id INT NOT NULL,  -- BATCH NUMBER
    target_store ENUM('PHYSICAL', 'ONLINE') NOT NULL,
    quantity_restocked INT NOT NULL,
    restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(100),

    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_product (product_code),
    INDEX idx_main_inventory (main_inventory_id),
    INDEX idx_target_store (target_store),
    INDEX idx_restock_date (restock_date)
);

-- =============================================
-- MASTER DATA INSERTION - NEW HIERARCHICAL STRUCTURE
-- =============================================

-- Insert Main Categories
INSERT INTO category (category_name, category_code) VALUES
('Beverages', 'BV'),
('Chocolate', 'CH'),
('Snacks', 'SN'),
('Spreads', 'SP');

-- Insert Sub-Categories
-- Beverages Sub-categories
INSERT INTO subcategory (subcategory_name, subcategory_code, category_id) VALUES
('Energy Drink', 'ED', 1),
('Tea', 'TEA', 1),
('Water', 'WAT', 1),
('Soft Drinks', 'SD', 1);

-- Chocolate Sub-categories
INSERT INTO subcategory (subcategory_name, subcategory_code, category_id) VALUES
('Dark Chocolate', 'DK', 2),
('White Chocolate', 'WH', 2),
('Milk Chocolate', 'MK', 2);

-- Snacks Sub-categories
INSERT INTO subcategory (subcategory_name, subcategory_code, category_id) VALUES
('Chips', 'CHP', 3),
('Cookies', 'COK', 3),
('Crackers', 'CRK', 3);

-- Spreads Sub-categories
INSERT INTO subcategory (subcategory_name, subcategory_code, category_id) VALUES
('Chocolate Spreads', 'CS', 4),
('Jams', 'JAM', 4),
('Savory Paste', 'SAV', 4),
('Nut Butters', 'NUT', 4);

-- Insert Brands
-- Energy Drink Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Monster', 'MON'),
('Rockstar', 'ROC'),
('Red Bull', 'RB'),
('5 Hour Energy', '5HE');

-- Tea Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Lipton', 'LIP'),
('Arizona', 'ARI'),
('Pure Leaf', 'PL'),
('Snapple', 'SNP');

-- Water Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Fiji', 'FIJI'),
('Ozarka', 'OZA');

-- Soft Drink Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Fanta', 'FAN'),
('Coca-Cola', 'CC'),
('Pepsi', 'PEP'),
('Sprite', 'SPR');

-- Chocolate Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Lindt', 'LIN'),
('Ghirardelli', 'GHI'),
('Godiva', 'GOD'),
('Hersheys', 'HER'),
('Cadbury', 'CAD'),
('Milka', 'MIL');

-- Snack Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Lays', 'LAY'),
('Pringles', 'PRI'),
('Ruffles', 'RUF'),
('Doritos', 'DOR'),
('Oreo', 'ORE'),
('Chips Ahoy', 'CHA'),
('Keebler', 'KEE'),
('Pop Corners', 'POC'),
('Goldfish', 'GOL'),
('Triscuit', 'TRI'),
('Ritz', 'RIT');

-- Spread Brands
INSERT INTO brand (brand_name, brand_code) VALUES
('Nutella', 'NUT'),
('Smuckers', 'SMU'),
('Bonne Maman', 'BON'),
('Welchs', 'WEL'),
('Marmite', 'MAR'),
('Vegemite', 'VEG'),
('Jif', 'JIF'),
('Skippy', 'SKI');

-- =============================================
-- SAMPLE PRODUCTS WITH NEW SIMPLE CODES
-- =============================================

-- Sample Products (Format: [CATEGORY][SUBCATEGORY][BRAND][SEQUENCE])
-- BVEDRB001 = Beverages-EnergyDrink-RedBull-001

INSERT INTO product (product_code, product_name, category_id, subcategory_id, brand_id, unit_price, description, unit_of_measure) VALUES
-- Energy Drinks
('BVEDRB001', 'Red Bull Energy Drink 250ml', 1, 1, 3, 250.00, 'Original Red Bull energy drink', 'can'),
('BVEDMON001', 'Monster Energy Original 473ml', 1, 1, 1, 350.00, 'Monster Original energy drink', 'can'),

-- Soft Drinks
('BVSDCC001', 'Coca-Cola 330ml', 1, 4, 14, 120.00, 'Classic Coca-Cola', 'bottle'),
('BVSDPEP001', 'Pepsi 330ml', 1, 4, 15, 115.00, 'Pepsi cola', 'bottle'),

-- Dark Chocolate
('CHDKLIN001', 'Lindt Dark Chocolate 70% 100g', 2, 5, 17, 850.00, 'Premium dark chocolate', 'bar'),
('CHDKGHI001', 'Ghirardelli Dark 72% 100g', 2, 5, 18, 780.00, 'Intense dark chocolate', 'bar'),

-- Chips
('SNCHPLAY001', 'Lays Classic Potato Chips 150g', 3, 8, 23, 280.00, 'Original salted chips', 'bag'),
('SNCHPPRI001', 'Pringles Original 165g', 3, 8, 24, 320.00, 'Stackable potato crisps', 'can'),

-- Chocolate Spreads
('SPCSNUT001', 'Nutella Hazelnut Spread 350g', 4, 11, 33, 650.00, 'Hazelnut chocolate spread', 'jar');

-- =============================================
-- SAMPLE INVENTORY DATA
-- =============================================

-- Sample Main Inventory (Batches)
INSERT INTO main_inventory (product_code, quantity_received, purchase_price, purchase_date, expiry_date, supplier_name, remaining_quantity) VALUES
-- Red Bull - Batch 1 (Batch Number will be 1)
('BVEDRB001', 500, 200.00, '2024-09-15', '2025-09-15', 'Red Bull Lanka', 500),

-- Coca Cola - Batch 2 (Batch Number will be 2)
('BVSDCC001', 1000, 95.00, '2024-09-16', '2025-06-16', 'Coca Cola Bottlers', 1000),

-- Lindt Dark Chocolate - Batch 3 (Batch Number will be 3)
('CHDKLIN001', 200, 650.00, '2024-09-17', '2026-03-17', 'Lindt Imports', 200),

-- Lays Chips - Batch 4 (Batch Number will be 4)
('SNCHPLAY001', 300, 220.00, '2024-09-18', '2025-03-18', 'PepsiCo Lanka', 300),

-- Nutella - Batch 5 (Batch Number will be 5)
('SPCSNUT001', 150, 500.00, '2024-09-19', '2026-09-19', 'Ferrero Lanka', 150);

-- Sample Physical Store Inventory
INSERT INTO physical_store_inventory (product_code, main_inventory_id, quantity_on_shelf, restocked_date) VALUES
('BVEDRB001', 1, 100, '2024-09-20'),    -- 100 Red Bull from Batch 1
('BVSDCC001', 2, 200, '2024-09-20'),    -- 200 Coca Cola from Batch 2
('CHDKLIN001', 3, 50, '2024-09-20'),    -- 50 Lindt from Batch 3
('SNCHPLAY001', 4, 80, '2024-09-20'),   -- 80 Lays from Batch 4
('SPCSNUT001', 5, 30, '2024-09-20');    -- 30 Nutella from Batch 5

-- Sample Online Store Inventory
INSERT INTO online_store_inventory (product_code, main_inventory_id, quantity_available, restocked_date) VALUES
('BVEDRB001', 1, 80, '2024-09-20'),     -- 80 Red Bull from Batch 1
('BVSDCC001', 2, 150, '2024-09-20'),    -- 150 Coca Cola from Batch 2
('CHDKLIN001', 3, 40, '2024-09-20'),    -- 40 Lindt from Batch 3
('SNCHPLAY001', 4, 60, '2024-09-20'),   -- 60 Lays from Batch 4
('SPCSNUT001', 5, 25, '2024-09-20');    -- 25 Nutella from Batch 5

-- Update main inventory remaining quantities
UPDATE main_inventory SET remaining_quantity = remaining_quantity - 180 WHERE main_inventory_id = 1; -- Red Bull: 500-100-80=320
UPDATE main_inventory SET remaining_quantity = remaining_quantity - 350 WHERE main_inventory_id = 2; -- Coca Cola: 1000-200-150=650
UPDATE main_inventory SET remaining_quantity = remaining_quantity - 90 WHERE main_inventory_id = 3;  -- Lindt: 200-50-40=110
UPDATE main_inventory SET remaining_quantity = remaining_quantity - 140 WHERE main_inventory_id = 4; -- Lays: 300-80-60=160
UPDATE main_inventory SET remaining_quantity = remaining_quantity - 55 WHERE main_inventory_id = 5;  -- Nutella: 150-30-25=95

-- =============================================
-- VIEWS FOR REPORTING - UPDATED
-- =============================================

-- View: Complete Product Catalog with Hierarchy
CREATE VIEW v_product_catalog AS
SELECT
    p.product_code,
    p.product_name,
    c.category_name,
    sc.subcategory_name,
    b.brand_name,
    p.unit_price,
    p.unit_of_measure,
    p.description,
    p.is_active
FROM product p
JOIN category c ON p.category_id = c.category_id
JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
JOIN brand b ON p.brand_id = b.brand_id;

-- View: Current Physical Store Stock with Batch Info
CREATE VIEW v_physical_store_stock_with_batch AS
SELECT
    p.product_code,
    p.product_name,
    c.category_name,
    sc.subcategory_name,
    b.brand_name,
    psi.main_inventory_id as batch_number,
    psi.quantity_on_shelf,
    mi.expiry_date,
    mi.supplier_name,
    p.unit_price,
    DATEDIFF(mi.expiry_date, CURDATE()) as days_to_expiry
FROM physical_store_inventory psi
JOIN product p ON psi.product_code = p.product_code
JOIN category c ON p.category_id = c.category_id
JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
JOIN brand b ON p.brand_id = b.brand_id
JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
WHERE p.is_active = TRUE AND psi.quantity_on_shelf > 0
ORDER BY p.product_code, mi.expiry_date ASC;

-- View: Current Online Store Stock with Batch Info
CREATE VIEW v_online_store_stock_with_batch AS
SELECT
    p.product_code,
    p.product_name,
    c.category_name,
    sc.subcategory_name,
    b.brand_name,
    osi.main_inventory_id as batch_number,
    osi.quantity_available,
    mi.expiry_date,
    mi.supplier_name,
    p.unit_price,
    DATEDIFF(mi.expiry_date, CURDATE()) as days_to_expiry
FROM online_store_inventory osi
JOIN product p ON osi.product_code = p.product_code
JOIN category c ON p.category_id = c.category_id
JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
JOIN brand b ON p.brand_id = b.brand_id
JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
WHERE p.is_active = TRUE AND osi.quantity_available > 0
ORDER BY p.product_code, mi.expiry_date ASC;

-- View: Reorder Level Report (Items below 50 quantity) - Updated
CREATE VIEW v_reorder_report AS
SELECT
    'PHYSICAL' as store_type,
    p.product_code,
    p.product_name,
    c.category_name,
    sc.subcategory_name,
    COALESCE(SUM(psi.quantity_on_shelf), 0) as current_stock
FROM product p
JOIN category c ON p.category_id = c.category_id
JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
LEFT JOIN physical_store_inventory psi ON p.product_code = psi.product_code
WHERE p.is_active = TRUE
GROUP BY p.product_code, p.product_name, c.category_name, sc.subcategory_name
HAVING current_stock < 50

UNION ALL

SELECT
    'ONLINE' as store_type,
    p.product_code,
    p.product_name,
    c.category_name,
    sc.subcategory_name,
    COALESCE(SUM(osi.quantity_available), 0) as current_stock
FROM product p
JOIN category c ON p.category_id = c.category_id
JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
LEFT JOIN online_store_inventory osi ON p.product_code = osi.product_code
WHERE p.is_active = TRUE
GROUP BY p.product_code, p.product_name, c.category_name, sc.subcategory_name
HAVING current_stock < 50;

-- =============================================
-- UPDATED TRIGGERS
-- =============================================

-- Trigger to create inventory transaction when bill item is inserted
DELIMITER //
CREATE TRIGGER tr_bill_item_inventory_update_v2
AFTER INSERT ON bill_item
FOR EACH ROW
BEGIN
    DECLARE store_type_val ENUM('PHYSICAL', 'ONLINE', 'MAIN');

    -- Get store type from bill
    SELECT b.store_type INTO store_type_val
    FROM bill b
    WHERE b.bill_id = NEW.bill_id;

    -- Insert inventory transaction
    INSERT INTO inventory_transaction (
        product_code, main_inventory_id, transaction_type, store_type,
        quantity_changed, bill_id, remarks
    ) VALUES (
        NEW.product_code, NEW.main_inventory_id, 'SALE', store_type_val,
        -NEW.quantity, NEW.bill_id, 'Sale transaction'
    );

    -- Update appropriate inventory based on the batch used
    IF store_type_val = 'PHYSICAL' THEN
        UPDATE physical_store_inventory
        SET quantity_on_shelf = quantity_on_shelf - NEW.quantity
        WHERE product_code = NEW.product_code
        AND main_inventory_id = NEW.main_inventory_id
        AND quantity_on_shelf >= NEW.quantity;
    ELSEIF store_type_val = 'ONLINE' THEN
        UPDATE online_store_inventory
        SET quantity_available = quantity_available - NEW.quantity
        WHERE product_code = NEW.product_code
        AND main_inventory_id = NEW.main_inventory_id
        AND quantity_available >= NEW.quantity;
    END IF;

    -- Update main inventory for the specific batch
    UPDATE main_inventory
    SET remaining_quantity = remaining_quantity - NEW.quantity
    WHERE main_inventory_id = NEW.main_inventory_id;
END//
DELIMITER ;

-- =============================================
-- STORED PROCEDURES - UPDATED FOR NEW SYSTEM
-- =============================================

-- Procedure to generate next product code
DELIMITER //
CREATE PROCEDURE GenerateProductCode(
    IN p_category_id INT,
    IN p_subcategory_id INT,
    IN p_brand_id INT,
    OUT p_product_code VARCHAR(15)
)
BEGIN
    DECLARE v_category_code VARCHAR(5);
    DECLARE v_subcategory_code VARCHAR(5);
    DECLARE v_brand_code VARCHAR(5);
    DECLARE v_sequence INT DEFAULT 1;
    DECLARE v_base_code VARCHAR(15);

    -- Get codes
    SELECT c.category_code INTO v_category_code FROM category c WHERE c.category_id = p_category_id;
    SELECT sc.subcategory_code INTO v_subcategory_code FROM subcategory sc WHERE sc.subcategory_id = p_subcategory_id;
    SELECT b.brand_code INTO v_brand_code FROM brand b WHERE b.brand_id = p_brand_id;

    -- Create base code
    SET v_base_code = CONCAT(v_category_code, v_subcategory_code, v_brand_code);

    -- Find next sequence number
    SELECT COALESCE(MAX(CAST(RIGHT(product_code, 3) AS UNSIGNED)), 0) + 1
    INTO v_sequence
    FROM product
    WHERE product_code LIKE CONCAT(v_base_code, '%');

    -- Generate final product code
    SET p_product_code = CONCAT(v_base_code, LPAD(v_sequence, 3, '0'));
END//
DELIMITER ;

-- Procedure to get available batches for a product (FIFO order)
DELIMITER //
CREATE PROCEDURE GetAvailableBatches(
    IN p_product_code VARCHAR(15),
    IN p_store_type ENUM('PHYSICAL', 'ONLINE')
)
BEGIN
    IF p_store_type = 'PHYSICAL' THEN
        SELECT
            psi.main_inventory_id as batch_number,
            psi.quantity_on_shelf as available_quantity,
            mi.expiry_date,
            mi.purchase_date,
            mi.supplier_name
        FROM physical_store_inventory psi
        JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
        WHERE psi.product_code = p_product_code
        AND psi.quantity_on_shelf > 0
        ORDER BY mi.expiry_date ASC, mi.purchase_date ASC;
    ELSE
        SELECT
            osi.main_inventory_id as batch_number,
            osi.quantity_available as available_quantity,
            mi.expiry_date,
            mi.purchase_date,
            mi.supplier_name
        FROM online_store_inventory osi
        JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
        WHERE osi.product_code = p_product_code
        AND osi.quantity_available > 0
        ORDER BY mi.expiry_date ASC, mi.purchase_date ASC;
    END IF;
END//
DELIMITER ;

-- Procedure to get next batch to use for sale (FIFO)
DELIMITER //
CREATE PROCEDURE GetNextBatchForSale(
    IN p_product_code VARCHAR(15),
    IN p_required_quantity INT,
    IN p_store_type ENUM('PHYSICAL', 'ONLINE'),
    OUT p_batch_number INT,
    OUT p_available_quantity INT
)
BEGIN
    SET p_batch_number = NULL;
    SET p_available_quantity = 0;

    IF p_store_type = 'PHYSICAL' THEN
        SELECT
            psi.main_inventory_id,
            psi.quantity_on_shelf
        INTO p_batch_number, p_available_quantity
        FROM physical_store_inventory psi
        JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
        WHERE psi.product_code = p_product_code
        AND psi.quantity_on_shelf >= p_required_quantity
        ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
        LIMIT 1;

        -- If no single batch has enough, get the batch with earliest expiry
        IF p_batch_number IS NULL THEN
            SELECT
                psi.main_inventory_id,
                psi.quantity_on_shelf
            INTO p_batch_number, p_available_quantity
            FROM physical_store_inventory psi
            JOIN main_inventory mi ON psi.main_inventory_id = mi.main_inventory_id
            WHERE psi.product_code = p_product_code
            AND psi.quantity_on_shelf > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            LIMIT 1;
        END IF;
    ELSE
        SELECT
            osi.main_inventory_id,
            osi.quantity_available
        INTO p_batch_number, p_available_quantity
        FROM online_store_inventory osi
        JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
        WHERE osi.product_code = p_product_code
        AND osi.quantity_available >= p_required_quantity
        ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
        LIMIT 1;

        -- If no single batch has enough, get the batch with earliest expiry
        IF p_batch_number IS NULL THEN
            SELECT
                osi.main_inventory_id,
                osi.quantity_available
            INTO p_batch_number, p_available_quantity
            FROM online_store_inventory osi
            JOIN main_inventory mi ON osi.main_inventory_id = mi.main_inventory_id
            WHERE osi.product_code = p_product_code
            AND osi.quantity_available > 0
            ORDER BY mi.expiry_date ASC, mi.purchase_date ASC
            LIMIT 1;
        END IF;
    END IF;
END//
DELIMITER ;

-- Procedure for complete product search (for CLI application)
DELIMITER //
CREATE PROCEDURE SearchProducts(IN p_search_term VARCHAR(100))
BEGIN
    SELECT
        p.product_code,
        p.product_name,
        c.category_name,
        sc.subcategory_name,
        b.brand_name,
        p.unit_price,
        p.unit_of_measure,

        -- Physical store availability
        COALESCE(SUM(psi.quantity_on_shelf), 0) as physical_stock,

        -- Online store availability
        COALESCE(SUM(osi.quantity_available), 0) as online_stock

    FROM product p
    JOIN category c ON p.category_id = c.category_id
    JOIN subcategory sc ON p.subcategory_id = sc.subcategory_id
    JOIN brand b ON p.brand_id = b.brand_id
    LEFT JOIN physical_store_inventory psi ON p.product_code = psi.product_code
    LEFT JOIN online_store_inventory osi ON p.product_code = osi.product_code
    WHERE p.is_active = TRUE
    AND (
        p.product_code LIKE CONCAT('%', p_search_term, '%')
        OR p.product_name LIKE CONCAT('%', p_search_term, '%')
        OR c.category_name LIKE CONCAT('%', p_search_term, '%')
        OR sc.subcategory_name LIKE CONCAT('%', p_search_term, '%')
        OR b.brand_name LIKE CONCAT('%', p_search_term, '%')
    )
    GROUP BY p.product_code, p.product_name, c.category_name, sc.subcategory_name, b.brand_name, p.unit_price, p.unit_of_measure
    ORDER BY p.product_code;
END//
DELIMITER ;

-- Sample customer data
INSERT INTO customer (customer_name, email, phone, address, registration_date) VALUES
('John Doe', 'john.doe@email.com', '+94771234567', '123 Main Street, Colombo 03', '2024-09-01'),
('Jane Smith', 'jane.smith@email.com', '+94777654321', '456 Galle Road, Colombo 04', '2024-09-05');

-- adding a password column to the customer table
ALTER TABLE customer
ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';
