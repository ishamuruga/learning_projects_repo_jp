# Prompt: Feature Specification Decomposition

## Purpose

Break down high-level feature requirements (from acceptance criteria) into granular, implementation-ready tasks with clear inputs, outputs, and dependencies.

## Input Template

```
Feature: [Feature Name]
Business Case: [Problem statement]
Acceptance Criteria: AC-XX: [Criterion text]
Related Spec Section: [Link to spec.md]

Key Entities:
- [Entity 1 with properties]
- [Entity 2 with properties]

User Roles Involved:
- [Role 1]: [Responsibilities for this feature]
- [Role 2]: [Responsibilities for this feature]

Design Constraints:
- [Constraint from spec or architecture]
- [Performance requirement]
- [Security/authorization rule]

Out of Scope:
- [What is explicitly NOT included]
```

## Output Format

Return a structured decomposition:

```
Feature: [Name]
Epic User Story: "As a [persona], I want to [action], so that [value]"

Phase 1: Domain Modeling
Task 1.1: Define Entity Interfaces
  Input: Entity requirements from spec
  Output: TypeScript interfaces (.ts files)
  Acceptance: All properties typed, no `any`, matches spec
  Dependencies: None

Task 1.2: Define Service Contracts
  Input: Entity interfaces, business logic requirements
  Output: Service interface with method signatures
  Acceptance: All methods documented, return types specified
  Dependencies: Task 1.1

Phase 2: Component Architecture
Task 2.1: Design Container Component
  Input: Feature requirements, entity models
  Output: Component spec with @Input/@Output contracts
  Acceptance: Clear data flow, event contracts defined
  Dependencies: Phase 1

Task 2.2: Design Presentational Components
  Input: UI mockups, data contracts
  Output: Component specs for each UI element
  Acceptance: Input/output props clear, reusable
  Dependencies: Task 2.1

Phase 3: Service Implementation
Task 3.1: Implement Business Logic
  Input: Entity interfaces, domain rules, authorization
  Output: Service class with methods
  Acceptance: Logic matches spec, authorization enforced
  Dependencies: Phase 1, 2.1

Phase 4: Component Implementation
Task 4.1: Implement Container Component
  Input: Component spec, service methods
  Output: Component .ts/.html/.scss files
  Acceptance: Data flow correct, event handling works
  Dependencies: Phase 2, 3

Task 4.2: Implement Presentational Components
  Input: Component specs, design tokens
  Output: Reusable presentational components
  Acceptance: Renders correctly, props handled
  Dependencies: Phase 2, 4.1

Phase 5: Testing
Task 5.1: Write Unit Tests for Service
  Input: Service implementation, business logic
  Output: Jasmine spec file with coverage
  Acceptance: All methods tested, edge cases covered, > 70% coverage
  Dependencies: Phase 3

Task 5.2: Write Component Tests
  Input: Component implementation, user interactions
  Output: Component spec file with functional tests
  Acceptance: User workflows tested, authorization validated
  Dependencies: Phase 4

Phase 6: Integration & Documentation
Task 6.1: Integration Testing
  Input: All components and services
  Output: End-to-end test scenarios
  Acceptance: Feature works end-to-end, AC criteria verified
  Dependencies: Phase 5

Task 6.2: Documentation
  Input: Code, design decisions
  Output: Inline comments, README section, architecture updates
  Acceptance: Future developers can understand code
  Dependencies: Phase 4
```

## Example: AC-01 Task Creation Decomposition

**Input:**
```
Feature: Create Task (AC-01)
Acceptance Criteria: 
  - User can create task with title, description, priority, due date, status
  - Task appears in dashboard
  - Ownership enforced (ownerId = currentUser.id)
  - Validation on required fields

Design Constraints:
  - Cannot log PII (NFR-01)
  - Form must validate before submission
  - Task ownership immutable after creation
```

**Output:**

```
Feature: Create Task

Epic User Story: 
"As a team member, I want to create a task with title, deadline, and priority, 
so that I can organize my work and assign it to teammates."

Phase 1: Domain Modeling
=========================

Task 1.1: Define TaskItem Interface
  Input: Entity model from spec.md
  Output: src/app/models/task.model.ts
  
  Generated Interface:
  ```typescript
  export interface TaskItem {
    id: number;
    title: string;
    description: string;
    priority: 'Low' | 'Medium' | 'High';
    dueDate: string; // ISO 8601
    status: 'To Do' | 'In Progress' | 'Done';
    ownerId: string;
    assigneeId: string;
  }
  ```
  
  Acceptance Criteria:
  - ✓ All properties match spec
  - ✓ Types are strict (no `any`)
  - ✓ Enums for closed-set values

Task 1.2: Define TaskService Interface
  Input: Business logic requirements for AC-01
  Output: src/app/services/task.service.ts (interface)
  
  Generated Interface:
  ```typescript
  export interface ITaskService {
    createTask(data: CreateTaskRequest, ownerId: string): TaskItem;
    validateTaskData(data: Partial<TaskItem>): ValidationResult;
  }
  ```
  
  Acceptance Criteria:
  - ✓ createTask takes dto + owner context
  - ✓ Validation separated as method
  - ✓ Clear return type

Phase 2: Component Architecture
===============================

Task 2.1: Design TaskFormComponent
  Input: AC-01 requirements, form fields
  Output: Design spec for form component
  
  Component Contract:
  ```typescript
  @Component({
    selector: 'app-task-form',
    inputs: ['initialTask', 'users'],
    outputs: ['taskCreated']
  })
  class TaskFormComponent {
    @Input() initialTask?: Partial<TaskItem>;
    @Input() users: TeamUser[];
    @Output() taskCreated = new EventEmitter<TaskItem>();
    
    form: FormGroup; // Reactive form with validators
  }
  ```
  
  Acceptance Criteria:
  - ✓ Inputs/outputs defined
  - ✓ Form validation included
  - ✓ Reusable across contexts

Task 2.2: Define Form Validation Rules
  Input: AC-01 field requirements
  Output: Validation rule set
  
  Rules:
  - Title: Required, max 100 chars
  - Description: Optional, max 500 chars
  - DueDate: Required, valid ISO date
  - Priority: Required, one of Low/Medium/High
  - Status: Required, one of To Do/In Progress/Done
  - Assignee: Required, valid userId
  
  Acceptance Criteria:
  - ✓ All required fields marked
  - ✓ Pattern validators specified
  - ✓ Custom validators identified

Phase 3: Service Implementation
===============================

Task 3.1: Implement TaskService.createTask()
  Input: TaskItem interface, validation rules
  Output: src/app/services/task.service.ts
  
  Implementation Steps:
  1. Validate input data (required fields, formats)
  2. Enforce ownership: task.ownerId = ownerId (parameter)
  3. Generate unique task ID
  4. Set defaults for optional fields
  5. Return TaskItem
  
  Generated Code Outline:
  ```typescript
  createTask(data: Partial<TaskItem>, ownerId: string): TaskItem {
    // Validate required fields
    if (!data.title?.trim() || !data.dueDate) {
      throw new ValidationError('Required fields missing');
    }
    
    // Create with enforced ownership
    const task: TaskItem = {
      id: this.nextId++,
      title: data.title.trim(),
      description: data.description?.trim() || '',
      priority: data.priority || 'Medium',
      dueDate: data.dueDate,
      status: data.status || 'To Do',
      ownerId, // Ownership enforced here
      assigneeId: data.assigneeId || ownerId
    };
    
    return task;
  }
  ```
  
  Acceptance Criteria:
  - ✓ Validation throws on invalid input
  - ✓ Ownership set from parameter (immutable)
  - ✓ No PII in error messages
  - ✓ Defaults applied correctly

Phase 4: Component Implementation
=================================

Task 4.1: Implement TaskFormComponent
  Input: Component design spec, service, validation rules
  Output: src/app/task-form/task-form.component.ts/html/scss
  
  Files to Generate:
  1. task-form.component.ts — Logic, form setup
  2. task-form.component.html — Template with two-way binding
  3. task-form.component.scss — Styled inputs, validation feedback
  
  Template Features:
  - Reactive form with all fields
  - Error messages for each field type
  - Submit button disabled while invalid
  - Clear and reset buttons
  
  Acceptance Criteria:
  - ✓ Form renders all fields
  - ✓ Validation feedback shown
  - ✓ Submit calls service.createTask()
  - ✓ Ownership bound to currentUser
  - ✓ Responsive styling

Phase 5: Testing
===============

Task 5.1: Unit Tests for TaskService.createTask()
  Input: Service implementation
  Output: src/app/services/task.service.spec.ts
  
  Test Cases:
  1. Create task with all fields
  2. Create task with required fields only (test defaults)
  3. Reject missing title
  4. Reject missing dueDate
  5. Enforce ownership (task.ownerId matches parameter)
  6. Trim whitespace from title
  7. Increment task ID correctly
  
  Acceptance Criteria:
  - ✓ All tests pass
  - ✓ Happy path covered
  - ✓ Edge cases covered
  - ✓ Ownership validation explicit
  - ✓ > 70% coverage

Task 5.2: Component Functional Tests
  Input: Component implementation, form logic
  Output: src/app/task-form/task-form.component.spec.ts
  
  Test Cases:
  1. Form renders with all input fields
  2. Submit button disabled when form invalid
  3. Submit button enabled when form valid
  4. Form calls service.createTask on submit
  5. Form emits taskCreated event with result
  6. Form resets after successful creation
  7. Error message shown for validation failures
  
  Acceptance Criteria:
  - ✓ All tests pass
  - ✓ User workflows covered
  - ✓ Error scenarios handled
  - ✓ > 70% coverage

Phase 6: Integration & Documentation
===================================

Task 6.1: End-to-End Integration Test
  Input: All components, services, models
  Output: Integration test scenario
  
  Test Scenario:
  1. Render TaskFormComponent
  2. Fill form with valid data
  3. Submit form
  4. Verify service.createTask() called
  5. Verify taskCreated event emitted
  6. Verify task appears in dashboard
  
  Acceptance Criteria:
  - ✓ AC-01 acceptance criteria verified
  - ✓ Task appears in visibleTasks
  - ✓ Ownership correct

Task 6.2: Inline Documentation
  Input: Code implementation
  Output: JSDoc comments, README section
  
  Deliverables:
  1. TaskFormComponent documented with usage example
  2. TaskService.createTask documented with parameter semantics
  3. Validation rules documented
  4. Update architecture.md with AC-01 component diagram
  
  Acceptance Criteria:
  - ✓ All public methods have JSDoc
  - ✓ Usage examples provided
  - ✓ Architecture diagram updated
```

## Decomposition Patterns

### Pattern: Simple CRUD Feature
1. Domain Modeling (interfaces)
2. Component Design (form)
3. Service Implementation (CRUD method)
4. Component Implementation (template + logic)
5. Testing (unit + functional)
6. Documentation

### Pattern: Report Generation Feature
1. Domain Modeling (data structures)
2. Aggregation Logic (service methods)
3. Report Component (display, filtering)
4. Authorization Guards (role checks)
5. Testing (accuracy of aggregations, access control)
6. Documentation

### Pattern: Background Job Feature (Reminders)
1. Domain Modeling (notification entity)
2. Job Logic (detection, scheduling)
3. UI Panel Component (display)
4. Persistence Layer (queue/log)
5. Testing (job execution, accuracy)
6. Documentation

## Acceptance Criteria for Decomposition Itself

A good decomposition is:
- ✓ **Complete:** Covers all functional and non-functional requirements
- ✓ **Hierarchical:** Clear dependencies and ordering
- ✓ **Sized:** Each task is 1-2 day effort (for hackathon)
- ✓ **Testable:** Each task has clear acceptance criteria
- ✓ **Traceable:** Maps back to AC and spec.md
- ✓ **Implementable:** Developer can clearly execute each step

---

**Prompt Version:** 1.0  
**Last Updated:** June 2026
