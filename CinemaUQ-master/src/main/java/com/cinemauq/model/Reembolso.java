package com.cinemauq.model;

import java.time.LocalDateTime;

public class Reembolso {
    private final String codigo;
    private final Compra compra;
    private final double porcentajeAplicado;
    private final double valorDevuelto;
    private final LocalDateTime fecha;
    private boolean ejecutado;

    public Reembolso(String codigo, Compra compra, double porcentajeAplicado) {
        this.codigo = codigo;
        this.compra = compra;
        this.porcentajeAplicado = porcentajeAplicado;
        this.valorDevuelto = compra.getTotal() * porcentajeAplicado;
        this.fecha = LocalDateTime.now();
        this.ejecutado = false;
    }

    public String getCodigo() { return codigo; }
    public Compra getCompra() { return compra; }
    public double getPorcentajeAplicado() { return porcentajeAplicado; }
    public double getValorDevuelto() { return valorDevuelto; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isEjecutado() { return ejecutado; }

    public void procesarReembolso() {
        if (ejecutado) {
            throw new IllegalStateException("Este reembolso ya fue ejecutado.");
        }
        compra.getTarjetaUtilizada().acreditar(valorDevuelto);
        this.ejecutado = true;
    }

    @Override
    public String toString() {
        return String.format("Reembolso[%s] %.0f%% -> $%.2f (compra %s)", codigo, porcentajeAplicado * 100, valorDevuelto, compra.getId());
    }
}
