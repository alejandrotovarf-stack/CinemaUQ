package com.cinemauq.service;

import com.cinemauq.exception.*;
import com.cinemauq.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ServiciosTest {
    private Cliente cliente(double saldo) {
        Cliente cliente = new Cliente("1", "Ana", "ana@test.com", "clave");
        TarjetaVirtual tarjeta = new TarjetaVirtual("T1");
        tarjeta.recargar(saldo);
        cliente.setTarjetaVirtual(tarjeta);
        return cliente;
    }

    private Funcion funcion(int dias) {
        return new Funcion("F1", new Pelicula("P1", "Prueba", "", 90, "", "", ""),
                new Sala("S1", 1, 2), LocalDate.now().plusDays(dias), LocalTime.NOON, 20000);
    }

    @Test void sesionDistingueRolesYCierre() {
        GestorSesion gestor = new GestorSesion();
        assertFalse(gestor.haySesionActiva());
        gestor.iniciarSesion(cliente(100));
        assertTrue(gestor.esCliente());
        assertFalse(gestor.esAdministrador());
        gestor.cerrarSesion();
        assertFalse(gestor.haySesionActiva());
        gestor.iniciarSesion(new Administrador("2", "Admin", "admin@test.com", "clave", "E1"));
        assertTrue(gestor.esAdministrador());
    }

    @Test void facadeProcesaCompraYObserver() {
        Cliente cliente = cliente(50000);
        Funcion funcion = funcion(5);
        Asiento asiento = funcion.getSala().getAsientos().get(0);
        ServicioCompraFacade facade = new ServicioCompraFacade();
        Compra compra = facade.iniciarProcesoCompra(cliente, funcion, List.of(asiento), List.of(), null, 0);
        assertFalse(funcion.estaDisponible(asiento));
        assertTrue(facade.procesarPagoYConfirmar(compra));
        assertEquals(30000, cliente.getTarjetaVirtual().getSaldo());
        assertEquals("CONFIRMADA", compra.getEstado().getNombre());
        assertEquals(1, cliente.getHistorialCompras().size());
        assertEquals(1, facade.getObservadorNotificaciones().getBandeja().size());
    }

    @Test void facadeRechazaAsientoOcupado() {
        Cliente cliente = cliente(50000);
        Funcion funcion = funcion(5);
        Asiento asiento = funcion.getSala().getAsientos().get(0);
        funcion.marcarOcupado(asiento);
        assertThrows(AsientoNoDisponibleException.class, () ->
                new ServicioCompraFacade().iniciarProcesoCompra(cliente, funcion, List.of(asiento), List.of(), null, 0));
    }

    @Test void facadeLiberaAsientoTrasSaldoInsuficiente() {
        Cliente cliente = cliente(100);
        Funcion funcion = funcion(5);
        Asiento asiento = funcion.getSala().getAsientos().get(0);
        ServicioCompraFacade facade = new ServicioCompraFacade();
        Compra compra = facade.iniciarProcesoCompra(cliente, funcion, List.of(asiento), List.of(), null, 0);
        assertThrows(SaldoInsuficienteException.class, () -> facade.procesarPagoYConfirmar(compra));
        assertTrue(funcion.estaDisponible(asiento));
    }

    @Test void facadeLiberaAsientoTrasTarjetaInactiva() {
        Cliente cliente = cliente(50000);
        Funcion funcion = funcion(5);
        Asiento asiento = funcion.getSala().getAsientos().get(0);
        ServicioCompraFacade facade = new ServicioCompraFacade();
        Compra compra = facade.iniciarProcesoCompra(cliente, funcion, List.of(asiento), List.of(), null, 0);
        cliente.getTarjetaVirtual().setActiva(false);
        assertThrows(TarjetaInactivaException.class, () -> facade.procesarPagoYConfirmar(compra));
        assertTrue(funcion.estaDisponible(asiento));
    }

    @Test void cancelacionDevuelveSaldoYLiberaAsiento() {
        Cliente cliente = cliente(50000);
        Funcion funcion = funcion(5);
        Asiento asiento = funcion.getSala().getAsientos().get(0);
        ServicioCompraFacade facade = new ServicioCompraFacade();
        Compra compra = facade.iniciarProcesoCompra(cliente, funcion, List.of(asiento), List.of(), null, 0);
        facade.procesarPagoYConfirmar(compra);
        Reembolso reembolso = new ServicioCancelacion().solicitarCancelacion(compra);
        assertTrue(reembolso.isEjecutado());
        assertEquals(20000, reembolso.getValorDevuelto());
        assertEquals(50000, cliente.getTarjetaVirtual().getSaldo());
        assertEquals("CANCELADA", compra.getEstado().getNombre());
        assertTrue(funcion.estaDisponible(asiento));
        assertEquals(0, cliente.getCuentaPuntos().getSaldoPuntos());
    }

    @Test void cancelacionNoPermiteReembolsoDuplicado() {
        Cliente cliente = cliente(50000);
        Funcion funcion = funcion(5);
        ServicioCompraFacade facade = new ServicioCompraFacade();
        Compra compra = facade.iniciarProcesoCompra(cliente, funcion,
                List.of(funcion.getSala().getAsientos().get(0)), List.of(), null, 0);
        facade.procesarPagoYConfirmar(compra);
        ServicioCancelacion cancelacion = new ServicioCancelacion();
        cancelacion.solicitarCancelacion(compra);
        assertThrows(OperacionYaReembolsadaException.class, () -> cancelacion.solicitarCancelacion(compra));
    }

    @Test void redencionRechazaSaldoInsuficiente() {
        Cliente cliente = cliente(100);
        assertThrows(PuntosInsuficientesException.class, () -> new ServicioPuntos().redimir(cliente, 1000));
        cliente.getCuentaPuntos().acumular(30, 60, null);
        new ServicioPuntos().redimir(cliente, 1000);
        assertEquals(20, cliente.getCuentaPuntos().getSaldoPuntos());
    }
}