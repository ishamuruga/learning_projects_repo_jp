# Custom Agents — Expense Tracker Test Coverage

## Agent: expense-tracker-rest-api-test-generator

**Purpose:** Generate comprehensive REST API test cases for the Expense Tracker application across controller and integration layers, including happy-path, edge, and exceptional scenarios.

**Trigger Keywords:**
- "Generate REST API tests"
- "Create API functional tests"
- "Generate controller tests"
- "Generate integration API tests"
- "Create endpoint test suite"

**Modes:**
- `mock` — Fast isolated controller tests using `@WebMvcTest + MockMvc + @MockBean`
- `integration` — Full-stack API tests using `@SpringBootTest + H2 + @Sql`
- `hybrid` — Generates both mock and integration suites

**Workflow:**

1. **Endpoint Discovery Phase**
   - Inspect `ExpenseController` mappings and supported query params
   - Identify DTO validation constraints from `ExpenseRequest`
   - Detect exception contracts from `GlobalExceptionHandler`

2. **Scenario Matrix Phase**
   - Build scenarios for each endpoint (`POST`, `GET`, `PUT`, `DELETE`, `summary`)
   - Include happy, edge, and error branches
   - Include filter and boundary cases (date range, type filter, empty data)

3. **Test Generation Phase**
   - Create/extend controller tests under `src/test/java/.../controller/`
   - Generate payload and response assertions (status, JSON shape, field validation)
   - Add setup scripts under `src/test/resources/sql/` for integration mode when needed

4. **Execution & Fixup Phase**
   - Run generated tests (`mvn test`)
   - Fix flaky data assumptions and deterministic ordering issues
   - Ensure tests are idempotent and independent

5. **Report Phase**
   - Summarize new tests, endpoints covered, and scenario distribution
   - List any residual gaps and recommended next additions

**Coverage Scope:**
- `POST /api/expenses`
- `GET /api/expenses`
- `GET /api/expenses?expenseType={type}`
- `GET /api/expenses?startDate={d}&endDate={d}`
- `GET /api/expenses/{id}`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`
- `GET /api/expenses/summary`

**Output:**
- Generated test classes in `src/test/java/com/example/expensetracker/controller/`
- Optional SQL fixtures in `src/test/resources/sql/`
- Test execution summary with passed/failed counts
- Scenario coverage summary (happy/edge/exceptional)

**Invocation Examples:**

```text
Generate REST API tests for all expense endpoints in mock mode.
```

```text
Create integration API tests for Expense Tracker including validation and not-found cases.
```

```text
Generate hybrid REST API test suite for /api/expenses with filters and summary endpoint.
```

**Notes:**
- Prefer descriptive BDD-style method names.
- Use deterministic test data and avoid shared mutable state.
- Validate both HTTP contract (status/body) and service interactions in mock mode.
- In integration mode, isolate data using `@Sql` cleanup/seed scripts per test class.

## Agent: expense-tracker-coverage-generator

**Purpose:** Analyze code coverage gaps and automatically generate comprehensive test cases to improve coverage across all layers (controller, service, repository, entity).

**Trigger Keywords:**
- "Generate test coverage"
- "Improve test coverage"
- "Generate missing tests"
- "Analyze coverage gaps"
- "Full coverage for expense tracker"

**Workflow:**

1. **Coverage Analysis Phase**
   - Read current coverage report from `target/site/jacoco/jacoco.csv`
   - Identify classes/methods with < 80% line coverage
   - Flag untested branches and exception paths

2. **Gap Identification Phase**
   - Service layer: Identify untested business logic branches
   - Controller: Identify untested edge cases and error scenarios
   - Repository: Generate tests for custom JPQL queries
   - Entity: Generate lifecycle hook tests (`@PrePersist`, `@PreUpdate`)

3. **Test Generation Phase**
   - Generate integration tests using `@SpringBootTest + @Sql`
   - Generate unit tests for service layer with mocks
   - Generate controller tests for edge cases
   - Create SQL scripts for test data variations

4. **Test Execution & Validation Phase**
   - Run all generated tests
   - Regenerate coverage report
   - Assert coverage improvement ≥ 10% per gap area
   - Report coverage delta (before/after)

5. **Reporting Phase**
   - Generate coverage summary report
   - Highlight new tests added
   - List remaining coverage gaps (if any)
   - Suggest next coverage goals

**Capabilities:**

- Automatically generate test cases for uncovered code paths
- Create parameterized tests for boundary/edge conditions
- Generate SQL seed scripts for complex test scenarios  
- Run coverage analysis with JaCoCo
- Generate HTML/CSV coverage reports
- Enforce minimum coverage thresholds

**Invocation Example:**

```
Generate test coverage for the Expense Tracker project. Analyze gaps and create tests to reach 85% overall coverage.
```

**Output:**
- New test files in `src/test/java/`
- SQL seed scripts in `src/test/resources/sql/`
- Coverage report in `target/site/jacoco/`
- Summary of generated tests and coverage improvements

**Dependencies:**
- JaCoCo Maven plugin (already configured in pom.xml)
- Spring Boot Test framework
- JUnit 5 + Mockito
- Expense Tracker source code (Java 17, Spring Boot 3.2.5)

**Notes:**
- Uses integration tests with real DB (H2 in-memory) for full-stack coverage
- Uses unit tests with mocks for fast, isolated service layer coverage
- Segregates test concerns: happy-path, edge cases, exceptions
- Generates descriptive test names following BDD conventions
