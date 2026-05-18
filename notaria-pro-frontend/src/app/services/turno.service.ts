import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TurnoRequest, TurnoResponse } from '../models/turno.model';

@Injectable({ providedIn: 'root' })
export class TurnoService {
  private api = 'http://localhost:8080/api/v1/turnos';

  constructor(private http: HttpClient) {}

  registrar(request: TurnoRequest): Observable<TurnoResponse> {
    return this.http.post<TurnoResponse>(this.api, request);
  }

  llamarSiguiente(): Observable<TurnoResponse> {
    return this.http.put<TurnoResponse>(`${this.api}/siguiente`, {});
  }

  finalizar(id: number): Observable<TurnoResponse> {
    return this.http.put<TurnoResponse>(`${this.api}/${id}/finalizar`, {});
  }

  enAtencion(): Observable<TurnoResponse> {
    return this.http.get<TurnoResponse>(`${this.api}/en-atencion`);
  }

  esperando(): Observable<TurnoResponse[]> {
    return this.http.get<TurnoResponse[]>(`${this.api}/esperando`);
  }

  recientes(): Observable<TurnoResponse[]> {
    return this.http.get<TurnoResponse[]>(`${this.api}/recientes`);
  }

  todos(): Observable<TurnoResponse[]> {
    return this.http.get<TurnoResponse[]>(this.api);
  }
}
