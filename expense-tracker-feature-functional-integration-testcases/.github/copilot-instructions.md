# Copilot Instructions — Expense Tracker

## Product Overview

**Expense Tracker** is a RESTful backend API that allows users to record, categorise, query, and summarise personal or business expenses. The application is built as a self-contained Spring Boot service backed by an embedded H2 database, making it easy to run locally without any external infrastructure.

### Core Capabilities

| Feature | Description |
|---|---|
| Create expense | Record a new expense with title, amount, category, date, and optional description |
| Read expense(s) | Retrieve a single expense by ID, or list all expenses ordered by date (newest first) |
| Filter by category | List expenses for a specific expense type (case-insensitive) |
| Filter by date range | List expenses between two ISO dates (`startDate` / `endDate` query params) |
| Update expense | Full replacement of an existing expense record |
| Delete expense | Remove an expense record by ID |
| Summary | Return total count, total amount, and distinct category list across all expenses |
| Auto-seed data | `DataInitializer` (CommandLineRunner) seeds 5 expense types and 10 sample expenses on first startup |

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Web | Spring MVC (spring-boot-starter-web) | — |
| Persistence | Spring Data JPA (spring-boot-starter-data-jpa) | — |
| Validation | Jakarta Bean Validation (spring-boot-starter-validation) | — |
| Database | H2 (file-based, persistent) | runtime |
| ORM dialect | Hibernate / H2Dialect | — |
| Boilerplate reduction | Lombok 1.18.38 | — |
| Build tool | Maven (spring-boot-maven-plugin) | — |
| Testing | Spring Boot Test (JUnit 5, Mockito) | — |

---

## Project Structure

```
expense-tracker/
├── pom.xml
└── src/main/java/com/example/expensetracker/
    ├── ExpenseTrackerApplication.java       # @SpringBootApplication entry point
    ├── DataInitializer.java                 # CommandLineRunner – seeds sample data
    ├── controller/
    │   └── ExpenseController.java           # REST endpoints under /api/expenses
    ├── dto/
    │   ├── ExpenseRequest.java              # Input payload (validated)
    │   ├── ExpenseResponse.java             # Output payload
    │   └── ExpenseSummary.java              # Aggregate response
    ├── entity/
    │   ├── Expense.java                     # @Entity – expenses table
    │   └── ExpenseType.java                 # @Entity – expense_types lookup table
    ├── exception/
    │   ├── GlobalExceptionHandler.java      # @RestControllerAdvice – 404 / 400 / 500
    │   └── ResourceNotFoundException.java   # Custom runtime exception
    ├── repository/
    │   ├── ExpenseRepository.java           # JPA repo + custom JPQL queries
    │   └── ExpenseTypeRepository.java       # JPA repo for expense types
    └── service/
        └── ExpenseService.java              # Business logic, transaction management
```

---

## REST API Reference

**Base URL:** `http://localhost:8080/api/expenses`

### Endpoints

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/api/expenses` | Create expense | `201 Created` |
| `GET` | `/api/expenses` | List all expenses (newest first) | `200 OK` |
| `GET` | `/api/expenses?expenseType={type}` | Filter by category | `200 OK` |
| `GET` | `/api/expenses?startDate={d}&endDate={d}` | Filter by date range (ISO 8601) | `200 OK` |
| `GET` | `/api/expenses/{id}` | Get expense by ID | `200 OK` |
| `PUT` | `/api/expenses/{id}` | Replace expense | `200 OK` |
| `DELETE` | `/api/expenses/{id}` | Delete expense | `204 No Content` |
| `GET` | `/api/expenses/summary` | Aggregate totals & categories | `200 OK` |

### Request / Response Shapes

**ExpenseRequest** (POST / PUT body)
```json
{
  "title": "Grocery shopping",
  "description": "Weekly groceries",
  "amount": 45.75,
  "expenseType": "Food",
  "expenseDate": "2026-06-07"
}
```

**ExpenseResponse**
```json
{
  "id": 1,
  "title": "Grocery shopping",
  "description": "Weekly groceries",
  "amount": 45.75,
  "expenseType": "Food",
  "expenseDate": "2026-06-07",
  "createdAt": "2026-06-08T10:00:00",
  "updatedAt": "2026-06-08T10:00:00"
}
```

**ExpenseSummary**
```json
{
  "totalCount": 10,
  "totalAmount": 549.24,
  "categories": ["Entertainment", "Food", "Healthcare", "Transport", "Utilities"]
}
```

### Error Response Shape

```json
{
  "timestamp": "2026-06-08T10:00:00",
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": {
    "title": "Title is required",
    "amount": "Amount must be greater than 0"
  }
}
```

---

## Domain Model

### `Expense` entity (`expenses` table)

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PK, auto-generated |
| `title` | `VARCHAR` | NOT NULL |
| `description` | `VARCHAR(500)` | nullable |
| `amount` | `DECIMAL(10,2)` | NOT NULL, ≥ 0.01 |
| `expense_type_id` | `BIGINT` | FK → `expense_types.id`, NOT NULL |
| `expense_date` | `DATE` | NOT NULL |
| `created_at` | `TIMESTAMP` | NOT NULL, set on insert |
| `updated_at` | `TIMESTAMP` | NOT NULL, updated on change |

### `ExpenseType` entity (`expense_types` table)

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PK, auto-generated |
| `name` | `VARCHAR` | NOT NULL, UNIQUE |

**Seeded expense types:** Food, Transport, Utilities, Entertainment, Healthcare

---

## Validation Rules

- `title` — required, non-blank
- `amount` — required, minimum `0.01`
- `expenseType` — required, non-blank; auto-created if it does not yet exist in `expense_types`
- `expenseDate` — required

---

## Configuration (`application.properties`)

```properties
spring.application.name=expense-tracker

# H2 file-based datasource (persists to ./data/expensedb)
spring.datasource.url=jdbc:h2:file:./data/expensedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=sa

# Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 console (browser: http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8080
```

---

## Coding Conventions

- **Constructor injection** — always prefer constructor injection over field `@Autowired`.
- **DTOs for API boundaries** — never expose JPA entities directly in controller responses; use `ExpenseRequest` / `ExpenseResponse`.
- **Transaction management** — write methods are `@Transactional`; read-only methods use `@Transactional(readOnly = true)`.
- **Lombok** — use `@Data` on DTOs, `@Getter @Setter @NoArgsConstructor` on entities, `@RequiredArgsConstructor` on services.
- **Exception handling** — throw `ResourceNotFoundException` for missing resources; let `GlobalExceptionHandler` translate to HTTP responses.
- **Case-insensitive queries** — expense type lookups are always case-insensitive (`IgnoreCase` / `LOWER()` in JPQL).
- **Auto-create expense types** — `ExpenseService.findOrCreateExpenseType()` creates a new `ExpenseType` if one does not exist, preventing foreign-key errors without requiring clients to manage types separately.
- **Naming** — REST paths use kebab-case; Java follows standard camelCase/PascalCase conventions.

---

## Testing Guidelines

- Use **JUnit 5** and **Mockito** (included via `spring-boot-starter-test`).
- **Service tests** — unit test `ExpenseService` by mocking `ExpenseRepository` and `ExpenseTypeRepository`.
- **Controller tests** — use `@WebMvcTest(ExpenseController.class)` with a mocked `ExpenseService` to test HTTP behaviour, status codes, and JSON serialisation.
- **Integration tests** — use `@SpringBootTest` with the embedded H2 in-memory mode (`jdbc:h2:mem:testdb`) to test the full stack.
- Test both happy-path and error cases: missing resource (404), invalid input (400), successful CRUD.
- Use `MockMvc` for controller tests; avoid starting a real HTTP server unless explicitly testing networking.
- Assert response status, JSON fields, and error message payloads.

---

## Running the Application

```bash
# Build and run
mvn spring-boot:run

# Run tests
mvn test

# Package
mvn package
```

- API: `http://localhost:8080/api/expenses`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/expensedb`
  - User: `sa` / Password: `sa`

---

## Entity Design

### Class Diagram (UML-style)

```
┌─────────────────────────────────────┐       ┌──────────────────────────┐
│            Expense                  │       │       ExpenseType        │
├─────────────────────────────────────┤       ├──────────────────────────┤
│ - id           : Long               │  N:1  │ - id   : Long            │
│ - title        : String             │──────►│ - name : String          │
│ - description  : String             │       └──────────────────────────┘
│ - amount       : BigDecimal         │
│ - expenseType  : ExpenseType        │
│ - expenseDate  : LocalDate          │
│ - createdAt    : LocalDateTime      │
│ - updatedAt    : LocalDateTime      │
├─────────────────────────────────────┤
│ + onCreate()  : void  [@PrePersist] │
│ + onUpdate()  : void  [@PreUpdate]  │
└─────────────────────────────────────┘
```

### `Expense` — JPA Annotations & Design Decisions

| Concern | Design |
|---|---|
| Table | `@Table(name = "expenses")` |
| Primary Key | `@Id @GeneratedValue(strategy = IDENTITY)` — DB-assigned BIGINT |
| Relationship | `@ManyToOne(fetch = LAZY, optional = false)` to `ExpenseType` via `expense_type_id` FK |
| Auditing | `@PrePersist` sets both `createdAt` and `updatedAt`; `@PreUpdate` refreshes `updatedAt` only |
| Amount precision | `@Column(precision = 10, scale = 2)` — supports up to 99,999,999.99 |
| Description | `@Column(length = 500)` — nullable, max 500 chars |
| Lombok | `@Getter @Setter @NoArgsConstructor` — no `@EqualsAndHashCode` to avoid lazy-loading pitfalls |

### `ExpenseType` — JPA Annotations & Design Decisions

| Concern | Design |
|---|---|
| Table | `@Table(name = "expense_types", uniqueConstraints = @UniqueConstraint(columnNames = "name"))` |
| Primary Key | `@Id @GeneratedValue(strategy = IDENTITY)` |
| Uniqueness | DB-level UNIQUE constraint on `name`; also enforced at application layer via `findByNameIgnoreCase` before insert |
| Convenience constructor | `ExpenseType(String name)` for use in `DataInitializer` and `findOrCreateExpenseType()` |
| Lombok | `@Getter @Setter @NoArgsConstructor` |

### Relationship Summary

```
expense_types (1) ──── (N) expenses
```
- An `ExpenseType` can be referenced by many `Expense` records.
- An `Expense` must reference exactly one `ExpenseType` (NOT NULL FK).
- The relationship is unidirectional from `Expense` → `ExpenseType` (no back-reference collection on `ExpenseType`).

### Entity Lifecycle

```
NEW Expense
  │
  ▼ save()
@PrePersist ──► createdAt = now(), updatedAt = now()
  │
  ▼ INSERT → DB
MANAGED Expense
  │
  ▼ save() after mutation
@PreUpdate ──► updatedAt = now()
  │
  ▼ UPDATE → DB
```

---

## API Design

### Design Principles

- **RESTful resource model** — resource is `/api/expenses`; sub-resource `/api/expenses/{id}`; action resource `/api/expenses/summary`.
- **Stateless** — no server-side session; all state is in the DB.
- **Content negotiation** — `Content-Type: application/json` and `Accept: application/json` for all requests and responses.
- **Validation at boundary** — `@Valid` on controller method parameters; `GlobalExceptionHandler` translates constraint violations to structured 400 responses.
- **DTO insulation** — entities never cross the controller boundary; `ExpenseRequest` → entity mapping happens in `ExpenseService`.

### Endpoint Specification

#### POST `/api/expenses` — Create Expense

| Item | Detail |
|---|---|
| Request body | `ExpenseRequest` JSON |
| Success | `201 Created` + `ExpenseResponse` body |
| Validation failure | `400 Bad Request` + `fieldErrors` map |
| Notes | `expenseType` is auto-created if unknown |

#### GET `/api/expenses` — List / Filter Expenses

| Query Param | Type | Required | Description |
|---|---|---|---|
| `expenseType` | `String` | No | Case-insensitive filter by category name |
| `startDate` | `LocalDate` (ISO 8601) | No* | Range start (inclusive) |
| `endDate` | `LocalDate` (ISO 8601) | No* | Range end (inclusive) |

*`startDate` and `endDate` must be provided together for range filtering. If neither param is present, all expenses are returned ordered by `expenseDate` descending.

| Condition | Response |
|---|---|
| No params | `200 OK` + all expenses, newest-first |
| `?expenseType=Food` | `200 OK` + filtered list |
| `?startDate=…&endDate=…` | `200 OK` + date-range list |
| Empty result | `200 OK` + `[]` (never 404) |

#### GET `/api/expenses/{id}` — Get by ID

| Item | Detail |
|---|---|
| Path variable | `id` — Long |
| Success | `200 OK` + `ExpenseResponse` |
| Not found | `404 Not Found` + error body |

#### PUT `/api/expenses/{id}` — Replace Expense

| Item | Detail |
|---|---|
| Path variable | `id` — Long |
| Request body | `ExpenseRequest` JSON (full replacement, all fields required) |
| Success | `200 OK` + updated `ExpenseResponse` |
| Not found | `404 Not Found` |
| Validation failure | `400 Bad Request` + `fieldErrors` map |

#### DELETE `/api/expenses/{id}` — Delete Expense

| Item | Detail |
|---|---|
| Path variable | `id` — Long |
| Success | `204 No Content` (empty body) |
| Not found | `404 Not Found` + error body |

#### GET `/api/expenses/summary` — Aggregate Summary

| Item | Detail |
|---|---|
| Success | `200 OK` + `ExpenseSummary` |
| Empty DB | `200 OK` with `totalCount: 0`, `totalAmount: 0.00`, `categories: []` |

### HTTP Status Code Matrix

| Operation | Success | Not Found | Bad Input | Server Error |
|---|---|---|---|---|
| POST | 201 | — | 400 | 500 |
| GET (list) | 200 | — | — | 500 |
| GET (by id) | 200 | 404 | — | 500 |
| PUT | 200 | 404 | 400 | 500 |
| DELETE | 204 | 404 | — | 500 |
| GET summary | 200 | — | — | 500 |

### Error Response Contract

All error responses share this JSON shape:

```json
{
  "timestamp": "2026-06-08T10:00:00",
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": {
    "title": "Title is required",
    "amount": "Amount must be greater than 0"
  }
}
```

- `fieldErrors` is only present for `400` validation failures.
- `404` and `500` responses include `timestamp`, `status`, and `message` only.

---

## Table Design

### Entity-Relationship Diagram

```
┌──────────────────────────────────────┐
│           expense_types              │
├──────────────────────────────────────┤
│ PK  id    BIGINT  AUTO_INCREMENT     │
│     name  VARCHAR NOT NULL UNIQUE    │
└───────────────────┬──────────────────┘
                    │ 1
                    │
                    │ N
┌──────────────────────────────────────┐
│               expenses               │
├──────────────────────────────────────┤
│ PK  id              BIGINT  AUTO     │
│     title           VARCHAR NOT NULL │
│     description     VARCHAR(500)     │
│     amount          DECIMAL(10,2)    │
│ FK  expense_type_id BIGINT  NOT NULL │──► expense_types.id
│     expense_date    DATE    NOT NULL │
│     created_at      TIMESTAMP NN     │
│     updated_at      TIMESTAMP NN     │
└──────────────────────────────────────┘
```

### `expense_types` Table

| Column | SQL Type | Nullable | Default | Notes |
|---|---|---|---|---|
| `id` | `BIGINT` | NO | auto-increment | Primary key |
| `name` | `VARCHAR(255)` | NO | — | Unique; case-insensitive lookups done in JPQL with `LOWER()` |

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE (name)`

**Seeded rows:** Food, Transport, Utilities, Entertainment, Healthcare

### `expenses` Table

| Column | SQL Type | Nullable | Default | Notes |
|---|---|---|---|---|
| `id` | `BIGINT` | NO | auto-increment | Primary key |
| `title` | `VARCHAR(255)` | NO | — | |
| `description` | `VARCHAR(500)` | YES | NULL | Optional free-text |
| `amount` | `DECIMAL(10,2)` | NO | — | Minimum 0.01 enforced at app layer |
| `expense_type_id` | `BIGINT` | NO | — | Foreign key → `expense_types.id` |
| `expense_date` | `DATE` | NO | — | ISO 8601 date |
| `created_at` | `TIMESTAMP` | NO | — | Set once on insert via `@PrePersist` |
| `updated_at` | `TIMESTAMP` | NO | — | Refreshed on every update via `@PreUpdate` |

**Constraints:**
- `PRIMARY KEY (id)`
- `FOREIGN KEY (expense_type_id) REFERENCES expense_types(id)`

**Implicit indexes created by Hibernate/H2:**
- Primary key index on `id`
- Foreign key index on `expense_type_id`

### DDL (H2-compatible)

```sql
CREATE TABLE expense_types (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_expense_types PRIMARY KEY (id),
    CONSTRAINT uq_expense_types_name UNIQUE (name)
);

CREATE TABLE expenses (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    title           VARCHAR(255)   NOT NULL,
    description     VARCHAR(500),
    amount          DECIMAL(10,2)  NOT NULL,
    expense_type_id BIGINT         NOT NULL,
    expense_date    DATE           NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    updated_at      TIMESTAMP      NOT NULL,
    CONSTRAINT pk_expenses PRIMARY KEY (id),
    CONSTRAINT fk_expenses_expense_type
        FOREIGN KEY (expense_type_id) REFERENCES expense_types(id)
);
```

---

## Sequence Diagrams

### Create Expense — POST `/api/expenses`

```
Client          ExpenseController        ExpenseService        ExpenseTypeRepository   ExpenseRepository
  │                    │                       │                        │                      │
  │──POST /api/expenses│                       │                        │                      │
  │  {ExpenseRequest}  │                       │                        │                      │
  │                    │──addExpense(request)──►│                        │                      │
  │                    │                       │──findByNameIgnoreCase──►│                      │
  │                    │                       │◄── Optional<ExpenseType>│                      │
  │                    │                       │  (create if empty)      │                      │
  │                    │                       │──save(expenseType) ────►│                      │
  │                    │                       │◄── ExpenseType ─────────│                      │
  │                    │                       │──────────────────────────────save(expense) ───►│
  │                    │                       │◄──────────────────────────── Expense ──────────│
  │                    │◄── ExpenseResponse ───│                        │                      │
  │◄── 201 Created ────│                       │                        │                      │
  │    {ExpenseResponse}│                      │                        │                      │
```

### Get Expense by ID — GET `/api/expenses/{id}`

```
Client          ExpenseController        ExpenseService        ExpenseRepository
  │                    │                       │                      │
  │──GET /api/expenses/1                       │                      │
  │                    │──getExpenseById(1)────►│                      │
  │                    │                       │──findById(1)─────────►│
  │                    │                       │◄── Optional<Expense> ─│
  │                    │                       │  [found] map to DTO   │
  │                    │◄── ExpenseResponse ───│                      │
  │◄── 200 OK ─────────│                       │                      │
  │    {ExpenseResponse}│                      │                      │
  │                    │                       │  [not found]          │
  │                    │                       │  throw ResourceNotFoundException
  │                    │◄── GlobalExceptionHandler ──────────────────────────────
  │◄── 404 Not Found───│                       │                      │
```

### Delete Expense — DELETE `/api/expenses/{id}`

```
Client          ExpenseController        ExpenseService        ExpenseRepository
  │                    │                       │                      │
  │──DELETE /api/expenses/1                    │                      │
  │                    │──deleteExpense(1)─────►│                      │
  │                    │                       │──findById(1)─────────►│
  │                    │                       │◄── Optional<Expense>──│
  │                    │                       │  [found]              │
  │                    │                       │──delete(expense)─────►│
  │                    │◄── void ──────────────│                      │
  │◄── 204 No Content──│                       │                      │
  │                    │                       │  [not found]          │
  │                    │                       │  throw ResourceNotFoundException
  │◄── 404 Not Found───│                       │                      │
```

### Application Startup — DataInitializer Seed

```
Spring Boot           DataInitializer      ExpenseTypeRepository   ExpenseRepository
  │                        │                        │                      │
  │──CommandLineRunner.run()►                        │                      │
  │                        │──count() ──────────────►│                      │
  │                        │◄── 0 (first startup) ───│                      │
  │                        │──save("Food") ──────────►│                      │
  │                        │──save("Transport") ─────►│                      │
  │                        │──save("Utilities") ─────►│                      │
  │                        │──save("Entertainment") ──►│                      │
  │                        │──save("Healthcare") ─────►│                      │
  │                        │──saveAll(10 expenses) ───────────────────────►│
  │                        │◄─────────────────────────────────────────────│
  │◄── startup complete ───│                        │                      │
```

---

## Code Quality Design

### Layered Architecture

```
┌─────────────────────────────────────────────────┐
│                  HTTP Client                    │
└───────────────────────┬─────────────────────────┘
                        │ JSON over HTTP
┌───────────────────────▼─────────────────────────┐
│            Controller Layer                     │
│  ExpenseController (@RestController)            │
│  • HTTP mapping, request validation             │
│  • Delegates all logic to Service               │
│  • Never accesses Repository directly           │
└───────────────────────┬─────────────────────────┘
                        │ DTOs only
┌───────────────────────▼─────────────────────────┐
│              Service Layer                      │
│  ExpenseService (@Service @Transactional)       │
│  • Business logic, entity ↔ DTO mapping         │
│  • Transaction management                       │
│  • Throws ResourceNotFoundException             │
└────────────┬──────────────────────┬─────────────┘
             │ entities             │ entities
┌────────────▼────────┐  ┌──────────▼──────────────┐
│  ExpenseRepository  │  │ ExpenseTypeRepository   │
│  (JpaRepository)    │  │ (JpaRepository)         │
└────────────┬────────┘  └──────────┬──────────────┘
             │                      │
┌────────────▼──────────────────────▼─────────────┐
│          H2 Database (JPA / Hibernate)           │
└─────────────────────────────────────────────────┘
```

### Design Patterns Applied

| Pattern | Where | Purpose |
|---|---|---|
| Repository | `ExpenseRepository`, `ExpenseTypeRepository` | Abstraction over JPA data access |
| DTO (Data Transfer Object) | `ExpenseRequest`, `ExpenseResponse`, `ExpenseSummary` | Decouple API contract from entity model |
| Service Layer | `ExpenseService` | Centralise business logic and transactions |
| Template Method | `@PrePersist` / `@PreUpdate` on `Expense` | Lifecycle hook for audit timestamps |
| Find-or-Create | `findOrCreateExpenseType()` | Idempotent lookup + insert for `ExpenseType` |
| Global Exception Handler | `GlobalExceptionHandler` | Centralised cross-cutting error translation |
| Command (startup) | `DataInitializer` (`CommandLineRunner`) | One-time data seeding on application start |

### Transaction Boundaries

| Method | Annotation | Reason |
|---|---|---|
| `addExpense` | `@Transactional` (class-level) | Writes to `expenses` and possibly `expense_types` |
| `getAllExpenses` | `@Transactional(readOnly = true)` | Read-only — allows connection pool optimisation |
| `getExpenseById` | `@Transactional(readOnly = true)` | Read-only |
| `getExpensesByExpenseType` | `@Transactional(readOnly = true)` | Read-only |
| `getExpensesByDateRange` | `@Transactional(readOnly = true)` | Read-only |
| `updateExpense` | `@Transactional` (class-level) | Writes to `expenses` and possibly `expense_types` |
| `deleteExpense` | `@Transactional` (class-level) | Deletes from `expenses` |
| `getSummary` | `@Transactional(readOnly = true)` | Aggregate queries only |

### Exception Handling Strategy

```
Application code                GlobalExceptionHandler        HTTP Response
  │                                     │                          │
  │── ResourceNotFoundException ────────►│                          │
  │                                     │── 404 + error body ─────►│
  │── MethodArgumentNotValidException ──►│                          │
  │                                     │── 400 + fieldErrors ────►│
  │── Any other Exception ──────────────►│                          │
  │                                     │── 500 + message ────────►│
```

- **Never** let stack traces leak to the client.
- `ResourceNotFoundException` is a `RuntimeException` — does not force checked-exception handling at call sites.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) is the single place that converts exceptions to HTTP responses.

### Code Quality Rules

- **No business logic in controllers** — controllers only handle HTTP concerns (parsing, validation delegation, status codes).
- **No JPA in controllers** — repositories must never be injected into controllers; always go through the service.
- **Immutable audit fields** — `createdAt` is `updatable = false`; never set it manually.
- **Null safety** — `sumAllAmounts()` may return `null` if the table is empty; always guard with `!= null ? total : BigDecimal.ZERO`.
- **Test coverage targets** — service layer ≥ 90 % line coverage; controller layer 100 % endpoint coverage with `MockMvc`.
- **No raw SQL** — all queries use JPQL or Spring Data derived query methods; raw SQL is prohibited except in migrations.
