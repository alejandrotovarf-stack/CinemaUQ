package com.cinemauq.patterns.creational;

public class ReporteTXT extends Reporte {
    public ReporteTXT(String periodo, String datos) {
        super(periodo, datos);
    }

    @Override
    public String generarContenido() {
        return "==== REPORTE CINEMAUQ (TXT) ====\n"
                + "Periodo: " + periodo + "\n"
                + "Generado: " + fechaGeneracion + "\n"
                + "--------------------------------\n"
                + datos + "\n"
                + "================================";
    }
}
