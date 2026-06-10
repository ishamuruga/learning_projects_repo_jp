# API Contracts

Base path: `/api/todos`

Response envelope for all endpoints:
```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

## 1) Create Todo
- Method: `POST`
- Path: `/api/todos`
- Request body:
```json
{
  "title": "Set up CI/CD pipeline",
  "description": "Configure GitHub Actions",
  "priority": "HIGH"
}
```
- Validation:
  - `title` must be non-blank
  - `priority` must be provided
- Success:
  - Status: `201 Created`
  - Body: `ApiResponse<Todo>`
- Error:
  - Status: `400 Bad Request` for validation issues

## 2) List Todos
- Method: `GET`
- Path: `/api/todos`
- Query params (optional):
  - `status`: `OPEN|CLOSED|CANCELLED`
  - `priority`: `LOW|MEDIUM|HIGH`
- Filter precedence in current implementation:
  - If `status` is present, it is used.
  - Else if `priority` is present, it is used.
  - Else return all.
- Success:
  - Status: `200 OK`
  - Body: `ApiResponse<List<Todo>>`

## 3) Get Todo By Id
- Method: `GET`
- Path: `/api/todos/{id}`
- Success:
  - Status: `200 OK`
  - Body: `ApiResponse<Todo>`
- Not found:
  - Status: `404 Not Found`
  - Body: `ApiResponse<Void>` with error message

## 4) Close Todo
- Method: `PATCH`
- Path: `/api/todos/{id}/close`
- Request body:
```json
{
  "remarks": "Completed and verified"
}
```
- Validation:
  - `remarks` must be non-blank
- Success:
  - Status: `200 OK`
  - Body: `ApiResponse<Todo>`
- Error mappings:
  - `404 Not Found` when id does not exist
  - `409 Conflict` when todo is not OPEN
  - `400 Bad Request` for validation errors

## 5) Cancel Todo
- Method: `PATCH`
- Path: `/api/todos/{id}/cancel`
- Request body:
```json
{
  "remarks": "No longer needed"
}
```
- Validation and status/error behavior mirrors close endpoint.

## 6) Validate Even Number
- Method: `GET`
- Path: `/api/todos/validate-even`
- Query params (required):
  - `value`: integer value to validate
- Success:
  - Status: `200 OK`
  - Body: `ApiResponse<Integer>`
  - Message: `Even number accepted`
- Error:
  - Status: `400 Bad Request` when `value` is odd
  - Body: `ApiResponse<Void>` with message: `Odd numbers are not allowed: <value>`

## Domain Object Shape: Todo
```json
{
  "id": 1,
  "title": "Write unit tests",
  "description": "Add test coverage",
  "priority": "HIGH",
  "status": "OPEN",
  "remarks": null,
  "createdAt": "2026-06-02T12:34:56",
  "updatedAt": "2026-06-02T12:34:56"
}
```

## Common Error Envelope Examples
Validation error (`400`):
```json
{
  "success": false,
  "message": "title: Title is required; priority: Priority is required",
  "data": null
}
```

Not found (`404`):
```json
{
  "success": false,
  "message": "Todo not found with id: 99",
  "data": null
}
```

State conflict (`409`):
```json
{
  "success": false,
  "message": "Todo is already CLOSED and cannot be modified",
  "data": null
}
```
