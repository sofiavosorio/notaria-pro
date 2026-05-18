package com.example.NotariaPro.Entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_turno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroTurno;

    @NotBlank
    private String cedula;

    @NotBlank
    private String nombre;

    @Email
    @NotBlank
    private String email;

    private String telefono;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoPersona tipoPersona;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoRegistro tipoRegistro;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado = EstadoTurno.ESPERANDO;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAtencion;

    private LocalDateTime fechaFin;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoTurno.ESPERANDO;
    }
}
