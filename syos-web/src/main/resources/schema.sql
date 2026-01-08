-- SYOS Grocery Store Database Schema
-- MySQL 9.4

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS syos_grocery_store
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE syos_grocery_store;

-- =====================================================
-- Reference Tables
-- =====================================================

-- Category table
CREATE TABLE IF NOT EXISTS category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Subcategory table
CREATE TABLE IF NOT EXISTS subcategory (
    subcategory_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    subcategory_name VARCHAR(100) NOT NULL,
    subcategory_code VARCHAR(10) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    UNIQUE KEY uk_subcategory (category_id, subcategory_code)
) ENGINE=InnoDB;

-- Brand table
CREATE TABLE IF NOT EXISTS brand (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL,
    brand_code VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- Product Table
-- =====================================================

CREATE TABLE IF NOT EXISTS product (
    product_code VARCHAR(20) PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    category_id INT,
    subcategory_id INT,
    brand_id INT,
    unit_price DECIMAL(12, 2) NOT NULL,
    description TEXT,
    unit_of_measure VARCHAR(20) DEFAULT 'PCS',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (subcategory_id) REFERENCES subcategory(subcategory_id),
    FOREIGN KEY (brand_id) REFERENCES brand(brand_id),
    INDEX idx_product_name (product_name),
    INDEX idx_product_category (category_id),
    INDEX idx_product_active (is_active)
) ENGINE=InnoDB;

-- =====================================================
-- Inventory Tables
-- =====================================================

-- Main Inventory (Central Warehouse) - Batch tracking
CREATE TABLE IF NOT EXISTS main_inventory (
    main_inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL,
    quantity_received INT NOT NULL,
    remaining_quantity INT NOT NULL,
    purchase_price DECIMAL(12, 2),
    purchase_date DATE,
    expiry_date DATE,
    supplier_name VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    INDEX idx_main_inv_product (product_code),
    INDEX idx_main_inv_expiry (expiry_date),
    INDEX idx_main_inv_remaining (remaining_quantity)
) ENGINE=InnoDB;

-- Physical Store Inventory (Shelves)
CREATE TABLE IF NOT EXISTS physical_store_inventory (
    physical_store_inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL,
    main_inventory_id INT NOT NULL,
    quantity_on_shelf INT NOT NULL DEFAULT 0,
    restocked_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_physical_product (product_code),
    INDEX idx_physical_batch (main_inventory_id)
) ENGINE=InnoDB;

-- Online Store Inventory
CREATE TABLE IF NOT EXISTS online_store_inventory (
    online_store_inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL,
    main_inventory_id INT NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    restocked_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_online_product (product_code),
    INDEX idx_online_batch (main_inventory_id)
) ENGINE=InnoDB;

-- =====================================================
-- Customer Table
-- =====================================================

CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    password_hash VARCHAR(255) NOT NULL,
    registration_date DATE DEFAULT (CURRENT_DATE),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_email (email),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_active (is_active)
) ENGINE=InnoDB;

-- =====================================================
-- Bill Tables
-- =====================================================

-- Bill header
CREATE TABLE IF NOT EXISTS bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT,
    transaction_type ENUM('CASH', 'CREDIT', 'CARD', 'ONLINE') NOT NULL,
    store_type ENUM('PHYSICAL', 'ONLINE') NOT NULL,
    subtotal DECIMAL(12, 2) DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    tendered_amount DECIMAL(12, 2),
    change_amount DECIMAL(12, 2),
    cashier_id VARCHAR(50),
    bill_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    INDEX idx_bill_serial (serial_number),
    INDEX idx_bill_date (bill_date),
    INDEX idx_bill_customer (customer_id),
    INDEX idx_bill_store_type (store_type)
) ENGINE=InnoDB;

-- Bill items (line items)
CREATE TABLE IF NOT EXISTS bill_item (
    bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    product_code VARCHAR(20) NOT NULL,
    product_name VARCHAR(200),
    main_inventory_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_bill_item_bill (bill_id),
    INDEX idx_bill_item_product (product_code)
) ENGINE=InnoDB;

-- =====================================================
-- Inventory Transaction Log
-- =====================================================

CREATE TABLE IF NOT EXISTS inventory_transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL,
    main_inventory_id INT,
    transaction_type ENUM('PURCHASE', 'SALE', 'RESTOCK_PHYSICAL', 'RESTOCK_ONLINE', 'ADJUSTMENT', 'RETURN', 'EXPIRED') NOT NULL,
    store_type ENUM('PHYSICAL', 'ONLINE', 'WAREHOUSE'),
    quantity_changed INT NOT NULL,
    bill_id INT,
    remarks TEXT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    INDEX idx_inv_trans_product (product_code),
    INDEX idx_inv_trans_date (transaction_date),
    INDEX idx_inv_trans_type (transaction_type)
) ENGINE=InnoDB;

-- =====================================================
-- Bill Serial Number Sequence Table
-- =====================================================

CREATE TABLE IF NOT EXISTS bill_sequence (
    store_type ENUM('PHYSICAL', 'ONLINE') PRIMARY KEY,
    prefix VARCHAR(10) NOT NULL,
    last_number INT DEFAULT 0
) ENGINE=InnoDB;

-- Initialize sequences
INSERT INTO bill_sequence (store_type, prefix, last_number) VALUES
    ('PHYSICAL', 'PH', 0),
    ('ONLINE', 'ON', 0)
ON DUPLICATE KEY UPDATE store_type = store_type;

-- =====================================================
-- Online Orders Table
-- =====================================================

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    customer_name VARCHAR(200),
    customer_email VARCHAR(255),
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
    payment_method ENUM('CASH', 'CREDIT', 'CARD', 'ONLINE') DEFAULT 'ONLINE',
    shipping_address TEXT,
    shipping_phone VARCHAR(20),
    shipping_notes TEXT,
    subtotal DECIMAL(12, 2) DEFAULT 0.00,
    shipping_fee DECIMAL(12, 2) DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    bill_id INT,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME,
    shipped_at DATETIME,
    delivered_at DATETIME,
    cancelled_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    INDEX idx_order_number (order_number),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB;

-- Order items table
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_code VARCHAR(20) NOT NULL,
    product_name VARCHAR(200),
    main_inventory_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_code) REFERENCES product(product_code),
    FOREIGN KEY (main_inventory_id) REFERENCES main_inventory(main_inventory_id),
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_product (product_code)
) ENGINE=InnoDB;

-- Order sequence for generating order numbers
CREATE TABLE IF NOT EXISTS order_sequence (
    id INT PRIMARY KEY DEFAULT 1,
    last_number INT DEFAULT 0
) ENGINE=InnoDB;

INSERT INTO order_sequence (id, last_number) VALUES (1, 0)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- Sample Data (Categories, Subcategories, Brands)
-- =====================================================

-- Categories
INSERT INTO category (category_name, category_code, description) VALUES
    ('Beverages', 'BEV', 'Drinks and beverages'),
    ('Dairy', 'DAI', 'Dairy products'),
    ('Snacks', 'SNK', 'Snacks and confectionery'),
    ('Groceries', 'GRO', 'General grocery items'),
    ('Personal Care', 'PER', 'Personal care products')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- Subcategories
INSERT INTO subcategory (category_id, subcategory_name, subcategory_code, description) VALUES
    (1, 'Soft Drinks', 'SD', 'Carbonated beverages'),
    (1, 'Juices', 'JU', 'Fruit juices'),
    (1, 'Water', 'WA', 'Bottled water'),
    (2, 'Milk', 'MI', 'Fresh milk products'),
    (2, 'Cheese', 'CH', 'Cheese varieties'),
    (2, 'Yogurt', 'YO', 'Yogurt products'),
    (3, 'Chips', 'CP', 'Potato chips'),
    (3, 'Biscuits', 'BI', 'Biscuits and cookies'),
    (3, 'Chocolates', 'CO', 'Chocolate products'),
    (4, 'Rice', 'RI', 'Rice varieties'),
    (4, 'Flour', 'FL', 'Flour and baking'),
    (4, 'Oil', 'OI', 'Cooking oils'),
    (5, 'Soap', 'SO', 'Bathing soaps'),
    (5, 'Shampoo', 'SH', 'Hair care')
ON DUPLICATE KEY UPDATE subcategory_name = VALUES(subcategory_name);

-- Brands
INSERT INTO brand (brand_name, brand_code, description) VALUES
    ('Coca-Cola', 'CC', 'Coca-Cola Company'),
    ('Pepsi', 'PE', 'PepsiCo'),
    ('Nestle', 'NE', 'Nestle Products'),
    ('Unilever', 'UL', 'Unilever Products'),
    ('Local Brand', 'LO', 'Local manufacturers'),
    ('Premium', 'PR', 'Premium products')
ON DUPLICATE KEY UPDATE brand_name = VALUES(brand_name);

-- =====================================================
-- Sample Products
-- =====================================================

INSERT INTO product (product_code, product_name, category_id, subcategory_id, brand_id, unit_price, unit_of_measure) VALUES
    ('BEV-SD-CC-001', 'Coca-Cola 500ml', 1, 1, 1, 150.00, 'PCS'),
    ('BEV-SD-CC-002', 'Coca-Cola 1.5L', 1, 1, 1, 280.00, 'PCS'),
    ('BEV-SD-PE-001', 'Pepsi 500ml', 1, 1, 2, 140.00, 'PCS'),
    ('BEV-JU-NE-001', 'Nestle Mango Juice 1L', 1, 2, 3, 320.00, 'PCS'),
    ('DAI-MI-LO-001', 'Fresh Milk 1L', 2, 4, 5, 220.00, 'PCS'),
    ('DAI-YO-NE-001', 'Nestle Yogurt 500g', 2, 6, 3, 180.00, 'PCS'),
    ('SNK-CP-LO-001', 'Potato Chips 100g', 3, 7, 5, 120.00, 'PCS'),
    ('SNK-BI-NE-001', 'Nestle Biscuits 200g', 3, 8, 3, 95.00, 'PCS'),
    ('SNK-CO-NE-001', 'Nestle Chocolate Bar', 3, 9, 3, 250.00, 'PCS'),
    ('GRO-RI-LO-001', 'Basmati Rice 5kg', 4, 10, 5, 1500.00, 'KG'),
    ('GRO-OI-LO-001', 'Coconut Oil 1L', 4, 12, 5, 650.00, 'LITER'),
    ('PER-SO-UL-001', 'Lux Soap 100g', 5, 13, 4, 85.00, 'PCS'),
    ('PER-SH-UL-001', 'Sunsilk Shampoo 200ml', 5, 14, 4, 350.00, 'PCS')
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name);

-- =====================================================
-- Sample Main Inventory (Batches)
-- =====================================================

INSERT INTO main_inventory (product_code, quantity_received, remaining_quantity, purchase_price, purchase_date, expiry_date, supplier_name) VALUES
    ('BEV-SD-CC-001', 100, 100, 120.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'Coca-Cola Distributor'),
    ('BEV-SD-CC-002', 50, 50, 220.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'Coca-Cola Distributor'),
    ('BEV-SD-PE-001', 80, 80, 110.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'Pepsi Distributor'),
    ('DAI-MI-LO-001', 50, 50, 180.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'Local Dairy'),
    ('DAI-YO-NE-001', 40, 40, 140.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'Nestle Lanka'),
    ('SNK-CP-LO-001', 200, 200, 80.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 MONTH), 'Local Snacks'),
    ('SNK-BI-NE-001', 150, 150, 65.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 MONTH), 'Nestle Lanka'),
    ('GRO-RI-LO-001', 100, 100, 1200.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'Rice Mill'),
    ('PER-SO-UL-001', 300, 300, 60.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 'Unilever Lanka'),
    ('PER-SH-UL-001', 100, 100, 280.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 'Unilever Lanka')
ON DUPLICATE KEY UPDATE quantity_received = VALUES(quantity_received);

-- =====================================================
-- Sample Physical Store Inventory
-- =====================================================

INSERT INTO physical_store_inventory (product_code, main_inventory_id, quantity_on_shelf, restocked_date)
SELECT mi.product_code, mi.main_inventory_id, 20, CURDATE()
FROM main_inventory mi
WHERE mi.main_inventory_id <= 10;

-- =====================================================
-- Sample Online Store Inventory
-- =====================================================

INSERT INTO online_store_inventory (product_code, main_inventory_id, quantity_available, restocked_date)
SELECT mi.product_code, mi.main_inventory_id, 15, CURDATE()
FROM main_inventory mi
WHERE mi.main_inventory_id <= 10;

SELECT 'Schema created successfully!' AS status;
