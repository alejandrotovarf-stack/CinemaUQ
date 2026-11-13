package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class PatronesComportamientoTest {
    private Compra compra() {
        Cliente cliente = new Cliente("1", "Ana", "ana@test.com", "123");
        TarjetaVirtual tarjeta = new TarjetaVirtual("T1");
        cliente.setTarjetaVirtual(tarjeta);
        Funcion funcion = new Funcion("F1", new Pelicula("P1", "Prueba", "", 90, "", "", ""),
                new Sala("S1", 1, 1), LocalDate.now().plusDays(5), LocalTime.NOON, 15000);
        return new Compra.Builder().conCliente(cliente).conTarjeta(tarjeta)
                .conEntradas(List.of(new Entrada(funcion, funcion.getSala().getAsientos().get(0)))).build();
    }

    @Test void stateConfirmaYNotificaUnaVez() {
        Compra compra = compra();
        AtomicInteger llamadas = new AtomicInteger();
        compra.adjuntarObservador(c -> llamadas.incrementAndGet());
        assertEquals("PENDIENTE", compra.getEstado().getNombre());
        compra.confirmar();
        assertEquals("CONFIRMADA", compra.getEstado().getNombre());
        compra.confirmar();
        assertEquals(1, llamadas.get());
    }

    @Test void stateCancelaYProtegeCompraCancelada() {
        Compra compra = compra();
        compra.cancelarCompra();
        assertEquals("CANCELADA", compra.getEstado().getNombre());
        assertThrows(IllegalStateException.class, compra::confirmar);
        assertThrows(IllegalStateException.class, compra::cancelarCompra);
    }

    @Test void strategySeleccionaPoliticasEnLimites() {
        assertInstanceOf(PoliticaMenos2h.class, SelectorPoliticaCancelacion.seleccionar(1));
        assertInstanceOf(PoliticaEntre2y24h.class, SelectorPoliticaCancelacion.seleccionar(2));
        assertInstanceOf(PoliticaEntre2y24h.class, SelectorPoliticaCancelacion.seleccionar(24));
        assertInstanceOf(PoliticaMas24h.class, SelectorPoliticaCancelacion.seleccionar(25));
        assertEquals(0, new PoliticaMenos2h().calcularPorcentajeDevolucion(1));
        assertEquals(0.8, new PoliticaEntre2y24h().calcularPorcentajeDevolucion(12));
        assertEquals(1, new PoliticaMas24h().calcularPorcentajeDevolucion(48));
        assertTrue(new PoliticaMenos2h().esCancelacionPermitida(1));
        assertFalse(new PoliticaEntre2y24h().esCancelacionPermitida(1));
        assertTrue(new PoliticaMas24h().esCancelacionPermitida(25));
    }

    @Test void observerNotificacionesRecibeConfirmacion() {
        Compra compra = compra();
        ObservadorNotificaciones notificaciones = new ObservadorNotificaciones();
        compra.adjuntarObservador(notificaciones);
        compra.confirmar();
        assertEquals(1, notificaciones.getBandeja().size());
        assertTrue(notificaciones.getBandeja().get(0).contains(compra.getId()));
        assertTrue(notificaciones.getBandeja().get(0).contains("CONFIRMADA"));
    }

    @Test void observerPuntosAcumulaAlConfirmar() {
        Compra compra = compra();
        compra.adjuntarObservador(new ObservadorPuntos());
        compra.confirmar();
        assertEquals(1, compra.getCliente().getCuentaPuntos().getSaldoPuntos());
    }
}