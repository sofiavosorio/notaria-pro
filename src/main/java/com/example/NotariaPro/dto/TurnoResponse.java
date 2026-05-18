package com.example.NotariaPro.dto;

public record TurnoResponse(
    Long id,
    String numeroTurno,
    String cedula,
    String nombre,
    String email,
    String telefono,
    String tipoPersona,
    String tipoRegistro,
    String estado,
    String fechaCreacion,
    String fechaAtencion,
    String fechaFin,
    double pesoFinal
) {}
