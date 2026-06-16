# Skill: Test Coverage Generation for Expense Tracker

## Overview
This skill guides the automated generation of test cases to improve code coverage across the Expense Tracker REST API. It covers analysis, generation, execution, and reporting of tests targeting uncovered code paths and edge cases.

## When to Use This Skill
- User asks: "Generate test coverage", "Improve code coverage", "Fill coverage gaps"
- Targeting specific coverage percentage (e.g., "reach 85% coverage")
- Need comprehensive tests for a specific layer (controller, service, repository, entity)
- Want to automate test generation for newly added code

## Coverage Targets by Layer

### Controller Layer (`ExpenseController`)
**Current Gaps:** Edge cases, malformed input handling, concurrent request scenarios
**Test Types:**
- Happy-path POST/GET/PUT/DELETE with valid payloads
- Validation error scenarios (400) for each field
- Resource not found (404) for GET/PUT/DELETE with invalid IDs
- Empty result sets (200 OK with empty array)
- Partial query parameters (startDate without endDate)

**Example Test:**
```java
@Test
void updateExpense_withPartialPayload_returns400() {
    // Test payload missing required field and assert 400 with field error
}
```

### Service Layer (`ExpenseService`)
**Current Gaps:** Query result edge cases, null handling, transaction boundaries
**Test Types:**
- `findOrCreateExpenseType()`: new type creation, existing type lookup (case-insensitive)
- `getExpensesByDateRange()`: boundary dates, reverse range, single-day range
- `getSummary()`: empty DB, multiple categories, large amount aggregates
- Exception handling: `ResourceNotFoundException` for all find-by-id methods

**Example Test:**
```java
@Test
void findOrCreateExpenseType_withExistingType_returnsExisting() {
    // Mock repo to return existing type, assert no save call
}

@Test
void findOrCreateExpenseType_withNewType_createsAndReturns() {
    // Mock repo to return empty, assert save called once
}
```

### Repository Layer (`ExpenseRepository`)
**Current Gaps:** JPQL query correctness, custom query edge cases
**Test Types:**
- `findByExpenseTypeNameIgnoreCase()`: case variations, non-existent type
- `findByExpenseDateBetween()`: inclusive boundaries, reverse order
- `sumAllAmounts()`: null result when table empty
- `sumAmountByExpenseType()`: non-existent type returns null
- `findAllCategories()`: alphabetical order, distinct values

**Example Test:**
```java
@Test
void sumAllAmounts_whenNoExpenses_returnsNull() {
    // Assert null is returned, service converts to ZERO
}

@Test
void findByExpenseTypeNameIgnoreCase_mixedCase_returnsMatches() {
    // Insert "Food", query "fOoD", assert match
}
```

### Entity Layer (`Expense`, `ExpenseType`)
**Current Gaps:** Lifecycle hooks, null constraints, column precision
**Test Types:**
- `@PrePersist`: createdAt and updatedAt set to non-null LocalDateTime
- `@PreUpdate`: only updatedAt refreshed, createdAt unchanged
- Foreign key constraint: expense must reference valid expense_type
- Validation annotations: @NotBlank, @NotNull, @DecimalMin

**Example Test:**
```java
@Test
void expense_prePersist_setsCreatedAndUpdatedAt() {
    Expense expense = new Expense();
    expense.setTitle("Test");
    expense.setAmount(BigDecimal.TEN);
    expense.setExpenseType(expenseType);
    expense.setExpenseDate(LocalDate.now());
    
    repository.save(expense);
    
    assertNotNull(expense.getCreatedAt());
    assertNotNull(expense.getUpdatedAt());
}
```

## Workflow Steps

### Step 1: Analyze Current Coverage
```bash
mvn test
mvn jacoco:report
```
- Open `target/site/jacoco/index.html` in browser
- Note coverage % per class/package
- Identify classes < 80% coverage

### Step 2: Identify Gaps
For each low-coverage class:
1. Open source code and HTML coverage report side-by-side
2. Highlight red/yellow lines (uncovered/partially covered)
3. Document branch conditions not tested
4. Flag exception paths

### Step 3: Generate Tests
For each identified gap:
1. Choose test type (integration or unit)
2. Write test following naming convention: `{method}_{condition}_{expectedResult}`
3. Use `@Sql` annotations for integration tests with seed data
4. Use `@MockBean` for unit tests
5. Assert both status code and response body

### Step 4: Execute & Validate
```bash
mvn clean test
mvn jacoco:report
```
- Compare before/after coverage metrics
- Verify new tests pass (green)
- Check coverage improvement ≥ target per gap area

### Step 5: Report
Generate coverage summary:
- Total coverage before/after
- Coverage by layer
- Newly covered classes
- Remaining gaps (if any)

## Test Template: Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup.sql", "/sql/your-scenario.sql"}, 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ExpenseServiceIntegrationTest {
    
    @Autowired
    private ExpenseService service;
    
    @Test
    @DisplayName("methodName_condition_expectedResult")
    void testName() throws Exception {
        // Arrange: Data already loaded via @Sql
        
        // Act
        List<ExpenseResponse> result = service.getExpensesByExpenseType("Food");
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "Food".equals(e.getExpenseType())));
    }
}
```

## Test Template: Unit Test

```java
@ExtendWith(MockitoExtension.class)
class ExpenseServiceUnitTest {
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private ExpenseTypeRepository expenseTypeRepository;
    
    @InjectMocks
    private ExpenseService service;
    
    @Test
    @DisplayName("findOrCreateExpenseType_newType_createsAndReturns")
    void testFindOrCreateNew() {
        // Arrange
        when(expenseTypeRepository.findByNameIgnoreCase("Travel"))
            .thenReturn(Optional.empty());
        when(expenseTypeRepository.save(any(ExpenseType.class)))
            .thenAnswer(inv -> {
                ExpenseType arg = inv.getArgument(0);
                arg.setId(100L);
                return arg;
            });
        
        // Act
        ExpenseType result = service.findOrCreateExpenseType("Travel");
        
        // Assert
        assertEquals("Travel", result.getName());
        verify(expenseTypeRepository, times(1)).save(any());
    }
}
```

## SQL Seed Template

```sql
DELETE FROM expenses;
DELETE FROM expense_types;
ALTER TABLE expense_types ALTER COLUMN id RESTART WITH 1;
ALTER TABLE expenses ALTER COLUMN id RESTART WITH 1;

INSERT INTO expense_types (id, name) VALUES (1, 'Food');
INSERT INTO expense_types (id, name) VALUES (2, 'Transport');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (1, 'Lunch', 'Office lunch', 25.00, 1, DATE '2026-06-10', TIMESTAMP '2026-06-10 12:00:00', TIMESTAMP '2026-06-10 12:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (2, 'Cab', 'Airport ride', 45.00, 2, DATE '2026-06-05', TIMESTAMP '2026-06-05 05:00:00', TIMESTAMP '2026-06-05 05:00:00');

ALTER TABLE expense_types ALTER COLUMN id RESTART WITH 100;
ALTER TABLE expenses ALTER COLUMN id RESTART WITH 100;
```

## Coverage Metrics to Track

- **Overall Coverage:** Target ≥ 85% (line coverage)
- **Controller:** Target ≥ 95% (all endpoints exercised)
- **Service:** Target ≥ 90% (business logic branches)
- **Repository:** Target ≥ 80% (custom queries)
- **Entity:** Target ≥ 90% (lifecycle hooks, constraints)

## Files to Generate

Organized by gap area:

```
src/test/java/com/example/expensetracker/
├── controller/
│   ├── ExpenseControllerSqlIntegrationTest.java   ✓ (existing)
│   └── ExpenseControllerEdgeCasesTest.java        (to generate)
├── service/
│   └── ExpenseServiceUnitTest.java                 (to generate)
├── repository/
│   └── ExpenseRepositoryIntegrationTest.java      (to generate)
└── entity/
    └── ExpenseEntityLifecycleTest.java            (to generate)

src/test/resources/sql/
├── cleanup.sql                                     ✓ (existing)
├── base-data.sql                                   ✓ (existing)
├── edge-case-data.sql                             (to generate)
├── boundary-data.sql                              (to generate)
└── exception-scenario-data.sql                    (to generate)
```

## Success Criteria

✅ All new tests pass (green)
✅ Coverage improvement ≥ 10% per gap area
✅ No regression in existing tests
✅ HTML coverage report generated
✅ Test names follow BDD conventions
✅ SQL scripts are idempotent (cleanup + seed safe to repeat)
✅ Documentation updated with new test descriptions
