# Entity Design

## Domain Entity: Todo
Table: `todos`

### Fields
- `id: Long`
  - Primary key
  - Generated with `GenerationType.IDENTITY`
- `title: String`
  - Required
  - `@NotBlank`, `@Column(nullable = false)`
- `description: String`
  - Optional
  - `@Column(length = 1000)`
- `priority: Priority`
  - Required enum
  - Stored as string (`@Enumerated(EnumType.STRING)`)
  - `@Column(nullable = false)`
- `status: Status`
  - Enum state
  - Stored as string (`@Enumerated(EnumType.STRING)`)
  - Defaults to `OPEN`
  - `@Column(nullable = false)`
- `remarks: String`
  - Optional text
  - `@Column(length = 1000)`
  - Used when status changes to CLOSED or CANCELLED
- `createdAt: LocalDateTime`
  - Required
  - Set in `@PrePersist`
  - Non-updatable
- `updatedAt: LocalDateTime`
  - Updated in `@PrePersist` and `@PreUpdate`

## Enums
- `Priority`
  - `LOW`, `MEDIUM`, `HIGH`
- `Status`
  - `OPEN`, `CLOSED`, `CANCELLED`

## State Transition Rules
- Allowed transition: `OPEN -> CLOSED` with remarks.
- Allowed transition: `OPEN -> CANCELLED` with remarks.
- Any transition from `CLOSED` or `CANCELLED` is rejected.

## Repository Query Model
- `findByStatus(Status status)`
- `findByPriority(Priority priority)`
- `findAllByOrderByPriorityDescCreatedAtDesc()`

## DTO Contracts (Entity Adjacent)
- `CreateTodoRequest`
  - `title` required, `description` optional, `priority` required.
- `CloseOrCancelRequest`
  - `remarks` required.
- `ApiResponse<T>`
  - `{ success, message, data }` envelope for all API responses.
