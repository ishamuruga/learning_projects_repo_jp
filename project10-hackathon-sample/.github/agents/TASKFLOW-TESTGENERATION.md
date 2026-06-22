# Agent: TaskFlow-TestGeneration

**Purpose:** Create comprehensive unit, functional, and integration tests aligned to TaskFlow acceptance criteria and architectural design.

## Activation Trigger

Invoke this agent when:
- Generating tests for newly created components or services
- Implementing full coverage for AC-01 through AC-06
- Refactoring code to ensure test evidence is maintained
- Validating test mappings to acceptance criteria
- Running CI validation after code generation

## Agent Instructions

### Test Mapping Framework

Each acceptance criterion maps to specific test scenarios:

| AC | Feature | Test Category | Test Cases |
|----|---------|---------------|-----------|
| AC-01 | Create Task | Unit | Form validation, service method, ownership enforcement |
| AC-01 | Create Task | Functional | Form submission, state update, dashboard refresh |
| AC-02 | Update Status | Unit | State transition logic, authorization checks |
| AC-02 | Assign Task | Unit | Assignment service, role checks |
| AC-02 | Assign Task | Functional | UI assignment dropdown, update confirmation |
| AC-03 | Filter Dashboard | Unit | Filter logic, visibility rules, sorting |
| AC-03 | Filter Dashboard | Functional | Filter UI interaction, result updates |
| AC-04 | Team Report | Unit | Aggregation logic, calculation accuracy |
| AC-04 | Team Report | Functional | Report rendering, data accuracy |
| AC-05 | Reminders | Unit | Date calculation, 24-hour detection |
| AC-05 | Reminders | Functional | Reminder panel display, empty state |
| AC-06 | CI Pipeline | Integration | Build success, test execution, coverage reporting |

### Test Structure Template

```typescript
describe('Feature: [Feature Name] (AC-XX)', () => {
  let component: ComponentUnderTest;
  let service: ServiceUnderTest;
  let fixture: ComponentFixture<ComponentUnderTest>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ComponentUnderTest],
      providers: [ServiceUnderTest]
    }).compileComponents();

    fixture = TestBed.createComponent(ComponentUnderTest);
    component = fixture.componentInstance;
    service = TestBed.inject(ServiceUnderTest);
    fixture.detectChanges();
  });

  // Acceptance Criteria Tests
  describe('Acceptance Criterion: [AC-XX criterion text]', () => {
    describe('Happy Path', () => {
      it('should [expected behavior]', () => {
        // Setup
        // Act
        // Assert
      });
    });

    describe('Edge Cases', () => {
      it('should [handle edge case]', () => {
        // Setup
        // Act
        // Assert
      });
    });

    describe('Error Handling', () => {
      it('should [error scenario]', () => {
        // Setup
        // Act
        // Assert
      });
    });
  });
});
```

### AC-01 Test Suite: Create Task

**Test Focus:** Form validation, service creation, ownership enforcement

```typescript
describe('AC-01: Create Task', () => {
  let component: AppComponent;
  let service: TaskService;

  // Unit Tests
  describe('TaskService.createTask()', () => {
    it('should create task with all fields', () => {
      const task = service.createTask({
        title: 'Prepare sprint',
        description: 'Align team on sprint goals',
        priority: 'High',
        dueDate: '2099-12-31',
        status: 'To Do'
      }, 'u1');

      expect(task.title).toBe('Prepare sprint');
      expect(task.ownerId).toBe('u1'); // Ownership enforced
      expect(task.id).toBeGreaterThan(0);
    });

    it('should enforce task ownership by ownerId parameter', () => {
      const task = service.createTask(
        { title: 'Task', dueDate: '2099-12-31' },
        'u2' // Owner ID
      );

      expect(task.ownerId).toBe('u2');
    });

    it('should reject missing title (required field)', () => {
      expect(() => service.createTask(
        { dueDate: '2099-12-31' },
        'u1'
      )).toThrow();
    });

    it('should reject missing dueDate (required field)', () => {
      expect(() => service.createTask(
        { title: 'Task' },
        'u1'
      )).toThrow();
    });

    it('should set defaults for optional fields', () => {
      const task = service.createTask({
        title: 'Task',
        dueDate: '2099-12-31'
      }, 'u1');

      expect(task.priority).toBe('Medium'); // Default
      expect(task.status).toBe('To Do'); // Default
    });

    it('should trim whitespace from title', () => {
      const task = service.createTask({
        title: '  Prepare sprint  ',
        dueDate: '2099-12-31'
      }, 'u1');

      expect(task.title).toBe('Prepare sprint');
    });
  });

  // Functional Tests
  describe('TaskFormComponent', () => {
    it('should create task form with validation', () => {
      expect(component.taskForm).toBeTruthy();
      expect(component.taskForm.title).toBeTruthy();
      expect(component.taskForm.dueDate).toBeTruthy();
    });

    it('should disable submit button when form invalid', () => {
      const button = fixture.debugElement.query(
        By.css('button.primary-btn')
      ).nativeElement;

      component.taskForm.title = '';
      fixture.detectChanges();

      expect(button.disabled).toBe(true);
    });

    it('should submit form and create task', () => {
      spyOn(service, 'createTask').and.returnValue({
        id: 1, title: 'Test', ownerId: 'u1', ...
      });

      component.taskForm.title = 'Test Task';
      component.taskForm.dueDate = '2099-12-31';
      component.createTask();

      expect(service.createTask).toHaveBeenCalled();
    });

    it('should reset form after successful creation', () => {
      component.taskForm.title = 'Test';
      component.createTask();

      expect(component.taskForm.title).toBe('');
    });
  });
});
```

### AC-02 Test Suite: Assign & Update Task

**Test Focus:** Authorization, status transitions, assignment logic

```typescript
describe('AC-02: Assign & Update Task', () => {
  it('should update task status for authorized user', () => {
    const task = { id: 1, ownerId: 'u2', status: 'To Do', ... };
    component.currentUserId = 'u2'; // Owner

    component.updateStatus(task, 'In Progress');

    expect(task.status).toBe('In Progress');
  });

  it('should deny status update for unauthorized user', () => {
    const task = { id: 1, ownerId: 'u1', status: 'To Do', ... };
    component.currentUserId = 'u2'; // Not owner, not assigned

    expect(() => component.updateStatus(task, 'In Progress'))
      .toThrow('Unauthorized');
  });

  it('should allow lead to update any task', () => {
    const task = { id: 1, ownerId: 'u2', status: 'To Do', ... };
    component.currentUserId = 'u1'; // Lead
    component.currentUserRole = 'lead';

    component.updateStatus(task, 'Done');

    expect(task.status).toBe('Done');
  });

  it('should assign task to another user', () => {
    const task = { id: 1, ownerId: 'u1', assigneeId: 'u1', ... };
    component.currentUserId = 'u1'; // Owner

    component.assignTask(task, 'u2');

    expect(task.assigneeId).toBe('u2');
  });

  it('should deny assignment from non-owner', () => {
    const task = { id: 1, ownerId: 'u1', assigneeId: 'u1', ... };
    component.currentUserId = 'u2'; // Not owner

    expect(() => component.assignTask(task, 'u3'))
      .toThrow('Unauthorized');
  });
});
```

### AC-03 Test Suite: Filtered Dashboard

**Test Focus:** Filter logic, visibility rules, sorting

```typescript
describe('AC-03: Filtered Dashboard', () => {
  beforeEach(() => {
    component.tasks = [
      { id: 1, ownerId: 'u1', status: 'To Do', priority: 'High', ... },
      { id: 2, ownerId: 'u2', status: 'In Progress', priority: 'Low', ... },
      { id: 3, ownerId: 'u1', status: 'Done', priority: 'Medium', ... }
    ];
  });

  it('should filter by owner', () => {
    component.filters.ownerId = 'u1';
    component.filters.status = 'all';

    expect(component.visibleTasks.map(t => t.id)).toEqual([1, 3]);
  });

  it('should filter by status', () => {
    component.filters.ownerId = 'all';
    component.filters.status = 'In Progress';

    expect(component.visibleTasks.length).toBe(1);
    expect(component.visibleTasks[0].id).toBe(2);
  });

  it('should filter by priority', () => {
    component.filters.ownerId = 'all';
    component.filters.priority = 'High';

    expect(component.visibleTasks[0].priority).toBe('High');
  });

  it('should combine multiple filters', () => {
    component.filters.ownerId = 'u1';
    component.filters.status = 'To Do';

    expect(component.visibleTasks.length).toBe(1);
    expect(component.visibleTasks[0].id).toBe(1);
  });

  it('should sort by due date', () => {
    const sorted = component.visibleTasks;

    for (let i = 1; i < sorted.length; i++) {
      const prev = new Date(sorted[i-1].dueDate);
      const curr = new Date(sorted[i].dueDate);
      expect(prev.getTime()).toBeLessThanOrEqual(curr.getTime());
    }
  });

  it('should enforce role-aware visibility', () => {
    component.currentUserId = 'u2'; // Member
    component.currentUserRole = 'member';

    // Task 1 owned by u1, not assigned to u2: not visible
    expect(component.visibleTasks.map(t => t.id))
      .not.toContain(1);
  });
});
```

### AC-04 Test Suite: Team Productivity Report

**Test Focus:** Report accuracy, lead-only access, aggregation

```typescript
describe('AC-04: Team Productivity Report (Lead Only)', () => {
  beforeEach(() => {
    component.tasks = [
      { id: 1, assigneeId: 'u2', status: 'Done', ... },
      { id: 2, assigneeId: 'u2', status: 'To Do', ... },
      { id: 3, assigneeId: 'u2', status: 'To Do', ... },
      { id: 4, assigneeId: 'u3', status: 'Done', ... }
    ];
  });

  it('should deny report access for member', () => {
    component.currentUserRole = 'member';

    expect(component.teamReport.length).toBe(0);
  });

  it('should grant report access for lead', () => {
    component.currentUserRole = 'lead';

    expect(component.teamReport.length).toBeGreaterThan(0);
  });

  it('should calculate open tasks per assignee', () => {
    component.currentUserRole = 'lead';
    const report = component.teamReport
      .find(r => r.assignee === 'Priya Nair');

    expect(report.open).toBe(2); // Tasks 2 & 3
  });

  it('should calculate completed tasks per assignee', () => {
    component.currentUserRole = 'lead';
    const report = component.teamReport
      .find(r => r.assignee === 'Priya Nair');

    expect(report.completed).toBe(1);
  });
});
```

### AC-05 Test Suite: Reminder Notifications

**Test Focus:** Due date calculation, 24-hour detection

```typescript
describe('AC-05: Reminder Notifications', () => {
  it('should detect task due within 24 hours', () => {
    const now = new Date();
    const dueSoon = new Date(now);
    dueSoon.setHours(now.getHours() + 12);

    const task = {
      id: 1,
      dueDate: dueSoon.toISOString().split('T')[0],
      status: 'To Do',
      ...
    };

    expect(component.isDueWithin24Hours(task, now)).toBe(true);
  });

  it('should not mark task due > 24 hours as reminder', () => {
    const now = new Date();
    const dueFar = new Date(now);
    dueFar.setDate(now.getDate() + 2);

    const task = {
      id: 1,
      dueDate: dueFar.toISOString().split('T')[0],
      ...
    };

    expect(component.isDueWithin24Hours(task, now)).toBe(false);
  });

  it('should not remind for tasks already done', () => {
    const now = new Date();
    const dueSoon = new Date(now);
    dueSoon.setHours(now.getHours() + 12);

    const task = {
      ...
      dueDate: dueSoon.toISOString().split('T')[0],
      status: 'Done'
    };

    // Reminder panel should filter out Done tasks
    expect(component.reminderTasks).not.toContain(
      jasmine.objectContaining({ id: task.id })
    );
  });

  it('should display reminder panel for visible tasks', () => {
    component.reminderTasks = [
      { id: 1, title: 'Urgent task', ... }
    ];

    const panel = fixture.debugElement.query(
      By.css('.reminder-panel')
    );
    const items = panel.queryAll(By.css('li'));

    expect(items.length).toBe(1);
    expect(items[0].nativeElement.textContent)
      .toContain('Urgent task');
  });
});
```

### Test Execution & Coverage

Generate and maintain coverage reports:

```bash
npm run test -- --code-coverage --watch=false
```

**Coverage Target:** > 70% for AC-critical code paths

**Coverage Report Locations:**
- `coverage/index.html` — Interactive coverage dashboard
- `coverage/coverage-summary.json` — Machine-readable coverage data

### Test Organization

```
src/
├── app/
│   ├── app.component.ts
│   ├── app.component.spec.ts
│   ├── services/
│   │   ├── task.service.ts
│   │   ├── task.service.spec.ts
│   │   ├── reminder.service.ts
│   │   └── reminder.service.spec.ts
│   └── ...
```

### Code Generation Checklist

Generated test code must:

- [ ] Map all tests to specific AC criterion
- [ ] Include happy path tests
- [ ] Include edge case and error handling tests
- [ ] Use clear, descriptive test names
- [ ] Include setup/teardown (beforeEach/afterEach)
- [ ] Mock external dependencies
- [ ] Assert both positive and negative outcomes
- [ ] Cover authorization boundary conditions
- [ ] Verify role-based access enforcement
- [ ] Test state mutations (immutability where applicable)
- [ ] Execute successfully (all tests pass)
- [ ] Contribute to coverage > 70%

---

**Agent Version:** 1.0  
**Last Updated:** June 2026
