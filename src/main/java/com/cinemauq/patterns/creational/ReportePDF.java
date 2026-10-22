package com.cinemauq.patterns.creational;

public class ReportePDF extends Reporte {
    public ReportePDF(String periodo, String datos) {
        super(periodo, datos);
    }

    @Override
    public String generarContenido() {
        return "[PDF] CinemaUQ - Periodo " + periodo + " - Generado " + fechaGeneracion
                + "\n" + datos;
    }
}
