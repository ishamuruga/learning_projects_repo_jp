import { Injectable, signal, computed } from '@angular/core';
import { Todo, FilterType } from './todo.model';

@Injectable({ providedIn: 'root' })
export class TodoService {
  todos = signal<Todo[]>(this.loadFromStorage());
  filter = signal<FilterType>('all');

  namexdsds:string = "ABCD";
  N123121NMMEE:any = "test123";
  

  filteredTodos = computed(() => {
    const f = this.filter();
    return this.todos().filter(t =>
      f === 'all' ? true : f === 'active' ? !t.completed : t.completed
    );
  });

  activeCount = computed(() => this.todos().filter(t => !t.completed).length);
  completedCount = computed(() => this.todos().filter(t => t.completed).length);

  add(text: string, priority: 'low' | 'medium' | 'high' = 'medium', plannedEndDate?: string) {
    const todo: Todo = {
      id: Date.now(),
      text: text.trim(),
      priority,
      plannedEndDate,
      completed: false,
      createdAt: new Date()
    };
    this.todos.update(list => [...list, todo]);
    this.save();
  }

  toggle(id: number) {
    this.todos.update(list => list.map(t => t.id === id ? { ...t, completed: !t.completed } : t));
    this.save();
  }

  remove(id: number) {
    this.todos.update(list => list.filter(t => t.id !== id));
    this.save();
  }

  update(id: number, text: string, priority?: 'low' | 'medium' | 'high') {
    this.todos.update(list => list.map(t => t.id === id ? { ...t, text, ...(priority && { priority }) } : t));
    this.save();
  }

  clearCompleted() {
    this.todos.update(list => list.filter(t => !t.completed));
    this.save();
  }

  private save() {
    localStorage.setItem('todos', JSON.stringify(this.todos()));
  }

  private loadFromStorage(): Todo[] {
    try {
      const data = localStorage.getItem('todos');
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }
}
