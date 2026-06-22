import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'TaskFlow' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('TaskFlow');
  });

  it('should create a task with required fields', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const previousCount = app.tasks.length;

    app.taskForm.title = 'Prepare release notes';
    app.taskForm.description = 'Collect updates and share with stakeholders';
    app.taskForm.dueDate = '2099-12-31';
    app.taskForm.priority = 'High';
    app.taskForm.status = 'To Do';
    app.taskForm.assigneeId = 'u2';
    app.createTask();

    expect(app.tasks.length).toBe(previousCount + 1);
    expect(app.tasks[0].title).toBe('Prepare release notes');
    expect(app.tasks[0].ownerId).toBe(app.currentUser.id);
  });

  it('should enforce role-aware visibility for member users', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    app.tasks = [
      {
        id: 99,
        title: 'Restricted task',
        description: '',
        priority: 'Low',
        dueDate: '2099-12-31',
        status: 'To Do',
        ownerId: 'u1',
        assigneeId: 'u1'
      }
    ];
    app.currentUserId = 'u2';

    expect(app.visibleTasks.length).toBe(0);
  });

  it('should mark reminders for tasks due within 24 hours', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const now = new Date();
    const dueSoon = new Date(now);
    dueSoon.setHours(now.getHours() + 12);

    const task = {
      id: 120,
      title: 'Due soon',
      description: '',
      priority: 'Medium' as const,
      dueDate: dueSoon.toISOString().split('T')[0],
      status: 'To Do' as const,
      ownerId: app.currentUser.id,
      assigneeId: app.currentUser.id
    };

    expect(app.isDueWithin24Hours(task, now)).toBeTrue();
  });
});
