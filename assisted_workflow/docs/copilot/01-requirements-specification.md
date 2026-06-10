# Requirements Specification

## Functional Requirements
- The system shall expose a REST API to manage todo items.
- The system shall allow creating a todo with title, optional description, and required priority.
- The system shall list todos, with optional filtering by `status` or `priority`.
- The system shall return a single todo by id.
- The system shall allow closing an OPEN todo with mandatory remarks.
- The system shall allow cancelling an OPEN todo with mandatory remarks.
- The system shall prevent closing or cancelling todos that are not OPEN.
- The system shall return uniform API envelopes for success and error responses.

## Data and State Requirements
- A todo shall have states: `OPEN`, `CLOSED`, `CANCELLED`.
- Newly created todos shall default to `OPEN`.
- Priority shall be one of: `LOW`, `MEDIUM`, `HIGH`.
- `createdAt` shall be set on insert and must not be updated later.
- `updatedAt` shall be updated on every entity update.
- Close or cancel operations shall capture non-empty remarks.

## Validation Requirements
- Create request requires non-blank `title`.
- Create request requires non-null `priority`.
- Close/cancel request requires non-blank `remarks`.
- Validation errors shall return HTTP 400 with field-level messages aggregated into one string.

## Error Handling Requirements
- Missing todo id in close/cancel flow shall return HTTP 404 with an error envelope.
- Invalid state transitions (not OPEN) shall return HTTP 409 with an error envelope.
- Unexpected failures shall return HTTP 500 with an error envelope.

## Non-Functional Requirements
- Runtime stack: Java 17, Spring Boot 3.2.x, Spring Web, Spring Data JPA, Bean Validation.
- Persistence: H2 file-based database.
- API must support CORS for all origins (`*`) as currently configured.
- Development visibility: SQL logging enabled.

## Seed Data Behavior
- Startup runner currently clears all todos and re-inserts sample data for demonstration.
- Seed dataset includes OPEN, CLOSED, and CANCELLED examples.
