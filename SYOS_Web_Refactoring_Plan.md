# SYOS Web Application Refactoring Plan

## Executive Summary

This document outlines the comprehensive plan to refactor the SYOS CLI application into a modern, multithreaded Java web application deployed on Apache Tomcat. The refactoring will preserve all existing SOLID principles and design patterns while introducing web technologies, concurrent processing, and improved scalability.

---

## Table of Contents

1. [Current State Analysis](#current-state-analysis)
2. [Target Architecture](#target-architecture)
3. [Technology Stack](#technology-stack)
4. [Design Patterns & SOLID Principles to Preserve](#design-patterns--solid-principles-to-preserve)
5. [New Architectural Components](#new-architectural-components)
6. [Multithreading Strategy](#multithreading-strategy)
7. [Project Structure](#project-structure)
8. [Implementation Phases](#implementation-phases)
9. [Database Optimization](#database-optimization)
10. [Security Considerations](#security-considerations)
11. [Testing Strategy](#testing-strategy)
12. [Deployment Configuration](#deployment-configuration)

---

## 1. Current State Analysis

### Existing Architecture Strengths

**Design Patterns Identified:**
- **Repository Pattern**: Clean data access abstraction (`Repository<T, ID>`)
- **Command Pattern**: Undo-capable inventory operations (`InventoryCommand`)
- **Strategy Pattern**: Pluggable batch selection algorithms (`BatchSelectionStrategy`)
- **Factory Pattern**: Product code generation (`ProductCodeGenerator`)
- **Singleton Pattern**: Database connection management (`DatabaseConnection`)
- **Service Layer Pattern**: Business logic encapsulation
- **Value Object Pattern**: `Money`, `ProductCode`, `BillSerialNumber`

**SOLID Principles Applied:**
- **Single Responsibility**: Each service has a focused purpose
- **Open/Closed**: Strategy pattern allows extension without modification
- **Liskov Substitution**: Interface-based design
- **Interface Segregation**: Focused interfaces (`PaymentService`, `BillingService`)
- **Dependency Inversion**: Controllers depend on service abstractions

**Current Layers:**
```
UI Layer (ConsoleUserInterface)
    ↓
Controller Layer (MainMenuController, PhysicalStoreController, etc.)
    ↓
Service Layer (BillingService, InventoryService, ProductService, etc.)
    ↓
Repository Layer (ProductRepository, InventoryRepository, etc.)
    ↓
Database Layer (MySQL)
```

---

## 2. Target Architecture

### Web Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Layer (Browser)                    │
│  - HTML5/CSS3/JavaScript                                     │
│  - AJAX for async operations                                 │
│  - Responsive design (Bootstrap/Tailwind)                    │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/HTTPS
┌────────────────────▼────────────────────────────────────────┐
│               Presentation Layer (Servlets/JSP)              │
│  - RESTful API Servlets                                      │
│  - Request/Response handling                                 │
│  - Session management                                        │
│  - Authentication filters                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                  Controller Layer (Web)                      │
│  - CashierController                                         │
│  - InventoryManagerController                                │
│  - OnlineStoreController                                     │
│  - SyosManagerController                                     │
│  - CustomerController                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    Service Layer                             │
│  - BillingService (with async processing)                   │
│  - InventoryService (thread-safe operations)                │
│  - ProductService                                            │
│  - CustomerService                                           │
│  - ReportService (with background generation)               │
│  - NotificationService (new - email/alerts)                 │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                 Repository Layer                             │
│  - Thread-safe repository implementations                    │
│  - Connection pooling (HikariCP)                            │
│  - Transaction management                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                  Database Layer (MySQL)                      │
│  - Optimized indexes                                         │
│  - Stored procedures for complex operations                 │
│  - Row-level locking for inventory                          │
└─────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────────┐
         │   Background Processing Layer        │
         │  - Report generation queue           │
         │  - Inventory alerts                  │
         │  - Email notifications                │
         │  - Batch operations                   │
         └──────────────────────────────────────┘
```

---

## 3. Technology Stack

### Core Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Server** | Apache Tomcat 11.0.15 | Servlet container |
| **Java Version** | Java 25.0.1 (OpenJDK) | Latest OpenJDK version |
| **Web Framework** | Jakarta Servlets 6.0 | HTTP request handling |
| **View Technology** | JSP 3.1 + JSTL | Server-side rendering |
| **Frontend** | HTML5, CSS3, JavaScript | Client-side UI |
| **CSS Framework** | Tailwind CSS 3.x | Utility-first CSS framework |
| **AJAX** | Fetch API / Axios | Async communication |
| **Database** | MySQL 9.4.0 | Data persistence |
| **Connection Pool** | HikariCP 5.x | Database connection management |
| **Build Tool** | Maven 3.9.x | Project management |
| **Testing** | JUnit 5 + Mockito | Unit testing |
| **Integration Testing** | Testcontainers | Database testing |
| **Logging** | SLF4J + Logback | Application logging |
| **JSON** | Jackson / Gson | JSON serialization |

### Additional Libraries

- **Apache Commons**: Utilities (Lang, Collections, IO)
- **Java Concurrency Utilities**: `ExecutorService`, `CompletableFuture`
- **Session Management**: HttpSession with Redis (optional for scaling)
- **Email**: JavaMail API for notifications
- **PDF Generation**: iText / Apache PDFBox for reports
- **Excel**: Apache POI for Excel reports

---

## 4. Design Patterns & SOLID Principles to Preserve

### Design Patterns Migration

#### 4.1 Repository Pattern ✅
**Preserve and Enhance:**
```java
// Make thread-safe with connection pooling
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void delete(ID id);
    boolean existsById(ID id);
    
    // New methods for web
    List<T> findWithPagination(int page, int size);
    long count();
}

// Thread-safe implementation
public class ProductRepositoryImpl implements ProductRepository {
    private final DataSource dataSource; // HikariCP pool
    
    @Override
    public synchronized Optional<Product> findById(String productCode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            // Implementation
        }
    }
}
```

#### 4.2 Command Pattern ✅
**Preserve with async execution:**
```java
public interface InventoryCommand {
    CommandResult execute() throws InventoryException;
    CommandResult undo() throws InventoryException;
    boolean canUndo();
    
    // New for web
    CompletableFuture<CommandResult> executeAsync();
}

// Async command executor
public class AsyncCommandExecutor {
    private final ExecutorService executorService;
    private final Deque<InventoryCommand> commandHistory;
    
    public CompletableFuture<CommandResult> execute(InventoryCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CommandResult result = command.execute();
                if (result.isSuccess() && command.canUndo()) {
                    commandHistory.push(command);
                }
                return result;
            } catch (Exception e) {
                return CommandResult.failure(e.getMessage());
            }
        }, executorService);
    }
}
```

#### 4.3 Strategy Pattern ✅
**Preserve exactly as is:**
```java
// Already perfect for web - no changes needed
public interface BatchSelectionStrategy {
    Optional<MainInventory> selectBatch(List<MainInventory> availableBatches,
                                        ProductCode productCode,
                                        int requiredQuantity);
    String getSelectionReason(MainInventory selectedBatch, 
                             List<MainInventory> availableBatches);
    String getStrategyName();
}
```

#### 4.4 Factory Pattern ✅
**Preserve with additional factories:**
```java
// Existing
public interface ProductCodeGenerator {
    ProductCode generateCode(String categoryCode, String brandCode, 
                            String itemName, LocalDate purchaseDate, 
                            LocalDate expiryDate);
}

// New for web
public class ResponseFactory {
    public static JsonResponse success(Object data) {
        return new JsonResponse(true, data, null);
    }
    
    public static JsonResponse error(String message) {
        return new JsonResponse(false, null, message);
    }
}
```

#### 4.5 Singleton Pattern
**Evolve to use Dependency Injection:**
```java
// OLD: Singleton DatabaseConnection
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private DatabaseConnection() {}
    public static synchronized DatabaseConnection getInstance() {...}
}

// NEW: Service Locator / DI Container pattern
public class ServiceRegistry {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    public static <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
}

// In ServletContextListener
public class AppInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize data source
        HikariDataSource dataSource = createDataSource();
        
        // Register services
        ServiceRegistry.register(DataSource.class, dataSource);
        ServiceRegistry.register(ProductRepository.class, 
            new ProductRepositoryImpl(dataSource));
        // ... register all services
    }
}
```

### SOLID Principles in Web Context

#### Single Responsibility Principle (SRP)
**Servlet Responsibilities:**
```java
// BAD: Servlet doing everything
public class ProductServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Parsing request ❌
        // Business logic ❌
        // Database access ❌
        // Response formatting ❌
    }
}

// GOOD: Separated concerns
public class ProductApiServlet extends HttpServlet {
    private final ProductService productService;
    private final RequestParser requestParser;
    private final ResponseFormatter responseFormatter;
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // Only orchestration
            ProductRequest request = requestParser.parse(req);
            Product product = productService.createProduct(request);
            responseFormatter.sendJson(resp, product);
        } catch (Exception e) {
            responseFormatter.sendError(resp, e.getMessage());
        }
    }
}
```

#### Open/Closed Principle (OCP)
**Extensible request handling:**
```java
// Strategy for different report formats
public interface ReportGenerator {
    byte[] generate(ReportData data);
    String getContentType();
}

public class PdfReportGenerator implements ReportGenerator { ... }
public class ExcelReportGenerator implements ReportGenerator { ... }
public class CsvReportGenerator implements ReportGenerator { ... }

// Servlet delegates to strategy
public class ReportServlet extends HttpServlet {
    private final Map<String, ReportGenerator> generators;
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String format = req.getParameter("format");
        ReportGenerator generator = generators.get(format);
        // Generate and send
    }
}
```

#### Liskov Substitution Principle (LSP)
**Interchangeable implementations:**
```java
// Any BillingService implementation works
public class BillingApiServlet extends HttpServlet {
    private final BillingService billingService; // Could be any implementation
    
    // PhysicalStoreBillingService or OnlineBillingService
    // Both work identically
}
```

#### Interface Segregation Principle (ISP)
**Focused servlet interfaces:**
```java
// Don't force servlets to implement unused methods
public abstract class BaseApiServlet extends HttpServlet {
    protected abstract void handleGet(HttpServletRequest req, HttpServletResponse resp);
    protected abstract void handlePost(HttpServletRequest req, HttpServletResponse resp);
    
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handleGet(req, resp);
    }
}

// Implement only what's needed
public class ProductApiServlet extends BaseApiServlet {
    @Override
    protected void handleGet(...) { /* list products */ }
    
    @Override
    protected void handlePost(...) { /* create product */ }
    // No need for PUT, DELETE if not used
}
```

#### Dependency Inversion Principle (DIP)
**Depend on abstractions:**
```java
// Controller depends on service interface
public class CashierController {
    private final BillingService billingService;      // Interface
    private final ProductService productService;      // Interface
    private final InventoryService inventoryService;  // Interface
    
    // Dependencies injected via constructor
    public CashierController(BillingService billingService,
                            ProductService productService,
                            InventoryService inventoryService) {
        this.billingService = billingService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }
}
```

---

## 5. New Architectural Components

### 5.1 Web Layer Components

#### Servlets (REST API)
```
/api/products/*          → ProductApiServlet
/api/inventory/*         → InventoryApiServlet
/api/billing/*           → BillingApiServlet
/api/customers/*         → CustomerApiServlet
/api/reports/*           → ReportApiServlet
/api/auth/*              → AuthenticationServlet
```

#### Filters
```java
// Authentication filter
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            // Redirect to login
        } else {
            chain.doFilter(request, response);
        }
    }
}

// CORS filter for AJAX
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        chain.doFilter(request, response);
    }
}

// Logging filter
public class RequestLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("{} {} - {}ms", 
                httpRequest.getMethod(), 
                httpRequest.getRequestURI(), 
                duration);
        }
    }
}
```

#### Listeners
```java
// Application initialization
public class AppContextListener implements ServletContextListener {
    private ExecutorService executorService;
    private HikariDataSource dataSource;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize thread pool
        executorService = Executors.newFixedThreadPool(10);
        
        // Initialize database connection pool
        dataSource = createHikariDataSource();
        
        // Initialize and register all services
        initializeServices(sce.getServletContext());
        
        // Start background tasks
        startBackgroundTasks();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Shutdown thread pool
        executorService.shutdown();
        
        // Close data source
        dataSource.close();
    }
}

// Session listener for cleanup
public class SessionListener implements HttpSessionListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Clean up user-specific resources
        HttpSession session = se.getSession();
        // Remove from active users, clear carts, etc.
    }
}
```

### 5.2 DTOs and Request/Response Models

```java
// Request DTOs
public class CreateProductRequest {
    private String productName;
    private String categoryId;
    private String brandId;
    private BigDecimal unitPrice;
    // Validation annotations
    
    // Getters, setters
}

public class BillingRequest {
    private String storeType;
    private Integer customerId;
    private List<BillItemRequest> items;
    private BigDecimal cashTendered;
    // Getters, setters
}

// Response DTOs
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now().toString();
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = LocalDateTime.now().toString();
        return response;
    }
}

public class ProductResponse {
    private String productCode;
    private String productName;
    private String categoryName;
    private String brandName;
    private BigDecimal unitPrice;
    private int availableStock;
    // Getters, setters
}
```

### 5.3 Utility Classes

```java
// JSON utility
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    public static String toJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) 
            throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }
}

// Request parser
public class RequestParser {
    public static <T> T parseBody(HttpServletRequest request, Class<T> clazz) 
            throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return JsonUtil.fromJson(sb.toString(), clazz);
    }
}

// Response formatter
public class ResponseFormatter {
    public static void sendJson(HttpServletResponse response, Object data) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(ApiResponse.success(data)));
    }
    
    public static void sendError(HttpServletResponse response, String message, 
                                int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(ApiResponse.error(message)));
    }
}
```

---

## 6. Multithreading Strategy

### 6.1 Thread Safety Requirements

**Critical Sections Requiring Synchronization:**
1. **Inventory Updates**: Multiple cashiers selling simultaneously
2. **Bill Serial Number Generation**: Ensuring uniqueness
3. **Stock Restocking**: Preventing race conditions
4. **Report Generation**: Resource-intensive operations

### 6.2 Concurrency Patterns

#### Thread-Safe Repository Implementation
```java
public class ProductRepositoryImpl implements ProductRepository {
    private final DataSource dataSource; // HikariCP handles pooling
    
    // Connection pooling ensures thread safety
    @Override
    public Optional<Product> findById(String productCode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            stmt.setString(1, productCode);
            try (ResultSet rs = stmt.executeQuery()) {
                // Each thread gets its own connection
                return rs.next() ? Optional.of(mapToProduct(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding product", e);
        }
    }
}
```

#### Thread-Safe Inventory Operations
```java
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final Lock inventoryLock = new ReentrantLock();
    
    @Override
    public void reduceStock(String productCode, int quantity, String storeType) 
            throws InsufficientStockException {
        // Lock to prevent race conditions
        inventoryLock.lock();
        try {
            // 1. Check available stock
            int available = inventoryRepository.getAvailableStock(productCode, storeType);
            
            if (available < quantity) {
                throw new InsufficientStockException(
                    "Insufficient stock for " + productCode);
            }
            
            // 2. Reduce stock with database-level locking
            inventoryRepository.reduceStockWithLock(productCode, quantity, storeType);
            
        } finally {
            inventoryLock.unlock();
        }
    }
}
```

#### Database-Level Locking
```sql
-- Pessimistic locking for inventory updates
UPDATE physical_store_inventory 
SET quantity_on_shelf = quantity_on_shelf - ? 
WHERE product_code = ? 
  AND quantity_on_shelf >= ?
  FOR UPDATE; -- Row-level lock

-- Optimistic locking with version field (alternative)
ALTER TABLE physical_store_inventory ADD COLUMN version INT DEFAULT 0;

UPDATE physical_store_inventory 
SET quantity_on_shelf = quantity_on_shelf - ?, 
    version = version + 1
WHERE product_code = ? 
  AND version = ?
  AND quantity_on_shelf >= ?;
```

#### Async Report Generation
```java
public class ReportServiceImpl implements ReportService {
    private final ExecutorService reportExecutor;
    private final ReportRepository reportRepository;
    private final Map<String, CompletableFuture<Report>> pendingReports;
    
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
        this.reportExecutor = Executors.newFixedThreadPool(3);
        this.pendingReports = new ConcurrentHashMap<>();
    }
    
    public String generateSalesReportAsync(LocalDate startDate, LocalDate endDate) {
        String reportId = UUID.randomUUID().toString();
        
        CompletableFuture<Report> future = CompletableFuture.supplyAsync(() -> {
            try {
                // Heavy computation
                List<SalesData> data = reportRepository.getSalesData(startDate, endDate);
                Report report = buildReport(data);
                return report;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, reportExecutor);
        
        pendingReports.put(reportId, future);
        return reportId;
    }
    
    public Optional<Report> getReport(String reportId) {
        CompletableFuture<Report> future = pendingReports.get(reportId);
        if (future == null) return Optional.empty();
        
        if (future.isDone()) {
            try {
                return Optional.of(future.get());
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty(); // Still processing
    }
}
```

#### Billing Service with Transactions
```java
public class BillingServiceImpl implements BillingService {
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final BillRepository billRepository;
    private final DataSource dataSource;
    
    @Override
    public Bill createBill(BillingRequest request) throws BillingException {
        Connection conn = null;
        try {
            // Get connection and start transaction
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Create bill
            Bill bill = new Bill(/* ... */);
            billRepository.save(bill, conn);
            
            // 2. Process each item
            for (BillItemRequest itemRequest : request.getItems()) {
                // Check and reduce inventory
                inventoryService.reduceStock(
                    itemRequest.getProductCode(),
                    itemRequest.getQuantity(),
                    request.getStoreType(),
                    conn // Use same connection
                );
                
                // Add bill item
                BillItem item = new BillItem(/* ... */);
                billRepository.addBillItem(item, conn);
            }
            
            // 3. Commit transaction
            conn.commit();
            return bill;
            
        } catch (Exception e) {
            // Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Log rollback error
                }
            }
            throw new BillingException("Failed to create bill", e);
        } finally {
            // Return connection to pool
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Log error
                }
            }
        }
    }
}
```

### 6.3 Thread Pool Configuration

```java
public class ThreadPoolConfig {
    // API request handling
    public static ExecutorService createApiThreadPool() {
        return new ThreadPoolExecutor(
            10,  // Core pool size
            50,  // Maximum pool size
            60L, TimeUnit.SECONDS, // Keep alive time
            new LinkedBlockingQueue<>(100), // Queue capacity
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }
    
    // Background tasks (reports, notifications)
    public static ScheduledExecutorService createBackgroundTaskExecutor() {
        return Executors.newScheduledThreadPool(5);
    }
    
    // Inventory operations (higher priority)
    public static ExecutorService createInventoryThreadPool() {
        return new ThreadPoolExecutor(
            5,
            20,
            30L, TimeUnit.SECONDS,
            new PriorityBlockingQueue<>(), // Priority queue
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
```

### 6.4 Background Tasks

```java
public class BackgroundTaskManager {
    private final ScheduledExecutorService scheduler;
    
    public void startScheduledTasks() {
        // Check for expiring products every hour
        scheduler.scheduleAtFixedRate(() -> {
            checkExpiringProducts();
        }, 0, 1, TimeUnit.HOURS);
        
        // Generate daily sales report at midnight
        scheduler.scheduleAtFixedRate(() -> {
            generateDailySalesReport();
        }, getTimeUntilMidnight(), 24, TimeUnit.HOURS);
        
        // Clean up old sessions every 30 minutes
        scheduler.scheduleAtFixedRate(() -> {
            cleanupExpiredSessions();
        }, 30, 30, TimeUnit.MINUTES);
    }
    
    private void checkExpiringProducts() {
        try {
            LocalDate threshold = LocalDate.now().plusDays(7);
            List<Product> expiring = inventoryService.getExpiringProducts(threshold);
            
            if (!expiring.isEmpty()) {
                notificationService.sendExpiryAlert(expiring);
            }
        } catch (Exception e) {
            logger.error("Error checking expiring products", e);
        }
    }
}
```

---

## 7. Project Structure

```
syos-web/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── syos/
│   │   │           ├── config/
│   │   │           │   ├── AppConfig.java
│   │   │           │   ├── DataSourceConfig.java
│   │   │           │   ├── ThreadPoolConfig.java
│   │   │           │   └── ServiceRegistry.java
│   │   │           │
│   │   │           ├── domain/
│   │   │           │   ├── models/
│   │   │           │   │   ├── Product.java
│   │   │           │   │   ├── Bill.java
│   │   │           │   │   ├── BillItem.java
│   │   │           │   │   ├── Customer.java
│   │   │           │   │   ├── MainInventory.java
│   │   │           │   │   ├── PhysicalStoreInventory.java
│   │   │           │   │   └── OnlineStoreInventory.java
│   │   │           │   │
│   │   │           │   ├── valueobjects/
│   │   │           │   │   ├── ProductCode.java
│   │   │           │   │   ├── Money.java
│   │   │           │   │   └── BillSerialNumber.java
│   │   │           │   │
│   │   │           │   └── enums/
│   │   │           │       ├── StoreType.java
│   │   │           │       ├── TransactionType.java
│   │   │           │       └── UnitOfMeasure.java
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   ├── interfaces/
│   │   │           │   │   ├── Repository.java
│   │   │           │   │   ├── ProductRepository.java
│   │   │           │   │   ├── BillRepository.java
│   │   │           │   │   ├── CustomerRepository.java
│   │   │           │   │   ├── InventoryRepository.java
│   │   │           │   │   └── ReportRepository.java
│   │   │           │   │
│   │   │           │   └── impl/
│   │   │           │       ├── ProductRepositoryImpl.java
│   │   │           │       ├── BillRepositoryImpl.java
│   │   │           │       ├── CustomerRepositoryImpl.java
│   │   │           │       ├── InventoryRepositoryImpl.java
│   │   │           │       └── ReportRepositoryImpl.java
│   │   │           │
│   │   │           ├── service/
│   │   │           │   ├── interfaces/
│   │   │           │   │   ├── ProductService.java
│   │   │           │   │   ├── BillingService.java
│   │   │           │   │   ├── InventoryService.java
│   │   │           │   │   ├── CustomerService.java
│   │   │           │   │   ├── ReportService.java
│   │   │           │   │   ├── AuthenticationService.java
│   │   │           │   │   ├── NotificationService.java
│   │   │           │   │   ├── InventoryCommand.java
│   │   │           │   │   ├── BatchSelectionStrategy.java
│   │   │           │   │   └── ProductCodeGenerator.java
│   │   │           │   │
│   │   │           │   └── impl/
│   │   │           │       ├── ProductServiceImpl.java
│   │   │           │       ├── BillingServiceImpl.java
│   │   │           │       ├── InventoryServiceImpl.java
│   │   │           │       ├── CustomerServiceImpl.java
│   │   │           │       ├── ReportServiceImpl.java
│   │   │           │       ├── AuthenticationServiceImpl.java
│   │   │           │       ├── EmailNotificationService.java
│   │   │           │       ├── ProductCodeGeneratorImpl.java
│   │   │           │       ├── FIFOWithExpiryStrategy.java
│   │   │           │       ├── AddBatchCommand.java
│   │   │           │       ├── RemoveBatchCommand.java
│   │   │           │       └── IssueStockCommand.java
│   │   │           │
│   │   │           ├── web/
│   │   │           │   ├── servlet/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── ProductApiServlet.java
│   │   │           │   │   │   ├── InventoryApiServlet.java
│   │   │           │   │   │   ├── BillingApiServlet.java
│   │   │           │   │   │   ├── CustomerApiServlet.java
│   │   │           │   │   │   ├── ReportApiServlet.java
│   │   │           │   │   │   └── AuthApiServlet.java
│   │   │           │   │   │
│   │   │           │   │   └── view/
│   │   │           │   │       ├── HomeServlet.java
│   │   │           │   │       ├── CashierServlet.java
│   │   │           │   │       ├── InventoryManagerServlet.java
│   │   │           │   │       ├── ManagerServlet.java
│   │   │           │   │       └── OnlineStoreServlet.java
│   │   │           │   │
│   │   │           │   ├── filter/
│   │   │           │   │   ├── AuthenticationFilter.java
│   │   │           │   │   ├── CorsFilter.java
│   │   │           │   │   ├── LoggingFilter.java
│   │   │           │   │   └── EncodingFilter.java
│   │   │           │   │
│   │   │           │   ├── listener/
│   │   │           │   │   ├── AppContextListener.java
│   │   │           │   │   ├── SessionListener.java
│   │   │           │   │   └── RequestListener.java
│   │   │           │   │
│   │   │           │   └── dto/
│   │   │           │       ├── request/
│   │   │           │       │   ├── CreateProductRequest.java
│   │   │           │       │   ├── BillingRequest.java
│   │   │           │       │   ├── LoginRequest.java
│   │   │           │       │   └── RestockRequest.java
│   │   │           │       │
│   │   │           │       └── response/
│   │   │           │           ├── ApiResponse.java
│   │   │           │           ├── ProductResponse.java
│   │   │           │           ├── BillResponse.java
│   │   │           │           └── ReportResponse.java
│   │   │           │
│   │   │           ├── exception/
│   │   │           │   ├── BillingException.java
│   │   │           │   ├── InsufficientStockException.java
│   │   │           │   ├── ProductNotFoundException.java
│   │   │           │   ├── CustomerNotFoundException.java
│   │   │           │   ├── InvalidLoginException.java
│   │   │           │   └── InventoryException.java
│   │   │           │
│   │   │           └── util/
│   │   │               ├── JsonUtil.java
│   │   │               ├── RequestParser.java
│   │   │               ├── ResponseFormatter.java
│   │   │               ├── ValidationUtil.java
│   │   │               ├── DateUtil.java
│   │   │               └── PasswordUtil.java
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── logback.xml
│   │   │   └── db/
│   │   │       ├── schema.sql
│   │   │       └── data.sql
│   │   │
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   ├── views/
│   │       │   │   ├── index.jsp
│   │       │   │   ├── login.jsp
│   │       │   │   ├── cashier/
│   │       │   │   │   ├── dashboard.jsp
│   │       │   │   │   ├── billing.jsp
│   │       │   │   │   └── receipt.jsp
│   │       │   │   ├── inventory/
│   │       │   │   │   ├── dashboard.jsp
│   │       │   │   │   ├── products.jsp
│   │       │   │   │   ├── stock.jsp
│   │       │   │   │   └── restock.jsp
│   │       │   │   ├── manager/
│   │       │   │   │   ├── dashboard.jsp
│   │       │   │   │   ├── reports.jsp
│   │       │   │   │   └── analytics.jsp
│   │       │   │   └── online/
│   │       │   │       ├── home.jsp
│   │       │   │       ├── products.jsp
│   │       │   │       ├── cart.jsp
│   │       │   │       └── checkout.jsp
│   │       │   │
│   │       │   └── tags/
│   │       │       └── common.tag
│   │       │
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── main.css
│   │       │   │   ├── cashier.css
│   │       │   │   ├── inventory.css
│   │       │   │   └── online-store.css
│   │       │   │
│   │       │   ├── js/
│   │       │   │   ├── main.js
│   │       │   │   ├── api-client.js
│   │       │   │   ├── cashier.js
│   │       │   │   ├── inventory.js
│   │       │   │   ├── reports.js
│   │       │   │   └── online-store.js
│   │       │   │
│   │       │   └── images/
│   │       │       ├── logo.png
│   │       │       └── icons/
│   │       │
│   │       └── index.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── syos/
│                   ├── repository/
│                   │   └── ProductRepositoryImplTest.java
│                   ├── service/
│                   │   ├── BillingServiceTest.java
│                   │   ├── InventoryServiceTest.java
│                   │   └── ConcurrencyTest.java
│                   ├── web/
│                   │   └── servlet/
│                   │       └── ProductApiServletTest.java
│                   └── integration/
│                       └── BillingIntegrationTest.java
│
├── pom.xml
├── README.md
└── .gitignore
```

---

## 8. Implementation Phases

### Phase 1: Foundation & Infrastructure (Week 1-2)

**Goals:**
- Set up project structure
- Configure Maven build
- Set up database connection pooling
- Implement base classes and interfaces

**Tasks:**
1. Create Maven project with proper structure
2. Add all dependencies to pom.xml
3. Migrate domain models (as-is)
4. Migrate value objects (as-is)
5. Set up HikariCP configuration
6. Create ServiceRegistry for DI
7. Implement base servlet classes
8. Set up logging (Logback)
9. Create exception hierarchy
10. Write utility classes (JsonUtil, RequestParser, etc.)

**Deliverables:**
- Compilable project skeleton
- Database connection working
- Basic configuration complete

### Phase 2: Repository Layer (Week 3)

**Goals:**
- Migrate all repository interfaces
- Implement thread-safe repositories
- Add pagination support

**Tasks:**
1. Migrate Repository interface
2. Implement ProductRepositoryImpl with HikariCP
3. Implement InventoryRepositoryImpl with row locking
4. Implement BillRepositoryImpl
5. Implement CustomerRepositoryImpl
6. Implement ReportRepositoryImpl
7. Add pagination methods to all repositories
8. Write repository unit tests with Testcontainers
9. Test concurrent access scenarios

**Deliverables:**
- All repositories working with connection pool
- 100% test coverage for repositories
- Thread safety verified

### Phase 3: Service Layer (Week 4-5)

**Goals:**
- Migrate business logic
- Implement thread-safe services
- Add async operations

**Tasks:**
1. Migrate ProductService
2. Migrate and enhance InventoryService (add locking)
3. Migrate BillingService (add transaction support)
4. Migrate CustomerService
5. Migrate ReportService (add async generation)
6. Implement AuthenticationService (new)
7. Implement NotificationService (new)
8. Preserve Command pattern (AddBatchCommand, etc.)
9. Preserve Strategy pattern (BatchSelectionStrategy)
10. Preserve Factory pattern (ProductCodeGenerator)
11. Write service unit tests
12. Write concurrency tests

**Deliverables:**
- All services working with thread safety
- Command pattern with async support
- Service tests passing

### Phase 4: Web Layer - API (Week 6)

**Goals:**
- Create RESTful API servlets
- Implement filters
- Set up session management

**Tasks:**
1. Create base servlet classes
2. Implement ProductApiServlet
3. Implement InventoryApiServlet
4. Implement BillingApiServlet
5. Implement CustomerApiServlet
6. Implement ReportApiServlet
7. Implement AuthApiServlet
8. Create AuthenticationFilter
9. Create CorsFilter
10. Create LoggingFilter
11. Implement request/response DTOs
12. Write servlet integration tests

**Deliverables:**
- RESTful API fully functional
- Authentication working
- API documentation

### Phase 5: Web Layer - Views (Week 7-8)

**Goals:**
- Create JSP pages
- Implement frontend JavaScript
- Create responsive UI

**Tasks:**
1. Design UI wireframes
2. Create login page
3. Create cashier dashboard
4. Create billing interface
5. Create inventory manager interface
6. Create manager dashboard
7. Create online store interface
8. Implement AJAX calls
9. Add form validation
10. Make responsive (Bootstrap/Tailwind)
11. Add loading indicators
12. Implement error handling in UI

**Deliverables:**
- Complete web interface
- All user roles functional
- Responsive design

### Phase 6: Advanced Features (Week 9)

**Goals:**
- Implement background tasks
- Add real-time features
- Enhance reporting

**Tasks:**
1. Implement async report generation
2. Create background task scheduler
3. Add expiry alerts
4. Implement email notifications
5. Add export to PDF/Excel
6. Implement real-time stock updates (WebSocket/SSE)
7. Add cart functionality for online store
8. Implement order tracking

**Deliverables:**
- Background tasks running
- Real-time updates working
- Advanced features complete

### Phase 7: Testing & Optimization (Week 10)

**Goals:**
- Comprehensive testing
- Performance optimization
- Security hardening

**Tasks:**
1. Load testing (JMeter)
2. Concurrent user testing
3. Database query optimization
4. Add caching where appropriate
5. Security audit
6. Fix performance bottlenecks
7. Complete integration testing
8. User acceptance testing

**Deliverables:**
- Performance benchmarks
- All tests passing
- Security audit complete

### Phase 8: Deployment (Week 11)

**Goals:**
- Deploy to Tomcat
- Configure production environment
- Documentation

**Tasks:**
1. Create WAR file
2. Configure Tomcat server
3. Set up production database
4. Configure connection pooling for production
5. Set up logging
6. Create deployment scripts
7. Write deployment documentation
8. Write user manual
9. Create API documentation
10. Conduct final testing in production

**Deliverables:**
- Application deployed on Tomcat
- Complete documentation
- Production-ready system

---

## 9. Database Optimization

### 9.1 Schema Enhancements

```sql
-- Add indexes for frequent queries
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_bill_date ON bill(bill_date);
CREATE INDEX idx_bill_customer ON bill(customer_id);
CREATE INDEX idx_inventory_product ON main_inventory(product_code);
CREATE INDEX idx_inventory_expiry ON main_inventory(expiry_date);

-- Composite indexes for common queries
CREATE INDEX idx_physical_inventory_product_batch 
    ON physical_store_inventory(product_code, main_inventory_id);
CREATE INDEX idx_online_inventory_product_batch 
    ON online_store_inventory(product_code, main_inventory_id);

-- Add version column for optimistic locking
ALTER TABLE physical_store_inventory ADD COLUMN version INT DEFAULT 0;
ALTER TABLE online_store_inventory ADD COLUMN version INT DEFAULT 0;
ALTER TABLE main_inventory ADD COLUMN version INT DEFAULT 0;
```

### 9.2 Stored Procedures

```sql
-- Procedure for stock reduction with validation
DELIMITER $$

CREATE PROCEDURE reduce_physical_stock(
    IN p_product_code VARCHAR(50),
    IN p_quantity INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE available_stock INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_success = FALSE;
        SET p_message = 'Error reducing stock';
        ROLLBACK;
    END;
    
    START TRANSACTION;
    
    -- Lock and check stock
    SELECT SUM(quantity_on_shelf) INTO available_stock
    FROM physical_store_inventory
    WHERE product_code = p_product_code
    FOR UPDATE;
    
    IF available_stock >= p_quantity THEN
        -- Reduce stock (FIFO logic here)
        UPDATE physical_store_inventory
        SET quantity_on_shelf = quantity_on_shelf - p_quantity
        WHERE product_code = p_product_code
        LIMIT 1;
        
        SET p_success = TRUE;
        SET p_message = 'Stock reduced successfully';
        COMMIT;
    ELSE
        SET p_success = FALSE;
        SET p_message = 'Insufficient stock';
        ROLLBACK;
    END IF;
END$$

DELIMITER ;
```

### 9.3 Connection Pool Configuration

```properties
# HikariCP configuration
hikari.maximumPoolSize=20
hikari.minimumIdle=5
hikari.connectionTimeout=30000
hikari.idleTimeout=600000
hikari.maxLifetime=1800000
hikari.leakDetectionThreshold=60000

# MySQL optimization
hikari.dataSource.cachePrepStmts=true
hikari.dataSource.prepStmtCacheSize=250
hikari.dataSource.prepStmtCacheSqlLimit=2048
hikari.dataSource.useServerPrepStmts=true
hikari.dataSource.useLocalSessionState=true
hikari.dataSource.rewriteBatchedStatements=true
hikari.dataSource.cacheResultSetMetadata=true
hikari.dataSource.cacheServerConfiguration=true
hikari.dataSource.elideSetAutoCommits=true
hikari.dataSource.maintainTimeStats=false
```

---

## 10. Security Considerations

### 10.1 Authentication

```java
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}

// Password hashing with BCrypt
public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = 
        new BCryptPasswordEncoder(12);
    
    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
```

### 10.2 Session Management

```java
public class AuthenticationFilter implements Filter {
    private static final List<String> PUBLIC_URLS = Arrays.asList(
        "/login", "/api/auth/login", "/static/"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // Allow public URLs
        if (isPublicURL(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check session
        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? 
            (User) session.getAttribute("user") : null;
        
        if (user != null) {
            // Update last activity
            session.setAttribute("lastActivity", System.currentTimeMillis());
            chain.doFilter(request, response);
        } else {
            // Redirect to login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
    
    private boolean isPublicURL(String uri) {
        return PUBLIC_URLS.stream().anyMatch(uri::startsWith);
    }
}
```

### 10.3 SQL Injection Prevention

```java
// ALWAYS use PreparedStatement
public Optional<Product> findByCode(String productCode) {
    String sql = "SELECT * FROM product WHERE product_code = ?";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, productCode); // Safe parameterization
        
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(mapToProduct(rs)) : Optional.empty();
        }
    } catch (SQLException e) {
        throw new RepositoryException("Error finding product", e);
    }
}

// NEVER concatenate user input
// BAD: "SELECT * FROM product WHERE product_code = '" + productCode + "'";
```

### 10.4 XSS Prevention

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Escape all user input -->
<p>Product Name: <c:out value="${product.name}" /></p>

<!-- JavaScript escaping -->
<script>
    var productName = '<c:out value="${product.name}" escapeXml="true" />';
</script>
```

### 10.5 CSRF Protection

```java
public class CsrfFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String sessionToken = (String) httpRequest.getSession()
                .getAttribute("csrf-token");
            String requestToken = httpRequest.getParameter("csrf-token");
            
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                ((HttpServletResponse) response).sendError(403, "Invalid CSRF token");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## 11. Testing Strategy

### 11.1 Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
    @Mock
    private ProductService productService;
    
    @Mock
    private InventoryService inventoryService;
    
    @Mock
    private BillRepository billRepository;
    
    @InjectMocks
    private BillingServiceImpl billingService;
    
    @Test
    void shouldCreateBillSuccessfully() throws Exception {
        // Given
        BillingRequest request = createBillingRequest();
        when(productService.findByCode(anyString()))
            .thenReturn(Optional.of(createProduct()));
        when(inventoryService.hasStock(anyString(), anyInt()))
            .thenReturn(true);
        
        // When
        Bill bill = billingService.createBill(request);
        
        // Then
        assertNotNull(bill);
        assertEquals(2, bill.getItems().size());
        verify(inventoryService, times(2)).reduceStock(anyString(), anyInt(), anyString());
    }
}
```

### 11.2 Integration Testing

```java
@Testcontainers
class BillingIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("syos_test")
        .withUsername("test")
        .withPassword("test");
    
    private DataSource dataSource;
    private BillingService billingService;
    
    @BeforeEach
    void setUp() {
        dataSource = createDataSource(mysql);
        // Initialize all dependencies
        ProductRepository productRepo = new ProductRepositoryImpl(dataSource);
        InventoryRepository inventoryRepo = new InventoryRepositoryImpl(dataSource);
        // ... initialize services
        billingService = new BillingServiceImpl(...);
    }
    
    @Test
    void shouldCreateBillAndReduceInventory() throws Exception {
        // Test actual database operations
    }
}
```

### 11.3 Concurrency Testing

```java
@Test
void shouldHandleConcurrentBillingCorrectly() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    int productStock = 100;
    int numberOfThreads = 10;
    int quantityPerThread = 5;
    
    // Setup: Create product with 100 stock
    setupProductWithStock("P001", productStock);
    
    // Execute: 10 threads trying to buy 5 items each
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    List<CompletableFuture<Bill>> futures = new ArrayList<>();
    
    for (int i = 0; i < numberOfThreads; i++) {
        CompletableFuture<Bill> future = CompletableFuture.supplyAsync(() -> {
            try {
                latch.countDown();
                latch.await(); // All threads start together
                BillingRequest request = createRequest("P001", quantityPerThread);
                return billingService.createBill(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
        futures.add(future);
    }
    
    // Wait for all to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // Verify: Stock should be reduced correctly
    int remainingStock = inventoryService.getAvailableStock("P001");
    assertEquals(50, remainingStock); // 100 - (10 * 5)
}
```

### 11.4 Load Testing (JMeter)

```xml
<!-- JMeter Test Plan -->
<jmeterTestPlan>
  <hashTree>
    <TestPlan>
      <ThreadGroup>
        <numberThreads>100</numberThreads>
        <rampTime>10</rampTime>
        <loopCount>10</loopCount>
      </ThreadGroup>
      <HTTPSamplerProxy>
        <path>/api/billing</path>
        <method>POST</method>
        <body>{...}</body>
      </HTTPSamplerProxy>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

---

## 12. Deployment Configuration

### 12.1 pom.xml

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
    
    <properties>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Jakarta Servlet API -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
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
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
        
        <!-- HikariCP -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>
        
        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.16.1</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.16.1</version>
        </dependency>
        
        <!-- Logging -->
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
        
        <!-- Password Hashing -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
        
        <!-- JavaMail -->
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>jakarta.mail</artifactId>
            <version>2.0.1</version>
        </dependency>
        
        <!-- PDF Generation -->
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itext7-core</artifactId>
            <version>8.0.3</version>
            <type>pom</type>
        </dependency>
        
        <!-- Excel -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.5</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.8.0</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>5.8.0</version>
            <scope>test</scope>
        </dependency>
        
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
    </dependencies>
    
    <build>
        <finalName>syos</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.3</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 12.2 web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    
    <display-name>SYOS Retail Management System</display-name>
    
    <!-- Context Parameters -->
    <context-param>
        <param-name>database.url</param-name>
        <param-value>jdbc:mysql://localhost:3306/syos_db</param-value>
    </context-param>
    
    <!-- Listeners -->
    <listener>
        <listener-class>com.syos.web.listener.AppContextListener</listener-class>
    </listener>
    
    <listener>
        <listener-class>com.syos.web.listener.SessionListener</listener-class>
    </listener>
    
    <!-- Filters -->
    <filter>
        <filter-name>EncodingFilter</filter-name>
        <filter-class>com.syos.web.filter.EncodingFilter</filter-class>
    </filter>
    
    <filter>
        <filter-name>LoggingFilter</filter-name>
        <filter-class>com.syos.web.filter.LoggingFilter</filter-class>
    </filter>
    
    <filter>
        <filter-name>AuthenticationFilter</filter-name>
        <filter-class>com.syos.web.filter.AuthenticationFilter</filter-class>
    </filter>
    
    <filter>
        <filter-name>CorsFilter</filter-name>
        <filter-class>com.syos.web.filter.CorsFilter</filter-class>
    </filter>
    
    <!-- Filter Mappings -->
    <filter-mapping>
        <filter-name>EncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
    <filter-mapping>
        <filter-name>LoggingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
    <filter-mapping>
        <filter-name>AuthenticationFilter</filter-name>
        <url-pattern>/api/*</url-pattern>
        <url-pattern>/cashier/*</url-pattern>
        <url-pattern>/inventory/*</url-pattern>
        <url-pattern>/manager/*</url-pattern>
    </filter-mapping>
    
    <filter-mapping>
        <filter-name>CorsFilter</filter-name>
        <url-pattern>/api/*</url-pattern>
    </filter-mapping>
    
    <!-- Servlets -->
    <!-- API Servlets -->
    <servlet>
        <servlet-name>ProductApiServlet</servlet-name>
        <servlet-class>com.syos.web.servlet.api.ProductApiServlet</servlet-class>
    </servlet>
    
    <servlet>
        <servlet-name>BillingApiServlet</servlet-name>
        <servlet-class>com.syos.web.servlet.api.BillingApiServlet</servlet-class>
    </servlet>
    
    <servlet>
        <servlet-name>InventoryApiServlet</servlet-name>
        <servlet-class>com.syos.web.servlet.api.InventoryApiServlet</servlet-class>
    </servlet>
    
    <!-- Servlet Mappings -->
    <servlet-mapping>
        <servlet-name>ProductApiServlet</servlet-name>
        <url-pattern>/api/products/*</url-pattern>
    </servlet-mapping>
    
    <servlet-mapping>
        <servlet-name>BillingApiServlet</servlet-name>
        <url-pattern>/api/billing/*</url-pattern>
    </servlet-mapping>
    
    <servlet-mapping>
        <servlet-name>InventoryApiServlet</servlet-name>
        <url-pattern>/api/inventory/*</url-pattern>
    </servlet-mapping>
    
    <!-- Session Configuration -->
    <session-config>
        <session-timeout>30</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
            <secure>false</secure> <!-- Set to true in production with HTTPS -->
        </cookie-config>
    </session-config>
    
    <!-- Welcome Files -->
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
    
    <!-- Error Pages -->
    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/views/error/404.jsp</location>
    </error-page>
    
    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/views/error/500.jsp</location>
    </error-page>
</web-app>
```

### 12.3 Tomcat Configuration

```xml
<!-- context.xml -->
<Context>
    <!-- Database Connection Pool -->
    <Resource name="jdbc/SyosDB"
              auth="Container"
              type="javax.sql.DataSource"
              factory="com.zaxxer.hikari.HikariJNDIFactory"
              driverClassName="com.mysql.cj.jdbc.Driver"
              jdbcUrl="jdbc:mysql://localhost:3306/syos_db?useSSL=false&amp;serverTimezone=UTC"
              username="syos_user"
              password="your_password"
              maximumPoolSize="20"
              minimumIdle="5"
              connectionTimeout="30000"
              idleTimeout="600000"
              maxLifetime="1800000"/>
</Context>
```

### 12.4 application.properties

```properties
# Application Configuration
app.name=SYOS Retail Management System
app.version=2.0.0

# Database Configuration
db.url=jdbc:mysql://localhost:3306/syos_db?useSSL=false&serverTimezone=UTC
db.username=syos_user
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver

# HikariCP Configuration
hikari.maximumPoolSize=20
hikari.minimumIdle=5
hikari.connectionTimeout=30000
hikari.idleTimeout=600000
hikari.maxLifetime=1800000
hikari.leakDetectionThreshold=60000

# Thread Pool Configuration
threadpool.core.size=10
threadpool.max.size=50
threadpool.queue.capacity=100

# Session Configuration
session.timeout.minutes=30

# Email Configuration
email.host=smtp.gmail.com
email.port=587
email.username=noreply@syos.com
email.password=your_email_password
email.from=noreply@syos.com

# Report Configuration
report.output.directory=/var/syos/reports
report.async.enabled=true

# Logging Configuration
logging.level=INFO
logging.file.path=/var/log/syos
```

---

## 13. Migration Checklist

### Pre-Migration
- [ ] Review all existing code
- [ ] Document current functionality
- [ ] Identify all design patterns used
- [ ] List all dependencies
- [ ] Backup current database

### Phase 1: Setup
- [ ] Create Maven project
- [ ] Add all dependencies
- [ ] Set up project structure
- [ ] Configure logging
- [ ] Set up version control

### Phase 2: Domain Layer
- [ ] Migrate all domain models
- [ ] Migrate all value objects
- [ ] Migrate all enums
- [ ] Write tests

### Phase 3: Repository Layer
- [ ] Set up HikariCP
- [ ] Migrate repository interfaces
- [ ] Implement thread-safe repositories
- [ ] Add pagination support
- [ ] Write repository tests

### Phase 4: Service Layer
- [ ] Migrate all service interfaces
- [ ] Implement thread-safe services
- [ ] Add async operations
- [ ] Preserve all design patterns
- [ ] Write service tests
- [ ] Write concurrency tests

### Phase 5: Web Layer
- [ ] Create servlet base classes
- [ ] Implement API servlets
- [ ] Create filters
- [ ] Create listeners
- [ ] Implement DTOs
- [ ] Write servlet tests

### Phase 6: UI Layer
- [ ] Design wireframes
- [ ] Create JSP pages
- [ ] Implement JavaScript
- [ ] Add CSS styling
- [ ] Make responsive
- [ ] Test UI

### Phase 7: Testing
- [ ] Unit test coverage >80%
- [ ] Integration tests
- [ ] Concurrency tests
- [ ] Load tests
- [ ] Security tests

### Phase 8: Deployment
- [ ] Create WAR file
- [ ] Configure Tomcat
- [ ] Deploy to test environment
- [ ] User acceptance testing
- [ ] Deploy to production
- [ ] Monitor and optimize

---

## 14. Key Success Metrics

### Performance Metrics
- **Response Time**: < 200ms for 95% of requests
- **Throughput**: > 1000 requests/second
- **Concurrent Users**: Support 100+ simultaneous users
- **Database Connection Pool**: < 5ms average wait time

### Code Quality Metrics
- **Test Coverage**: > 80%
- **Code Duplication**: < 3%
- **Cyclomatic Complexity**: < 10 per method
- **SOLID Principles**: 100% compliance

### Reliability Metrics
- **Uptime**: > 99.9%
- **Error Rate**: < 0.1%
- **Transaction Success Rate**: > 99.99%

---

## 15. Risk Mitigation

| Risk | Mitigation Strategy |
|------|---------------------|
| Data loss during migration | Comprehensive backup before migration, staged rollout |
| Performance degradation | Load testing, profiling, optimization |
| Concurrency bugs | Extensive concurrency testing, code reviews |
| Security vulnerabilities | Security audit, penetration testing |
| Scope creep | Strict phase adherence, change control |
| Knowledge gaps | Training, documentation, pair programming |

---

## Conclusion

This refactoring plan provides a comprehensive roadmap to transform your SYOS CLI application into a robust, multithreaded web application while preserving all existing SOLID principles and design patterns. The phased approach ensures systematic development with continuous testing and validation.

**Key Takeaways:**
1. ✅ All design patterns preserved (Repository, Command, Strategy, Factory)
2. ✅ SOLID principles maintained throughout
3. ✅ Thread safety as first-class concern
4. ✅ Modern web architecture with Servlets and JSP
5. ✅ Comprehensive testing strategy
6. ✅ Production-ready deployment configuration

Follow this plan phase by phase, and you'll have a scalable, maintainable, and high-performance web application ready for deployment on Tomcat.
