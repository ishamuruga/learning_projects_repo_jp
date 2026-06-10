import { Component, inject, signal } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TodoService } from './todo.service';
import { FilterType } from './todo.model';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  todoService = inject(TodoService);
  authService = inject(AuthService);
  router = inject(Router);

  newTodoText = '';
  newTodoPriority = signal<'low' | 'medium' | 'high'>('medium');
  newTodoPlannedEndDate = '';
  editingId = signal<number | null>(null);
  editText = signal('');

  addTodo() {
    if (this.newTodoText.trim()) {
      this.todoService.add(
        this.newTodoText,
        this.newTodoPriority(),
        this.newTodoPlannedEndDate || undefined
      );
      this.newTodoText = '';
      this.newTodoPriority.set('medium');
      this.newTodoPlannedEndDate = '';
    }
  }

  formatPlannedEndDate(date: string | undefined) {
    if (!date) {
      return '';
    }

    return new Date(date).toLocaleDateString();
  }

  startEdit(id: number, text: string) {
    this.editingId.set(id);
    this.editText.set(text);
  }

  saveEdit(id: number) {
    const text = this.editText().trim();
    if (text) {
      this.todoService.update(id, text);
    } else {
      this.todoService.remove(id);
    }
    this.editingId.set(null);
  }

  cancelEdit() {
    this.editingId.set(null);
  }

  setFilter(f: string) {
    this.todoService.filter.set(f as FilterType);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
