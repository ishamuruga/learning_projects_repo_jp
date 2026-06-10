import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  userId = '';
  password = '';
  error = '';

  message:any="Test Message123";   

  login() {
    this.error = '';

    const isLoggedIn = this.authService.login(this.userId.trim(), this.password.trim());

    if (isLoggedIn) {
      this.router.navigate(['/home']);
      return;
    }

    this.error = 'Invalid user id or password';
  }
}
