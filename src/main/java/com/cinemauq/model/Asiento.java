package com.cinemauq.model;

import java.util.Objects;

public class Asiento {
    private final int numero;
    private final String fila;
    private boolean disponible;

    public Asiento(String fila, int numero) {
        this.fila = fila;
        this.numero = numero;
        this.disponible = true;
    }

    public int getNumero() { return numero; }
    public String getFila() { return fila; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getCodigo() { return fila + numero; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asiento)) return false;
        Asiento asiento = (Asiento) o;
        return numero == asiento.numero && Objects.equals(fila, asiento.fila);
    }

    @Override
    public int hashCode() { return Objects.hash(fila, numero); }

    @Override
    public String toString() { return getCodigo(); }
}
