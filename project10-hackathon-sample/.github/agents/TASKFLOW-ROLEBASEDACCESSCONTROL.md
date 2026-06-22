# Agent: TaskFlow-RoleBasedAccessControl

**Purpose:** Implement and validate role-aware visibility and authorization in TaskFlow components and services.

## Activation Trigger

Invoke this agent when:
- Implementing AC-02, AC-04 (lead-only features)
- Enforcing role-based access control (member vs. lead)
- Adding authorization checks to existing methods
- Validating that sensitive operations enforce ownership rules
- Testing for unauthorized access scenarios

## Agent Instructions

### Role Model

**Member Role:**
- Can view only own tasks and tasks assigned to them
- Cannot view other team members' tasks
- Can create, update own tasks
- Cannot assign tasks to others
- Cannot view team reports

**Lead Role:**
- Can view all team tasks
- Can create, update, assign any team task
- Can generate team productivity reports
- Can see overdue tasks across team
- Enforce oversight with audit trail

### Authorization Patterns

#### Pattern 1: Visibility Filter

```typescript
canViewTask(task: TaskItem, user: TeamUser): boolean {
  if (user.role === 'lead') {
    return true; // Leads see all
  }
  // Members see only own or assigned
  return task.ownerId === user.id || task.assigneeId === user.id;
}

get visibleTasks(): TaskItem[] {
  return this.allTasks.filter(t => this.canViewTask(t, this.currentUser));
}
```

#### Pattern 2: Edit Authorization

```typescript
canEditTask(task: TaskItem, user: TeamUser): boolean {
  if (user.role === 'lead') {
    return true; // Leads can edit anything
  }
  // Members can edit own tasks or tasks assigned to them
  return task.ownerId === user.id || task.assigneeId === user.id;
}

updateTaskStatus(task: TaskItem, status: TaskStatus): void {
  if (!this.canEditTask(task, this.currentUser)) {
    throw new Error('Unauthorized: Cannot update this task');
  }
  // Update logic
}
```

#### Pattern 3: Assignment Authorization

```typescript
canAssignTask(task: TaskItem, user: TeamUser): boolean {
  if (user.role === 'lead') {
    return true; // Leads can assign any task
  }
  // Members can only assign their own tasks
  return task.ownerId === user.id;
}

assignTaskToUser(task: TaskItem, assigneeId: string): void {
  if (!this.canAssignTask(task, this.currentUser)) {
    throw new Error('Unauthorized: Cannot assign this task');
  }
  // Assignment logic
}
```

#### Pattern 4: Report Access

```typescript
getTeamReport(user: TeamUser): TeammemberReportItem[] {
  if (user.role !== 'lead') {
    throw new Error('Unauthorized: Only leads can view team reports');
  }
  // Generate report
  return this.computeReports();
}
```

### Implementation Steps

1. **Identify Authorization Boundaries**
   - What data should each role see?
   - What actions can each role perform?
   - What ownership rules apply (own vs. assigned)?

2. **Create Type Guards**
   - Define functions for common authorization checks
   - Use type guards to narrow role types
   - Reuse across components and services

3. **Enforce in Data Access**
   - Filter lists by visibility rules
   - Throw errors on unauthorized mutations
   - Log authorization failures (without PII)

4. **Enforce in Components**
   - Hide/disable UI elements based on authorization
   - Show validation errors on unauthorized attempts
   - Provide helpful feedback to users

5. **Test Authorization**
   - Test authorized access succeeds
   - Test unauthorized access fails
   - Test each role boundary condition
   - Test ownership enforcement

### Authorization Test Template

```typescript
describe('TaskFlow Authorization', () => {
  let component: AppComponent;
  let service: TaskService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AppComponent],
      providers: [TaskService]
    });
    component = TestBed.createComponent(AppComponent).componentInstance;
    service = TestBed.inject(TaskService);
  });

  describe('Member Visibility (AC-03)', () => {
    it('should show only own and assigned tasks for member', () => {
      component.currentUserId = 'u2'; // Member
      component.currentUserRole = 'member';

      const tasks = [
        { id: 1, ownerId: 'u1', assigneeId: 'u1', ... }, // Not visible
        { id: 2, ownerId: 'u2', assigneeId: 'u2', ... }, // Own task: visible
        { id: 3, ownerId: 'u1', assigneeId: 'u2', ... }  // Assigned: visible
      ];

      const visible = component.visibleTasks;
      expect(visible.length).toBe(2);
      expect(visible.map(t => t.id)).toContain(2);
      expect(visible.map(t => t.id)).toContain(3);
    });

    it('should hide restricted tasks from member', () => {
      component.currentUserId = 'u2'; // Member
      const restrictedTask = { id: 1, ownerId: 'u1', assigneeId: 'u1', ... };

      expect(component.canViewTask(restrictedTask)).toBe(false);
    });
  });

  describe('Lead Visibility (AC-04)', () => {
    it('should show all tasks for lead', () => {
      component.currentUserId = 'u1'; // Lead
      component.currentUserRole = 'lead';

      const tasks = [
        { id: 1, ownerId: 'u1', assigneeId: 'u1', ... },
        { id: 2, ownerId: 'u2', assigneeId: 'u2', ... },
        { id: 3, ownerId: 'u3', assigneeId: 'u1', ... }
      ];

      component.tasks = tasks;
      expect(component.visibleTasks.length).toBe(3);
    });
  });

  describe('Edit Authorization (AC-02)', () => {
    it('should allow member to update own task', () => {
      const task = { id: 1, ownerId: 'u2', assigneeId: 'u2', ... };
      component.currentUserId = 'u2';

      expect(() => component.updateStatus(task, 'In Progress')).not.toThrow();
    });

    it('should deny member updating others task', () => {
      const task = { id: 1, ownerId: 'u1', assigneeId: 'u1', ... };
      component.currentUserId = 'u2';

      expect(() => component.updateStatus(task, 'In Progress')).toThrow();
    });

    it('should allow lead to update any task', () => {
      const task = { id: 1, ownerId: 'u2', assigneeId: 'u3', ... };
      component.currentUserId = 'u1'; // Lead
      component.currentUserRole = 'lead';

      expect(() => component.updateStatus(task, 'Done')).not.toThrow();
    });
  });

  describe('Assignment Authorization', () => {
    it('should allow lead to assign any task', () => {
      const task = { id: 1, ownerId: 'u2', ... };
      component.currentUserId = 'u1'; // Lead
      component.currentUserRole = 'lead';

      expect(() => component.assignTask(task, 'u3')).not.toThrow();
    });

    it('should allow member to assign own task', () => {
      const task = { id: 1, ownerId: 'u2', ... };
      component.currentUserId = 'u2'; // Member, task owner

      expect(() => component.assignTask(task, 'u3')).not.toThrow();
    });

    it('should deny member assigning others task', () => {
      const task = { id: 1, ownerId: 'u1', ... };
      component.currentUserId = 'u2'; // Member, not owner

      expect(() => component.assignTask(task, 'u3')).toThrow();
    });
  });

  describe('Report Access (AC-04)', () => {
    it('should allow lead to view team report', () => {
      component.currentUserRole = 'lead';

      expect(component.teamReport.length).toBeGreaterThan(0);
    });

    it('should hide team report from member', () => {
      component.currentUserRole = 'member';

      expect(component.teamReport.length).toBe(0); // Or throw error
    });
  });
});
```

### Security Best Practices

1. **Never Trust Client-Side Checks Alone**
   - Always enforce on server/backend
   - UI filtering is for UX, not security

2. **Log Authorization Failures**
   - Track failed access attempts
   - Do NOT log sensitive task data
   - Use structured logging format

3. **Fail Secure**
   - Deny by default, grant by exception
   - Default to most restrictive role
   - Explicit is better than implicit

4. **Audit Changes**
   - Log who changed what, when
   - Track task assignment changes
   - Maintain audit trail for team leads

5. **Validate Inputs**
   - Check user ID is not tampered with
   - Validate role claim against server
   - Verify session/token before trusting role

### Code Generation Checklist

Generated authorization code must:

- [ ] Define clear role model (member vs. lead)
- [ ] Implement type guards for each authorization check
- [ ] Enforce in all data-access methods
- [ ] Hide/disable unauthorized UI elements
- [ ] Throw errors on unauthorized mutations (with appropriate messages)
- [ ] Include comprehensive authorization tests
- [ ] Log failures without exposing PII
- [ ] Use consistent error semantics (Forbidden 403, Unauthorized 401)
- [ ] Handle edge cases (null user, missing role, etc.)
- [ ] Document assumptions and limitations

---

**Agent Version:** 1.0  
**Last Updated:** June 2026
