# Project Documentation Skill

## Purpose
Generate and maintain implementation documentation for this Spring Boot Todo API project.

This skill covers two documentation tracks:
1. JavaDoc generation and refresh for core layers.
2. API sequence diagrams in PlantUML for every endpoint.

## Scope
- Java source scope:
	- `src/main/java/com/example/todo/controller`
	- `src/main/java/com/example/todo/service`
	- `src/main/java/com/example/todo/model`
	- `src/main/java/com/example/todo/repository`
- API scope:
	- `POST /api/todos`
	- `GET /api/todos`
	- `GET /api/todos/{id}`
	- `PATCH /api/todos/{id}/close`
	- `PATCH /api/todos/{id}/cancel`

## Inputs
- Source files under `src/main/java/com/example/todo/**`
- API behavior contract from `docs/copilot/04-api-contracts.md`
- Architecture contract from `docs/copilot/02-architecture.md`

## Required Activities

### Activity 1: Generate JavaDoc for Controller Layer
Add or refresh JavaDoc in controller classes with these minimums:
- Class-level summary with endpoint base path and responsibilities.
- Method-level JavaDoc for each API operation including:
	- Intent and business outcome.
	- Request parameters/body.
	- Success status code and envelope type (`ApiResponse<T>`).
	- Error mappings (`400`, `404`, `409`) where applicable.

Controller target file for this project:
- `src/main/java/com/example/todo/controller/TodoController.java`

### Activity 2: Generate JavaDoc for Service Layer
Add or refresh JavaDoc in service classes with these minimums:
- Class-level description of business responsibilities and transaction model.
- Method-level JavaDoc for:
	- create
	- findAll
	- findById
	- findByStatus
	- findByPriority
	- close
	- cancel
- Include preconditions and business rule notes, especially:
	- only OPEN todos can move to CLOSED or CANCELLED.
	- not found -> `IllegalArgumentException`.
	- invalid state transition -> `IllegalStateException`.

Service target file for this project:
- `src/main/java/com/example/todo/service/TodoService.java`

### Activity 3: Generate JavaDoc for Entity Layer
Add or refresh JavaDoc in entity and related enum classes with these minimums:
- Entity class-level summary with persistence mapping overview.
- Field-level JavaDoc for business meaning, nullability, and state semantics.
- Lifecycle callback JavaDoc (`@PrePersist`, `@PreUpdate`) for timestamp behavior.
- Enum JavaDoc for allowed values and semantic meaning.

Entity targets for this project:
- `src/main/java/com/example/todo/model/Todo.java`
- `src/main/java/com/example/todo/model/Status.java`
- `src/main/java/com/example/todo/model/Priority.java`

### Activity 4: Generate JavaDoc for Repository Layer
Add or refresh JavaDoc in repository interfaces with these minimums:
- Interface-level summary of persistence role.
- Method-level JavaDoc for derived queries including:
	- filtering intent.
	- sort behavior where relevant.
	- expected usage from service layer.

Repository target for this project:
- `src/main/java/com/example/todo/repository/TodoRepository.java`

### Activity 5: Generate PlantUML Sequence Diagrams per API Endpoint
Create one PlantUML file per endpoint under:
- `docs/diagrams/api/`

Required files:
- `docs/diagrams/api/create-todo-sequence.puml`
- `docs/diagrams/api/list-todos-sequence.puml`
- `docs/diagrams/api/get-todo-by-id-sequence.puml`
- `docs/diagrams/api/close-todo-sequence.puml`
- `docs/diagrams/api/cancel-todo-sequence.puml`

Diagram standards:
- Use `@startuml` / `@enduml`.
- Participants should include at least:
	- `Client`
	- `TodoController`
	- `TodoService`
	- `TodoRepository`
	- `Database` (or persistence store)
- Show request/response message flow with HTTP status outcomes.
- Use `alt/else` blocks for branches:
	- validation errors (`400`)
	- not found (`404`)
	- conflict on invalid state (`409`)
- Reflect implementation details from current controller/service logic.

Endpoint-specific expectations:
- `POST /api/todos`: validation path and successful creation (`201`).
- `GET /api/todos`: branch for `status` filter, else `priority` filter, else list all.
- `GET /api/todos/{id}`: found (`200`) vs not found (`404`).
- `PATCH /api/todos/{id}/close`: OPEN check, success (`200`), not found (`404`), conflict (`409`).
- `PATCH /api/todos/{id}/cancel`: OPEN check, success (`200`), not found (`404`), conflict (`409`).

### Activity 6: Add Diagram Index
Create or update index file:
- `docs/diagrams/api/README.md`

Include:
- Endpoint-to-diagram mapping table.
- Short notes on how to render `.puml` files.

## Quality Gates
- JavaDoc text must match actual runtime behavior in:
	- `src/main/java/com/example/todo/controller/TodoController.java`
	- `src/main/java/com/example/todo/service/TodoService.java`
	- `src/main/java/com/example/todo/repository/TodoRepository.java`
- Do not document unsupported behavior.
- Keep wording aligned with `ApiResponse<T>` envelope usage.
- Keep layering intent consistent: controller -> service -> repository.

## Optional Verification
- Build and test:
	- `mvn clean test`
- If javadoc plugin is configured, generate API docs:
	- `mvn javadoc:javadoc`

## Expected Output Summary
- Updated Java source files with complete JavaDoc in controller/service/entity/repository layers.
- 5 PlantUML sequence diagrams under `docs/diagrams/api/`.
- Diagram index at `docs/diagrams/api/README.md`.
