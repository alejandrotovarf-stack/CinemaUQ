package com.cinemauq.patterns.structural;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatronesEstructuralesTest {
    @Test void adaptadoresImplementanContratoDeExportacion() {
        assertInstanceOf(ExportadorReporte.class, new AdaptadorPDF());
        assertInstanceOf(ExportadorReporte.class, new AdaptadorTXT());
    }

    @Test void graficoExponeEstructuraDeDatos() {
        assertNotNull(ReporteGrafico.class);
    }
}