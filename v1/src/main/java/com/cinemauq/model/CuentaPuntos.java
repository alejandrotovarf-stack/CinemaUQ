package com.cinemauq.model;

public class CuentaPuntos {
    private Cliente titular;
    private int saldoPuntos;

    public CuentaPuntos(Cliente titular) {
        this.titular = titular;
        this.saldoPuntos = 0;
    }

    public int getSaldoPuntos() { return saldoPuntos; }

    public void acumular(int puntos) {
        this.saldoPuntos += puntos;
    }

    public boolean consumir(int puntos) {
        if (puntos > saldoPuntos) return false;
        saldoPuntos -= puntos;
        return true;
    }
}
