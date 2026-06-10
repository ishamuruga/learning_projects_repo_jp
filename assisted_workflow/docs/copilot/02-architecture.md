# Architecture

## Overview
This project uses a layered Spring Boot architecture:
- Controller layer: HTTP routing and request/response shaping.
- Service layer: business rules and transactional behavior.
- Repository layer: JPA-based persistence operations.
- Domain model and DTO layer: entity and request/response payload contracts.
- Global exception layer: cross-cutting API error handling.

## Package Structure
- `com.example.todo`
  - `TodoApplication`: Spring Boot entry point.
  - `DataLoader`: startup data seeding.
- `com.example.todo.controller`
  - `TodoController`: REST endpoints under `/api/todos`.
- `com.example.todo.service`
  - `TodoService`: core business operations and state transition rules.
- `com.example.todo.repository`
  - `TodoRepository`: Spring Data JPA repository.
- `com.example.todo.model`
  - `Todo`, `Priority`, `Status`: domain model.
- `com.example.todo.dto`
  - Request DTOs and generic `ApiResponse<T>` envelope.
- `com.example.todo.exception`
  - `GlobalExceptionHandler`: validation and generic exception mapping.

## Request Flow
1. Incoming request reaches `TodoController`.
2. Bean validation runs on DTOs annotated with `@Valid`.
3. Controller delegates to `TodoService`.
4. Service executes business logic and repository calls in a transaction.
5. Controller returns `ApiResponse<T>` wrapped in `ResponseEntity`.
6. Exceptions are translated by `GlobalExceptionHandler` or local try/catch blocks.

## Persistence and Transactions
- `TodoRepository` extends `JpaRepository<Todo, Long>`.
- `TodoService` is annotated with `@Transactional` at class level.
- Read methods use `@Transactional(readOnly = true)`.
- Entity lifecycle callbacks set timestamp fields (`@PrePersist`, `@PreUpdate`).

## Runtime and Configuration
- Embedded server on port `8080`.
- H2 file database at `./data/tododb`.
- Hibernate DDL mode is `update`.
- H2 console enabled at `/h2-console`.
