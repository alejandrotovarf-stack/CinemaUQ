package com.cinemauq.model;

public class PrecioBase implements PrecioCalculable {
    private final double valor;

    public PrecioBase(double valor) {
        this.valor = valor;
    }

    @Override
    public double calcularPrecio() {
        return valor;
    }
}
