package com.cinemauq.service;

import com.cinemauq.exception.AsientoNoDisponibleException;
import com.cinemauq.exception.SaldoInsuficienteException;
import com.cinemauq.exception.TarjetaInactivaException;
import com.cinemauq.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServicioCompraFacade {

    private final ServicioPuntos servicioPuntos;
    private final List<String> notificaciones = new ArrayList<>();

    public ServicioCompraFacade(ServicioPuntos servicioPuntos) {
        this.servicioPuntos = servicioPuntos;
    }

    public List<String> getNotificaciones() { return Collections.unmodifiableList(notificaciones); }

    public Compra iniciarProcesoCompra(Cliente cliente, Funcion funcion, List<Asiento> asientosSeleccionados,
                                        List<ItemConfiteria> confiteria, String nombrePromocion, double porcentajeDescuento) {

        for (Asiento asiento : asientosSeleccionados) {
            if (!funcion.estaDisponible(asiento)) {
                throw new AsientoNoDisponibleException("El asiento " + asiento.getCodigo() + " ya no esta disponible para esta funcion.");
            }
        }
        for (Asiento asiento : asientosSeleccionados) {
            funcion.marcarOcupado(asiento);
        }

        try {
            List<Entrada> entradas = asientosSeleccionados.stream()
                    .map(a -> new Entrada(funcion, a))
                    .toList();

            Compra.Builder builder = new Compra.Builder()
                    .conCliente(cliente)
                    .conEntradas(entradas)
                    .conConfiteria(confiteria)
                    .conTarjeta(cliente.getTarjetaVirtual());

            if (nombrePromocion != null && porcentajeDescuento > 0) {
                builder.conPromocion(nombrePromocion, porcentajeDescuento);
            }

            return builder.build();
        } catch (RuntimeException ex) {
            for (Asiento asiento : asientosSeleccionados) {
                funcion.liberarAsiento(asiento);
            }
            throw ex;
        }
    }

    public boolean procesarPagoYConfirmar(Compra compra) {
        TarjetaVirtual tarjeta = compra.getTarjetaUtilizada();
        if (!tarjeta.isActiva()) {
            liberarAsientosPorFallo(compra);
            throw new TarjetaInactivaException("La tarjeta virtual esta inactiva.");
        }
        if (tarjeta.getSaldo() < compra.getTotal()) {
            liberarAsientosPorFallo(compra);
            throw new SaldoInsuficienteException("Saldo insuficiente para completar la compra.");
        }

        boolean debitado = tarjeta.debitar(compra.getTotal());
        if (!debitado) {
            liberarAsientosPorFallo(compra);
            return false;
        }

        if (compra.confirmar()) {
            servicioPuntos.acumularPorCompra(compra);
            registrarNotificacion(compra);
        }
        compra.getCliente().registrarCompra(compra);
        return true;
    }

    private void registrarNotificacion(Compra compra) {
        String mensaje = "Tu compra " + compra.getId() + " ahora esta " + compra.getEstado().getNombre();
        notificaciones.add(mensaje);
        System.out.println("[Notificacion] " + mensaje);
    }

    private void liberarAsientosPorFallo(Compra compra) {
        for (Entrada e : compra.getEntradas()) {
            e.getFuncion().liberarAsiento(e.getAsiento());
        }
    }
}
