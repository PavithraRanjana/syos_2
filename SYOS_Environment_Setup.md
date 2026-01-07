# SYOS Web Application - Environment-Specific Setup Guide

## Your System Configuration

Based on your macOS system, here's your specific setup:

### Installed Software Versions

| Component | Version | Location |
|-----------|---------|----------|
| **Apache Tomcat** | 11.0.15 | `/opt/homebrew/Cellar/tomcat/11.0.15` |
| **Java (OpenJDK)** | 25.0.1 | `/opt/homebrew/opt/openjdk` |
| **MySQL** | 9.4.0 | Homebrew installation |
| **OS** | macOS 15 (ARM64) | Apple Silicon |

### Tomcat Configuration Path
- **Config files**: `/opt/homebrew/etc/tomcat`
- **Webapps**: `/opt/homebrew/opt/tomcat/libexec/webapps`
- **Logs**: `/opt/homebrew/opt/tomcat/libexec/logs`

---

## Project Configuration for Your Environment

### 1. Maven pom.xml Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.syos</groupId>
    <artifactId>syos-web</artifactId>
    <version>2.0.0</version>
    <packaging>war</packaging>
    
    <name>SYOS Retail Management System</name>
    
    <properties>
        <!-- Java 25 Configuration -->
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- Dependency Versions -->
        <jakarta.servlet.version>6.0.0</jakarta.servlet.version>
        <mysql.version>9.4.0</mysql.version>
        <hikaricp.version>5.1.0</hikaricp.version>
        <jackson.version>2.16.1</jackson.version>
        <junit.version>5.10.1</junit.version>
        <mockito.version>5.8.0</mockito.version>
    </properties>
    
    <dependencies>
        <!-- Jakarta Servlet API 6.0 (for Tomcat 11) -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>${jakarta.servlet.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Jakarta JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- JSTL -->
        <dependency>
            <groupId>jakarta.servlet.jsp.jstl</groupId>
            <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
            <version>3.0.0</version>
        </dependency>
        
        <dependency>
            <groupId>org.glassfish.web</groupId>
            <artifactId>jakarta.servlet.jsp.jstl</artifactId>
            <version>3.0.1</version>
        </dependency>
        
        <!-- MySQL Connector (matching your version) -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        
        <!-- HikariCP Connection Pool -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${hikaricp.version}</version>
        </dependency>
        
        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        
        <!-- Logging with SLF4J and Logback -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.11</version>
        </dependency>
        
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.14</version>
        </dependency>
        
        <!-- Apache Commons -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.14.0</version>
        </dependency>
        
        <!-- BCrypt for password hashing -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        
        <!-- Testcontainers for integration testing -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <finalName>syos</finalName>
        
        <plugins>
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>25</source>
                    <target>25</target>
                    <release>25</release>
                    <compilerArgs>
                        <arg>--enable-preview</arg> <!-- If using preview features -->
                    </compilerArgs>
                </configuration>
            </plugin>
            
            <!-- Maven WAR Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
            
            <!-- Maven Surefire Plugin for testing -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.3</version>
                <configuration>
                    <argLine>--enable-preview</argLine> <!-- If using preview features -->
                </configuration>
            </plugin>
            
            <!-- Tomcat Maven Plugin (optional - for local testing) -->
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat11-maven-plugin</artifactId>
                <version>3.0-r1655215</version>
                <configuration>
                    <url>http://localhost:8080/manager/text</url>
                    <server>TomcatServer</server>
                    <path>/syos</path>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2. Database Configuration (application.properties)

```properties
# Application Information
app.name=SYOS Retail Management System
app.version=2.0.0
app.environment=development

# Database Configuration for MySQL 9.4.0
db.url=jdbc:mysql://localhost:3306/syos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=syos_user
db.password=your_secure_password
db.driver=com.mysql.cj.jdbc.Driver

# HikariCP Configuration
hikari.maximumPoolSize=20
hikari.minimumIdle=5
hikari.connectionTimeout=30000
hikari.idleTimeout=600000
hikari.maxLifetime=1800000
hikari.leakDetectionThreshold=60000
hikari.poolName=SyosHikariPool

# MySQL Specific Optimizations
hikari.dataSource.cachePrepStmts=true
hikari.dataSource.prepStmtCacheSize=250
hikari.dataSource.prepStmtCacheSqlLimit=2048
hikari.dataSource.useServerPrepStmts=true

# Thread Pool Configuration
threadpool.core.size=10
threadpool.max.size=50
threadpool.queue.capacity=100
threadpool.keepalive.seconds=60

# Session Configuration
session.timeout.minutes=30
session.cookie.httponly=true
session.cookie.secure=false

# Logging Configuration
logging.level=INFO
logging.file.path=/opt/homebrew/var/log/syos
logging.pattern=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Development Mode
dev.mode=true
dev.show.sql=true
```

---

## 3. Tomcat Deployment Steps for Your System

### Start Tomcat

```bash
# Start Tomcat as a service
brew services start tomcat

# Or run directly
/opt/homebrew/opt/tomcat/bin/catalina run

# Check if running
ps aux | grep tomcat
```

### Build and Deploy WAR File

```bash
# Navigate to project directory
cd /path/to/syos-web

# Clean and build
mvn clean package

# Copy WAR to Tomcat webapps
cp target/syos.war /opt/homebrew/opt/tomcat/libexec/webapps/

# Tomcat will auto-deploy the WAR file
# Check deployment logs
tail -f /opt/homebrew/opt/tomcat/libexec/logs/catalina.out
```

### Access Application

```
http://localhost:8080/syos
```

### Stop Tomcat

```bash
# Stop service
brew services stop tomcat

# Or if running directly
/opt/homebrew/opt/tomcat/bin/catalina stop
```

---

## 4. MySQL Setup for Your Environment

### Create Database and User

```bash
# Connect to MySQL
mysql -u root -p

# Run these SQL commands
```

```sql
-- Create database
CREATE DATABASE syos_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'syos_user'@'localhost' IDENTIFIED BY 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON syos_db.* TO 'syos_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify
USE syos_db;
SHOW TABLES;
```

### Import Schema

```bash
# Import your schema
mysql -u syos_user -p syos_db < /path/to/SYOS_v2.sql
```

---

## 5. Tailwind CSS Integration (No Bootstrap)

### Option 1: CDN (Quick Start - Development)

Add to your JSP layout file:

```jsp
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SYOS - ${pageTitle}</title>
    
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- Optional: Custom Tailwind Config -->
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'syos-primary': '#1e40af',
                        'syos-secondary': '#7c3aed',
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-gray-50">
    <!-- Your content -->
</body>
</html>
```

### Option 2: Build Process (Production - Recommended)

#### Install Node.js and Tailwind

```bash
# Navigate to webapp directory
cd src/main/webapp

# Initialize npm
npm init -y

# Install Tailwind CSS
npm install -D tailwindcss

# Initialize Tailwind
npx tailwindcss init
```

#### Configure Tailwind (tailwind.config.js)

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./WEB-INF/views/**/*.jsp",
    "./static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        'syos-primary': '#1e40af',
        'syos-secondary': '#7c3aed',
        'syos-success': '#10b981',
        'syos-warning': '#f59e0b',
        'syos-danger': '#ef4444',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
```

#### Create Input CSS (static/css/input.css)

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom Components */
@layer components {
  .btn-primary {
    @apply bg-syos-primary hover:bg-blue-800 text-white font-semibold py-2 px-4 rounded transition duration-200;
  }
  
  .btn-secondary {
    @apply bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded transition duration-200;
  }
  
  .card {
    @apply bg-white rounded-lg shadow-md p-6;
  }
  
  .input-field {
    @apply w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-syos-primary focus:border-transparent;
  }
}
```

#### Build Script (package.json)

```json
{
  "name": "syos-web-styles",
  "scripts": {
    "build:css": "tailwindcss -i ./static/css/input.css -o ./static/css/output.css --minify",
    "watch:css": "tailwindcss -i ./static/css/input.css -o ./static/css/output.css --watch"
  },
  "devDependencies": {
    "tailwindcss": "^3.4.0",
    "@tailwindcss/forms": "^0.5.7"
  }
}
```

#### Build and Watch

```bash
# Build once
npm run build:css

# Watch for changes (during development)
npm run watch:css
```

#### Use in JSP

```jsp
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/output.css">
```

---

## 6. Example JSP Page with Tailwind

`WEB-INF/views/cashier/billing.jsp`:

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Billing - SYOS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/output.css">
</head>
<body class="bg-gray-100 min-h-screen">
    <!-- Header -->
    <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
            <div class="flex items-center justify-between">
                <h1 class="text-2xl font-bold text-gray-900">SYOS - Cashier Terminal</h1>
                <div class="flex items-center space-x-4">
                    <span class="text-sm text-gray-600">Cashier: ${sessionScope.user.name}</span>
                    <a href="${pageContext.request.contextPath}/logout" 
                       class="text-sm text-red-600 hover:text-red-800">Logout</a>
                </div>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- Product Search Panel -->
            <div class="lg:col-span-2">
                <div class="card">
                    <h2 class="text-xl font-semibold mb-4">Product Search</h2>
                    
                    <div class="mb-4">
                        <input type="text" 
                               id="productSearch" 
                               placeholder="Enter product code or name..."
                               class="input-field">
                    </div>
                    
                    <div id="productResults" class="space-y-2">
                        <!-- Dynamic product results will appear here -->
                    </div>
                </div>
                
                <!-- Cart Items -->
                <div class="card mt-6">
                    <h2 class="text-xl font-semibold mb-4">Cart Items</h2>
                    
                    <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Product
                                    </th>
                                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Price
                                    </th>
                                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Qty
                                    </th>
                                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Total
                                    </th>
                                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody id="cartTableBody" class="bg-white divide-y divide-gray-200">
                                <!-- Cart items will be added here dynamically -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Bill Summary Panel -->
            <div class="lg:col-span-1">
                <div class="card sticky top-4">
                    <h2 class="text-xl font-semibold mb-4">Bill Summary</h2>
                    
                    <div class="space-y-3 mb-6">
                        <div class="flex justify-between text-sm">
                            <span class="text-gray-600">Subtotal:</span>
                            <span id="subtotal" class="font-medium">Rs. 0.00</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="text-gray-600">Discount:</span>
                            <span id="discount" class="font-medium text-green-600">Rs. 0.00</span>
                        </div>
                        <div class="border-t pt-3 flex justify-between">
                            <span class="font-semibold">Total:</span>
                            <span id="total" class="text-xl font-bold text-syos-primary">Rs. 0.00</span>
                        </div>
                    </div>
                    
                    <div class="space-y-3">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">
                                Cash Tendered
                            </label>
                            <input type="number" 
                                   id="cashTendered" 
                                   placeholder="0.00"
                                   class="input-field">
                        </div>
                        
                        <div class="bg-gray-50 p-3 rounded">
                            <div class="flex justify-between text-sm">
                                <span class="text-gray-600">Change:</span>
                                <span id="change" class="font-semibold text-lg">Rs. 0.00</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="mt-6 space-y-2">
                        <button id="completeBillBtn" class="btn-primary w-full">
                            Complete Bill
                        </button>
                        <button id="clearCartBtn" class="btn-secondary w-full">
                            Clear Cart
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- JavaScript -->
    <script src="${pageContext.request.contextPath}/static/js/cashier.js"></script>
</body>
</html>
```

---

## 7. JavaScript Example with Fetch API

`static/js/cashier.js`:

```javascript
// API Base URL
const API_BASE = '/syos/api';

// State
let cart = [];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadProducts();
});

// Event Listeners
function setupEventListeners() {
    document.getElementById('productSearch').addEventListener('input', debounce(searchProducts, 300));
    document.getElementById('completeBillBtn').addEventListener('click', completeBill);
    document.getElementById('clearCartBtn').addEventListener('click', clearCart);
    document.getElementById('cashTendered').addEventListener('input', calculateChange);
}

// Search Products
async function searchProducts(e) {
    const query = e.target.value.trim();
    
    if (query.length < 2) {
        document.getElementById('productResults').innerHTML = '';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/products?search=${encodeURIComponent(query)}`);
        const result = await response.json();
        
        if (result.success) {
            displayProducts(result.data);
        }
    } catch (error) {
        console.error('Error searching products:', error);
        showError('Failed to search products');
    }
}

// Display Products
function displayProducts(products) {
    const container = document.getElementById('productResults');
    
    container.innerHTML = products.map(product => `
        <div class="flex items-center justify-between p-3 bg-gray-50 rounded hover:bg-gray-100 cursor-pointer"
             onclick="addToCart('${product.productCode}')">
            <div>
                <div class="font-medium">${product.productName}</div>
                <div class="text-sm text-gray-600">${product.productCode}</div>
            </div>
            <div class="text-right">
                <div class="font-semibold">Rs. ${product.unitPrice.toFixed(2)}</div>
                <div class="text-xs text-gray-500">Stock: ${product.availableStock}</div>
            </div>
        </div>
    `).join('');
}

// Add to Cart
async function addToCart(productCode) {
    try {
        const response = await fetch(`${API_BASE}/products/${productCode}`);
        const result = await response.json();
        
        if (result.success) {
            const product = result.data;
            
            // Check if already in cart
            const existing = cart.find(item => item.productCode === productCode);
            
            if (existing) {
                existing.quantity++;
            } else {
                cart.push({
                    productCode: product.productCode,
                    productName: product.productName,
                    unitPrice: product.unitPrice,
                    quantity: 1
                });
            }
            
            updateCartDisplay();
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
        showError('Failed to add product to cart');
    }
}

// Update Cart Display
function updateCartDisplay() {
    const tbody = document.getElementById('cartTableBody');
    
    tbody.innerHTML = cart.map((item, index) => `
        <tr>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${item.productName}</div>
                <div class="text-sm text-gray-500">${item.productCode}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                Rs. ${item.unitPrice.toFixed(2)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <input type="number" 
                       value="${item.quantity}" 
                       min="1"
                       class="w-20 px-2 py-1 border border-gray-300 rounded"
                       onchange="updateQuantity(${index}, this.value)">
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                Rs. ${(item.unitPrice * item.quantity).toFixed(2)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm">
                <button onclick="removeFromCart(${index})" 
                        class="text-red-600 hover:text-red-900">
                    Remove
                </button>
            </td>
        </tr>
    `).join('');
    
    updateBillSummary();
}

// Update Bill Summary
function updateBillSummary() {
    const subtotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
    const discount = 0; // Implement discount logic
    const total = subtotal - discount;
    
    document.getElementById('subtotal').textContent = `Rs. ${subtotal.toFixed(2)}`;
    document.getElementById('discount').textContent = `Rs. ${discount.toFixed(2)}`;
    document.getElementById('total').textContent = `Rs. ${total.toFixed(2)}`;
    
    calculateChange();
}

// Calculate Change
function calculateChange() {
    const total = parseFloat(document.getElementById('total').textContent.replace('Rs. ', ''));
    const tendered = parseFloat(document.getElementById('cashTendered').value) || 0;
    const change = tendered - total;
    
    document.getElementById('change').textContent = `Rs. ${Math.max(0, change).toFixed(2)}`;
}

// Complete Bill
async function completeBill() {
    const total = parseFloat(document.getElementById('total').textContent.replace('Rs. ', ''));
    const cashTendered = parseFloat(document.getElementById('cashTendered').value) || 0;
    
    if (cart.length === 0) {
        showError('Cart is empty');
        return;
    }
    
    if (cashTendered < total) {
        showError('Insufficient cash tendered');
        return;
    }
    
    const billData = {
        storeType: 'PHYSICAL',
        items: cart.map(item => ({
            productCode: item.productCode,
            quantity: item.quantity
        })),
        cashTendered: cashTendered
    };
    
    try {
        const response = await fetch(`${API_BASE}/billing`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(billData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            showSuccess('Bill completed successfully!');
            printReceipt(result.data);
            clearCart();
        } else {
            showError(result.message);
        }
    } catch (error) {
        console.error('Error completing bill:', error);
        showError('Failed to complete bill');
    }
}

// Utility Functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function showError(message) {
    // Implement error notification
    alert('Error: ' + message);
}

function showSuccess(message) {
    // Implement success notification
    alert('Success: ' + message);
}

function clearCart() {
    cart = [];
    updateCartDisplay();
    document.getElementById('cashTendered').value = '';
}

function updateQuantity(index, quantity) {
    cart[index].quantity = parseInt(quantity);
    updateCartDisplay();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    updateCartDisplay();
}

function printReceipt(bill) {
    // Implement receipt printing or display
    console.log('Receipt:', bill);
}
```

---

## 8. Development Workflow

### Daily Development

```bash
# Terminal 1: Watch Tailwind CSS
cd src/main/webapp
npm run watch:css

# Terminal 2: Build and deploy
cd /path/to/syos-web
mvn clean package && cp target/syos.war /opt/homebrew/opt/tomcat/libexec/webapps/

# Terminal 3: Watch logs
tail -f /opt/homebrew/opt/tomcat/libexec/logs/catalina.out
```

### Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test
mvn test -Dtest=ProductRepositoryImplTest
```

---

## 9. Production Deployment Checklist

- [ ] Update `application.properties` for production
- [ ] Set `dev.mode=false`
- [ ] Enable HTTPS (set `session.cookie.secure=true`)
- [ ] Build optimized CSS: `npm run build:css`
- [ ] Build production WAR: `mvn clean package -Pprod`
- [ ] Backup database before deployment
- [ ] Deploy WAR to Tomcat
- [ ] Verify all endpoints
- [ ] Monitor logs for errors
- [ ] Set up automated backups

---

## 10. Useful Commands Reference

```bash
# Tomcat Management
brew services start tomcat          # Start Tomcat
brew services stop tomcat           # Stop Tomcat
brew services restart tomcat        # Restart Tomcat

# View Tomcat logs
tail -f /opt/homebrew/opt/tomcat/libexec/logs/catalina.out

# MySQL Management
mysql.server start                  # Start MySQL
mysql.server stop                   # Stop MySQL
mysql -u syos_user -p syos_db      # Connect to database

# Maven Commands
mvn clean                          # Clean build artifacts
mvn compile                        # Compile source code
mvn test                          # Run tests
mvn package                       # Build WAR file
mvn clean package                 # Clean build
mvn install                       # Install to local repo

# Build and Deploy in one command
mvn clean package && cp target/syos.war /opt/homebrew/opt/tomcat/libexec/webapps/

# Tailwind CSS
npm run build:css                 # Build CSS once
npm run watch:css                 # Watch for changes
```

---

This configuration is specifically tailored for your macOS ARM64 system with Tomcat 11.0.15, Java 25, and MySQL 9.4.0. All paths and configurations match your Homebrew installation!
