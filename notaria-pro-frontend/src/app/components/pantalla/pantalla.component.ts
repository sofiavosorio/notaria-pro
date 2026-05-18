import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TurnoService } from '../../services/turno.service';
import { TurnoResponse } from '../../models/turno.model';

@Component({
  selector: 'app-pantalla',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pantalla.component.html'
})
export class PantallaComponent implements OnInit, OnDestroy {
  turnoActual: TurnoResponse | null = null;
  proximos: TurnoResponse[] = [];
  recientes: TurnoResponse[] = [];
  hora = '';
  fecha = '';
  private intervalId: any;

  constructor(private turnoService: TurnoService) {}

  ngOnInit(): void {
    this.actualizarReloj();
    this.cargarDatos();
    this.intervalId = setInterval(() => {
      this.actualizarReloj();
      this.cargarDatos();
    }, 5000);
  }

  ngOnDestroy(): void { clearInterval(this.intervalId); }

  cargarDatos(): void {
    this.turnoService.enAtencion().subscribe({
      next: (t) => { this.turnoActual = (t as unknown) as TurnoResponse | null; },
      error: () => { this.turnoActual = null; }
    });
    this.turnoService.esperando().subscribe({
      next: (lista) => { this.proximos = lista.slice(0, 5); },
      error: () => { this.proximos = []; }
    });
    this.turnoService.recientes().subscribe({
      next: (lista) => { this.recientes = lista; },
      error: () => { this.recientes = []; }
    });
  }

  actualizarReloj(): void {
    const now = new Date();
    this.hora = now.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    this.fecha = now.toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  iconoPersona(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: 'accessible', ADULTO_MAYOR: 'elderly', REGULAR: 'person' };
    return m[tipo] ?? 'person';
  }

  colorPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: '#ba1a1a', ADULTO_MAYOR: '#0051d5', REGULAR: '#75777e' };
    return m[tipo] ?? '#75777e';
  }

  bgPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: '#ffdad6', ADULTO_MAYOR: '#dbe1ff', REGULAR: '#dce2f3' };
    return m[tipo] ?? '#dce2f3';
  }

  etiquetaPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: 'Prioridad Alta', ADULTO_MAYOR: 'Prioridad Media', REGULAR: 'General' };
    return m[tipo] ?? 'General';
  }

  etiquetaRegistro(tipo: string): string {
    const m: Record<string, string> = { RAPIDO: 'Autenticación', INTERMEDIO: 'Registro Civil', COMPLEJO: 'Escrituración' };
    return m[tipo] ?? tipo;
  }
}
