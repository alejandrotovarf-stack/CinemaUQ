package com.cinemauq.model;

public abstract class PromocionDecorator implements PrecioCalculable {
    protected final PrecioCalculable componenteBase;

    protected PromocionDecorator(PrecioCalculable componenteBase) {
        this.componenteBase = componenteBase;
    }

    @Override
    public abstract double calcularPrecio();
}
