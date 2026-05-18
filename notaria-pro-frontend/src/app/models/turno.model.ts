export interface TurnoRequest {
  cedula: string;
  nombre: string;
  email: string;
  telefono: string;
  tipoPersona: 'PRIORITARIO' | 'ADULTO_MAYOR' | 'REGULAR';
  tipoRegistro: 'RAPIDO' | 'INTERMEDIO' | 'COMPLEJO';
}

export interface TurnoResponse {
  id: number;
  numeroTurno: string;
  cedula: string;
  nombre: string;
  email: string;
  telefono: string;
  tipoPersona: string;
  tipoRegistro: string;
  estado: 'ESPERANDO' | 'EN_ATENCION' | 'FINALIZADO';
  fechaCreacion: string;
  fechaAtencion: string;
  fechaFin: string;
  pesoFinal: number;
}
