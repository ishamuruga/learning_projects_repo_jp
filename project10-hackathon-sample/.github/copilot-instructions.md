# GitHub Copilot Instructions for TaskFlow

This document provides structured guidance for GitHub Copilot to generate, refactor, validate, and test TaskFlow application features while adhering to specification, architecture, and coding standards.

## 1. Context & Constraints

**Application:** TaskFlow — Daily Task Management and Productivity Platform  
**Framework:** Angular 18+  
**Language:** TypeScript  
**Specification:** [spec.md](../../spec.md)  
**Architecture:** [docs/application_architecture.md](../../docs/application_architecture.md)  
**Design Rules:** [docs/application_coding_rules_guidelines.md](../../docs/application_coding_rules_guidelines.md)  

**Core Principles:**
- AI-native development: Use Copilot as primary substrate for feature generation
- Spec-first: All work traces back to acceptance criteria and user stories
- Test-driven: Write tests before or immediately after code generation
- Role-aware security: Enforce authorization boundaries in all operations
- Observable & maintainable: Clear naming, documented intent, structured error handling

## 2. Custom Agents for Scoped Tasks

### Agent: TaskFlow-FeatureGeneration
**Purpose:** Generate Angular components, services, and business logic for new features  
**Trigger:** When implementing AC-01 through AC-05  
**Process:**
1. Read spec.md to extract feature requirements and acceptance criteria
2. Review architecture.md to understand component boundaries and data flow
3. Generate TypeScript interfaces/types matching the entity model
4. Create Angular component(s) with template, styles, and logic
5. Generate service layer if API integration required
6. Write unit and functional tests using Jasmine/Karma
7. Validate against coding rules (NFRs, authorization, performance)

**Input Example:**
```
Generate the task creation feature (AC-01) including:
- Form component with title, description, priority, due date, status fields
- Validation rules (required fields, date format)
- Task creation service method
- Jasmine unit tests for form validation and service
- SCSS styles following design tokens
```

### Agent: TaskFlow-RoleBasedAccessControl
**Purpose:** Implement and validate role-aware visibility and authorization  
**Trigger:** When implementing AC-02, AC-04 (lead-only features)  
**Process:**
1. Extract role and ownership rules from spec.md and AC criteria
2. Design role-based access matrix (member vs. lead permissions)
3. Generate type guards and service methods for authorization checks
4. Implement visibility filters in component logic
5. Add authorization tests to the component spec
6. Verify no unauthorized data exposure in debug logs or templates

**Key Rules:**
- Members see only own tasks and assigned tasks
- Leads see all team tasks
- Assignment and status updates enforce canEdit rules
- No PII logged in plain text (NFR-01)

### Agent: TaskFlow-TestGeneration
**Purpose:** Create comprehensive unit and functional tests aligned to acceptance criteria  
**Trigger:** After each feature component is generated  
**Process:**
1. Map test cases to acceptance criteria (AC-01 → AC-06)
2. Generate Jasmine describe/it blocks for each AC
3. Create TestBed fixtures and mock dependencies
4. Write assertions validating business logic and edge cases
5. Ensure test names reflect spec language (e.g., "should enforce role-aware visibility")
6. Add tests to spec file and update coverage tracking

**Test Categories:**
- **Unit:** Component logic, service methods, date calculations
- **Functional:** Form submissions, filter interactions, status updates
- **Integration:** Multi-component workflows, data flow end-to-end
- **Authorization:** Role-based access, visibility boundaries

## 3. Reusable Prompts

### Prompt: feature-specification-decomposition
**File:** [.github/prompts/feature-spec-decomposition.md](#)  
**Purpose:** Break down high-level feature requirements into granular implementation tasks  
**Input:** Feature name, acceptance criteria, entity model  
**Output:** Structured task list with inputs, outputs, dependencies

**Example:**
```
Feature: Task Creation (AC-01)
Acceptance Criteria: User can create task with title, description, priority, due date, status
Decompose into:
1. Task entity interface and type definitions
2. Reactive form component with inputs and validation
3. TaskService.createTask() method with business logic
4. Unit tests for form and service
5. Integration tests for end-to-end creation workflow
```

### Prompt: component-template-generation
**File:** [.github/prompts/component-template-generation.md](#)  
**Purpose:** Generate Angular component templates with proper binding, validation feedback, accessibility  
**Input:** Component purpose, form fields, data bindings, event handlers  
**Output:** HTML template with two-way binding, error display, responsive layout

### Prompt: authorization-validation
**File:** [.github/prompts/authorization-validation.md](#)  
**Purpose:** Verify that component logic enforces role and ownership rules  
**Input:** Component logic, role definitions, ownership model  
**Output:** Validated logic with authorization checks, test cases for boundary violations

### Prompt: test-case-generation
**File:** [.github/prompts/test-case-generation.md](#)  
**Purpose:** Generate complete test suites mapping to acceptance criteria  
**Input:** Feature name, acceptance criteria, component/service under test  
**Output:** Jasmine spec file with test cases, fixtures, mocks, assertions

### Prompt: ci-validation
**File:** [.github/prompts/ci-validation.md](#)  
**Purpose:** Ensure CI pipeline runs all checks and captures evidence  
**Input:** Build command, test frameworks, coverage thresholds  
**Output:** GitHub Actions workflow YAML with build, test, coverage, lint steps

## 4. Integration with Design & Code Generation

### Reference Architecture
- **Source:** [docs/application_architecture.md](../../docs/application_architecture.md)
- **Use When:** Designing new components or services
- **Key Concepts:** 
  - Component tree hierarchy
  - Smart vs. presentational components
  - Service layer responsibilities
  - Data flow direction

### Reference Entity Design
- **Source:** [docs/application_entity_design.md](../../docs/application_entity_design.md)
- **Use When:** Defining data structures, interfaces, models
- **Key Concepts:**
  - TaskItem structure and invariants
  - TeamUser roles and permissions
  - Task lifecycle and state transitions

### Reference Component Design
- **Source:** [docs/application_component_design.md](../../docs/application_component_design.md)
- **Use When:** Creating new components, modifying existing ones
- **Key Concepts:**
  - Component responsibilities (form, list, report, etc.)
  - Input/output contracts
  - Lifecycle hooks usage

### Reference Coding Rules
- **Source:** [docs/application_coding_rules_guidelines.md](../../docs/application_coding_rules_guidelines.md)
- **Use When:** Generating any code
- **Key Constraints:**
  - TypeScript strict mode, no `any` types
  - immutable data patterns
  - Error handling and edge cases
  - Accessibility (a11y) in templates
  - Performance guards (change detection, memoization)

## 5. Workflow for End-to-End Feature Generation

### Step 1: Requirement Extraction
Use **TaskFlow-FeatureGeneration** agent to:
1. Read spec.md for feature goal and AC
2. Identify inputs (form fields, API params), outputs (state updates, notifications), dependencies (other features)
3. Design interfaces and types

### Step 2: Component & Service Generation
Use **feature-specification-decomposition** prompt to break down into:
1. Component(s) — template, styles, logic
2. Service(s) — business logic, persistence interface
3. Types/interfaces — data contracts

### Step 3: Authorization & Business Logic
Use **TaskFlow-RoleBasedAccessControl** agent to:
1. Add role checks (canView, canEdit)
2. Validate visibility filters
3. Write authorization tests

### Step 4: Test Generation
Use **TaskFlow-TestGeneration** agent to:
1. Map test cases to acceptance criteria
2. Generate unit and functional tests
3. Ensure coverage of happy path and edge cases

### Step 5: CI Validation
Use **ci-validation** prompt to:
1. Update/create GitHub Actions workflow
2. Add build, test, lint, coverage steps
3. Configure test reporting

### Step 6: Code Review & Refinement
1. Review generated code against coding rules
2. Check for test evidence and coverage
3. Validate traceability to spec and acceptance criteria
4. Refine or regenerate if needed

## 6. Acceptance Criteria Mapping to Code Artifacts

| AC ID | Feature | Component(s) | Service(s) | Tests | Documentation |
|-------|---------|-------------|-----------|-------|----------------|
| AC-01 | Create Task | TaskFormComponent | TaskService.createTask() | create-task.spec.ts | component-design.md |
| AC-02 | Assign & Update | TaskCardComponent, TaskListComponent | TaskService.updateStatus(), assignTask() | update-task.spec.ts | component-design.md |
| AC-03 | Filtered Dashboard | TaskDashboardComponent, FilterComponent | TaskService.getFilteredTasks() | dashboard.spec.ts | component-design.md |
| AC-04 | Productivity Report | ReportComponent | ReportService.generateTeamReport() | report.spec.ts | component-design.md |
| AC-05 | Reminders | ReminderPanelComponent | ReminderService.detectDueSoon() | reminder.spec.ts | component-design.md |
| AC-06 | CI Pipeline | N/A (build artifact) | N/A | All tests | workflow .yml |

## 7. Quality Gates & Validation Checklist

Before marking a feature complete, verify:

- [ ] **Specification Traceability:** Feature implementation maps to AC and spec.md
- [ ] **Code Quality:** No TypeScript/linting errors, all types defined, no `any`
- [ ] **Authorization:** Role checks and visibility filters enforced, no unauthorized access
- [ ] **Testing:** Unit tests pass, functional tests pass, coverage > 70%
- [ ] **Performance:** Dashboard load < 2s (NFR-03), no memory leaks
- [ ] **Documentation:** Inline comments, function signatures, design decisions noted
- [ ] **CI Passing:** Build, tests, lint all green in GitHub Actions
- [ ] **Design Alignment:** Component follow architecture and design guidelines

## 8. Common Patterns & Recipes

### Pattern: Safe Task Creation
```typescript
createTask(): void {
  // Validate required fields
  const title = this.taskForm.title.trim();
  if (!title || !this.taskForm.dueDate) return;

  // Create entity matching spec
  const newTask: TaskItem = {
    id: this.nextTaskId++,
    title,
    description: this.taskForm.description.trim(),
    priority: this.taskForm.priority,
    dueDate: this.taskForm.dueDate,
    status: this.taskForm.status,
    ownerId: this.currentUser.id, // Enforce ownership
    assigneeId: this.taskForm.assigneeId
  };

  // Persist and update state immutably
  this.tasks = [newTask, ...this.tasks];
  this.resetForm();
}
```

### Pattern: Role-Based Visibility
```typescript
canViewTask(task: TaskItem): boolean {
  if (this.isLead) return true; // Leads see all
  // Members see only own or assigned
  return task.ownerId === this.currentUser.id || 
         task.assigneeId === this.currentUser.id;
}

get visibleTasks(): TaskItem[] {
  return this.tasks.filter(t => this.canViewTask(t));
}
```

### Pattern: Reminder Detection
```typescript
isDueWithin24Hours(task: TaskItem, reference: Date = new Date()): boolean {
  const due = new Date(task.dueDate);
  due.setHours(23, 59, 59, 999); // End of day
  const diffMs = due.getTime() - reference.getTime();
  return diffMs > 0 && diffMs <= 24 * 60 * 60 * 1000;
}
```

### Pattern: Test for Authorization
```typescript
it('should enforce role-aware visibility for members', () => {
  app.tasks = [{ id: 1, ownerId: 'u1', assigneeId: 'u1', ... }];
  app.currentUserId = 'u2'; // Different user, not assigned
  expect(app.visibleTasks.length).toBe(0);
});
```

## 9. Escalation & Refinement

If generated code does not meet quality gates:

1. **Review Failures:**
   - Compare against coding rules and architecture
   - Check for missing error handling, edge cases
   - Validate test coverage

2. **Refine Prompt:**
   - Add architectural constraints to prompt
   - Specify code style and naming conventions
   - Include relevant reference doc sections

3. **Regenerate:**
   - Use updated prompt with context
   - Review output inline
   - Iterate until quality gates pass

4. **Document Decisions:**
   - Record why refinement was needed
   - Update reusable prompts if pattern is recurring
   - Share learnings with team

---

**Document Version:** 1.0  
**Maintained By:** GitHub Copilot Engineering Team  
**Last Updated:** June 2026  
**Next Review:** Post-Hackathon
