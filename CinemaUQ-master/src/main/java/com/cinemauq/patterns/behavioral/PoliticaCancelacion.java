package com.cinemauq.patterns.behavioral;

public interface PoliticaCancelacion {
    double calcularPorcentajeDevolucion(long horasRestantes);
    boolean esCancelacionPermitida(long horasRestantes);
}
