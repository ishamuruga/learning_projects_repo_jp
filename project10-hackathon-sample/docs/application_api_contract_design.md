# Application API Contract Design

## Overview

This document specifies TaskFlow API contracts for future integration with backend services. Currently, the application uses in-memory state in Phase 1. Phase 2 will implement these HTTP-based contracts.

## API Design Principles

1. **RESTful:** Use standard HTTP methods and status codes
2. **Stateless:** Each request contains all context needed
3. **Versioned:** Include API version in URL (`/api/v1/`)
4. **Documented:** Specify request/response schemas and examples
5. **Secure:** All endpoints require authentication and enforce authorization

## Base URL and Authentication

```
Base URL: https://api.taskflow.example.com/api/v1
Authentication: Bearer Token (JWT)
Content-Type: application/json
```

### Example Authorization Header
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Task Endpoints

### 1. Create Task

**Endpoint:** `POST /tasks`

**Purpose:** Create a new task.

**Authorization:** Authenticated user (any role)

**Request Body:**

```json
{
  "title": "Prepare sprint goals",
  "description": "Align team on sprint roadmap",
  "priority": "High",
  "dueDate": "2099-12-31",
  "status": "To Do",
  "assigneeId": "u2"
}
```

**Validation:**
- `title`: Required, 1-100 characters, non-empty after trim
- `description`: Optional, 0-500 characters
- `priority`: Required, one of Low/Medium/High
- `dueDate`: Required, valid ISO 8601 date (YYYY-MM-DD)
- `status`: Optional, defaults to "To Do"
- `assigneeId`: Optional, must be valid user ID

**Response (201 Created):**

```json
{
  "id": 1,
  "title": "Prepare sprint goals",
  "description": "Align team on sprint roadmap",
  "priority": "High",
  "dueDate": "2099-12-31",
  "status": "To Do",
  "ownerId": "u2",
  "assigneeId": "u2",
  "createdAt": "2026-06-22T10:30:00Z",
  "updatedAt": "2026-06-22T10:30:00Z"
}
```

**Error Responses:**

| Status | Scenario |
|--------|----------|
| 400 | Invalid request (missing/invalid fields) |
| 401 | Unauthorized (no auth token) |
| 422 | Validation failed (e.g., invalid date) |

**Example cURL:**
```bash
curl -X POST https://api.taskflow.example.com/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Prepare sprint goals",
    "priority": "High",
    "dueDate": "2099-12-31"
  }'
```

### 2. Get Tasks (Filtered)

**Endpoint:** `GET /tasks`

**Purpose:** Retrieve tasks visible to current user, optionally filtered.

**Authorization:** Authenticated user (visibility enforced by server)

**Query Parameters:**

| Parameter | Type | Default | Purpose |
|-----------|------|---------|---------|
| `ownerId` | string | - | Filter by owner (lead only) |
| `status` | string | - | Filter by status (To Do, In Progress, Done) |
| `priority` | string | - | Filter by priority (Low, Medium, High) |
| `dueBucket` | string | - | Filter by due date (overdue, today, week) |
| `sortBy` | string | dueDate | Sort field (dueDate, priority, status) |
| `sortOrder` | string | asc | Sort direction (asc, desc) |
| `page` | integer | 1 | Page number for pagination |
| `pageSize` | integer | 50 | Items per page |

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": 1,
      "title": "Prepare sprint goals",
      "description": "Align team on sprint roadmap",
      "priority": "High",
      "dueDate": "2099-12-31",
      "status": "To Do",
      "ownerId": "u2",
      "assigneeId": "u2"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 50,
    "totalCount": 3,
    "totalPages": 1
  }
}
```

**Authorization Rules:**
- Members see only own tasks and tasks assigned to them
- Leads see all team tasks
- Server enforces filtering

**Example cURL:**
```bash
curl -X GET 'https://api.taskflow.example.com/api/v1/tasks?status=To%20Do&sortBy=dueDate' \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Update Task Status

**Endpoint:** `PATCH /tasks/{taskId}`

**Purpose:** Update task status or assignment.

**Authorization:** Task owner, assignee, or lead

**Request Body:**

```json
{
  "status": "In Progress"
}
```

or

```json
{
  "assigneeId": "u3"
}
```

**Allowed Updates:**
- `status`: Any valid status (To Do, In Progress, Done)
- `assigneeId`: Any valid user ID
- `priority`: Any valid priority (Low, Medium, High)

**Response (200 OK):**

```json
{
  "id": 1,
  "title": "Prepare sprint goals",
  "status": "In Progress",
  "assigneeId": "u3",
  "updatedAt": "2026-06-22T11:00:00Z"
}
```

**Error Responses:**

| Status | Scenario |
|--------|----------|
| 404 | Task not found |
| 403 | User not authorized to update |
| 422 | Invalid status/assignee value |

**Example cURL:**
```bash
curl -X PATCH https://api.taskflow.example.com/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "In Progress"}'
```

### 4. Delete Task

**Endpoint:** `DELETE /tasks/{taskId}`

**Purpose:** Delete a task (soft delete: mark inactive).

**Authorization:** Task owner or lead

**Response (204 No Content)** or **200 OK:**

```json
{
  "message": "Task deleted successfully"
}
```

**Error Responses:**

| Status | Scenario |
|--------|----------|
| 404 | Task not found |
| 403 | User not authorized to delete |

## User Endpoints

### 1. Get Current User

**Endpoint:** `GET /users/me`

**Purpose:** Retrieve current authenticated user.

**Authorization:** Any authenticated user

**Response (200 OK):**

```json
{
  "id": "u2",
  "name": "Priya Nair",
  "email": "priya@example.com",
  "role": "member"
}
```

### 2. Get All Users (Team)

**Endpoint:** `GET /users`

**Purpose:** Retrieve all team members.

**Authorization:** Authenticated user

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": "u1",
      "name": "Alex Morgan",
      "role": "lead"
    },
    {
      "id": "u2",
      "name": "Priya Nair",
      "role": "member"
    },
    {
      "id": "u3",
      "name": "Daniel Kim",
      "role": "member"
    }
  ]
}
```

## Report Endpoints

### 1. Get Team Productivity Report

**Endpoint:** `GET /reports/team-productivity`

**Purpose:** Retrieve aggregated task summary by team member.

**Authorization:** Lead only

**Query Parameters:**

| Parameter | Type | Purpose |
|-----------|------|---------|
| `startDate` | string | ISO 8601 date (optional, filters by dueDate) |
| `endDate` | string | ISO 8601 date (optional, filters by dueDate) |

**Response (200 OK):**

```json
{
  "generatedAt": "2026-06-22T12:00:00Z",
  "data": [
    {
      "assigneeId": "u2",
      "assigneeName": "Priya Nair",
      "completedCount": 5,
      "overdueCount": 1,
      "openCount": 3
    },
    {
      "assigneeId": "u3",
      "assigneeName": "Daniel Kim",
      "completedCount": 3,
      "overdueCount": 0,
      "openCount": 2
    }
  ]
}
```

**Error Responses:**

| Status | Scenario |
|--------|----------|
| 403 | User not authorized (not lead) |

### 2. Get Reminder Notifications

**Endpoint:** `GET /reminders`

**Purpose:** Retrieve tasks due within 24 hours.

**Authorization:** Authenticated user

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": 1,
      "title": "Finalize sprint goals",
      "dueDate": "2026-06-22",
      "hoursUntilDue": 12,
      "assigneeName": "Priya Nair"
    }
  ]
}
```

## Error Response Format

All errors follow standard format:

```json
{
  "error": {
    "message": "Descriptive error message",
    "code": "VALIDATION_ERROR",
    "details": [
      {
        "field": "title",
        "message": "Title is required"
      }
    ]
  }
}
```

**Error Codes:**

| Code | HTTP Status | Meaning |
|------|------------|---------|
| `VALIDATION_ERROR` | 400/422 | Invalid input data |
| `UNAUTHORIZED` | 401 | Missing or invalid auth token |
| `FORBIDDEN` | 403 | User lacks permission |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Data conflict (e.g., duplicate) |
| `INTERNAL_ERROR` | 500 | Server error |

## Rate Limiting

**Rate Limits (Phase 2):**
- 100 requests per minute per user
- 1000 requests per hour per user
- Excess requests return 429 Too Many Requests

## Versioning Strategy

**Current Version:** v1

**URL Pattern:** `/api/v1/`

**Future Versions:** `/api/v2/`, `/api/v3/`, etc.

**Deprecation Policy:**
- Major version bumps for breaking changes
- Maintain previous version for 6 months
- Announce deprecation 3 months in advance

## Mock API Implementation (Phase 1)

During Phase 1 hackathon, these contracts are simulated in-memory:

```typescript
// In-memory implementation
export class TaskService {
  private tasks: TaskItem[] = [];
  
  createTask(data: Partial<TaskItem>, ownerId: string): TaskItem {
    // Mock: generate ID, return result
  }
  
  getTasks(): TaskItem[] {
    // Mock: return inmemory array
  }
  
  // Future Phase 2: Replace with actual HttpClient calls
  // private http: HttpClient
  // createTask(data): Observable<TaskItem> {
  //   return this.http.post<TaskItem>('/api/v1/tasks', data);
  // }
}
```

## Integration Steps (Phase 2)

1. **Environment Configuration:**
   - Add API base URL to environment files
   - Configure HTTP interceptor for auth token

2. **HttpClient Implementation:**
   - Inject HttpClient in services
   - Replace in-memory operations with HTTP calls
   - Handle loading/error states with Observables

3. **Error Handling:**
   - Implement HTTP error interceptor
   - Show user-friendly error messages
   - Log errors for debugging

4. **Caching Strategy:**
   - Cache GET requests with Angular HttpClient
   - Invalidate cache on mutations
   - Consider Redux/NgRx for complex cache logic

5. **Testing:**
   - Use HttpTestingController for service tests
   - Mock API responses
   - Test error scenarios

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Status:** Design for Phase 2 Backend Integration
