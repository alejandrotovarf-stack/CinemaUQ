package com.cinemauq.model;

public class Entrada {
    private Funcion funcion;
    private Asiento asiento;
    private double precioBase;

    public Entrada(Funcion funcion, Asiento asiento) {
        this.funcion = funcion;
        this.asiento = asiento;
        this.precioBase = funcion.getPrecioBase();
    }

    public Funcion getFuncion() { return funcion; }
    public Asiento getAsiento() { return asiento; }
    public double getPrecioBase() { return precioBase; }

    @Override
    public String toString() {
        return "Entrada[" + funcion.getPelicula().getTitulo() + " - " + asiento.getCodigo() + " - $" + precioBase + "]";
    }
}
