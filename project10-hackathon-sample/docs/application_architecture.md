# Application Architecture

## Overview

TaskFlow is a single-page web application built with Angular 18+ that enables users to manage daily tasks, collaborate with team members, and track productivity metrics. The architecture follows component-driven design with a clear separation of concerns between smart (container) and presentational components.

## Architecture Layers

### 1. Presentation Layer (Components)

**Smart Components (Containers):**
- `AppComponent` — Root container managing application state, task list, filters, and role-based visibility
- Responsible for: Data fetching, state management, authorization checks, event coordination

**Presentational Components (Dumb):**
- `TaskFormComponent` — Form for task creation (if extracted)
- `TaskCardComponent` — Individual task display widget (if extracted)
- `FilterPanelComponent` — Filter controls (if extracted)
- `ReportComponent` — Team productivity report (if extracted)
- Responsible for: Rendering data, emitting user events, no data fetching

### 2. Service Layer

**Core Services:**
- `TaskService` — Task CRUD operations, business logic
- `ReminderService` — Reminder detection and notification logic
- `ReportService` — Team report aggregation

**Responsibilities:**
- Business logic implementation
- Data transformation
- Authorization enforcement
- API communication (future backend integration)

### 3. Model Layer

**Data Models:**
- `TaskItem` — Task entity with properties (id, title, description, priority, dueDate, status, ownerId, assigneeId)
- `TeamUser` — User entity with role (member or lead)

## Component Tree

```
AppComponent (Smart/Container)
├── header.topbar
├── dashboard-grid
│   ├── metric-card (Completed)
│   ├── metric-card (In Progress)
│   ├── metric-card (Visible Tasks)
│   └── metric-card (Overdue)
├── content-grid
│   ├── form-panel
│   │   └── TaskFormComponent (if extracted)
│   └── reminder-panel
├── task-panel
│   ├── task-header
│   ├── filters
│   └── task-list
│       └── task-card (repeated)
└── report-panel (if isLead)
    └── ReportComponent (if extracted)
```

## Data Flow

### Task Creation Flow (AC-01)
1. User fills TaskFormComponent
2. Form emits `taskCreated` event with TaskItem
3. AppComponent receives event via `(taskCreated)="onTaskCreated($event)"`
4. AppComponent calls `taskService.createTask()`
5. Service validates, enforces ownership, returns TaskItem
6. AppComponent updates `tasks` array immutably
7. Change detection updates template, task appears in dashboard

### Task Filter Flow (AC-03)
1. User selects filter (owner, status, priority, due date)
2. Filter change updates `filters` object
3. `visibleTasks` getter re-computed
4. Template re-renders with filtered list

### Authorization Flow
1. Component checks `isLead` computed property
2. Calls `canViewTask()` or `canEditTask()` guards
3. Hides/disables unauthorized UI elements
4. Throws error if unauthorized action attempted
5. Component catches error and shows message

## State Management

**Current Approach:** Component state using TypeScript properties and getters

```typescript
export class AppComponent {
  users: TeamUser[];        // Team members
  tasks: TaskItem[];        // All tasks
  currentUserId: string;    // Logged-in user
  filters: {...};           // Active filters
  taskForm: {...};          // Form state
  
  // Computed properties (getters)
  get visibleTasks(): TaskItem[] { ... }
  get reminderTasks(): TaskItem[] { ... }
  get teamReport(): ReportItem[] { ... }
  
  // Methods
  createTask(): void { ... }
  updateStatus(task, status): void { ... }
  assignTask(task, assigneeId): void { ... }
}
```

**Future State Management (Phase 2):**
- RxJS Observables and Subjects for reactive updates
- Potential NgRx store for complex state
- Event sourcing for task history

## Change Detection Strategy

- Default OnPush change detection to optimize performance
- Immutable data updates to trigger change detection
- Computed properties (getters) for derived state

```typescript
// Immutable task list update
this.tasks = [newTask, ...this.tasks];

// Immutable status update
this.tasks = this.tasks.map(t =>
  t.id === task.id ? { ...t, status } : t
);
```

## Authorization Architecture

**Role-Based Access Control (RBAC):**

| Role | View Own | View Assigned | View All | Edit Own | Edit Any | Assign | Report |
|------|----------|---------------|----------|----------|----------|--------|--------|
| Member | ✓ | ✓ | ✗ | ✓ | ✗ | (own) | ✗ |
| Lead | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

**Implementation:**
- Type guards in component: `canViewTask()`, `canEditTask()`, `canAssignTask()`
- Visibility filters in data getters: `visibleTasks`
- Template guards: `@if (isLead)`
- Event handler guards: check before mutation

## Performance Considerations

### Change Detection Optimization
- Use `OnPush` strategy when Angular 18 fully mature
- Memoize computed properties (visibleTasks, reminderTasks, teamReport)
- Lazy-load heavy components (reports)

### Rendering Optimization
- Virtual scrolling for large task lists (future: CDK/ScrollingModule)
- Pagination or infinite scroll for dashboard

### Network Optimization (Phase 2)
- Lazy load features via Router
- API response caching with HttpClient interceptors
- Debounce filter changes to avoid excess API calls

## Deployment Architecture

### Frontend Hosting
- Standalone Angular app bundled as static assets
- Deploy to CDN (e.g., CloudFront, Azure Storage Static Hosting)
- No server-side rendering required

### Backend Integration (Future)
- RESTful API endpoints for task CRUD
- WebSocket for real-time updates (optional)
- Scheduled jobs for reminder notifications

### Database (Future)
- Relational (PostgreSQL) or document (MongoDB) store
- Task, User, TaskHistory collections/tables
- Indexed on (ownerId, dueDate, status) for query performance

## Module Dependencies

**Current Dependencies (Angular 18):**
- `@angular/core` — Core framework
- `@angular/common` — CommonModule for *ngIf, *ngFor
- `@angular/forms` — FormsModule for ngModel
- `@angular/platform-browser` — DOM rendering
- Jasmine/Karma — Testing

**Future Dependencies (Phase 2):**
- `@angular/router` — Multi-page routing
- `@angular/http` — HTTP client
- `rxjs` — Reactive programming
- Optional: `@ngrx/store` — State management
- Optional: `@angular/cdk` — Component dev kit (virtual scroll, etc.)

## Scalability & Extensibility

### Adding New Features
1. Create new component in `src/app/features/[feature-name]/`
2. Add service methods to existing or new service
3. Update main AppComponent to integrate or create router module
4. Write tests for new feature

### Adding New Roles
1. Update `Role` type in models
2. Add role cases to RBAC guards
3. Add template conditions for new role
4. Update tests

### API Integration
1. Inject HttpClient into services
2. Replace in-memory task array with HTTP calls
3. Handle loading/error states with Observables
4. Update tests with HttpTestingController

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Status:** Current Architecture (Phase 1)
