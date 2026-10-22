package com.cinemauq.model;

import java.util.ArrayList;
import java.util.List;

public class TarjetaVirtual {
    private String numeroTarjeta;
    private double saldo;
    private boolean activa;
    private final List<MovimientoTarjeta> movimientos = new ArrayList<>();

    public TarjetaVirtual(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
        this.saldo = 0.0;
        this.activa = true;
    }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public double getSaldo() { return saldo; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public List<MovimientoTarjeta> getMovimientos() { return movimientos; }

    public boolean debitar(double valor) {
        if (!activa || valor > saldo) return false;
        saldo -= valor;
        movimientos.add(new MovimientoTarjeta("DEBITO", valor));
        return true;
    }

    public void recargar(double valor) {
        saldo += valor;
        movimientos.add(new MovimientoTarjeta("RECARGA", valor));
    }

    public void acreditar(double valor) {
        saldo += valor;
        movimientos.add(new MovimientoTarjeta("REEMBOLSO", valor));
    }
}
