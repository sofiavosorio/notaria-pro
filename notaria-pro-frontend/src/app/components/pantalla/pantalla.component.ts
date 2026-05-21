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
  private ultimoTurnoAnunciado: string | null = null;

  constructor(private turnoService: TurnoService) {}

  ngOnInit(): void {
    // Precargar voces (Chrome las carga de forma asíncrona)
    if ('speechSynthesis' in window) {
      window.speechSynthesis.getVoices();
      window.speechSynthesis.onvoiceschanged = () => window.speechSynthesis.getVoices();
    }
    this.actualizarReloj();
    this.cargarDatos();
    this.intervalId = setInterval(() => {
      this.actualizarReloj();
      this.cargarDatos();
    }, 5000);
  }

  ngOnDestroy(): void { clearInterval(this.intervalId); }

  // ── Síntesis de voz ────────────────────────────────────────────────────────
  private anunciarTurno(turno: TurnoResponse): void {
    if (!('speechSynthesis' in window)) return;

    const numero = turno.numeroTurno
      .split('')
      .join(' ');                          // "A-001" → "A - 0 0 1" (más claro)

    const texto = `Ticket ${numero}. Please proceed to Window 01.`;

    // Cancelar cualquier anuncio previo
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(texto);
    utterance.lang   = 'en-US';
    utterance.rate   = 0.88;   // un poco más lento, como las clínicas
    utterance.pitch  = 1.0;
    utterance.volume = 1.0;

    // Preferir voz femenina si está disponible
    const voces = window.speechSynthesis.getVoices();
    const vozIngles = voces.find(v => v.lang.startsWith('en') && v.name.toLowerCase().includes('female'))
                   ?? voces.find(v => v.lang.startsWith('en'))
                   ?? null;
    if (vozIngles) utterance.voice = vozIngles;

    // Repetir el anuncio 2 veces con pausa (como clínicas)
    let repeticion = 0;
    utterance.onend = () => {
      repeticion++;
      if (repeticion < 2) {
        setTimeout(() => window.speechSynthesis.speak(new SpeechSynthesisUtterance(texto)), 1200);
      }
    };

    window.speechSynthesis.speak(utterance);
  }

  cargarDatos(): void {
    this.turnoService.enAtencion().subscribe({
      next: (t) => {
        const turno = (t as unknown) as TurnoResponse | null;
        // Anunciar solo cuando cambia el turno en atención
        if (turno && turno.numeroTurno !== this.ultimoTurnoAnunciado) {
          this.ultimoTurnoAnunciado = turno.numeroTurno;
          this.anunciarTurno(turno);
        }
        this.turnoActual = turno;
      },
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
    this.hora = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    this.fecha = now.toLocaleDateString('en-US', { weekday: 'long', day: 'numeric', month: 'long' });
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
    const m: Record<string, string> = { PRIORITARIO: 'High Priority', ADULTO_MAYOR: 'Senior Citizen', REGULAR: 'General' };
    return m[tipo] ?? 'General';
  }

  etiquetaRegistro(tipo: string): string {
    const m: Record<string, string> = { RAPIDO: 'Authentication', INTERMEDIO: 'Civil Registry', COMPLEJO: 'Notarial Deed' };
    return m[tipo] ?? tipo;
  }
}
