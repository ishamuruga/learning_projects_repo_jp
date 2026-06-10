# Expense Tracker — Design & Architecture

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

## Layered Architecture

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

**Constraints:** `PRIMARY KEY (id)`, `UNIQUE (name)`

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

**Constraints:** `PRIMARY KEY (id)`, `FOREIGN KEY (expense_type_id) REFERENCES expense_types(id)`

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

*`startDate` and `endDate` must be provided together for range filtering.

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

## Design Patterns Applied

| Pattern | Where | Purpose |
|---|---|---|
| Repository | `ExpenseRepository`, `ExpenseTypeRepository` | Abstraction over JPA data access |
| DTO (Data Transfer Object) | `ExpenseRequest`, `ExpenseResponse`, `ExpenseSummary` | Decouple API contract from entity model |
| Service Layer | `ExpenseService` | Centralise business logic and transactions |
| Template Method | `@PrePersist` / `@PreUpdate` on `Expense` | Lifecycle hook for audit timestamps |
| Find-or-Create | `findOrCreateExpenseType()` | Idempotent lookup + insert for `ExpenseType` |
| Global Exception Handler | `GlobalExceptionHandler` | Centralised cross-cutting error translation |
| Command (startup) | `DataInitializer` (`CommandLineRunner`) | One-time data seeding on application start |

---

## Transaction Boundaries

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

---

## Exception Handling Strategy

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

## Code Quality Rules

- **No business logic in controllers** — controllers only handle HTTP concerns (parsing, validation delegation, status codes).
- **No JPA in controllers** — repositories must never be injected into controllers; always go through the service.
- **Immutable audit fields** — `createdAt` is `updatable = false`; never set it manually.
- **Null safety** — `sumAllAmounts()` may return `null` if the table is empty; always guard with `!= null ? total : BigDecimal.ZERO`.
- **Test coverage targets** — service layer ≥ 90 % line coverage; controller layer 100 % endpoint coverage with `MockMvc`.
- **No raw SQL** — all queries use JPQL or Spring Data derived query methods; raw SQL is prohibited except in migrations.
