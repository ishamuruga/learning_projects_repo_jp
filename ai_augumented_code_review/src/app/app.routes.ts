import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { LoginComponent } from './login.component';
import { authGuard, loginGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];
  