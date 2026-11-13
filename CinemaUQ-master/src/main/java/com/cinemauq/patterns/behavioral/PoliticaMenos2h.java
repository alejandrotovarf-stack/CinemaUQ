package com.cinemauq.patterns.behavioral;

public class PoliticaMenos2h implements PoliticaCancelacion {
    @Override
    public double calcularPorcentajeDevolucion(long horasRestantes) { return 0.0; }

    @Override
    public boolean esCancelacionPermitida(long horasRestantes) { return horasRestantes < 2; }
}
