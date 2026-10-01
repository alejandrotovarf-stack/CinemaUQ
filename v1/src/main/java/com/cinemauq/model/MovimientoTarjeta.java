package com.cinemauq.model;

import java.time.LocalDateTime;

public class MovimientoTarjeta {
    private String tipo;
    private double valor;
    private LocalDateTime fecha;

    public MovimientoTarjeta(String tipo, double valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.fecha = LocalDateTime.now();
    }

    public String getTipo() { return tipo; }
    public double getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return "[" + fecha + "] " + tipo + ": $" + valor;
    }
}
