# Application Entity Design

## Overview

This document specifies the data models and entity relationships for TaskFlow, including invariants, constraints, and state transitions.

## Entity: TaskItem

### Properties

```typescript
interface TaskItem {
  // Identity
  id: number;                           // Unique task identifier (immutable)
  
  // Task Content
  title: string;                        // Task name (required, 1-100 chars)
  description: string;                  // Task details (optional, 0-500 chars)
  priority: 'Low' | 'Medium' | 'High';  // Priority level (required)
  
  // Timeline
  dueDate: string;                      // ISO 8601 date (required, no time component)
  
  // Status & Ownership
  status: 'To Do' | 'In Progress' | 'Done'; // Task lifecycle state
  ownerId: string;                      // User who created task (immutable after creation)
  assigneeId: string;                   // User currently working on task (mutable)
}
```

### Invariants

1. **ID Uniqueness:**
   - Each task has a unique positive integer ID
   - ID never reused or changed

2. **Ownership Immutability:**
   - `ownerId` set at creation time
   - Cannot change ownerId after task created
   - Prevents data loss due to accidental reassignment

3. **Title Non-Empty:**
   - Title must be non-empty after trimming whitespace
   - Required field for task creation

4. **Due Date Validity:**
   - Must be valid ISO 8601 date (YYYY-MM-DD)
   - Can be in past, present, or future
   - Used for sorting and reminder calculations

5. **Status Transition:**
   - Valid transitions:
     - To Do → In Progress
     - To Do → Done (direct completion)
     - In Progress → To Do (reopen)
     - In Progress → Done
     - Done → To Do (reopen)
     - Done → In Progress (resume)
   - Invalid: No status required at creation; defaults to "To Do"

6. **Priority Classification:**
   - Exactly one of three values: Low, Medium, High
   - Default: Medium

7. **Assignee Valid:**
   - `assigneeId` must refer to valid TeamUser by ID
   - Can be same as `ownerId` (self-assign)
   - Can change after creation (reassignment)

### Constraints

| Constraint | Rule | Enforcement |
|-----------|------|-------------|
| Title Length | 1-100 characters | Form validation, service validation |
| Description Length | 0-500 characters | Form validation |
| Due Date Format | ISO 8601 date only | Form input type=date, service parsing |
| Priority Values | Must be Low, Medium, or High | TypeScript enum/union |
| Status Values | Specific set of values | TypeScript union type |
| Duplicate ID | No two tasks same ID | Unique ID generator in service |

## Entity: TeamUser

### Properties

```typescript
interface TeamUser {
  id: string;              // User identifier (immutable)
  name: string;            // Display name
  role: 'member' | 'lead'; // Role in organization
}
```

### Invariants

1. **ID Uniqueness:**
   - Each user has unique text ID
   - Used for task ownership and assignment

2. **Role Authority:**
   - Only two roles: 'member' and 'lead'
   - Defines access control boundaries

3. **Name Required:**
   - Non-empty display name for UI rendering

### Role Semantics

**Member Role:**
- Default role for most team users
- Can create and manage own tasks
- Can view only own and assigned tasks
- Cannot view team productivity report
- Cannot assign tasks to others (except owner)

**Lead Role:**
- Administrative role for team leadership
- Can create, view, and manage all team tasks
- Can assign tasks to any team member
- Can generate team productivity reports
- Can see overdue tasks and summaries

## Entity Relationships

### Task Ownership

```
TeamUser (1) ──owns──> TaskItem (*)
  - id = ownerId
  
  Semantics:
  - Every task has exactly one owner
  - Owner created the task
  - Owner is often assignee initially
  - Owner cannot be changed
```

### Task Assignment

```
TeamUser (1) ──assigned──> TaskItem (*)
  - id = assigneeId
  
  Semantics:
  - Every task is assigned to exactly one user
  - Assigned user responsible for task completion
  - Can be reassigned (multi-assign out of scope)
  - Usually different from owner, but can be same
```

### Team Composition

```
TaskFlow Application
├── TeamUser[0..N]
│   ├── Created Tasks
│   └── Assigned Tasks
└── TaskItem[0..M]
    ├── Owner (TeamUser)
    └── Assignee (TeamUser)
```

## State Machines

### Task Lifecycle (Status Transitions)

```
┌─────────┐
│  To Do  │
└────┬────┘
     │ Started
     v
┌──────────────┐
│ In Progress  │
└─────┬────────┘
      │ Completed
      v
┌──────┐
│ Done │
└──────┘

Valid Transitions:
- To Do ──→ In Progress (started work)
- To Do ──→ Done (direct completion)
- In Progress ──→ To Do (paused/reopened)
- In Progress ──→ Done (completed)
- Done ──→ To Do (reopened)
- Done ──→ In Progress (resumed)
```

**Interpretation:**
- Status reflects current task progress
- No "hidden" or "archived" states in MVP
- Can transition between any states for flexibility

## Duplication & Cascading

### On Task Owner Deletion
- Handling TBD in Phase 2 (backend)
- Options:
  - Soft delete (mark inactive, reassign tasks)
  - Reassign to team lead
  - Archive old tasks

### On Task Assignee Deletion
- Handling TBD in Phase 2
- Consider auto-reassign to owner

## Data Size & Volume Assumptions

### MVP Scale
- Team size: 3-20 users
- Tasks per user: 10-50 active at any time
- Total stored tasks: 50-500 across team
- Dashboard query: 100-200 visible tasks per user
- Performance target: Filter/sort < 2 seconds

### Phase 2+ Scale
- Team size: 100+ users
- Tasks: 10,000+ active, 100,000+ total historical
- Archive/prune strategy needed
- Database indexing on (ownerId, dueDate, status)
- Pagination or lazy-load for large lists

## Validation Rules

### TaskItem Creation

```typescript
function validateTaskCreation(data: Partial<TaskItem>): { valid: boolean; errors: string[] } {
  const errors: string[] = [];

  // Title validation
  if (!data.title?.trim()) {
    errors.push('Title is required');
  } else if (data.title.length > 100) {
    errors.push('Title must be 1-100 characters');
  }

  // Description validation
  if (data.description && data.description.length > 500) {
    errors.push('Description must be 0-500 characters');
  }

  // Due date validation
  if (!data.dueDate) {
    errors.push('Due date is required');
  } else if (!/^\d{4}-\d{2}-\d{2}$/.test(data.dueDate)) {
    errors.push('Due date must be valid ISO 8601 date');
  }

  // Priority validation
  const validPriorities = ['Low', 'Medium', 'High'];
  if (data.priority && !validPriorities.includes(data.priority)) {
    errors.push('Priority must be Low, Medium, or High');
  }

  // Status validation (optional at creation)
  const validStatuses = ['To Do', 'In Progress', 'Done'];
  if (data.status && !validStatuses.includes(data.status)) {
    errors.push('Status must be valid');
  }

  // Assignee validation
  if (data.assigneeId && !isValidUserId(data.assigneeId)) {
    errors.push('Assignee must be valid user');
  }

  return {
    valid: errors.length === 0,
    errors
  };
}
```

### Transition Validation

```typescript
function canTransitionStatus(from: TaskStatus, to: TaskStatus): boolean {
  // All transitions allowed for MVP
  // Can refine later if needed
  return from !== to && ['To Do', 'In Progress', 'Done'].includes(to);
}
```

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Status:** MVP Specification
