package com.cinemauq.patterns.behavioral;

public class PoliticaMas24h implements PoliticaCancelacion {
    @Override
    public double calcularPorcentajeDevolucion(long horasRestantes) { return 1.0; }

    @Override
    public boolean esCancelacionPermitida(long horasRestantes) { return horasRestantes > 24; }
}
