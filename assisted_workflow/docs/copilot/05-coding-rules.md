# Coding Rules

## Language and Platform
- Use Java 17 language features only.
- Keep compatibility with Spring Boot `3.2.x` ecosystem.

## Layering and Responsibilities
- Controller layer (`controller` package):
  - Handle HTTP concerns only (routing, request params/body, response status mapping).
  - Do not embed business rules directly in controllers.
- Service layer (`service` package):
  - Own business logic and state transition rules.
  - Keep transactional boundaries here.
- Repository layer (`repository` package):
  - Restrict to persistence query methods and Spring Data conventions.
- Model and DTO packages:
  - Keep entity and transport objects separate.
  - Avoid leaking request DTOs into repository contracts.

## API Consistency
- Return `ApiResponse<T>` envelope for success and error responses.
- Prefer explicit `ResponseEntity` with clear HTTP status codes.
- Preserve existing endpoint naming style (`/close`, `/cancel`).

## Validation and Error Handling
- Use Jakarta Bean Validation annotations in DTOs and entities where appropriate.
- Use `@Valid` in controller method signatures for request bodies.
- Add exception mappings in `GlobalExceptionHandler` for cross-cutting cases.
- For domain rule violations, map to semantically correct status codes (for example, `409` for invalid state transitions).

## Entity and Persistence Rules
- Keep enum fields persisted as strings (`EnumType.STRING`).
- Maintain timestamp lifecycle callbacks in entity (`@PrePersist`, `@PreUpdate`).
- Do not bypass service methods for state transitions.

## Transaction Rules
- Keep write operations within default service transaction scope.
- Mark read-only queries with `@Transactional(readOnly = true)`.

## Configuration and Security Baseline
- Keep H2 console disabled in production profiles.
- Avoid broad CORS (`*`) in production deployments unless explicitly required.
- Avoid exposing raw exception details in production-grade error responses.

## Testability Rules
- Add/maintain tests for:
  - DTO validation behavior
  - Service state transitions
  - Controller HTTP status mappings
- Keep test data deterministic and isolated.

## Documentation Rules
- When adding/changing endpoints, update `docs/copilot/04-api-contracts.md`.
- When changing domain model, update `docs/copilot/03-entity-design.md`.
- When changing architecture or conventions, update corresponding docs modules and `copilot-instructions.md` links if needed.
