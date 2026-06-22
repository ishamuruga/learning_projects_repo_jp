# Application Component Design

## Overview

This document specifies the design, responsibilities, and interactions of TaskFlow Angular components. Each component is classified as either a "smart" (container) component or a "presentational" (dumb) component based on responsibility.

## Component Hierarchy

```
AppComponent (Smart/Container)
├── Purpose: Root container, state management, orchestration
├── Responsibilities:
│   ├── Manage task list
│   ├── Manage filters
│   ├── Manage current user session
│   ├── Orchestrate child component events
│   └── Enforce role-based visibility
└── Child Integration:
    ├── TaskForm (future extraction)
    ├── ReminderPanel (future extraction)
    ├── TaskList (future extraction)
    └── ReportComponent (future extraction)
```

## Component Specifications

### 1. AppComponent (Smart Container)

**Type:** Smart / Container Component

**Purpose:** Root application component managing global state, user session, and task lifecycle.

**Location:** `src/app/app.component.ts`

#### Inputs
- None (root component)

#### Outputs
- None (root component)

#### Properties

```typescript
export class AppComponent {
  // User & Session
  users: TeamUser[];
  currentUserId: string;
  
  // Task State
  tasks: TaskItem[];
  nextTaskId: number;
  
  // UI State
  taskForm: {
    title: string;
    description: string;
    priority: Priority;
    dueDate: string;
    status: TaskStatus;
    assigneeId: string;
  };
  
  filters: {
    ownerId: string;      // 'all' or specific user ID
    status: string;        // 'all' or status value
    priority: string;      // 'all' or priority value
    dueBucket: string;     // 'all', 'overdue', 'today', 'week'
  };
}
```

#### Computed Properties (Getters)

| Getter | Returns | Purpose |
|--------|---------|---------|
| `currentUser` | TeamUser | Current logged-in user object |
| `isLead` | boolean | Whether current user is lead |
| `visibleTasks` | TaskItem[] | Filtered, sorted tasks respecting role-aware visibility |
| `reminderTasks` | TaskItem[] | Tasks due within 24 hours |
| `completedCount` | number | Count of done tasks in visible list |
| `overdueCount` | number | Count of overdue tasks in visible list |
| `inProgressCount` | number | Count of in-progress tasks |
| `teamReport` | TeamMemberReportItem[] | Aggregated report (lead only) |

#### Methods

| Method | Signature | Purpose | Authorization |
|--------|-----------|---------|--------------|
| `createTask` | `(): void` | Validate form and add task | Owner (currentUser) |
| `updateStatus` | `(task, status): void` | Change task status | Lead or task owner/assignee |
| `assignTask` | `(task, assigneeId): void` | Reassign task to user | Lead or task owner |
| `canViewTask` | `(task): boolean` | Check if user can see task | Role-based |
| `canEditTask` | `(task): boolean` | Check if user can edit task | Role-based |
| `ownerName` | `(userId): string` | Get user display name | Public |
| `isOverdue` | `(task): boolean` | Check if task past due | Public |
| `isDueToday` | `(task): boolean` | Check if task due today | Public |
| `isDueThisWeek` | `(task): boolean` | Check if due within 7 days | Public |
| `isDueWithin24Hours` | `(task, reference?): boolean` | Check if due within 24 hrs | Public |

#### Template Sections

| Section | Feature | Acceptance Criteria |
|---------|---------|-------------------|
| Header/Topbar | Sign-in simulation, title | AC-* all |
| Dashboard Grid | Metric cards (visible, in progress, completed, overdue) | AC-03 |
| Content Grid | Task form + reminder panel | AC-01, AC-05 |
| Task Panel | Filter controls + task list/cards | AC-03 |
| Report Panel | Team productivity table (lead only) | AC-04 |

#### State Management Approach

- Component-level state with TypeScript properties
- Immutable data updates (create new arrays/objects)
- Computed properties via getters for derived state
- No external state management (Phase 1)

**Future Enhancement (Phase 2):**
- Move to RxJS Observables
- Consider NgRx store for complex state

### 2. TaskFormComponent (Presentational, Future Extraction)

**Type:** Presentational / Dumb Component

**Purpose:** Form UI for creating new tasks with validation feedback.

**When Extracting:**
- Create `src/app/task-form/task-form.component.ts`
- Use `@Input()` and `@Output()` decorators

#### Component Contract

```typescript
@Component({
  selector: 'app-task-form',
  standalone: true,
  inputs: ['users', 'assigneeDefault'],
  outputs: ['taskCreated'],
  template: `...`,
  styleUrls: ['./task-form.component.scss']
})
export class TaskFormComponent {
  @Input() users!: TeamUser[];
  @Input() assigneeDefault: string = '';
  @Output() taskCreated = new EventEmitter<TaskItem>();
  
  form: FormGroup;
  
  onSubmit(): void {
    if (!this.form.valid) return;
    const task = this.buildTask();
    this.taskCreated.emit(task);
    this.form.reset();
  }
}
```

#### Form Fields

| Field | Type | Validation | Default |
|-------|------|-----------|---------|
| Title | text | Required, max 100 | '' |
| Description | textarea | Optional, max 500 | '' |
| Due Date | date | Required, valid date | Today + 2 days |
| Priority | select | Required | 'Medium' |
| Status | select | Optional | 'To Do' |
| Assignee | select | Required | currentUser |

### 3. TaskListComponent (Presentational, Future Extraction)

**Type:** Presentational / Dumb Component

**Purpose:** Display filtered list of tasks with status controls.

#### Component Contract

```typescript
@Component({
  selector: 'app-task-list',
  inputs: ['tasks', 'currentUserId', 'canEdit'],
  outputs: ['statusChanged', 'assigneeChanged'],
  template: `...`
})
export class TaskListComponent {
  @Input() tasks!: TaskItem[];
  @Input() currentUserId!: string;
  @Input() canEdit: (task: TaskItem) => boolean;
  @Output() statusChanged = new EventEmitter<{ task: TaskItem; status: TaskStatus }>();
  @Output() assigneeChanged = new EventEmitter<{ task: TaskItem; assigneeId: string }>();
}
```

### 4. TaskCardComponent (Presentational, Future Extraction)

**Type:** Presentational / Dumb Component

**Purpose:** Individual task display widget with status/assignment controls.

#### Component Contract

```typescript
@Component({
  selector: 'app-task-card',
  inputs: ['task', 'users', 'canEdit'],
  outputs: ['statusChanged', 'assignChanged'],
  template: `...`
})
export class TaskCardComponent {
  @Input() task!: TaskItem;
  @Input() users!: TeamUser[];
  @Input() canEdit: boolean = false;
  @Output() statusChanged = new EventEmitter<TaskStatus>();
  @Output() assignChanged = new EventEmitter<string>(); // New assignee ID
}
```

### 5. ReminderPanelComponent (Presentational, Future Extraction)

**Type:** Presentational / Dumb Component

**Purpose:** Display upcoming reminders (tasks due within 24 hours).

#### Component Contract

```typescript
@Component({
  selector: 'app-reminder-panel',
  inputs: ['tasks', 'users'],
  template: `...`
})
export class ReminderPanelComponent {
  @Input() tasks!: TaskItem[];
  @Input() users!: TeamUser[];
}
```

### 6. ReportComponent (Presentational, Future Extraction)

**Type:** Smart Container (for lead only)

**Purpose:** Display team productivity report (lead only).

#### Component Contract

```typescript
@Component({
  selector: 'app-report',
  inputs: ['reportData', 'isUserLead'],
  template: `...`
})
export class ReportComponent {
  @Input() reportData!: TeamMemberReportItem[];
  @Input() isUserLead: boolean = false;
}
```

#### Report Data Structure

```typescript
interface TeamMemberReportItem {
  assignee: string;      // User display name
  completed: number;     // Done task count
  overdue: number;       // Overdue task count
  open: number;          // Not done task count
}
```

## Data Flow Patterns

### Pattern 1: Parent ← Child (Event Emission)

**Use:** Child component notifies parent of user action.

```typescript
// Parent (AppComponent)
<app-task-form (taskCreated)="onTaskCreated($event)"></app-task-form>

onTaskCreated(task: TaskItem): void {
  this.tasks = [task, ...this.tasks];
}

// Child (TaskFormComponent)
@Output() taskCreated = new EventEmitter<TaskItem>();

onSubmit(): void {
  const task = this.buildTask();
  this.taskCreated.emit(task); // Notify parent
}
```

### Pattern 2: Parent → Child (Property Binding)

**Use:** Parent passes data to child for display.

```typescript
// Parent (AppComponent)
<app-task-card [task]="myTask" [users]="allUsers"></app-task-card>

// Child (TaskCardComponent)
@Input() task!: TaskItem;
@Input() users!: TeamUser[];

template: `{{ task.title }}`
```

### Pattern 3: Two-Way Binding (ngModel)

**Use:** Simple form binding in single component.

```typescript
// Component (AppComponent)
<input [(ngModel)]="taskForm.title" name="title" />

// Sets component.taskForm.title when input changes
// Updates input when component.taskForm.title changes
```

## Change Detection Optimization

### Current Approach (Phase 1)
- Default change detection strategy
- Immutable data updates to trigger detection
- Computed properties (getters) for derived state

### Recommended Optimization (Phase 2)
- Migrate to `ChangeDetectionStrategy.OnPush`
- Convert to RxJS Observables with async pipe
- Use OnPush on presentational components

```typescript
@Component({
  selector: 'app-task-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  ...
})
export class TaskCardComponent {
  @Input() task!: TaskItem; // Must be immutable
  
  constructor(private cdr: ChangeDetectorRef) {}
  
  onStatusChange(): void {
    // Manually trigger if needed
    this.cdr.markForCheck();
  }
}
```

## Event Handling

### Button Click Events

```html
<!-- Status update buttons -->
<button (click)="updateStatus(task, 'To Do')">To Do</button>
<button (click)="updateStatus(task, 'In Progress')">In Progress</button>
<button (click)="updateStatus(task, 'Done')">Done</button>

<!-- Authorization check in method -->
updateStatus(task: TaskItem, status: TaskStatus): void {
  if (!this.canEditTask(task)) {
    // Silently return or show error
    return;
  }
  // Update logic
}
```

### Form Submission

```html
<form (ngSubmit)="createTask()">
  <input [(ngModel)]="taskForm.title" />
  <button type="submit" [disabled]="!isFormValid">Create</button>
</form>
```

### Select/Dropdown Change

```html
<select [(ngModel)]="filters.status" name="statusFilter">
  <option value="all">All Status</option>
  <option value="To Do">To Do</option>
</select>

<!-- Computed property automatically updates on change -->
```

## Styling & CSS Classes

### BEM Naming Convention

```scss
// Block
.task-card {
  // Element
  .task-card__title { }
  .task-card__status { }
  
  // Modifier
  &.task-card--overdue { }
  &.task-card--completed { }
}
```

### Design Tokens

```scss
:host {
  --brand: #0f8b8d;
  --brand-dark: #0a6b6c;
  --accent: #e07a5f;
  --warning: #c0392b;
  --bg-soft: #f4f8fb;
  --card: #ffffff;
}

.alert {
  color: var(--warning);
}
```

### Responsive Breakpoints

```scss
@media (max-width: 960px) {
  // Tablet adjustments
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  // Mobile adjustments
  .form-grid {
    grid-template-columns: 1fr;
  }
}
```

## Testing Strategy

### Unit Tests (Service Methods)

```typescript
describe('TaskService', () => {
  it('should create task with enforced ownership', () => {
    const task = service.createTask({ title: 'Test' }, 'u1');
    expect(task.ownerId).toBe('u1');
  });
});
```

### Component Tests (UI Interaction)

```typescript
describe('TaskFormComponent', () => {
  it('should emit taskCreated on form submit', () => {
    component.taskForm.title = 'Test';
    fixture.detectChanges();
    component.onSubmit();
    expect(component.taskCreated.emit).toHaveBeenCalled();
  });
});
```

### Integration Tests (End-to-End)

```typescript
describe('Task Creation Workflow', () => {
  it('should create task end-to-end', () => {
    // Fill form
    // Submit
    // Verify task appears in list
  });
});
```

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Status:** MVP Specification with Future Extraction Plan
