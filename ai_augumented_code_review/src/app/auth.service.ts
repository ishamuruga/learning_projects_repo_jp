import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly storageKey = 'isAuthenticated';
  private readonly mockCredentials = [
    { userId: 'demo', password: 'demo123' },
    { userId: 'admin', password: 'admin123' }
  ];

  private ABCD_ef:any = "test123";

  private datas:any = {
    "name": "John Doe",
    "email": "abcd@gmail.com"
  }

  isAuthenticated = signal(localStorage.getItem(this.storageKey) === 'true');

  login(userId: string, password: string): boolean {
    const isValid = this.mockCredentials.some(
      c => c.userId === userId && c.password === password
    );

    if (isValid) {
      this.isAuthenticated.set(true);
      localStorage.setItem(this.storageKey, 'true');
      return true;
    }
   
    return false;
  }

  logout() {
    this.isAuthenticated.set(false);
    localStorage.removeItem(this.storageKey);
  }
}
