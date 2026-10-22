package com.cinemauq.model;

import java.util.ArrayList;
import java.util.List;

public class Combo implements ItemConfiteria {
    private String nombre;
    private double descuento;
    private final List<ItemConfiteria> componentes = new ArrayList<>();

    public Combo(String nombre, double descuento) {
        this.nombre = nombre;
        this.descuento = descuento;
    }

    public void agregarItem(ItemConfiteria item) {
        componentes.add(item);
    }

    public List<ItemConfiteria> getComponentes() { return componentes; }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public double getPrecio() {
        double suma = 0;
        for (ItemConfiteria item : componentes) {
            suma += item.getPrecio();
        }
        return Math.max(0, suma - descuento);
    }

    @Override
    public String toString() { return nombre + " (combo) - $" + getPrecio(); }
}
