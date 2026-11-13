package com.cinemauq.service;

import com.cinemauq.exception.CancelacionNoPermitidaException;
import com.cinemauq.exception.OperacionYaReembolsadaException;
import com.cinemauq.model.Compra;
import com.cinemauq.model.Entrada;
import com.cinemauq.model.Funcion;
import com.cinemauq.model.Reembolso;
import com.cinemauq.patterns.behavioral.PoliticaCancelacion;
import com.cinemauq.patterns.behavioral.SelectorPoliticaCancelacion;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServicioCancelacion {

    private final Set<String> comprasReembolsadas = new HashSet<>();

    public Reembolso solicitarCancelacion(Compra compra) {
        if (comprasReembolsadas.contains(compra.getId())) {
            throw new OperacionYaReembolsadaException("La compra " + compra.getId() + " ya fue reembolsada anteriormente.");
        }

        long horasRestantes = calcularHorasRestantes(compra);
        PoliticaCancelacion politica = SelectorPoliticaCancelacion.seleccionar(horasRestantes);

        if (!politica.esCancelacionPermitida(horasRestantes)) {
            throw new CancelacionNoPermitidaException(
                    "No se permite cancelar: faltan " + horasRestantes + " hora(s) para la funcion (RN-08).");
        }

        double porcentaje = politica.calcularPorcentajeDevolucion(horasRestantes);
        return ejecutarReembolso(compra, porcentaje);
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
