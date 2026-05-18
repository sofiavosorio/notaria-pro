package com.example.NotariaPro.controller;

import com.example.NotariaPro.Service.TurnoService;
import com.example.NotariaPro.dto.TurnoRequest;
import com.example.NotariaPro.dto.TurnoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
@CrossOrigin(origins = "*")
@Tag(name = "Turnos", description = "Gestión de turnos Notaría Pro")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Operation(summary = "Registrar nuevo turno")
    @PostMapping
    public ResponseEntity<TurnoResponse> registrar(@Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.registrarTurno(request));
    }

    @Operation(summary = "Llamar siguiente turno (aplica prioridad + aging + ratio 3:1)")
    @PutMapping("/siguiente")
    public ResponseEntity<TurnoResponse> llamarSiguiente() {
        return ResponseEntity.ok(turnoService.llamarSiguiente());
    }

    @Operation(summary = "Finalizar turno en atención")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<TurnoResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.finalizarTurno(id));
    }

    @Operation(summary = "Ver turno actualmente en atención")
    @GetMapping("/en-atencion")
    public ResponseEntity<TurnoResponse> enAtencion() {
        return turnoService.turnoEnAtencion()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Listar turnos en espera con su peso actual (incluye aging)")
    @GetMapping("/esperando")
    public ResponseEntity<List<TurnoResponse>> listarEsperando() {
        return ResponseEntity.ok(turnoService.listarEsperando());
    }

    @Operation(summary = "Últimos 3 turnos finalizados (historial pantalla)")
    @GetMapping("/recientes")
    public ResponseEntity<List<TurnoResponse>> listarRecientes() {
        return ResponseEntity.ok(turnoService.listarRecientes());
    }

    @Operation(summary = "Listar todos los turnos")
    @GetMapping
    public ResponseEntity<List<TurnoResponse>> listarTodos() {
        return ResponseEntity.ok(turnoService.listarTodos());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception e) {
        return ResponseEntity.badRequest().body(e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}
