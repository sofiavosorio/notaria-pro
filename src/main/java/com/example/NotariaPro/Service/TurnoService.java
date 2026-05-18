package com.example.NotariaPro.Service;

import com.example.NotariaPro.Entidad.EstadoTurno;
import com.example.NotariaPro.Entidad.Turno;
import com.example.NotariaPro.Entidad.TipoPersona;
import com.example.NotariaPro.Entidad.TipoRegistro;
import com.example.NotariaPro.dto.TurnoRequest;
import com.example.NotariaPro.dto.TurnoResponse;
import com.example.NotariaPro.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    // Contador en memoria para el ratio 3:1
    private int contadorPrioritariosConsecutivos = 0;

    // ─── Registrar turno ────────────────────────────────────────────────────────
    @Transactional
    public TurnoResponse registrarTurno(TurnoRequest request) {
        Turno turno = new Turno();
        turno.setCedula(request.getCedula());
        turno.setNombre(request.getNombre());
        turno.setEmail(request.getEmail());
        turno.setTelefono(request.getTelefono());
        turno.setTipoPersona(TipoPersona.valueOf(request.getTipoPersona()));
        TipoRegistro tipoReg = TipoRegistro.valueOf(request.getTipoRegistro());
        turno.setTipoRegistro(tipoReg);
        turno.setNumeroTurno(generarNumeroTurno(tipoReg));

        turno = turnoRepository.save(turno);

        // Enviar turno por WhatsApp
        whatsAppService.enviarTurno(
                turno.getTelefono(),
                turno.getNumeroTurno(),
                turno.getNombre(),
                turno.getTipoRegistro().name(),
                turno.getTipoPersona().name()
        );

        return toResponse(turno);
    }

    // ─── Llamar siguiente turno (prioridad + aging + ratio 3:1) ─────────────────
    @Transactional
    public TurnoResponse llamarSiguiente() {
        List<Turno> esperando = turnoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoTurno.ESPERANDO);

        if (esperando.isEmpty()) {
            throw new RuntimeException("No hay turnos en espera");
        }

        // Verificar si hay turno EN_ATENCION activo (debe finalizarse primero)
        boolean hayEnAtencion = turnoRepository.existsByEstado(EstadoTurno.EN_ATENCION);
        if (hayEnAtencion) {
            throw new RuntimeException("Hay un turno en atención. Finalícelo antes de llamar el siguiente.");
        }

        Turno seleccionado = seleccionarSiguiente(esperando);
        seleccionado.setEstado(EstadoTurno.EN_ATENCION);
        seleccionado.setFechaAtencion(LocalDateTime.now());
        turnoRepository.save(seleccionado);

        // Actualizar contador ratio
        if (seleccionado.getTipoPersona() == TipoPersona.REGULAR) {
            contadorPrioritariosConsecutivos = 0;
        } else {
            contadorPrioritariosConsecutivos++;
        }

        return toResponse(seleccionado);
    }

    // ─── Finalizar turno ────────────────────────────────────────────────────────
    @Transactional
    public TurnoResponse finalizarTurno(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        if (turno.getEstado() != EstadoTurno.EN_ATENCION) {
            throw new RuntimeException("El turno no está en atención");
        }

        turno.setEstado(EstadoTurno.FINALIZADO);
        turno.setFechaFin(LocalDateTime.now());
        turnoRepository.save(turno);

        return toResponse(turno);
    }

    // ─── Listar turnos en espera (ordenados por peso final desc) ─────────────────
    public List<TurnoResponse> listarEsperando() {
        return turnoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoTurno.ESPERANDO)
                .stream()
                .map(this::toResponse)
                .sorted(Comparator.comparingDouble(TurnoResponse::pesoFinal).reversed())
                .collect(Collectors.toList());
    }

    // ─── Últimos 3 turnos finalizados (para historial en pantalla) ───────────────
    public List<TurnoResponse> listarRecientes() {
        return turnoRepository.findTop3ByEstadoOrderByFechaFinDesc(EstadoTurno.FINALIZADO)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Listar todos los turnos ─────────────────────────────────────────────────
    public List<TurnoResponse> listarTodos() {
        return turnoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Turno en atención actual ────────────────────────────────────────────────
    public java.util.Optional<TurnoResponse> turnoEnAtencion() {
        return turnoRepository.findFirstByEstado(EstadoTurno.EN_ATENCION)
                .map(this::toResponse);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ALGORITMO DE SELECCIÓN: Prioridad → FIFO → Aging → Ratio 3:1
    // ════════════════════════════════════════════════════════════════════════════
    private Turno seleccionarSiguiente(List<Turno> esperando) {
        boolean hayRegulares = esperando.stream()
                .anyMatch(t -> t.getTipoPersona() == TipoPersona.REGULAR);

        boolean hayPrioritarios = esperando.stream()
                .anyMatch(t -> t.getTipoPersona() != TipoPersona.REGULAR);

        // Ratio 3:1 — si ya pasaron 3 prioritarios consecutivos y hay regulares, forzar regular
        if (contadorPrioritariosConsecutivos >= 3 && hayRegulares) {
            return esperando.stream()
                    .filter(t -> t.getTipoPersona() == TipoPersona.REGULAR)
                    .max(Comparator.comparingDouble(this::calcularPesoFinal))
                    .orElseThrow();
        }

        // Ordenar por peso final (incluye aging) de mayor a menor
        return esperando.stream()
                .max(Comparator.comparingDouble(this::calcularPesoFinal))
                .orElseThrow();
    }

    // ─── Fórmula de Aging ───────────────────────────────────────────────────────
    // Peso Final = (Peso Persona + Peso Trámite) + (Minutos Espera / 10)
    public double calcularPesoFinal(Turno turno) {
        long minutosEspera = Duration.between(turno.getFechaCreacion(), LocalDateTime.now()).toMinutes();
        return (turno.getTipoPersona().getPeso() + turno.getTipoRegistro().getPeso())
                + (minutosEspera / 10.0);
    }

    // ─── Número correlativo por tipo: A-001, C-001, E-001 ───────────────────────
    private String generarNumeroTurno(TipoRegistro tipo) {
        long total = turnoRepository.contarPorTipoRegistro(tipo);
        String prefijo = switch (tipo) {
            case RAPIDO     -> "A"; // Autenticaciones
            case INTERMEDIO -> "C"; // Registro Civil
            case COMPLEJO   -> "E"; // Escrituración
        };
        return String.format("%s-%03d", prefijo, total + 1);
    }

    // ─── Convertir Entidad → DTO ─────────────────────────────────────────────────
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private TurnoResponse toResponse(Turno turno) {
        double pesoFinal = turno.getEstado() == EstadoTurno.ESPERANDO
                ? calcularPesoFinal(turno)
                : turno.getTipoPersona().getPeso() + turno.getTipoRegistro().getPeso();

        return new TurnoResponse(
                turno.getId(),
                turno.getNumeroTurno(),
                turno.getCedula(),
                turno.getNombre(),
                turno.getEmail(),
                turno.getTelefono(),
                turno.getTipoPersona().name(),
                turno.getTipoRegistro().name(),
                turno.getEstado().name(),
                turno.getFechaCreacion() != null ? turno.getFechaCreacion().format(FMT) : null,
                turno.getFechaAtencion() != null ? turno.getFechaAtencion().format(FMT) : null,
                turno.getFechaFin()      != null ? turno.getFechaFin().format(FMT)      : null,
                pesoFinal
        );
    }
}
