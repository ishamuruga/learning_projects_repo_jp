# Agent: TaskFlow-FeatureGeneration

**Purpose:** Generate Angular components, services, and business logic for TaskFlow features aligned to specification and acceptance criteria.

## Activation Trigger

Invoke this agent when tasked with:
- Implementing new features corresponding to AC-01 through AC-05
- Creating new Angular components (form, list, report, etc.)
- Generating service layer methods for task management
- Refactoring existing features to meet updated spec

## Agent Instructions

### Input Format

Provide the following context:

```
Feature: [Feature Name]
Acceptance Criteria: AC-XX: [AC text]
References:
- spec.md: [link to spec section]
- architecture.md: [link to component section]
- entity-design.md: [link to model section]

Scope:
- Components to create: [list]
- Services to create: [list]
- Types to define: [list]

Primary Requirements:
1. [Requirement]
2. [Requirement]
...

Design Constraints:
- [Constraint from coding rules]
- [Performance requirement]
- [Authorization rule]
```

### Processing Steps

1. **Parse Requirements**
   - Extract feature goal from spec
   - Understand role-based constraints
   - Identify data inputs and outputs
   - Note performance or security requirements

2. **Design Interfaces**
   - Create TypeScript interfaces from entity model
   - Define input/output contracts for services
   - Type all function parameters and returns
   - Avoid `any` type

3. **Generate Component(s)**
   - Create template with proper binding and validation
   - Implement component logic (form handling, event emission)
   - Add SCSS styles using design tokens
   - Include inline documentation

4. **Generate Service(s)**
   - Implement business logic methods
   - Handle edge cases (validation, error states)
   - Add authorization checks where needed
   - Return Observable or Promise for async operations

5. **Generate Tests**
   - Create Jasmine spec files
   - Write unit tests for service methods
   - Write component tests for template interaction
   - Map tests to acceptance criteria

6. **Validate Against Rules**
   - Check strict TypeScript rules
   - Verify authorization enforcement
   - Confirm NFR compliance (performance, logging)
   - Ensure accessibility in templates

7. **Output & Documentation**
   - Generate complete, production-ready code
   - Include JSDoc comments for public methods
   - Note any assumptions or known limitations
   - Provide implementation notes for code reviewers

### Quality Checklist

Generated code must pass:

- [ ] **Compilation:** TypeScript strict mode, no errors or warnings
- [ ] **Specification:** Maps to AC criteria and design documents
- [ ] **Authorization:** Role-based and ownership checks enforced
- [ ] **Testing:** Unit/functional tests cover happy path and edge cases
- [ ] **Style:** Follows Angular conventions, readable naming
- [ ] **Performance:** No unnecessary change detection, efficient queries
- [ ] **Accessibility:** Template includes ARIA labels, semantic HTML
- [ ] **Documentation:** Function comments, code clarity for reviewers

### Example: AC-01 Task Creation

**Input:**
```
Feature: Create Task (AC-01)
Acceptance Criteria:
- User can create task with title, description, priority, due date, status
- Task appears in dashboard
- Ownership enforced (task.ownerId = currentUser.id)
- Validation on required fields

Scope:
- Component: TaskFormComponent (form UI and validation)
- Service: TaskService.createTask() method
- Types: TaskItem interface

Design Constraints:
- Form must validate required fields before submission
- Task ownership immutable after creation
- No PII logged
```

**Output:**
1. Component file: `src/app/task-form/task-form.component.ts`
2. Template file: `src/app/task-form/task-form.component.html`
3. Styles file: `src/app/task-form/task-form.component.scss`
4. Service method: Added to `src/app/services/task.service.ts`
5. Spec file: `src/app/task-form/task-form.component.spec.ts`
6. Types: Updated in `src/app/models/task.model.ts`

**Generated Code Structure:**

```typescript
// task-form.component.ts
export class TaskFormComponent {
  @Output() taskCreated = new EventEmitter<TaskItem>();
  
  form: FormGroup; // Reactive form with validators
  
  onSubmit(): void {
    if (!this.form.valid) return; // Validation check
    
    const task = this.taskService.createTask(
      this.form.value,
      this.currentUser.id
    );
    
    this.taskCreated.emit(task);
    this.form.reset();
  }
}

// task.service.ts
export class TaskService {
  createTask(data: Partial<TaskItem>, ownerId: string): TaskItem {
    // Validation
    if (!data.title?.trim() || !data.dueDate) {
      throw new Error('Required fields missing');
    }
    
    // Create with ownership
    const task: TaskItem = {
      id: this.nextId++,
      title: data.title.trim(),
      description: data.description?.trim() || '',
      priority: data.priority || 'Medium',
      dueDate: data.dueDate,
      status: data.status || 'To Do',
      ownerId, // Enforce ownership
      assigneeId: data.assigneeId || ownerId
    };
    
    return task;
  }
}

// task-form.component.spec.ts
describe('TaskFormComponent', () => {
  it('should create task with required fields', () => {
    // Test setup
    const task = service.createTask({
      title: 'Test Task',
      dueDate: '2099-12-31'
    }, 'u1');
    
    expect(task.ownerId).toBe('u1'); // Ownership enforced
    expect(task.title).toBe('Test Task');
  });
  
  it('should reject task without title', () => {
    expect(() => service.createTask({}, 'u1')).toThrow();
  });
});
```

## Refinement Loop

If generated code fails quality checks:

1. Review error message and failing test
2. Identify root cause (e.g., missing authorization, incorrect type)
3. Provide corrected context and retry
4. Document the fix for future similar features

## Integration with Other Agents

- **TaskFlow-RoleBasedAccessControl:** Call this agent after generating initial service method if authorization is complex
- **TaskFlow-TestGeneration:** Use this agent to enhance test coverage after feature code is generated
- **CI Validation Prompt:** Use after all code is generated to verify build and test pass

---

**Agent Version:** 1.0  
**Last Updated:** June 2026
