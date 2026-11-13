package com.cinemauq.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class ModeloTest {
    @Test void asientoIdentidadYDisponibilidad() {
        Asiento asiento = new Asiento("B", 4);
        assertEquals("B4", asiento.getCodigo());
        assertEquals(new Asiento("B", 4), asiento);
        assertEquals(asiento.hashCode(), new Asiento("B", 4).hashCode());
        assertNotEquals(asiento, new Asiento("B", 5));
        assertTrue(asiento.isDisponible());
        asiento.setDisponible(false);
        assertFalse(asiento.isDisponible());
    }

    @Test void salaYFuncionGestionanAsientos() {
        Sala sala = new Sala("S1", 2, 3);
        assertEquals(6, sala.getCapacidad());
        assertEquals("A1", sala.getAsientos().get(0).getCodigo());
        assertEquals("B3", sala.getAsientos().get(5).getCodigo());
        Pelicula pelicula = new Pelicula("P1", "Prueba", "Drama", 90, "TP", "Sinopsis", "");
        Funcion funcion = new Funcion("F1", pelicula, sala, LocalDate.now().plusDays(5), LocalTime.NOON, 18000);
        Asiento asiento = sala.getAsientos().get(0);
        assertTrue(funcion.estaDisponible(asiento));
        funcion.marcarOcupado(asiento);
        assertFalse(funcion.estaDisponible(asiento));
        funcion.liberarAsiento(asiento);
        assertTrue(funcion.estaDisponible(asiento));
        funcion.setCancelada(true);
        assertFalse(funcion.estaDisponible(asiento));
        assertEquals(18000, new Entrada(funcion, asiento).getPrecioBase());
    }

    @Test void peliculaTieneIdentidadPorIdentificador() {
        Pelicula primera = new Pelicula("P1", "Uno", "Drama", 100, "TP", "", "");
        Pelicula segunda = new Pelicula("P1", "Otro", "Accion", 120, "TP", "", "");
        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
        assertEquals("Uno", primera.getTitulo());
    }

    @Test void tarjetaRegistraMovimientosYControlaSaldo() {
        TarjetaVirtual tarjeta = new TarjetaVirtual("1001");
        assertEquals(0, tarjeta.getSaldo());
        tarjeta.recargar(20000);
        assertTrue(tarjeta.debitar(5000));
        assertFalse(tarjeta.debitar(50000));
        tarjeta.acreditar(2000);
        assertEquals(17000, tarjeta.getSaldo());
        assertEquals(3, tarjeta.getMovimientos().size());
        tarjeta.setActiva(false);
        assertFalse(tarjeta.debitar(100));
    }

    @Test void cuentaPuntosAcumulaConsumeYRechazaExcesos() {
        Cliente cliente = new Cliente("1", "Ana", "ana@test.com", "clave");
        CuentaPuntos cuenta = cliente.getCuentaPuntos();
        cuenta.acumular(10, 60, null);
        cuenta.acumular(5, 60, null);
        assertEquals(15, cuenta.getSaldoPuntos());
        assertTrue(cuenta.consumir(7));
        assertEquals(8, cuenta.getSaldoPuntos());
        assertFalse(cuenta.consumir(9));
        assertEquals(8, cuenta.getSaldoPuntos());
    }

    @Test void compositeConfiteriaCalculaPrecio() {
        ProductoConfiteria bebida = new ProductoConfiteria("B", "Bebida", "", 8000, true);
        ProductoConfiteria comida = new ProductoConfiteria("C", "Comida", "", 12000, true);
        Combo combo = new Combo("Combo", 3000);
        combo.agregarItem(bebida);
        combo.agregarItem(comida);
        assertEquals(17000, combo.getPrecio());
        Combo exterior = new Combo("Doble", 1000);
        exterior.agregarItem(combo);
        exterior.agregarItem(bebida);
        assertEquals(24000, exterior.getPrecio());
    }

    @Test void decoratorAplicaDescuento() {
        PrecioCalculable precio = new PrecioBase(100000);
        Promocion promocion = new Promocion(precio, "Promo", 0.15);
        assertEquals(85000, promocion.calcularPrecio());
        assertEquals("Promo", promocion.getNombre());
        assertEquals(0.15, promocion.getPorcentajeDescuento());
    }
}