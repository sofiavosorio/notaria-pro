package com.example.NotariaPro.Entidad;

public enum TipoPersona {
    PRIORITARIO(3),   // Discapacidad / Embarazo
    ADULTO_MAYOR(2),
    REGULAR(1);

    private final int peso;

    TipoPersona(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
