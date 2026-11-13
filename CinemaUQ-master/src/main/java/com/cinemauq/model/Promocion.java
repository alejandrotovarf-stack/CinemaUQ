package com.cinemauq.model;

public class Promocion extends PromocionDecorator {
    private final String nombre;
    private final double porcentajeDescuento;

    public Promocion(PrecioCalculable componenteBase, String nombre, double porcentajeDescuento) {
        super(componenteBase);
        this.nombre = nombre;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getNombre() { return nombre; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    @Override
    public double calcularPrecio() {
        double previo = componenteBase.calcularPrecio();
        return previo - (previo * porcentajeDescuento);
    }
}
