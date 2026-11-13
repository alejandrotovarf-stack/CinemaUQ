package com.cinemauq.patterns.creational;

import com.cinemauq.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatronesCreacionalesTest {
    @Test void singletonComparteInstancia() {
        assertSame(ConfiguracionCinema.getInstancia(), ConfiguracionCinema.getInstancia());
        ConfiguracionCinema config = ConfiguracionCinema.getInstancia();
        assertEquals(1, config.calcularPuntosGanados(10000));
        assertEquals(2, config.calcularCostoEnPuntos(150));
    }

    @Test void factoryMethodProduceReportesDelTipoCorrecto() {
        Reporte pdf = new GeneradorReportePDF().generar("2026", "ventas");
        Reporte txt = new GeneradorReporteTXT().generar("2026", "ventas");
        assertInstanceOf(ReportePDF.class, pdf);
        assertInstanceOf(ReporteTXT.class, txt);
        assertTrue(pdf.generarContenido().contains("ventas"));
        assertTrue(txt.generarContenido().contains("ventas"));
    }

    @Test void builderValidaCamposYAplicaPromocion() {
        Cliente cliente = new Cliente("1", "Ana", "ana@test.com", "clave");
        TarjetaVirtual tarjeta = new TarjetaVirtual("T1");
        Funcion funcion = new Funcion("F1", new Pelicula("P1", "Prueba", "", 90, "", "", ""),
                new Sala("S1", 1, 1), LocalDate.now().plusDays(3), LocalTime.NOON, 20000);
        Entrada entrada = new Entrada(funcion, funcion.getSala().getAsientos().get(0));
        assertThrows(IllegalStateException.class, () -> new Compra.Builder().build());
        assertThrows(IllegalStateException.class, () -> new Compra.Builder().conCliente(cliente).build());
        assertThrows(IllegalStateException.class, () -> new Compra.Builder().conCliente(cliente).conEntradas(List.of(entrada)).build());
        Compra compra = new Compra.Builder().conCliente(cliente).conEntradas(List.of(entrada)).conTarjeta(tarjeta)
                .conPromocion("Descuento", 0.1).build();
        assertEquals(20000, compra.getSubtotal());
        assertEquals(18000, compra.getTotal());
        assertEquals("PENDIENTE", compra.getEstado().getNombre());
        assertThrows(UnsupportedOperationException.class, () -> compra.getEntradas().clear());
    }
}