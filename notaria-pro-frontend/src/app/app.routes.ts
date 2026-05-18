import { Routes } from '@angular/router';
import { RegistroComponent } from './components/registro/registro.component';
import { PantallaComponent } from './components/pantalla/pantalla.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: 'registro', pathMatch: 'full' },
  { path: 'registro', component: RegistroComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'pantalla', component: PantallaComponent },
  { path: '**', redirectTo: 'registro' }
];
