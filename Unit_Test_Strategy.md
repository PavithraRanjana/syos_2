# SYOS Unit Test Strategy Document

## 1. Overview

This document describes the unit testing strategy, technologies, and patterns used in the SYOS Retail Management System. The test suite covers four architectural layers — **Domain**, **Repository**, **Service**, and **Web (Servlet/Filter)** — with approximately **450+ unit tests** across **37 test classes**.

---

## 2. Technologies and Dependencies

| Technology | Version | Purpose |
|---|---|---|
| **JUnit 5 (Jupiter)** | 5.10.1 | Core test framework — test lifecycle, assertions, nested classes |
| **Mockito** | 5.8.0 | Mocking framework — stubs, spies, verification |
| **Mockito JUnit Jupiter** | 5.8.0 | `@ExtendWith(MockitoExtension.class)` integration |
| **JaCoCo** | 0.8.11 | Code coverage measurement and enforcement |
| **Maven Surefire** | 3.2.3 | Test execution plugin |
| **Testcontainers** | 1.19.3 | Available for integration tests (MySQL container) |
| **Java** | 21 | Language version |

### Build Tool

- **Apache Maven** with the `maven-surefire-plugin` for test execution.
- Tests are run via `mvn test` and coverage reports are generated automatically in the `test` phase.

### Coverage Enforcement

JaCoCo is configured with a **minimum 80% line coverage** threshold at the bundle level. The build fails if coverage drops below this.

```xml
<limit>
    <counter>LINE</counter>
    <value>COVEREDRATIO</value>
    <minimum>0.80</minimum>
</limit>
```

---

## 3. Test Organisation

### 3.1 Directory Structure

```
syos-web/src/test/java/com/syos/
├── domain/                    # 15 test classes — Domain model tests
│   ├── BillTest.java
│   ├── BillItemTest.java
│   ├── BrandTest.java
│   ├── CartTest.java
│   ├── CartItemTest.java
│   ├── CategoryTest.java
│   ├── CustomerTest.java
│   ├── InventoryTransactionTest.java
│   ├── MainInventoryTest.java
│   ├── MoneyTest.java
│   ├── OnlineStoreInventoryTest.java
│   ├── OrderTest.java
│   ├── PhysicalStoreInventoryTest.java
│   ├── ProductCodeTest.java
│   └── ProductTest.java
├── repository/impl/           # 12 test classes — Repository layer tests
│   ├── BillRepositoryImplTest.java
│   ├── BillItemRepositoryImplTest.java
│   ├── BrandRepositoryImplTest.java
│   ├── CategoryRepositoryImplTest.java
│   ├── CustomerRepositoryImplTest.java
│   ├── InventoryTransactionRepositoryImplTest.java
│   ├── MainInventoryRepositoryImplTest.java
│   ├── OnlineStoreInventoryRepositoryImplTest.java
│   ├── OrderRepositoryImplTest.java
│   ├── PhysicalStoreInventoryRepositoryImplTest.java
│   ├── ProductRepositoryImplTest.java
│   └── SubcategoryRepositoryImplTest.java
├── service/                   # 9 test classes — Service layer tests
│   ├── BackgroundTaskServiceImplTest.java
│   ├── BillingServiceImplTest.java
│   ├── CartServiceImplTest.java
│   ├── CustomerServiceImplTest.java
│   ├── InventoryServiceImplTest.java
│   ├── OrderServiceImplTest.java
│   ├── ProductServiceImplTest.java
│   ├── ReportServiceImplTest.java
│   └── StoreInventoryServiceImplTest.java
├── web/
│   ├── servlet/
│   │   ├── api/               # 11 test classes — API servlet tests
│   │   │   ├── AdminApiServletTest.java
│   │   │   ├── AuthApiServletTest.java
│   │   │   ├── BillingApiServletTest.java
│   │   │   ├── CartApiServletTest.java
│   │   │   ├── CategoryApiServletTest.java
│   │   │   ├── CustomerApiServletTest.java
│   │   │   ├── InventoryApiServletTest.java
│   │   │   ├── OrderApiServletTest.java
│   │   │   ├── ProductApiServletTest.java
│   │   │   ├── ReportApiServletTest.java
│   │   │   └── StoreInventoryApiServletTest.java
│   │   └── view/              # 9 test classes — View servlet tests
│   │       ├── AdminViewServletTest.java
│   │       ├── AuthViewServletTest.java
│   │       ├── CustomerViewServletTest.java
│   │       ├── DashboardServletTest.java
│   │       ├── InventoryViewServletTest.java
│   │       ├── POSServletTest.java
│   │       ├── ProductViewServletTest.java
│   │       ├── ReportViewServletTest.java
│   │       └── StoreStockViewServletTest.java
│   └── filter/                # 3 test classes — Filter tests
│       ├── SecurityFilterTest.java
│       ├── RequestLoggingFilterTest.java
│       └── EncodingFilterTest.java
└── util/                      # 2 utility test classes
    ├── ManagerPromoterTest.java
    └── UserPromoterTest.java
```

### 3.2 Naming Convention

- Test classes are named `<ClassUnderTest>Test.java`.
- Test methods use descriptive names prefixed with `should`: `shouldCreateBillWithSerialNumber`, `shouldThrowWhenQuantityIsNegative`.

### 3.3 Test Grouping with `@Nested`

All test classes use JUnit 5 `@Nested` inner classes to group related tests by concern:

```java
class BillTest {
    @Nested @DisplayName("Constructor Tests")
    class ConstructorTests { ... }

    @Nested @DisplayName("Process Cash Payment Tests")
    class ProcessCashPaymentTests { ... }

    @Nested @DisplayName("Remove Item Tests")
    class RemoveItemTests { ... }
}
```

Every nested class and test method uses `@DisplayName` for human-readable output.

---

## 4. Testing Strategy by Layer

### 4.1 Domain Layer Tests (15 classes, ~180 tests)

**Goal:** Validate business logic, value object invariants, state transitions, and calculations with no external dependencies.

**Approach:** Pure unit tests — no mocks, no I/O. Objects are instantiated directly.

| What is tested | Example |
|---|---|
| Constructor validation | `ProductCode` rejects null, empty, or over-length codes |
| Value object immutability | `Money` arithmetic returns new instances; `ZERO` constant is immutable |
| Business rules | `Order` state machine enforces valid transitions (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED) |
| Aggregate operations | `Bill.addItem()`, `Cart.addItem()`, `Order.calculateTotals()` |
| Edge cases | `Bill.processCashPayment()` with insufficient tender, `MainInventory.reduceQuantity()` below zero |
| Equality & hashing | `ProductCode` and `CartItem` equality based on business identity |

**Key Pattern — Helper Methods:**
```java
private BillItem createTestBillItem(String code, int qty, double price) {
    return new BillItem(new ProductCode(code), "Test Product", qty, new Money(price), 1);
}
```

---

### 4.2 Repository Layer Tests (12 classes, ~180 tests)

**Goal:** Verify SQL interaction logic — correct parameter binding, result set mapping, and error handling — without a real database.

**Approach:** Mock the entire JDBC chain: `DataSource → Connection → PreparedStatement → ResultSet`.

**Setup Pattern (common across all 12 test classes):**
```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillRepositoryImplTest {
    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private BillRepositoryImpl repository;

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        repository = new BillRepositoryImpl(dataSource);
    }
}
```

**ResultSet Mocking Pattern:**
```java
private void mockBillResultSet() throws SQLException {
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getInt("bill_id")).thenReturn(1);
    when(resultSet.getString("serial_number")).thenReturn("PHY-000001");
    when(resultSet.getBigDecimal("total_amount")).thenReturn(new BigDecimal("1500.00"));
    // ... remaining columns
}
```

| What is tested | Example |
|---|---|
| Save (insert & update) | Verifies parameter binding with `verify(stmt).setString(1, value)` |
| Finder methods | `findById`, `findByProductCode`, `findByDateRange`, etc. |
| Aggregation queries | `getTotalSalesForDate`, `getTopSellingProducts`, `getStockSummary` |
| Error handling | `assertThrows(RepositoryException.class, ...)` when SQL throws |
| Transaction management | `verify(connection).commit()` for order saves |
| Pagination | `findAll(offset, limit)` parameter verification |

---

### 4.3 Service Layer Tests (9 classes, ~350 tests)

**Goal:** Test business orchestration logic — service methods coordinate repositories and other services correctly.

**Approach:** Mock all repository and service dependencies. Verify interactions and return values.

**Setup Pattern:**
```java
@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {
    @Mock private BillRepository billRepository;
    @Mock private BillItemRepository billItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StoreInventoryService storeInventoryService;
    @Mock private InventoryTransactionRepository transactionRepository;

    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingServiceImpl(
            billRepository, billItemRepository, productRepository,
            storeInventoryService, transactionRepository
        );
    }
}
```

| What is tested | Example |
|---|---|
| Happy path orchestration | `checkout()` creates a bill, adds items, deducts stock, logs transactions |
| Input validation | `addBatch()` rejects null product codes, negative quantities |
| Exception propagation | `ProductNotFoundException` when product code not found |
| Inter-service calls | `OrderServiceImpl.createOrderFromCart()` calls `CartService`, `CustomerService`, `BillingService` |
| Async operations | `CompletableFuture` methods tested with `.get()` to await completion |
| State management | `BillingServiceImpl` tracks in-progress bills in an internal map |

**Mocking Patterns Used:**

| Pattern | Usage |
|---|---|
| `when(...).thenReturn(...)` | Standard return value stubbing |
| `when(...).thenAnswer(inv -> ...)` | Dynamic return values (e.g., returning the input argument) |
| `when(...).thenThrow(...)` | Simulating failures |
| `verify(mock).method(...)` | Confirming a method was called |
| `verify(mock, never()).method(...)` | Confirming a method was NOT called |
| `ArgumentMatchers` (`any()`, `eq()`, `anyInt()`) | Flexible argument matching |

**Strictness:** Three service tests use `@MockitoSettings(strictness = Strictness.LENIENT)` to avoid unnecessary stubbing errors in complex test setups: `StoreInventoryServiceImplTest`, `ReportServiceImplTest`, `BackgroundTaskServiceImplTest`.

---

### 4.4 Web Layer Tests (23 classes, ~250 tests)

#### 4.4.1 API Servlet Tests (11 classes)

**Goal:** Verify HTTP routing, request parsing, response formatting, and correct delegation to service layer.

**Approach:** Mock `HttpServletRequest`, `HttpServletResponse`, and service dependencies. Services are injected via reflection.

**Service Injection via Reflection:**
```java
@BeforeEach
void setUp() throws Exception {
    servlet = new ProductApiServlet();
    Field field = ProductApiServlet.class.getDeclaredField("productService");
    field.setAccessible(true);
    field.set(servlet, productService);
}
```

| What is tested | Example |
|---|---|
| URL routing | `/api/products` vs `/api/products/search` vs `/api/products/{code}` |
| HTTP method dispatch | GET, POST, PUT, DELETE handled correctly |
| JSON response output | `verify(response).setContentType("application/json")` |
| Error responses | 404 for not found, 400 for validation errors |
| Authentication context | `request.getSession()` returning user ID and role |

#### 4.4.2 View Servlet Tests (9 classes)

**Goal:** Verify JSP view forwarding, request attribute population, and redirect logic.

**Key Verifications:**
```java
verify(request).setAttribute("products", productList);
verify(request).getRequestDispatcher("/WEB-INF/views/products/list.jsp");
verify(requestDispatcher).forward(request, response);
```

| What is tested | Example |
|---|---|
| View routing | `/products` → list view, `/products/add` → add form |
| Model attributes | Service results set as request attributes |
| Error handling | Service exceptions lead to error pages or redirects |
| Role-based redirects | `AuthViewServlet` redirects based on `UserRole` |

#### 4.4.3 Filter Tests (3 classes)

**Goal:** Verify cross-cutting concerns — security, encoding, logging.

| Filter | What is tested |
|---|---|
| **SecurityFilter** | Public paths bypass auth; role-based access control; unauthenticated users redirected to `/login`; API requests receive `401` |
| **EncodingFilter** | UTF-8 encoding set on request/response; security headers (`X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`) added |
| **RequestLoggingFilter** | Request ID generation; existing `X-Request-ID` header preserved; static resources skipped; MDC cleanup in `@AfterEach` |

---

## 5. Test Patterns Summary

### 5.1 Arrange–Act–Assert

All tests follow the AAA pattern:

```java
@Test
void shouldReduceQuantityCorrectly() {
    // Arrange
    MainInventory inventory = new MainInventory(code, 100, price, date, expiry, "Supplier");

    // Act
    inventory.reduceQuantity(30);

    // Assert
    assertEquals(70, inventory.getRemainingQuantity());
}
```

### 5.2 Exception Testing

```java
@Test
void shouldThrowWhenReducingMoreThanAvailable() {
    assertThrows(IllegalArgumentException.class, () -> {
        inventory.reduceQuantity(999);
    });
}
```

### 5.3 Test Data Helpers

Private helper methods create consistent test objects:
```java
private Product createTestProduct() { ... }
private Bill createTestBill() { ... }
private MainInventory createTestBatch() { ... }
```

### 5.4 Async Testing

```java
@Test
void shouldRunLowStockCheckAsync() throws Exception {
    when(inventoryService.findExpiringWithinDays(anyInt())).thenReturn(List.of());
    CompletableFuture<Void> future = service.runLowStockCheck();
    future.get(); // blocks until complete
    verify(inventoryService).findExpiringWithinDays(anyInt());
}
```

---

## 6. Test Count Summary

| Layer | Test Classes | Approximate Tests |
|---|---|---|
| Domain Models | 15 | ~180 |
| Repository Implementations | 12 | ~180 |
| Service Implementations | 9 | ~350 |
| API Servlets | 11 | ~130 |
| View Servlets | 9 | ~100 |
| Filters | 3 | ~24 |
| Utilities | 2 | ~2 |
| **Total** | **61** | **~450+** |

---

## 7. Coverage Target

- **Minimum enforced:** 80% line coverage (JaCoCo, bundle level).
- **Service layer target:** 80%+ (confirmed by commit history).
- Coverage reports are generated automatically during `mvn test` and the build fails if the threshold is not met.

---

## 8. How to Run

```bash
# Run all unit tests
mvn test

# Run tests for a specific layer
mvn test -Dtest="com.syos.domain.*"
mvn test -Dtest="com.syos.service.*"
mvn test -Dtest="com.syos.repository.impl.*"
mvn test -Dtest="com.syos.web.**"

# Run a single test class
mvn test -Dtest="BillingServiceImplTest"

# Generate coverage report (output: target/site/jacoco/index.html)
mvn test jacoco:report
```
