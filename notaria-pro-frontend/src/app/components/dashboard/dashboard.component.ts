import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TurnoService } from '../../services/turno.service';
import { TurnoResponse } from '../../models/turno.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {
  turnoActual: TurnoResponse | null = null;
  esperando: TurnoResponse[] = [];
  cargandoSiguiente = false;
  cargandoFinalizar = false;
  mensaje = '';
  mensajeExito = false;
  private intervalId: any;

  constructor(private turnoService: TurnoService) {}

  ngOnInit(): void {
    this.cargarDatos();
    this.intervalId = setInterval(() => this.cargarDatos(), 5000);
  }

  ngOnDestroy(): void { clearInterval(this.intervalId); }

  cargarDatos(): void {
    this.turnoService.enAtencion().subscribe({
      next: (t) => { this.turnoActual = (t as unknown) as TurnoResponse | null; },
      error: () => { this.turnoActual = null; }
    });
    this.turnoService.esperando().subscribe({
      next: (lista) => { this.esperando = lista; },
      error: () => { this.esperando = []; }
    });
  }

  llamarSiguiente(): void {
    this.cargandoSiguiente = true;
    this.turnoService.llamarSiguiente().subscribe({
      next: (t) => {
        this.turnoActual = t;
        this.cargandoSiguiente = false;
        this.mostrarMensaje('Ticket ' + t.numeroTurno + ' called', true);
        this.cargarDatos();
      },
      error: (err) => {
        this.cargandoSiguiente = false;
        this.mostrarMensaje(err.status === 400 || err.status === 404 ? 'No tickets in queue' : 'Error calling next ticket', false);
      }
    });
  }

  finalizar(): void {
    if (!this.turnoActual) return;
    this.cargandoFinalizar = true;
    this.turnoService.finalizar(this.turnoActual.id).subscribe({
      next: () => {
        this.mostrarMensaje('Ticket completed successfully', true);
        this.turnoActual = null;
        this.cargandoFinalizar = false;
        this.cargarDatos();
      },
      error: () => {
        this.cargandoFinalizar = false;
        this.mostrarMensaje('Error completing ticket', false);
      }
    });
  }

  mostrarMensaje(texto: string, exito: boolean): void {
    this.mensaje = texto;
    this.mensajeExito = exito;
    setTimeout(() => { this.mensaje = ''; }, 3000);
  }

  tiempoEspera(fechaCreacion: string): string {
    const diff = Date.now() - new Date(fechaCreacion).getTime();
    const min = Math.floor(diff / 60000);
    if (min < 1) return 'Just arrived';
    if (min === 1) return '1 min';
    return `${min} min`;
  }

  colorPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: '#ba1a1a', ADULTO_MAYOR: '#0051d5', REGULAR: '#75777e' };
    return m[tipo] ?? '#75777e';
  }

  bgPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: '#ffdad6', ADULTO_MAYOR: '#dbe1ff', REGULAR: '#dce2f3' };
    return m[tipo] ?? '#dce2f3';
  }

  iconoPersona(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: 'accessible', ADULTO_MAYOR: 'elderly', REGULAR: 'person' };
    return m[tipo] ?? 'person';
  }

  etiquetaPrioridad(tipo: string): string {
    const m: Record<string, string> = { PRIORITARIO: 'Priority', ADULTO_MAYOR: 'Senior Citizen', REGULAR: 'Regular' };
    return m[tipo] ?? tipo;
  }

  etiquetaRegistro(tipo: string): string {
    const m: Record<string, string> = { RAPIDO: 'Authentication', INTERMEDIO: 'Civil Registry', COMPLEJO: 'Notarial Deed' };
    return m[tipo] ?? tipo;
  }

  iconoRegistro(tipo: string): string {
    const m: Record<string, string> = { RAPIDO: 'bolt', INTERMEDIO: 'assignment_ind', COMPLEJO: 'history_edu' };
    return m[tipo] ?? 'description';
  }
}
