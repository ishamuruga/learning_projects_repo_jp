# Test Coverage Generation Agent — Quick Start

## How to Invoke the Agent

Use one of these prompts in Copilot Chat to trigger automated test coverage generation:

### Basic Invocation
```
Generate test coverage for the Expense Tracker project.
```

### With Coverage Target
```
Generate test coverage for the Expense Tracker project. Target 85% overall coverage across all layers.
```

### Layer-Specific
```
Generate unit tests for the ExpenseService layer to improve service coverage to 90%.
```

```
Generate integration tests for the ExpenseRepository layer covering all custom JPQL queries.
```

### Full Workflow
```
Analyze code coverage gaps in the Expense Tracker project. Generate comprehensive tests to reach 85% coverage:
- Controller: validate all HTTP scenarios (happy path, validation errors, 404s)
- Service: test business logic branches, null handling, exception paths
- Repository: test custom JPQL queries with edge cases
- Entity: test lifecycle hooks (@PrePersist, @PreUpdate) and constraints
Execute tests and report coverage improvements.
```

## Agent Capabilities

The **expense-tracker-coverage-generator** agent will:

1. **Analyze Current Coverage**
   - Read JaCoCo coverage report
   - Identify uncovered lines and branches
   - Highlight classes <80% coverage

2. **Generate Tests**
   - Create integration tests with `@SpringBootTest + @Sql` 
   - Create unit tests with `@ExtendWith(MockitoExtension.class)`
   - Follow naming convention: `{method}_{condition}_{expectedResult}`
   - Cover happy paths, edge cases, and exceptions

3. **Create SQL Seed Scripts**
   - Generate `cleanup.sql` for each test scenario
   - Generate `seed-*.sql` files with realistic test data
   - Ensure scripts are idempotent

4. **Execute & Validate**
   - Run full test suite
   - Regenerate JaCoCo coverage report
   - Assert coverage improvements meet targets

5. **Report Results**
   - Show coverage before/after metrics
   - List newly generated test files
   - Highlight remaining gaps
   - Suggest next coverage goals

## Example Outputs

### Generated Test File
```
src/test/java/com/example/expensetracker/service/ExpenseServiceUnitTest.java
- 12 new unit tests
- Coverage improved: 65% → 88%
```

### Generated SQL Scripts
```
src/test/resources/sql/service-tests-data.sql        (95 lines)
src/test/resources/sql/boundary-condition-data.sql   (112 lines)
src/test/resources/sql/exception-scenario-data.sql   (87 lines)
```

### Coverage Report
```
Overall Coverage:  72% → 84% (+12%)
- Controller:      94% → 96% (+2%)
- Service:         65% → 88% (+23%)
- Repository:      78% → 85% (+7%)
- Entity:          82% → 91% (+9%)
```

## Workflow Diagram

```
┌─────────────────────────────────────────┐
│  User Prompt: "Generate test coverage"  │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  expense-tracker-coverage-generator     │
│  Agent Invoked                          │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│ Analyze │  │Generate │  │ Execute │
│Coverage │  │  Tests  │  │ & Report│
└────┬────┘  └────┬────┘  └────┬────┘
     │            │             │
     └────────────┴─────────────┘
              │
              ▼
    ┌─────────────────────────┐
    │ New Test Files          │
    │ SQL Seed Scripts        │
    │ Coverage Report (HTML)  │
    │ Summary Metrics         │
    └─────────────────────────┘
```

## What Gets Generated

### Test Files (Java)
- `ExpenseControllerEdgeCasesTest.java` — Edge cases, partial params, empty results
- `ExpenseServiceUnitTest.java` — Service layer mocked tests for business logic
- `ExpenseRepositoryIntegrationTest.java` — Repository JPQL query tests
- `ExpenseEntityLifecycleTest.java` — Entity lifecycle hooks and constraints

### SQL Scripts
- `edge-case-data.sql` — Boundary conditions, null values, special chars
- `boundary-condition-data.sql` — Date boundaries, amount limits, type variations
- `exception-scenario-data.sql` — Missing data, invalid relationships

### Reports
- `target/site/jacoco/index.html` — Full coverage report with per-class breakdowns
- Console output with before/after metrics and test generation summary

## Pro Tips

1. **Run Incrementally:** Generate tests for one layer at a time, review, then proceed
   ```
   Generate unit tests for ExpenseService only.
   ```

2. **Target Specific Classes:** Focus on lowest-coverage classes first
   ```
   Generate tests for ExpenseRepository to improve coverage from 78% to 85%.
   ```

3. **Validate Before Merging:** Always run the full test suite after generation
   ```
   Generate tests, execute them, and show me the coverage report comparison.
   ```

4. **Preserve Existing Tests:** Agent appends new tests to existing files or creates separate files
   ```
   New tests are added to new *Test.java files; existing tests in 
   ExpenseControllerSqlIntegrationTest.java are never modified.
   ```

## Coverage Targets by Layer

| Layer | Target | Current | Gap |
|-------|--------|---------|-----|
| Controller | 95% | 92% | 3% |
| Service | 90% | 68% | 22% |
| Repository | 85% | 76% | 9% |
| Entity | 90% | 83% | 7% |
| **Overall** | **85%** | **80%** | **5%** |

---

**Ready?** Copy one of the invocation prompts above into Copilot Chat to start test coverage generation!
