# Application Coding Rules & Guidelines

## Purpose

This document establishes code quality standards, naming conventions, and architectural patterns for TaskFlow to ensure consistency, maintainability, and performance across AI-generated and human-written code.

## TypeScript Standards

### 1. Type Safety

**Rule:** Use strict TypeScript (`strict: true` in tsconfig.json). No `any` type.

```typescript
// ✓ Good
function createTask(title: string, dueDate: string, priority: 'Low' | 'Medium' | 'High'): TaskItem {
  return { ... };
}

// ✗ Bad: No `any`
function createTask(title: any, dueDate: any): any {
  return { ... };
}
```

### 2. Interfaces & Types

**Rule:** Define all public interfaces and types. Document intent.

```typescript
// ✓ Good
/**
 * Represents a task item in the system.
 * @property id - Immutable task identifier
 * @property ownerId - User who created the task (immutable)
 * @property assigneeId - User currently working on task (mutable)
 */
export interface TaskItem {
  id: number;
  title: string;
  description: string;
  priority: 'Low' | 'Medium' | 'High';
  dueDate: string;
  status: 'To Do' | 'In Progress' | 'Done';
  ownerId: string;
  assigneeId: string;
}

// ✗ Bad: Untyped object
const task = { id: 1, title: 'Test' };
```

### 3. Function Signatures

**Rule:** Type all parameters and return types. No implicit `any` return.

```typescript
// ✓ Good
function isTaskOverdue(task: TaskItem, reference: Date = new Date()): boolean {
  const due = new Date(task.dueDate);
  return due.getTime() < reference.getTime();
}

// ✗ Bad: No return type annotation
function isTaskOverdue(task: TaskItem, reference: Date) {
  // ...
}
```

### 4. Union Types & Enums

**Rule:** Use union types for closed sets of values. Consider enums for reuse.

```typescript
// ✓ Good: Union type (inline)
type Priority = 'Low' | 'Medium' | 'High';
type TaskStatus = 'To Do' | 'In Progress' | 'Done';

// ✓ Also good: Enum (if reused in multiple files)
enum Priority { Low = 'Low', Medium = 'Medium', High = 'High' }

// ✗ Bad: String literals scattered
function setPriority(p: string) { ... }
```

## Angular Best Practices

### 1. Component Structure

**Rule:** Organize components as either smart (container) or presentational (dumb).

```typescript
// ✓ Smart Component (Container)
@Component({
  selector: 'app-task-container',
  template: `{{ visibleTasks | json }}`,
  standalone: true
})
export class TaskContainerComponent {
  tasks: TaskItem[];
  currentUserId: string;

  constructor(private taskService: TaskService) {}

  get visibleTasks(): TaskItem[] {
    // Filter logic based on role
  }
}

// ✓ Presentational Component
@Component({
  selector: 'app-task-card',
  inputs: ['task', 'canEdit'],
  outputs: ['statusChanged'],
  template: `<div>{{ task.title }}</div>`,
  standalone: true
})
export class TaskCardComponent {
  @Input() task!: TaskItem;
  @Input() canEdit: boolean = false;
  @Output() statusChanged = new EventEmitter<TaskStatus>();
}
```

### 2. Change Detection

**Rule:** Use OnPush change detection strategy when appropriate.

```typescript
// ✓ Good: OnPush with immutable inputs
@Component({
  selector: 'app-task-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  ...
})
export class TaskListComponent {
  @Input() tasks!: TaskItem[]; // Immutable array
}

// ✗ Avoid: Default change detection runs on every event
@Component({
  selector: 'app-task-list',
  ...
})
```

### 3. Data Binding

**Rule:** Use two-way binding for simple forms. Use reactive patterns for complex logic.

```typescript
// ✓ Simple form with ngModel
<input [(ngModel)]="taskForm.title" name="title" />

// ✓ Complex form logic (future)
// Consider ReactiveFormsModule with FormBuilder, FormGroup, Validators
```

### 4. Template Syntax

**Rule:** Use new Angular control flow syntax (@if, @for) instead of *ngIf, *ngFor.

```typescript
// ✓ New syntax (Angular 17+)
@if (isLead) {
  <section class="report">Report</section>
}

@for (task of visibleTasks; track task.id) {
  <app-task-card [task]="task"></app-task-card>
}

// ✗ Old syntax (avoid)
<section *ngIf="isLead" class="report">Report</section>
<app-task-card *ngFor="let task of visibleTasks" [task]="task"></app-task-card>
```

## Naming Conventions

### Components

```typescript
// Format: [Feature][Type]Component
// ✓ Good
TaskFormComponent
TaskListComponent
TaskCardComponent
ReminderPanelComponent
ReportComponent

// ✗ Bad
TaskComp
Form
Panel123
```

### Services

```typescript
// Format: [Domain]Service
// ✓ Good
TaskService
ReminderService
ReportService

// ✗ Bad
TaskHelper
ServiceTask
Svc
```

### Properties & Variables

```typescript
// camelCase for properties and local variables
// ✓ Good
currentUserId: string;
visibleTasks: TaskItem[];
isDueWithin24Hours(): boolean;

// ✗ Bad
CurrentUserID: string;
visble_tasks: TaskItem[];
is_due_within_24_hours(): boolean;
```

### Constants

```typescript
// UPPER_SNAKE_CASE for constants
// ✓ Good
const TASK_STATUS_OPTIONS = ['To Do', 'In Progress', 'Done'];
const DEFAULT_PRIORITY = 'Medium';
const MAX_TITLE_LENGTH = 100;

// ✗ Bad
const taskStatusOptions = [...];
const DefaultPriority = 'Medium';
```

## Authorization & Security

### 1. Authorization Guards

**Rule:** Always check authorization before sensitive operations. No client-side checks alone.

```typescript
// ✓ Good
canViewTask(task: TaskItem): boolean {
  if (this.isLead) return true; // Leads see all
  return task.ownerId === this.currentUser.id || 
         task.assigneeId === this.currentUser.id;
}

updateStatus(task: TaskItem, status: TaskStatus): void {
  if (!this.canEditTask(task)) {
    throw new Error('Unauthorized: Cannot update this task');
  }
  // Perform update
}

// ✗ Bad: No authorization check
updateStatus(task: TaskItem, status: TaskStatus): void {
  task.status = status; // Anyone can do this
}
```

### 2. Logging

**Rule:** Log authorization failures and important state changes. Never log PII or sensitive data.

```typescript
// ✓ Good: Log failure without PII
console.warn('Unauthorized update attempt for task', task.id);

// ✗ Bad: Logs PII
console.log('User', currentUser.name, 'tried to update task', task);
```

## Performance

### 1. Immutable Data Updates

**Rule:** Use immutable patterns to enable change detection and prevent bugs.

```typescript
// ✓ Good: Create new array
this.tasks = [newTask, ...this.tasks];

// ✓ Good: Map and filter create new arrays
this.tasks = this.tasks.map(t =>
  t.id === taskId ? { ...t, status } : t
);

// ✗ Bad: Mutation (can cause change detection issues)
this.tasks[0].status = 'Done';
```

### 2. Computed Properties

**Rule:** Use getters for derived state. Cache if computation expensive.

```typescript
// ✓ Good: Simple getter
get visibleTasks(): TaskItem[] {
  return this.tasks.filter(t => this.canViewTask(t));
}

// ✓ Future: Memoized if expensive
private cachedVisibleTasks: TaskItem[] | null = null;
private cachedFilter: FilterState | null = null;

get visibleTasks(): TaskItem[] {
  if (this.cachedFilter === this.filters) {
    return this.cachedVisibleTasks!;
  }
  this.cachedVisibleTasks = this.tasks.filter(...);
  this.cachedFilter = { ...this.filters };
  return this.cachedVisibleTasks;
}
```

### 3. Change Detection Optimization

**Rule:** Minimize change detection cycles. Use ChangeDetectionStrategy.OnPush.

```typescript
// ✓ Good: OnPush with immutable @Inputs
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TaskCardComponent {
  @Input() task!: TaskItem; // Immutable
}

// Avoid: Frequent subscriptions to Observables in component
subscription = this.service.tasks$.subscribe(...);
```

## Error Handling

### 1. Validation

**Rule:** Validate user input and throw descriptive errors early.

```typescript
// ✓ Good
function createTask(data: Partial<TaskItem>, ownerId: string): TaskItem {
  if (!data.title?.trim()) {
    throw new Error('Task title is required');
  }
  if (!data.dueDate || !/^\d{4}-\d{2}-\d{2}$/.test(data.dueDate)) {
    throw new Error('Invalid due date format');
  }
  // Valid: proceed
  return { id: ..., title: data.title.trim(), ... };
}

// ✗ Bad: Silent failure or generic error
function createTask(data: any): any {
  try {
    return { title: data.title, ...data };
  } catch (e) {
    console.log('error'); // Non-descriptive
  }
}
```

### 2. Error Messages

**Rule:** Provide actionable, non-technical error messages to users.

```typescript
// ✓ Good: User-friendly
throw new Error('Please enter a task title (1-100 characters)');

// ✗ Bad: Technical jargon
throw new Error('ValidationError: Field "title" violates constraint MIN_LENGTH');
```

## Testing

### 1. Test Organization

**Rule:** One spec file per component/service. Describe tests by feature.

```typescript
describe('TaskService', () => {
  describe('createTask', () => {
    it('should create task with all fields', () => { ... });
    it('should enforce ownership', () => { ... });
    it('should reject missing title', () => { ... });
  });

  describe('updateStatus', () => {
    it('should allow authorized user to update', () => { ... });
    it('should deny unauthorized user', () => { ... });
  });
});
```

### 2. Test Coverage

**Rule:** Aim for > 70% code coverage. Cover happy path, edge cases, and errors.

```typescript
// Test happy path
it('should update task status', () => { ... });

// Test edge case
it('should handle null assignee', () => { ... });

// Test error
it('should throw on invalid status', () => { ... });
```

### 3. Mocking

**Rule:** Mock external dependencies (services, HTTP).

```typescript
// ✓ Good: Mock service
let mockService = jasmine.createSpyObj('TaskService', ['createTask']);
TestBed.configureTestingModule({
  providers: [{ provide: TaskService, useValue: mockService }]
});

// ✓ Good: Spy on method
spyOn(component, 'createTask').and.returnValue({ id: 1, ... });
```

## Documentation

### 1. JSDoc Comments

**Rule:** Document all public methods with intent and parameters.

```typescript
/**
 * Creates a new task and adds it to the task list.
 * 
 * @param data - Partial task data (title and dueDate required)
 * @param ownerId - User ID of task creator (immutable)
 * @returns The created TaskItem with assigned ID
 * @throws Error if title is empty or dueDate invalid
 * 
 * @example
 * const task = service.createTask({
 *   title: 'Review PRs',
 *   dueDate: '2099-12-31'
 * }, 'u1');
 */
createTask(data: Partial<TaskItem>, ownerId: string): TaskItem {
  // ...
}
```

### 2. Inline Comments

**Rule:** Comment complex logic and non-obvious decisions.

```typescript
// ✓ Good: Explains why
// Enforce ownership immutability: task ownerId cannot be overridden by caller
const ownerId = this.currentUser.id;

// ✗ Bad: Obvious comment adds noise
// Set the status
const status = 'To Do';
```

## Accessibility (a11y)

### 1. Semantic HTML

**Rule:** Use semantic HTML elements and ARIA labels.

```html
<!-- ✓ Good: Semantic and labeled -->
<label for="task-title">Task Title</label>
<input id="task-title" type="text" required />

<select aria-label="Filter by priority">
  <option>All</option>
  <option>High</option>
</select>

<!-- ✗ Bad: Non-semantic, no labels -->
<div>Title</div>
<input type="text" />
<div class="select">High</div>
```

### 2. Color & Contrast

**Rule:** Don't rely on color alone. Ensure sufficient contrast ratio (WCAG AA: 4.5:1).

```scss
// ✓ Good: Label + color
.alert {
  border-left: 4px solid #c0392b;
  color: #c0392b;
  
  &::before {
    content: '⚠ ';
  }
}

// ✗ Bad: Color alone
.alert {
  color: red; // Color-blind users may not see priority
}
```

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Enforced By:** Code review, linting (future), and automated tests
