# SYOS_Complete_Requirements_For_ClaudeCode

# SYOS Grocery Store - Complete Requirements & Features Documentation

## For Claude Code Implementation

This document contains all functional requirements, business rules, and implementation details from the current CLI-based SYOS system. Use this as the source of truth when implementing the multithreaded web application.

---

## Table of Contents

1. [Business Context](#business-context)
2. [Complete Feature Set](#complete-feature-set)
3. [User Roles & Capabilities](#user-roles--capabilities)
4. [Product & Inventory Management](#product--inventory-management)
5. [Billing System](#billing-system)
6. [Customer Management](#customer-management)
7. [Reporting System](#reporting-system)
8. [Business Rules & Validation](#business-rules--validation)
9. [Database Schema](#database-schema)
10. [Design Patterns Used](#design-patterns-used)
11. [Domain Models](#domain-models)
12. [Service Layer Architecture](#service-layer-architecture)
13. [Exception Handling](#exception-handling)
14. [Implementation Notes](#implementation-notes)
15. Testing Requirements
16. Performance Considerations
17. Security Requirements
18. Data Migration Notes
19. Quality Attributes
20. Concution

---

## 1. Business Context

### Problem Statement

SYOS (Synex Outlet Store) is a brick-and-mortar grocery store in Colombo facing:

- Long customer queues during rush hours
- Manual billing prone to calculation errors
- Time-consuming and tedious processes
- No automated inventory management
- No online sales capability

### Solution

Automated point-of-sale and inventory management system with:

- Physical store POS terminal (cash only)
- Online e-commerce platform
- Automated inventory tracking with FIFO + expiry priority
- Comprehensive reporting for management
- Separate inventory management for physical and online stores

---

## 2. Complete Feature Set

### 2.1 Physical Store (POS) Features

#### Cashier Terminal

- **Product lookup by code**: Enter short product codes (e.g., `BVEDRB001`)
- **Add items to bill**: Scan/enter products with quantities
- **Real-time total calculation**: Running total as items are added
- **Cash payment processing**: Cash tendered, change calculation
- **Bill generation**: Auto-incremented serial numbers (starting from 1)
- **Receipt printing**: Complete bill with all details
- **Automatic inventory reduction**: Stock reduced from appropriate batch on checkout

#### Inventory Constraints

- Physical store inventory tracked separately
- FIFO + expiry priority for batch selection
- Insufficient stock error handling
- Real-time stock availability checks

### 2.2 Online Store Features

#### Customer-Facing

- **Customer registration**:
    - Name (required, max 200 chars)
    - Email (required, unique, max 200 chars)
    - Phone (max 20 chars)
    - Address (text, required)
    - Password (min 6 chars, hashed)
- **Customer login**: Email + password authentication
- **Product browsing**:
    - Browse by category
    - Search by name, code, brand
    - View product details (name, price, unit, stock)
    - Filter by availability
- **Shopping cart**:
    - Add/remove items
    - Update quantities
    - View running total
    - Apply discounts (if applicable)
- **Order placement**:
    - Review cart
    - Confirm order
    - Generate order number
    - Email confirmation (future)
- **Order history**: View past orders with details

#### Backend

- Separate online inventory management
- Stock reduction on order placement
- Transaction tracking (ONLINE vs PHYSICAL)
- Customer order history

### 2.3 Inventory Manager Features

#### Product Management

- **Add new products**:
    - Auto-generate product codes based on category/subcategory/brand
    - Preview generated code before creation
    - Set unit price, description, unit of measure
    - Activate/deactivate products
- **Product code generation logic**:

```other
Format: [CATEGORY_CODE][SUBCATEGORY_CODE][BRAND_CODE][SEQUENCE]
Example: BVEDRB001
- BV = Beverages
- EDR = Energy Drinks
- RB = Red Bull
- 001 = Sequence number
```

#### Batch Management (Main Inventory)

- **Add new batches**:
    - Link to existing product
    - Quantity received
    - Purchase price
    - Purchase date (cannot be future)
    - Expiry date (optional)
    - Supplier name (optional, max 100 chars)
    - Batch number auto-generated
- **Remove batches**:
    - Only if not used in any inventory
    - Undo capability
- **Batch tracking**:
    - Each batch has unique ID (main_inventory_id)
    - Remaining quantity tracked
    - Purchase and expiry dates
    - Supplier information

#### Stock Distribution (Command Pattern)

- **Issue stock to physical store**:
    - Select product
    - Specify quantity
    - Batch selection using FIFO + expiry strategy
    - Detailed analysis of which batch selected and why
    - Undo capability
- **Issue stock to online store**:
    - Same as physical but to online inventory
    - Separate tracking

#### Batch Selection Strategy (FIFO + Expiry Priority)

Algorithm logic:

1. Filter batches with remaining quantity > 0
2. Prefer batches that can fulfill entire order
3. Among sufficient batches:
    - Prioritize earliest expiry date
    - If expiry same, prioritize oldest purchase date
1. If no single batch sufficient:
    - Select batch with earliest expiry
1. Warn if batch expiring within 30 days

#### Inventory Reports

- **View batch selection logic**: See detailed reasoning for batch choices
- **Expiring products report**:
    - Threshold: 30 days by default (configurable)
    - Shows batch number, expiry date, remaining qty, supplier
    - Urgency indicator for items expiring within 7 days
- **Product inventory status**:
    - Main inventory total
    - Physical store total
    - Online store total
    - Batch-wise breakdown with expiry dates

#### Undo Functionality

- Undo last operation:
    - Add batch
    - Remove batch
    - Issue stock
- Only one level of undo (last command only)

### 2.4 SYOS Manager Features

#### Daily Sales Report

Generate for specific date, filterable by:

- **Store type**: Physical, Online, or Both
- **Transaction type**: Cash, Online, or Both

Display:

- Items sold (code, name, quantity)
- Total revenue per item
- Grand total for the day

#### Reshelving Report

Items needing to be restocked (end of day):

- Product code
- Product name
- Quantity needed
- Current stock level

#### Reorder Level Report

Products below reorder threshold:

- Default threshold: 50 units
- Configurable threshold
- Shows product code, name, current stock
- Indicates urgency level

#### Stock Report (Batch-wise)

Complete inventory snapshot:

- Product details
- Batch number
- Purchase date
- Expiry date
- Supplier name
- Remaining quantity
- Days until expiry

#### Bill Report

All customer transactions:

- Bill serial number
- Date
- Customer (if online)
- Store type
- Transaction type
- Total amount
- Status

---

## 3. User Roles & Capabilities

### Role: Cashier (Physical Store)

**Capabilities:**

- Access POS terminal
- Search/lookup products
- Add items to bill
- Process cash payments
- Generate receipts
- View product availability

**Restrictions:**

- Cannot modify inventory
- Cannot access reports
- Cannot manage products
- No online store access

---

### Role: Inventory Manager

**Capabilities:**

- Add/remove products
- Manage batches (add/remove)
- Issue stock to stores
- View inventory reports
- Undo operations
- Preview product codes

**Restrictions:**

- Cannot process sales
- Cannot access financial reports
- Cannot manage customers

---

### Role: SYOS Manager

**Capabilities:**

- Generate all reports
- View sales analytics
- Monitor inventory levels
- View reorder needs
- Access all transaction history

**Restrictions:**

- Cannot modify inventory directly
- Cannot process sales directly

---

### Role: Online Customer

**Capabilities:**

- Register account
- Login/logout
- Browse products
- Search products by category/name
- Add items to cart
- Place orders
- View order history

**Restrictions:**

- Can only access online store
- Cannot view other customers' data
- Cannot access inventory details
- Cannot see batch information

---

## 4. Product & Inventory Management

### 4.1 Product Catalog Hierarchy

```other
Category (e.g., Food, Beverages, Chocolates)
  └─ Subcategory (e.g., Energy Drinks, Soft Drinks, Milk)
      └─ Brand (e.g., Red Bull, Coca Cola, Highland)
          └─ Product (specific item with code)
```

### 4.2 Product Code Generation

**Algorithm:**

1. Get category code from database
2. Get subcategory code from database
3. Get brand code from database
4. Find highest existing sequence for this combination
5. Increment sequence
6. Format as: `[CAT][SUBCAT][BRAND][SEQ]`

**Example:**

```other
Category: Beverages (BV)
Subcategory: Energy Drinks (EDR)
Brand: Red Bull (RB)
Sequence: 001

Product Code: BVEDRB001
```

**Validation Rules:**

- Category, subcategory, and brand must exist
- Code must be unique
- Sequence starts at 001, max 999
- Code is uppercase, exactly 15 characters max

### 4.3 Three-Tier Inventory System

#### Main Inventory (Central Stock)

- **Purpose**: Batch tracking and master inventory
- **Characteristics**:
    - Each purchase creates a new batch
    - Batch number = main_inventory_id (auto-increment)
    - Tracks: quantity received, purchase price, purchase date, expiry date, supplier
    - Remaining quantity updated on distribution
    - Never deleted (for audit trail)

#### Physical Store Inventory (Shelf Stock)

- **Purpose**: Items available for in-store purchase
- **Characteristics**:
    - References batches from main inventory
    - Multiple batches of same product can be on shelf
    - Reduced on physical store sales
    - Restocked from main inventory
    - FIFO + expiry priority for sales

#### Online Store Inventory

- **Purpose**: Items available for online orders
- **Characteristics**:
    - References batches from main inventory
    - Separate from physical store
    - Reduced on online order placement
    - Restocked from main inventory independently
    - Same FIFO + expiry priority logic

### 4.4 Batch Selection Strategy (Critical Algorithm)

**Implementation: FIFOWithExpiryStrategy**

```java
Priority Order:
1. Batches with sufficient quantity
2. Earliest expiry date
3. Oldest purchase date (if expiry same)
4. If no single batch sufficient, earliest expiring batch

Special Cases:
- Null expiry dates treated as "no expiry" (lowest priority)
- Critical expiry: < 30 days (warning shown)
- Urgent expiry: < 7 days (urgent warning)
```

**Selection Reason Generation:**
The strategy must explain WHY a batch was selected:

- "Selected batch #X because: expires in Y days"
- "Oldest batch purchased on [date]"
- "Newer batch #Y has earlier expiry than older batch #Z"

**Example Scenario:**

```other
Product: Red Bull
Batch 1: Purchased 2024-01-01, Expiry 2024-12-31, Qty 100
Batch 2: Purchased 2024-01-15, Expiry 2024-11-30, Qty 150

Request: Issue 50 units

Selected: Batch 2
Reason: "Newer batch but expires 31 days earlier than batch 1"
```

### 4.5 Stock Movement Flow

```other
Purchase → Main Inventory (Batch Created)
              ↓
      ┌───────┴──────────┐
      ↓                  ↓
Physical Store    Online Store
  Inventory          Inventory
      ↓                  ↓
Physical Sale      Online Order
      ↓                  ↓
  Bill Items        Bill Items
(batch tracked)   (batch tracked)
```

---

## 5. Billing System

### 5.1 Bill Structure

**Bill Entity:**

```other
- Bill ID (auto-increment)
- Bill Serial Number (unique, running: "1", "2", "3"...)
- Customer ID (null for physical walk-ins)
- Transaction Type (CASH or ONLINE)
- Store Type (PHYSICAL or ONLINE)
- Subtotal
- Discount Amount (default 0)
- Total Amount
- Cash Tendered (for cash transactions)
- Change Amount (for cash transactions)
- Bill Date
- Items (collection of BillItem)
```

**BillItem Entity:**

```other
- Product Code
- Product Name (for display)
- Quantity
- Unit Price
- Total Price (quantity * unit price)
- Batch Number (main_inventory_id - which batch was sold)
```

### 5.2 Billing Workflow

#### Physical Store (Cash)

1. Cashier starts new bill
2. Add items:
    - Enter product code
    - System validates product exists
    - Check physical store stock availability
    - Add to bill with current price
1. Calculate running total
2. Enter cash tendered
3. Validate sufficient cash
4. Calculate change
5. Save bill to database
6. **Trigger automatic inventory reduction** (via database trigger)
7. Print receipt

#### Online Store

1. Customer adds items to cart
2. For each item:
    - Check online stock availability
    - Add to order
1. Review order
2. Confirm order
3. Save bill to database
4. **Trigger automatic inventory reduction** (via database trigger)
5. Send confirmation (future: email)

### 5.3 Inventory Reduction Logic (Database Trigger)

**Critical: Implemented as MySQL trigger**

```sql
AFTER INSERT on bill_item:
1. Determine store_type from bill
2. IF PHYSICAL:
   - Reduce physical_store_inventory.quantity_on_shelf
   - WHERE product_code = NEW.product_code
   - AND main_inventory_id = NEW.main_inventory_id
3. IF ONLINE:
   - Reduce online_store_inventory.quantity_available
   - WHERE product_code = NEW.product_code
   - AND main_inventory_id = NEW.main_inventory_id
4. Reduce main_inventory.remaining_quantity
   - WHERE main_inventory_id = NEW.main_inventory_id
5. Create inventory_transaction record
```

### 5.4 Bill Serial Number Generation

**Format:** Simple running number

- Starts at 1
- Increments by 1 for each bill
- Unique across all store types
- String format for consistency
- Example: "1", "2", "3", ..., "100", "101"...

**Implementation:**

```java
// Query max existing + 1
SELECT COALESCE(MAX(CAST(bill_serial_number AS UNSIGNED)), 0) + 1
FROM bill
```

### 5.5 Payment Processing

#### Cash Payment (Strategy Pattern)

- **Interface**: `PaymentService`
- **Implementation**: `CashPaymentServiceImpl`

**Business Rules:**

1. Cash tendered must be >= total amount
2. Change = cash tendered - total
3. Both cash tendered and change stored in bill
4. Transaction type = CASH

**Validation:**

```java
if (cashTendered.compareTo(totalAmount) < 0) {
    throw new InvalidPaymentException(
        "Insufficient cash. Required: " + totalAmount + 
        ", Tendered: " + cashTendered
    );
}
```

---

## 6. Customer Management

### 6.1 Customer Registration

**Required Fields:**

- Customer Name (1-200 chars)
- Email (unique, valid format)
- Phone (1-20 chars)
- Address (not empty)
- Password (min 6 chars)

**Business Rules:**

1. Email must be unique
2. Email must be valid format
3. Password must be at least 6 characters
4. Password is hashed before storage
5. Name cannot be empty
6. Registration date = current date
7. Customer is_active = true by default

**Validation:**

```java
if (email already exists) {
    throw new CustomerRegistrationException(
        "Email already registered: " + email
    );
}

if (password.length() < 6) {
    throw new CustomerRegistrationException(
        "Password must be at least 6 characters"
    );
}
```

### 6.2 Customer Authentication

**Login Process:**

1. Accept email + password
2. Query customer by email
3. Verify password hash matches
4. Return customer object if valid
5. Throw InvalidLoginException if not

**Password Hashing:**

```java
// Current implementation (simple for demo)
public static String hashPassword(String plainPassword) {
    return "HASH_" + plainPassword.hashCode();
}

// Production: Use BCrypt
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hash = encoder.encode(plainPassword);
```

**Session Management:**

- Store customer object in session after login
- Check session for authenticated routes
- Clear session on logout

---

## 7. Reporting System

### 7.1 Daily Sales Report

**Inputs:**

- Start date
- End date (optional, default to start date)
- Store type filter (PHYSICAL, ONLINE, or ALL)
- Transaction type filter (CASH, ONLINE, or ALL)

**Output:**

```other
Product Code | Product Name | Quantity Sold | Unit Price | Total Revenue
-------------|--------------|---------------|------------|---------------
BVEDRB001    | Red Bull     | 150           | Rs. 250.00 | Rs. 37,500.00
FDMCCM001    | Milk         | 200           | Rs. 180.00 | Rs. 36,000.00
-------------|--------------|---------------|------------|---------------
                                        Grand Total: Rs. 73,500.00
```

**SQL Query Structure:**

```sql
SELECT 
    bi.product_code,
    bi.product_name,
    SUM(bi.quantity) as total_quantity,
    bi.unit_price,
    SUM(bi.total_price) as total_revenue
FROM bill_item bi
JOIN bill b ON bi.bill_id = b.bill_id
WHERE b.bill_date BETWEEN ? AND ?
  AND b.store_type IN (?, ?) -- if filtered
  AND b.transaction_type IN (?, ?) -- if filtered
GROUP BY bi.product_code, bi.product_name, bi.unit_price
ORDER BY total_revenue DESC
```

### 7.2 Reorder Level Report

**Purpose:** Identify products below minimum stock

**Default Threshold:** 50 units
**Configurable:** Manager can specify different threshold

**Calculation:**

```other
Total Stock = Physical Store Stock + Online Store Stock
If Total Stock < Threshold → Include in report
```

**Output:**

```other
Product Code | Product Name   | Current Stock | Threshold | Shortage
-------------|----------------|---------------|-----------|----------
BVEDRB001    | Red Bull       | 35            | 50        | 15
CHGOD001     | Godiva Choc    | 45            | 50        | 5
```

**Priority Levels:**

- Critical: < 20 units
- Warning: < 50 units
- Normal: >= 50 units

### 7.3 Stock Report (Batch-wise)

**Purpose:** Complete inventory visibility

**Output:**

```other
Product: Red Bull (BVEDRB001)
===============================================================
Batch # | Purchase Date | Expiry Date | Supplier | Remaining | Location
--------|---------------|-------------|----------|-----------|----------
    1   | 2024-01-15    | 2024-12-31  | ACME     | 120       | Main
        |               |             |          | 80        | Physical
        |               |             |          | 40        | Online
    2   | 2024-02-01    | 2024-11-30  | ACME     | 200       | Main
        |               |             |          | 150       | Physical
        |               |             |          | 50        | Online
--------|---------------|-------------|----------|-----------|----------
Total:                                            | 320       | Main
                                                  | 230       | Physical
                                                  | 90        | Online
```

### 7.4 Expiring Products Report

**Purpose:** Prevent waste, prioritize sales

**Threshold:** 30 days (default, configurable)

**Output:**

```other
Product Code | Batch # | Expiry Date | Days Left | Remaining | Supplier | Urgency
-------------|---------|-------------|-----------|-----------|----------|--------
BVEDRB001    | 5       | 2024-11-15  | 25        | 80        | ACME     | ⚠ WARNING
FDMCCM001    | 3       | 2024-11-05  | 5         | 120       | DairyFarm| 🔴 URGENT
```

**Urgency Levels:**

- 🔴 URGENT: < 7 days
- ⚠ WARNING: 7-30 days
- ✅ NORMAL: > 30 days

---

## 8. Business Rules & Validation

### 8.1 Product Code Validation

```java
Rules:
- Cannot be null or empty
- Max 15 characters
- Uppercase only
- Format: [CATEGORY][SUBCATEGORY][BRAND][SEQUENCE]
- Must be unique in system
```

### 8.2 Quantity Validation

```java
Rules:
- Must be positive integer (> 0)
- Cannot exceed available stock
- For batch removal: batch must exist and not be in use
```

### 8.3 Money/Price Validation

```java
Rules:
- Must be positive BigDecimal (> 0)
- Max 2 decimal places
- Represented as Money value object
- No negative amounts allowed
```

### 8.4 Date Validation

```java
Purchase Date Rules:
- Cannot be in future
- Cannot be before product creation date
- Format: YYYY-MM-DD

Expiry Date Rules:
- Optional (can be null for non-perishables)
- If provided, must be after purchase date
- Warning if < 30 days from current date
- Format: YYYY-MM-DD
```

### 8.5 String Length Limits

```java
Product Name: max 255 chars
Description: max 1000 chars
Supplier Name: max 100 chars
Customer Name: max 200 chars
Email: max 200 chars
Phone: max 20 chars
Product Code: max 15 chars
```

### 8.6 Inventory Business Rules

**Stock Reduction:**

```java
1. Cannot reduce stock below zero
2. Must reduce from correct inventory (physical vs online)
3. Must track which batch was used
4. Must update main inventory remaining quantity
```

**Stock Distribution:**

```java
1. Cannot issue more than available in main inventory
2. Must use FIFO + expiry priority strategy
3. Must create inventory transaction record
4. Supports undo (one level)
```

**Batch Management:**

```java
Add Batch:
- Product must exist
- Quantity received > 0
- Purchase price > 0
- Purchase date not in future

Remove Batch:
- Batch must exist
- Batch must not be referenced in any inventory
- Batch must not be in any bills
```

---

## 9. Database Schema

### 9.1 Core Tables

#### Category

```sql
category_id (PK, INT AUTO_INCREMENT)
category_name (VARCHAR(100))
category_code (VARCHAR(5))
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Subcategory

```sql
subcategory_id (PK, INT AUTO_INCREMENT)
subcategory_name (VARCHAR(100))
subcategory_code (VARCHAR(5))
category_id (FK → category)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Brand

```sql
brand_id (PK, INT AUTO_INCREMENT)
brand_name (VARCHAR(100))
brand_code (VARCHAR(5))
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Product

```sql
product_code (PK, VARCHAR(15))
product_name (VARCHAR(255))
category_id (FK → category)
subcategory_id (FK → subcategory)
brand_id (FK → brand)
unit_price (DECIMAL(10,2))
description (TEXT)
unit_of_measure (ENUM: KG, GRAM, LITER, ML, UNIT, PACK)
is_active (BOOLEAN)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Main_Inventory

```sql
main_inventory_id (PK, INT AUTO_INCREMENT) -- BATCH NUMBER
product_code (FK → product)
quantity_received (INT)
purchase_price (DECIMAL(10,2))
purchase_date (DATE)
expiry_date (DATE, nullable)
supplier_name (VARCHAR(200), nullable)
remaining_quantity (INT)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Physical_Store_Inventory

```sql
physical_inventory_id (PK, INT AUTO_INCREMENT)
product_code (FK → product)
main_inventory_id (FK → main_inventory)
quantity_on_shelf (INT)
restocked_date (DATE)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Online_Store_Inventory

```sql
online_inventory_id (PK, INT AUTO_INCREMENT)
product_code (FK → product)
main_inventory_id (FK → main_inventory)
quantity_available (INT)
restocked_date (DATE)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Customer

```sql
customer_id (PK, INT AUTO_INCREMENT)
customer_name (VARCHAR(200))
email (VARCHAR(200), UNIQUE)
phone (VARCHAR(20))
address (TEXT)
password_hash (VARCHAR(255))
registration_date (DATE)
is_active (BOOLEAN)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Bill

```sql
bill_id (PK, INT AUTO_INCREMENT)
bill_serial_number (VARCHAR(20), UNIQUE)
customer_id (FK → customer, nullable for walk-ins)
transaction_type (ENUM: CASH, ONLINE)
store_type (ENUM: PHYSICAL, ONLINE)
subtotal (DECIMAL(12,2))
discount_amount (DECIMAL(12,2))
total_amount (DECIMAL(12,2))
cash_tendered (DECIMAL(12,2), nullable)
change_amount (DECIMAL(12,2), nullable)
bill_date (DATE)
created_at (TIMESTAMP)
updated_at (TIMESTAMP)
```

#### Bill_Item

```sql
bill_item_id (PK, INT AUTO_INCREMENT)
bill_id (FK → bill)
product_code (FK → product)
main_inventory_id (FK → main_inventory) -- BATCH TRACKING
quantity (INT)
unit_price (DECIMAL(10,2))
total_price (DECIMAL(12,2))
created_at (TIMESTAMP)
```

#### Inventory_Transaction

```sql
transaction_id (PK, INT AUTO_INCREMENT)
product_code (FK → product)
main_inventory_id (FK → main_inventory)
transaction_type (ENUM: PURCHASE, SALE, RESTOCK, ADJUSTMENT)
store_type (ENUM: PHYSICAL, ONLINE, MAIN)
quantity_changed (INT) -- positive for addition, negative for reduction
bill_id (FK → bill, nullable)
transaction_date (TIMESTAMP)
remarks (TEXT)
```

#### Restock_Log

```sql
restock_id (PK, INT AUTO_INCREMENT)
product_code (FK → product)
main_inventory_id (FK → main_inventory)
target_store (ENUM: PHYSICAL, ONLINE)
quantity_restocked (INT)
restock_date (TIMESTAMP)
performed_by (VARCHAR(100))
```

### 9.2 Critical Indexes

```sql
-- Product lookups
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_product_active ON product(is_active);

-- Inventory queries
CREATE INDEX idx_main_inventory_product ON main_inventory(product_code);
CREATE INDEX idx_main_inventory_expiry ON main_inventory(expiry_date);
CREATE INDEX idx_physical_inventory_product ON physical_store_inventory(product_code);
CREATE INDEX idx_online_inventory_product ON online_store_inventory(product_code);

-- Bill queries
CREATE INDEX idx_bill_date ON bill(bill_date);
CREATE INDEX idx_bill_customer ON bill(customer_id);
CREATE INDEX idx_bill_serial ON bill(bill_serial_number);
CREATE INDEX idx_bill_store_type ON bill(store_type);

-- Customer lookups
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_phone ON customer(phone);
```

### 9.3 Critical Database Triggers

#### Trigger: Automatic Inventory Reduction

```sql
DELIMITER //
CREATE TRIGGER reduce_inventory_after_bill_item
AFTER INSERT ON bill_item
FOR EACH ROW
BEGIN
    DECLARE store_type_val ENUM('PHYSICAL', 'ONLINE');
    
    -- Get store type from bill
    SELECT b.store_type INTO store_type_val
    FROM bill b
    WHERE b.bill_id = NEW.bill_id;
    
    -- Reduce appropriate store inventory
    IF store_type_val = 'PHYSICAL' THEN
        UPDATE physical_store_inventory
        SET quantity_on_shelf = quantity_on_shelf - NEW.quantity
        WHERE product_code = NEW.product_code
        AND main_inventory_id = NEW.main_inventory_id;
    ELSE
        UPDATE online_store_inventory
        SET quantity_available = quantity_available - NEW.quantity
        WHERE product_code = NEW.product_code
        AND main_inventory_id = NEW.main_inventory_id;
    END IF;
    
    -- Reduce main inventory
    UPDATE main_inventory
    SET remaining_quantity = remaining_quantity - NEW.quantity
    WHERE main_inventory_id = NEW.main_inventory_id;
END//
DELIMITER ;
```

### 9.4 Stored Procedures

#### Generate Product Code

```sql
CREATE PROCEDURE GenerateProductCode(
    IN p_category_id INT,
    IN p_subcategory_id INT,
    IN p_brand_id INT,
    OUT p_product_code VARCHAR(15)
)
BEGIN
    -- Get codes, create base pattern, find sequence, return code
END
```

#### Get Available Batches (FIFO Order)

```sql
CREATE PROCEDURE GetAvailableBatches(
    IN p_product_code VARCHAR(15),
    IN p_store_type ENUM('PHYSICAL', 'ONLINE')
)
BEGIN
    -- Return batches ordered by expiry ASC, purchase_date ASC
END
```

#### Get Next Batch for Sale

```sql
CREATE PROCEDURE GetNextBatchForSale(
    IN p_product_code VARCHAR(15),
    IN p_required_quantity INT,
    IN p_store_type ENUM('PHYSICAL', 'ONLINE'),
    OUT p_batch_number INT,
    OUT p_available_quantity INT
)
BEGIN
    -- Implement FIFO + expiry logic
END
```

---

## 10. Design Patterns Used

### 10.1 Repository Pattern

**Purpose:** Abstract data access from business logic

**Interfaces:**

```java
Repository<T, ID>
  └─ ProductRepository
  └─ InventoryRepository
  └─ BillRepository
  └─ CustomerRepository
  └─ ReportRepository
```

**Generic Operations:**

```java
Optional<T> findById(ID id);
List<T> findAll();
T save(T entity);
void delete(ID id);
boolean existsById(ID id);
```

**Implementation:** JDBC with MySQL

### 10.2 Strategy Pattern

**Purpose:** Pluggable batch selection algorithms

**Interface:**

```java
public interface BatchSelectionStrategy {
    Optional<MainInventory> selectBatch(
        List<MainInventory> availableBatches,
        ProductCode productCode,
        int requiredQuantity
    );
    
    String getSelectionReason(
        MainInventory selectedBatch,
        List<MainInventory> availableBatches
    );
    
    String getStrategyName();
}
```

**Implementation:**

```java
public class FIFOWithExpiryStrategy implements BatchSelectionStrategy {
    // Prioritize expiry, then FIFO
}
```

**Context:**

```java
public class BatchSelectionContext {
    private BatchSelectionStrategy strategy;
    
    public void setStrategy(BatchSelectionStrategy strategy) {
        this.strategy = strategy;
    }
    
    public BatchSelectionResult selectBatch(...) {
        return strategy.selectBatch(...);
    }
}
```

### 10.3 Command Pattern

**Purpose:** Undo-capable inventory operations

**Interface:**

```java
public interface InventoryCommand {
    CommandResult execute() throws InventoryException;
    CommandResult undo() throws InventoryException;
    boolean canUndo();
    String getDescription();
    CommandType getCommandType();
}
```

**Implementations:**

```java
public class AddBatchCommand implements InventoryCommand {
    private final InventoryRepository inventoryRepository;
    private final ProductCode productCode;
    private final int quantityReceived;
    private final Money purchasePrice;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;
    private final String supplierName;
    private Integer createdBatchId;
    
    @Override
    public CommandResult execute() {
        // Add batch, store ID for undo
    }
    
    @Override
    public CommandResult undo() {
        // Remove batch using stored ID
    }
}

public class IssueStockCommand implements InventoryCommand {
    // Similar structure for stock distribution
}

public class RemoveBatchCommand implements InventoryCommand {
    // Similar structure for batch removal
}
```

**Command Types:**

```java
enum CommandType {
    ADD_BATCH,
    REMOVE_BATCH,
    ISSUE_STOCK,
    ADD_PRODUCT,
    UPDATE_STOCK
}
```

### 10.4 Factory Pattern

**Purpose:** Product creation with code generation

**Factory:**

```java
public class ProductFactory {
    private final ProductCodeGenerator codeGenerator;
    private final ProductRepository productRepository;
    
    public Product createProduct(
        String productName,
        int categoryId,
        int subcategoryId,
        int brandId,
        Money unitPrice,
        String description,
        UnitOfMeasure unitOfMeasure
    ) {
        // Generate code
        ProductCode code = codeGenerator.generateProductCode(
            categoryId, subcategoryId, brandId
        );
        
        // Create product
        return new Product(code, productName, ...);
    }
}
```

**Code Generator:**

```java
public interface ProductCodeGenerator {
    ProductCode generateProductCode(int categoryId, int subcategoryId, int brandId);
    int getNextSequenceNumber(int categoryId, int subcategoryId, int brandId);
}

public class ProductCodeGeneratorImpl implements ProductCodeGenerator {
    // Implementation using database queries
}
```

### 10.5 Singleton Pattern

**Purpose:** Database connection management

```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        // Initialize connection
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
}
```

**Note for Web Refactoring:** Replace with HikariCP connection pool

---

## 11. Domain Models

### 11.1 Value Objects (Immutable)

#### ProductCode

```java
public class ProductCode {
    private final String code;
    
    public ProductCode(String code) {
        validateCode(code);
        this.code = code.toUpperCase().trim();
    }
    
    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        if (code.length() > 15) {
            throw new IllegalArgumentException("Product code too long");
        }
    }
    
    public String getCode() { return code; }
    
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
}
```

#### Money

```java
public class Money {
    private final BigDecimal amount;
    
    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public BigDecimal getAmount() { return amount; }
    
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
}
```

#### BillSerialNumber

```java
public class BillSerialNumber {
    private final String serialNumber;
    
    public BillSerialNumber(String serialNumber) {
        validateSerialNumber(serialNumber);
        this.serialNumber = serialNumber;
    }
    
    private void validateSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty");
        }
    }
    
    public String getValue() { return serialNumber; }
    
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
}
```

### 11.2 Entities

#### Product

```java
public class Product {
    private final ProductCode productCode;
    private String productName;
    private final int categoryId;
    private final int subcategoryId;
    private final int brandId;
    private Money unitPrice;
    private String description;
    private UnitOfMeasure unitOfMeasure;
    private boolean isActive;
    
    // Constructor, getters, business methods
}
```

#### Bill

```java
public class Bill {
    private Integer billId;
    private final BillSerialNumber serialNumber;
    private final Integer customerId;
    private final TransactionType transactionType;
    private final StoreType storeType;
    private final List<BillItem> items;
    private Money subtotal;
    private Money discountAmount;
    private Money totalAmount;
    private Money cashTendered;
    private Money changeAmount;
    private final LocalDate billDate;
    
    // Business methods
    public void addItem(BillItem item) { ... }
    public void removeItem(BillItem item) { ... }
    public void calculateTotals() { ... }
    public boolean isEmpty() { ... }
}
```

#### BillItem

```java
public class BillItem {
    private Integer billItemId;
    private final ProductCode productCode;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;
    private final Money totalPrice;
    private final int batchNumber; // main_inventory_id
    
    // Constructor, getters
}
```

#### Customer

```java
public class Customer {
    private final Integer customerId;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String address;
    private final String passwordHash;
    private final LocalDate registrationDate;
    private final boolean isActive;
    
    // Password verification
    public boolean verifyPassword(String plainPassword) { ... }
    public static String hashPassword(String plainPassword) { ... }
}
```

#### MainInventory

```java
public class MainInventory {
    private final int batchNumber; // main_inventory_id
    private final ProductCode productCode;
    private final int quantityReceived;
    private final Money purchasePrice;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate; // nullable
    private final String supplierName; // nullable
    private int remainingQuantity;
    
    // Business methods
    public boolean hasStock() { return remainingQuantity > 0; }
    public boolean canFulfill(int required) { return remainingQuantity >= required; }
    public void reduceQuantity(int amount) { ... }
}
```

### 11.3 Enums

#### StoreType

```java
public enum StoreType {
    PHYSICAL,
    ONLINE
}
```

#### TransactionType

```java
public enum TransactionType {
    CASH,
    ONLINE
}
```

#### UnitOfMeasure

```java
public enum UnitOfMeasure {
    KILOGRAM("kg"),
    GRAM("g"),
    LITER("L"),
    MILLILITER("ml"),
    UNIT("unit"),
    PACK("pack");
    
    private final String symbol;
    
    UnitOfMeasure(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() { return symbol; }
}
```

---

## 12. Service Layer Architecture

### 12.1 Service Interfaces

#### ProductService

```java
public interface ProductService {
    Optional<Product> findByCode(ProductCode productCode);
    List<Product> findAll();
    List<Product> searchProducts(String searchTerm);
    int getAvailableStock(ProductCode productCode);
    boolean hasStock(ProductCode productCode, int quantity);
}
```

#### BillingService

```java
public interface BillingService {
    Bill createNewBill(StoreType storeType, Integer customerId);
    BillItem addItemToBill(Bill bill, ProductCode productCode, int quantity);
    void completeBill(Bill bill, Money cashTendered);
    Bill saveBill(Bill bill);
    Money calculateRunningTotal(Bill bill);
    List<Bill> getCustomerOrders(Integer customerId);
}
```

#### InventoryService

```java
public interface InventoryService {
    int getTotalPhysicalStock(ProductCode productCode);
    int getTotalOnlineStock(ProductCode productCode);
    MainInventory reserveStock(ProductCode productCode, int quantity);
    void reduceStock(ProductCode productCode, int quantity, StoreType storeType);
    List<MainInventory> getAvailableBatches(ProductCode productCode, StoreType storeType);
}
```

#### InventoryManagerService

```java
public interface InventoryManagerService {
    // Product management
    Product addNewProduct(...);
    boolean productExists(ProductCode productCode);
    String previewProductCode(int categoryId, int subcategoryId, int brandId);
    
    // Batch management
    CommandResult addBatch(...);
    CommandResult removeBatch(int batchNumber);
    
    // Stock distribution
    CommandResult issueStockToPhysicalStore(ProductCode productCode, int quantity);
    CommandResult issueStockToOnlineStore(ProductCode productCode, int quantity);
    
    // Reports
    List<MainInventory> getExpiringProducts(int thresholdDays);
    InventoryStatus getInventoryStatus(ProductCode productCode);
    
    // Undo
    CommandResult undoLastCommand();
    boolean canUndo();
}
```

#### CustomerService

```java
public interface CustomerService {
    Customer registerCustomer(String name, String email, String phone, 
                             String address, String password);
    Customer loginCustomer(String email, String password);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
}
```

#### ReportService

```java
public interface ReportService {
    // Sales reports
    SalesReport getDailySalesReport(LocalDate date, StoreType storeFilter, 
                                   TransactionType transactionFilter);
    
    // Inventory reports
    List<ProductStockLevel> getReorderLevelReport(int threshold);
    StockReport getBatchWiseStockReport();
    List<MainInventory> getExpiringProductsReport(int thresholdDays);
    
    // Bill reports
    List<Bill> getAllBills();
    List<Bill> getBillsByDate(LocalDate startDate, LocalDate endDate);
    List<Bill> getBillsByStoreType(StoreType storeType);
}
```

### 12.2 Service Implementations

All service implementations follow:

- Dependency injection via constructor
- Dependency on interfaces (DIP)
- Single responsibility
- Exception handling with custom exceptions
- Transaction management where needed

---

## 13. Exception Handling

### 13.1 Exception Hierarchy

```other
RuntimeException
  └─ BillingException
  └─ InventoryException
      └─ InsufficientStockException
      └─ ProductNotFoundException
  └─ CustomerRegistrationException
  └─ CustomerNotFoundException
  └─ InvalidLoginException
  └─ InvalidPaymentException
  └─ BusinessRuleException
```

### 13.2 Exception Descriptions

#### BillingException

**When:** Billing operations fail
**Examples:**

- Bill save fails
- Invalid bill state
- Empty bill completion attempt

#### InsufficientStockException

**When:** Requested quantity exceeds available stock
**Properties:**

```java
public class InsufficientStockException extends InventoryException {
    private final int availableStock;
    private final int requestedQuantity;
    
    public InsufficientStockException(String message, 
                                     int availableStock, 
                                     int requestedQuantity) {
        super(message);
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }
}
```

#### ProductNotFoundException

**When:** Product lookup fails
**Example:**

```java
throw new ProductNotFoundException("Product not found: " + productCode);
```

#### CustomerRegistrationException

**When:** Registration validation fails
**Examples:**

- Email already exists
- Password too short
- Required field empty

#### InvalidLoginException

**When:** Authentication fails
**Examples:**

- Email not found
- Password mismatch
- Account inactive

#### BusinessRuleException

**When:** Business rule violated
**Examples:**

- Negative amounts
- Future dates
- Invalid quantities
- String length violations

---

## 14. Implementation Notes

### 14.1 Critical Algorithms to Preserve

#### Batch Selection (FIFO + Expiry)

**Must preserve exact logic:**

1. Filter batches with stock > 0
2. Prefer batches that can fulfill entire order
3. Among sufficient batches:
    - Sort by expiry ASC (nulls last)
    - Then by purchase date ASC
1. If no sufficient batch:
    - Select earliest expiring batch
1. Generate detailed selection reason

#### Product Code Generation

**Must preserve format:**

```other
[CATEGORY_CODE][SUBCATEGORY_CODE][BRAND_CODE][SEQUENCE]
Example: BVEDRB001
```

**Process:**

1. Query codes from DB
2. Find max sequence for pattern
3. Increment
4. Format with zero-padding

#### Bill Serial Number

**Must be sequential:**

- Simple running number: "1", "2", "3"...
- Query max + 1
- No gaps allowed

### 14.2 Database Triggers (Critical)

**Inventory reduction trigger:**

- Fires AFTER INSERT on bill_item
- Automatically reduces inventory
- Updates both store inventory and main inventory
- Creates inventory transaction record

**DO NOT remove this trigger** - it's core to inventory integrity

### 14.3 Undo Implementation

**Command pattern requirements:**

1. Store last executed command
2. Only one level of undo
3. Can only undo if command supports it
4. Clear last command after undo

**Supported operations:**

- Add batch (undo = remove batch)
- Remove batch (undo = restore batch)
- Issue stock (undo = return stock to main)

### 14.4 Validation Layer

**All inputs must be validated at controller level BEFORE service:**

- Product codes
- Quantities
- Amounts
- Dates
- String lengths

**Validation methods in controller:**

```java
ProductCode parseProductCode(String input)
int parsePositiveInteger(String input)
BigDecimal parsePositiveBigDecimal(String input)
Money parseMoney(String input)
LocalDate parseBusinessDate(String input)
LocalDate parseOptionalExpiryDate(String input)
UnitOfMeasure parseUnitOfMeasureChoice(int choice)
```

### 14.5 Thread Safety Considerations for Web

**Critical sections requiring synchronization:**

1. Inventory reduction (use database row locking)
2. Bill serial number generation (use database sequence/auto-increment)
3. Product code generation (lock on sequence query)
4. Stock reservation (atomic operations)

**Use connection pooling:**

- Each request gets own connection
- No shared database connection
- Thread-safe repositories with HikariCP

### 14.6 UI/UX Patterns to Preserve

**Cashier workflow:**

1. Welcome screen
2. Product search/entry
3. Add items incrementally
4. Show running total
5. Enter cash tendered
6. Calculate and display change
7. Confirm and save
8. Print receipt

**Inventory manager workflow:**

1. Role selection
2. Operation menu
3. Input gathering (step by step)
4. Validation feedback
5. Confirmation
6. Detailed result display
7. Return to menu

**Online customer workflow:**

1. Login/Register
2. Browse/Search
3. Add to cart
4. Review cart
5. Confirm order
6. Order confirmation

---

## 15. Testing Requirements

### 15.1 Unit Test Coverage

**Target:** 80%+ coverage

**Priority areas:**

1. Value objects (ProductCode, Money, BillSerialNumber)
2. Business logic in services
3. Validation in controllers
4. Repository CRUD operations
5. Command pattern execution/undo
6. Strategy pattern batch selection

### 15.2 Test Frameworks

- **JUnit 5** for unit tests
- **Mockito** for mocking dependencies
- **Testcontainers** for integration tests with real database

### 15.3 Critical Test Cases

**Batch selection:**

- Sufficient quantity available
- Insufficient quantity (select earliest expiry)
- Multiple batches, same expiry (FIFO)
- Newer batch earlier expiry (prioritize expiry)
- Null expiry dates (low priority)

**Inventory reduction:**

- Reduce physical store stock
- Reduce online store stock
- Update main inventory
- Create transaction record
- Insufficient stock error

**Bill creation:**

- Add items
- Calculate totals
- Process payment
- Generate serial number
- Save to database

**Customer registration:**

- Valid registration
- Duplicate email
- Short password
- Empty fields

---

## 16. Performance Considerations

### 16.1 Database Optimization

**Indexes (already defined):**

- Product lookups by code, category, brand
- Inventory by product, expiry
- Bills by date, customer, serial number
- Customer by email

**Query optimization:**

- Use prepared statements
- Batch operations where possible
- Limit result sets
- Use database views for complex queries

### 16.2 Connection Pooling

**HikariCP configuration:**

```other
maximumPoolSize=20
minimumIdle=5
connectionTimeout=30000
idleTimeout=600000
maxLifetime=1800000
```

### 16.3 Caching Strategies

**Consider caching:**

- Product catalog (rarely changes)
- Category/subcategory/brand codes
- Customer session data

**Do NOT cache:**

- Inventory levels (constantly changing)
- Bill totals
- Stock availability

---

## 17. Security Requirements

### 17.1 Authentication

**Customer authentication:**

- Password hashing (BCrypt recommended)
- Session management
- Secure password storage
- Password minimum length: 6 chars

### 17.2 Authorization

**Role-based access:**

- Cashier: POS operations only
- Inventory Manager: Product/inventory management
- Manager: Reports and analytics
- Customer: Online shopping only

### 17.3 Data Validation

**Always validate:**

- User inputs
- Database queries (prepared statements)
- File uploads (if added)
- API requests (if added)

### 17.4 SQL Injection Prevention

**Use prepared statements:**

```java
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM product WHERE product_code = ?"
);
stmt.setString(1, productCode);
```

**NEVER concatenate user input:**

```java
// BAD - DO NOT DO THIS
String sql = "SELECT * FROM product WHERE product_code = '" + productCode + "'";
```

---

## 18. Data Migration Notes

When migrating from CLI to web:

### 18.1 Database Schema

**No changes needed** - Schema is identical

### 18.2 Data Preservation

All existing data transfers directly:

- Products
- Inventory batches
- Bills
- Customers
- Transactions

### 18.3 Code Migration

**Preserve exactly:**

- Domain models (Product, Bill, etc.)
- Value objects (Money, ProductCode, etc.)
- Enums (StoreType, TransactionType, etc.)
- Service interfaces
- Business logic in services
- Repository interfaces

**Refactor:**

- Controllers (CLI → Servlets)
- UI (Console → JSP/HTML)
- Database connection (Singleton → HikariCP)
- Add thread safety (locks, transactions)

---

## 19. Quality Attributes

### 19.1 Maintainability

- **Clean code** practices throughout
- **SOLID principles** rigorously applied
- **Design patterns** appropriately used
- **Comprehensive comments** on business logic
- **Unit tests** for verification

### 19.2 Reliability

- **Exception handling** at all layers
- **Data validation** before persistence
- **Transaction management** for consistency
- **Database triggers** for integrity
- **Undo capability** for critical operations

### 19.3 Usability

- **Clear error messages**
- **Step-by-step workflows**
- **Input validation with feedback**
- **Confirmation for destructive operations**
- **Detailed operation results**

### 19.4 Performance

- **Database indexing** on frequent queries
- **Connection pooling** for scalability
- **Efficient batch selection** algorithm
- **Minimal database round trips**
- **Prepared statements** for query optimization

---

## Conclusion

This document contains the complete specification of the SYOS system. When implementing the web version with Claude Code:

1. **Use this as the single source of truth**
2. **Preserve all business logic exactly**
3. **Maintain all design patterns**
4. **Keep the database schema identical**
5. **Add thread safety where needed**
6. **Replace CLI with web interface**
7. **Use connection pooling instead of singleton**
8. **Add session management for web**

The goal is a functionally identical system with a web interface and multithreading capability, not a redesign. Every feature, validation rule, and business process documented here must be preserved in the web version.

**Good luck with the implementation!** 🚀