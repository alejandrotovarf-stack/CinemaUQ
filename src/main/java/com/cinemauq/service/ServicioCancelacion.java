package com.cinemauq.service;

import com.cinemauq.exception.OperacionYaReembolsadaException;
import com.cinemauq.model.Compra;
import com.cinemauq.model.Entrada;
import com.cinemauq.model.Funcion;
import com.cinemauq.model.Reembolso;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServicioCancelacion {

    private static final long HORAS_REEMBOLSO_TOTAL = 24;
    private static final long HORAS_REEMBOLSO_PARCIAL = 2;
    private static final double PORCENTAJE_TOTAL = 1.0;
    private static final double PORCENTAJE_PARCIAL = 0.80;
    private static final double PORCENTAJE_NULO = 0.0;

    private final Set<String> comprasReembolsadas = new HashSet<>();

    public Reembolso solicitarCancelacion(Compra compra) {
        if (comprasReembolsadas.contains(compra.getId())) {
            throw new OperacionYaReembolsadaException("La compra " + compra.getId() + " ya fue reembolsada anteriormente.");
        }

        long horasRestantes = calcularHorasRestantes(compra);
        double porcentaje = calcularPorcentajeDevolucion(horasRestantes);
        return ejecutarReembolso(compra, porcentaje);
    }

    /**
     * RN-08: mas de 24 h -> 100 %; entre 2 h y 24 h (inclusive) -> 80 %; menos de 2 h -> 0 %.
     * La cancelacion se acepta en todos los tramos; solo cambia el porcentaje devuelto.
     */
    private double calcularPorcentajeDevolucion(long horasRestantes) {
        if (horasRestantes > HORAS_REEMBOLSO_TOTAL) return PORCENTAJE_TOTAL;
        if (horasRestantes >= HORAS_REEMBOLSO_PARCIAL) return PORCENTAJE_PARCIAL;
        return PORCENTAJE_NULO;
    }

    public Reembolso cancelarPorFuncionCancelada(Compra compra) {
        if (comprasReembolsadas.contains(compra.getId())) {
            throw new OperacionYaReembolsadaException("La compra " + compra.getId() + " ya fue reembolsada anteriormente.");
        }
        return ejecutarReembolso(compra, 1.0);
    }

    private Reembolso ejecutarReembolso(Compra compra, double porcentaje) {
        Reembolso reembolso = new Reembolso("RB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), compra, porcentaje);
        reembolso.procesarReembolso();

        compra.cancelarCompra();
        for (Entrada e : compra.getEntradas()) {
            e.getFuncion().liberarAsiento(e.getAsiento());
        }
        compra.getCliente().getCuentaPuntos().reversarPorCompra(compra);

        comprasReembolsadas.add(compra.getId());
        return reembolso;
    }

    private long calcularHorasRestantes(Compra compra) {
        if (compra.getEntradas().isEmpty()) return 0;
        Funcion funcion = compra.getEntradas().get(0).getFuncion();
        Duration duracion = Duration.between(LocalDateTime.now(), funcion.getFechaHora());
        return Math.max(0, duracion.toHours());
    }
}
