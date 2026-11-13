package com.cinemauq.patterns.behavioral;

public class PoliticaEntre2y24h implements PoliticaCancelacion {
    @Override
    public double calcularPorcentajeDevolucion(long horasRestantes) { return 0.80; }

    @Override
    public boolean esCancelacionPermitida(long horasRestantes) { return horasRestantes >= 2 && horasRestantes <= 24; }
}
