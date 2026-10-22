package com.cinemauq.model;

import java.time.LocalDate;

public class MovimientoPuntos {
    public enum Tipo { ACUMULACION, REDENCION, REVERSION }

    private final Tipo tipo;
    private int cantidad;
    private final int cantidadOriginal;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;
    private Compra compraOrigen;

    public MovimientoPuntos(Tipo tipo, int cantidad, LocalDate fechaObtencion, int diasVigencia, Compra compraOrigen) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.cantidadOriginal = cantidad;
        this.fechaObtencion = fechaObtencion;
        this.fechaVencimiento = fechaObtencion.plusDays(diasVigencia);
        this.compraOrigen = compraOrigen;
    }

    public Tipo getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public int getCantidadOriginal() { return cantidadOriginal; }
    public LocalDate getFechaObtencion() { return fechaObtencion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public Compra getCompraOrigen() { return compraOrigen; }

    public boolean estaVencido(LocalDate hoy) {
        return hoy.isAfter(fechaVencimiento);
    }

    public int consumir(int cantidadSolicitada) {
        int consumido = Math.min(this.cantidad, cantidadSolicitada);
        this.cantidad -= consumido;
        return consumido;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %d pts (obtenido %s, vence %s)", tipo, tipo == Tipo.ACUMULACION ? "+" : "-",
                cantidad, fechaObtencion, fechaVencimiento);
    }
}
