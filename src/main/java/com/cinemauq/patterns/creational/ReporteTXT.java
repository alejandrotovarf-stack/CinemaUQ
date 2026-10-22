package com.cinemauq.patterns.creational;

import java.time.format.DateTimeFormatter;

public class ReporteTXT extends Reporte {
    public ReporteTXT(String periodo, String datos) {
        super(periodo, datos);
    }

    @Override
    public String generarContenido() {
        String contenido = "==== REPORTE CINEMAUQ (TXT) ====\n"
                + "Periodo: " + periodo + "\n"
                + "Generado: " + fechaGeneracion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                + "--------------------------------\n"
                + datos + "\n"
                + "================================";
        return contenido.replace("\r\n", "\n").replace("\n", System.lineSeparator());
    }
}
