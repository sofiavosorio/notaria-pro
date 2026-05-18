package com.example.NotariaPro.repository;

import com.example.NotariaPro.Entidad.EstadoTurno;
import com.example.NotariaPro.Entidad.Turno;
import com.example.NotariaPro.Entidad.TipoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByEstadoOrderByFechaCreacionAsc(EstadoTurno estado);

    Optional<Turno> findFirstByEstado(EstadoTurno estado);

    boolean existsByEstado(EstadoTurno estado);

    // Contar turnos prioritarios atendidos consecutivamente (para ratio 3:1)
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.estado = 'FINALIZADO' AND t.tipoPersona != 'REGULAR' " +
           "AND t.fechaFin = (SELECT MAX(t2.fechaFin) FROM Turno t2 WHERE t2.estado = 'FINALIZADO')")
    long contarPrioritariosConsecutivos();

    // Ultimo turno atendido
    Optional<Turno> findTopByEstadoOrderByFechaFinDesc(EstadoTurno estado);

    // Últimos N turnos finalizados ordenados por fecha fin descendente
    List<Turno> findTop3ByEstadoOrderByFechaFinDesc(EstadoTurno estado);

    // Numero correlativo global
    @Query("SELECT COUNT(t) FROM Turno t")
    long contarTodos();

    // Numero correlativo por tipo de registro
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.tipoRegistro = :tipo")
    long contarPorTipoRegistro(com.example.NotariaPro.Entidad.TipoRegistro tipo);
}
