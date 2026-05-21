import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TurnoService } from '../../services/turno.service';
import { TurnoRequest } from '../../models/turno.model';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registro.component.html'
})
export class RegistroComponent {
  paso = 1;

  form: TurnoRequest = {
    cedula: '', nombre: '', email: '', telefono: '',
    tipoPersona: 'REGULAR',
    tipoRegistro: 'RAPIDO'
  };

  turnoRegistrado: any = null;
  cargando = false;
  errorRegistro = '';

  condiciones = [
    { val: 'PRIORITARIO' as TurnoRequest['tipoPersona'], label: 'Pregnancy / Disability', sub: 'Adds 3 points', icon: 'pregnant_woman', badge: 'High Priority', badgeBg: '#ba1a1a', badgeColor: '#fff',    color: '#ba1a1a', bg: '#fff0f0' },
    { val: 'ADULTO_MAYOR' as TurnoRequest['tipoPersona'], label: 'Senior Citizen',         sub: 'Adds 2 points', icon: 'elderly',        badge: '+62 years',    badgeBg: '#316bf3', badgeColor: '#fefcff', color: '#0051d5', bg: '#e7eefe' },
    { val: 'REGULAR'      as TurnoRequest['tipoPersona'], label: 'General Public',          sub: 'Base 1 point',  icon: 'person',         badge: 'Regular',      badgeBg: '#dce2f3', badgeColor: '#45464d', color: '#75777e', bg: '#fff'    },
  ];

  tramites = [
    { val: 'RAPIDO'     as TurnoRequest['tipoRegistro'], label: 'Authentications / Signature Recognition', badge: 'Quick',        icon: 'bolt',          iconBg: '#ffdeab', iconColor: '#271900', badgeBg: '#e3c28d', badgeColor: '#271900', color: '#372400', bg: '#fffbf0', tiempo: 'Est. 10 min', pts: 3 },
    { val: 'INTERMEDIO' as TurnoRequest['tipoRegistro'], label: 'Civil Registry / Exit Permits',           badge: 'Intermediate', icon: 'assignment_ind', iconBg: '#0051d5', iconColor: '#fff',    badgeBg: '#dbe1ff', badgeColor: '#00174b', color: '#0051d5', bg: '#eef1ff', tiempo: 'Est. 25 min', pts: 2 },
    { val: 'COMPLEJO'   as TurnoRequest['tipoRegistro'], label: 'Deeds / Estates / Mortgages',             badge: 'Complex',      icon: 'history_edu',   iconBg: '#04122e', iconColor: '#fff',    badgeBg: '#d9e2ff', badgeColor: '#0d1b37', color: '#04122e', bg: '#f0f3ff', tiempo: 'Est. 60 min', pts: 1 },
  ];

  constructor(private turnoService: TurnoService) {}

  siguientePaso(): void {
    if (this.paso === 2) {
      this.registrar();
    } else {
      this.paso++;
    }
  }

  anteriorPaso(): void { if (this.paso > 1) this.paso--; }

  registrar(): void {
    this.cargando = true;
    this.errorRegistro = '';
    this.turnoService.registrar(this.form).subscribe({
      next: (t) => { this.turnoRegistrado = t; this.cargando = false; this.paso = 3; },
      error: (err) => {
        this.cargando = false;
        if (err.status === 0) {
          this.errorRegistro = 'No connection. Verify the backend is running on port 8080.';
        } else if (typeof err.error === 'string' && err.error.trim()) {
          this.errorRegistro = err.error;
        } else {
          this.errorRegistro = `Error ${err.status as number}: could not register the ticket.`;
        }
        console.error('Error registering ticket:', err);
      }
    });
  }

  reiniciar(): void {
    this.paso = 1;
    this.turnoRegistrado = null;
    this.errorRegistro = '';
    this.form = { cedula: '', nombre: '', email: '', telefono: '', tipoPersona: 'REGULAR', tipoRegistro: 'RAPIDO' };
  }

  pesoEstimado(): number {
    const p: Record<string, number> = { PRIORITARIO: 3, ADULTO_MAYOR: 2, REGULAR: 1 };
    const r: Record<string, number> = { RAPIDO: 3, INTERMEDIO: 2, COMPLEJO: 1 };
    return (p[this.form.tipoPersona] ?? 1) + (r[this.form.tipoRegistro] ?? 1);
  }

  etiquetaPersona(t: string): string {
    const m: Record<string, string> = { PRIORITARIO: 'Pregnancy / Disability', ADULTO_MAYOR: 'Senior Citizen', REGULAR: 'Regular' };
    return m[t] ?? t;
  }

  etiquetaRegistro(t: string): string {
    const m: Record<string, string> = { RAPIDO: 'Authentications', INTERMEDIO: 'Civil Registry', COMPLEJO: 'Public Deed' };
    return m[t] ?? t;
  }

  hoy(): string {
    return new Date().toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
