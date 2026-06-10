# Expense Tracker — Specification

## Product Overview

**Expense Tracker** is a RESTful backend API that allows users to record, categorise, query, and summarise personal or business expenses. The application is built as a self-contained Spring Boot service backed by an embedded H2 database, making it easy to run locally without any external infrastructure.

---

## Core Capabilities

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

- `fieldErrors` is only present for `400` validation failures.
- `404` and `500` responses include `timestamp`, `status`, and `message` only.

### HTTP Status Code Matrix

| Operation | Success | Not Found | Bad Input | Server Error |
|---|---|---|---|---|
| POST | 201 | — | 400 | 500 |
| GET (list) | 200 | — | — | 500 |
| GET (by id) | 200 | 404 | — | 500 |
| PUT | 200 | 404 | 400 | 500 |
| DELETE | 204 | 404 | — | 500 |
| GET summary | 200 | — | — | 500 |

---

## Validation Rules

| Field | Rule |
|---|---|
| `title` | Required, non-blank |
| `amount` | Required, minimum `0.01` |
| `expenseType` | Required, non-blank; auto-created if it does not yet exist in `expense_types` |
| `expenseDate` | Required |

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
