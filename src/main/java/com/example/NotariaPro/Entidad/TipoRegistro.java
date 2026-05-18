package com.example.NotariaPro.Entidad;

public enum TipoRegistro {
    RAPIDO(3),        // Autenticaciones
    INTERMEDIO(2),    // Registros Civiles
    COMPLEJO(1);      // Escrituras / Sucesiones

    private final int peso;

    TipoRegistro(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
