package com.cinemauq.model;

public class ProductoConfiteria implements ItemConfiteria {
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean disponible;

    public ProductoConfiteria(String codigo, String nombre, String descripcion, double precio, boolean disponible) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
    }

    public String getCodigo() { return codigo; }
    @Override public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    @Override public double getPrecio() { return precio; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() { return nombre + " ($" + precio + ")"; }
}
