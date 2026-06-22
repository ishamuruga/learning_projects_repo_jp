import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Role = 'member' | 'lead';
type Priority = 'Low' | 'Medium' | 'High';
type TaskStatus = 'To Do' | 'In Progress' | 'Done';

interface TeamUser {
  id: string;
  name: string;
  role: Role;
}

interface TaskItem {
  id: number;
  title: string;
  description: string;
  priority: Priority;
  dueDate: string;
  status: TaskStatus;
  ownerId: string;
  assigneeId: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'TaskFlow';

  users: TeamUser[] = [
    { id: 'u1', name: 'Alex Morgan', role: 'lead' },
    { id: 'u2', name: 'Priya Nair', role: 'member' },
    { id: 'u3', name: 'Daniel Kim', role: 'member' }
  ];

  currentUserId = this.users[0].id;
  nextTaskId = 4;

  taskForm: {
    title: string;
    description: string;
    priority: Priority;
    dueDate: string;
    status: TaskStatus;
    assigneeId: string;
  } = {
    title: '',
    description: '',
    priority: 'Medium',
    dueDate: this.buildDateInputValue(2),
    status: 'To Do',
    assigneeId: this.users[1].id
  };

  filters: {
    ownerId: string;
    status: string;
    priority: string;
    dueBucket: string;
  } = {
    ownerId: 'all',
    status: 'all',
    priority: 'all',
    dueBucket: 'all'
  };

  tasks: TaskItem[] = [
    {
      id: 1,
      title: 'Finalize sprint goals',
      description: 'Align team priorities and publish sprint goals.',
      priority: 'High',
      dueDate: this.buildDateInputValue(1),
      status: 'In Progress',
      ownerId: 'u1',
      assigneeId: 'u2'
    },
    {
      id: 2,
      title: 'Write onboarding notes',
      description: 'Document recurring onboarding FAQ and setup checklist.',
      priority: 'Medium',
      dueDate: this.buildDateInputValue(3),
      status: 'To Do',
      ownerId: 'u2',
      assigneeId: 'u2'
    },
    {
      id: 3,
      title: 'Fix dashboard layout shift',
      description: 'Improve responsive card spacing for mobile screens.',
      priority: 'Low',
      dueDate: this.buildDateInputValue(-1),
      status: 'To Do',
      ownerId: 'u1',
      assigneeId: 'u3'
    }
  ];

  get currentUser(): TeamUser {
    return this.users.find((user) => user.id === this.currentUserId) ?? this.users[0];
  }

  get isLead(): boolean {
    return this.currentUser.role === 'lead';
  }

  get visibleTasks(): TaskItem[] {
    return this.tasks
      .filter((task) => this.canViewTask(task))
      .filter((task) => {
        if (this.filters.ownerId !== 'all' && task.ownerId !== this.filters.ownerId) {
          return false;
        }
        if (this.filters.status !== 'all' && task.status !== this.filters.status) {
          return false;
        }
        if (this.filters.priority !== 'all' && task.priority !== this.filters.priority) {
          return false;
        }
        if (this.filters.dueBucket === 'overdue' && !this.isOverdue(task)) {
          return false;
        }
        if (this.filters.dueBucket === 'today' && !this.isDueToday(task)) {
          return false;
        }
        if (this.filters.dueBucket === 'week' && !this.isDueThisWeek(task)) {
          return false;
        }
        return true;
      })
      .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
  }

  get reminderTasks(): TaskItem[] {
    return this.tasks.filter((task) => this.canViewTask(task) && this.isDueWithin24Hours(task) && task.status !== 'Done');
  }

  get completedCount(): number {
    return this.visibleTasks.filter((task) => task.status === 'Done').length;
  }

  get overdueCount(): number {
    return this.visibleTasks.filter((task) => this.isOverdue(task) && task.status !== 'Done').length;
  }

  get inProgressCount(): number {
    return this.visibleTasks.filter((task) => task.status === 'In Progress').length;
  }

  get teamReport(): Array<{ assignee: string; completed: number; overdue: number; open: number }> {
    if (!this.isLead) {
      return [];
    }

    return this.users.map((user) => {
      const assigned = this.tasks.filter((task) => task.assigneeId === user.id);
      return {
        assignee: user.name,
        completed: assigned.filter((task) => task.status === 'Done').length,
        overdue: assigned.filter((task) => task.status !== 'Done' && this.isOverdue(task)).length,
        open: assigned.filter((task) => task.status !== 'Done').length
      };
    });
  }

  createTask(): void {
    const title = this.taskForm.title.trim();
    const description = this.taskForm.description.trim();

    if (!title || !this.taskForm.dueDate) {
      return;
    }

    const newTask: TaskItem = {
      id: this.nextTaskId,
      title,
      description,
      priority: this.taskForm.priority,
      dueDate: this.taskForm.dueDate,
      status: this.taskForm.status,
      ownerId: this.currentUser.id,
      assigneeId: this.taskForm.assigneeId
    };

    this.tasks = [newTask, ...this.tasks];
    this.nextTaskId += 1;
    this.taskForm.title = '';
    this.taskForm.description = '';
    this.taskForm.priority = 'Medium';
    this.taskForm.status = 'To Do';
    this.taskForm.dueDate = this.buildDateInputValue(2);
    this.taskForm.assigneeId = this.currentUser.id;
  }

  updateStatus(task: TaskItem, status: TaskStatus): void {
    if (!this.canEditTask(task)) {
      return;
    }

    this.tasks = this.tasks.map((item) => (item.id === task.id ? { ...item, status } : item));
  }

  assignTask(task: TaskItem, assigneeId: string): void {
    if (!this.isLead && task.ownerId !== this.currentUser.id) {
      return;
    }

    this.tasks = this.tasks.map((item) => (item.id === task.id ? { ...item, assigneeId } : item));
  }

  ownerName(ownerId: string): string {
    return this.users.find((user) => user.id === ownerId)?.name ?? 'Unknown';
  }

  canViewTask(task: TaskItem): boolean {
    if (this.isLead) {
      return true;
    }
    return task.ownerId === this.currentUser.id || task.assigneeId === this.currentUser.id;
  }

  canEditTask(task: TaskItem): boolean {
    if (this.isLead) {
      return true;
    }
    return task.ownerId === this.currentUser.id || task.assigneeId === this.currentUser.id;
  }

  isOverdue(task: TaskItem): boolean {
    const now = new Date();
    const due = this.endOfDay(task.dueDate);
    return due.getTime() < now.getTime();
  }

  isDueToday(task: TaskItem): boolean {
    const today = new Date();
    const due = new Date(task.dueDate);
    return (
      due.getFullYear() === today.getFullYear() &&
      due.getMonth() === today.getMonth() &&
      due.getDate() === today.getDate()
    );
  }

  isDueThisWeek(task: TaskItem): boolean {
    const due = this.endOfDay(task.dueDate);
    const now = new Date();
    const sevenDays = new Date(now);
    sevenDays.setDate(now.getDate() + 7);
    return due.getTime() >= now.getTime() && due.getTime() <= sevenDays.getTime();
  }

  isDueWithin24Hours(task: TaskItem, referenceDate: Date = new Date()): boolean {
    const due = this.endOfDay(task.dueDate);
    const diffMs = due.getTime() - referenceDate.getTime();
    return diffMs > 0 && diffMs <= 24 * 60 * 60 * 1000;
  }

  private endOfDay(dateInput: string): Date {
    const date = new Date(dateInput);
    date.setHours(23, 59, 59, 999);
    return date;
  }

  private buildDateInputValue(offsetDays: number): string {
    const date = new Date();
    date.setDate(date.getDate() + offsetDays);
    return date.toISOString().split('T')[0];
  }
}
