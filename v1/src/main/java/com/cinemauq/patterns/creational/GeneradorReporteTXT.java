package com.cinemauq.patterns.creational;

public class GeneradorReporteTXT extends GeneradorReporte {
    @Override
    protected Reporte crearReporte(String periodo, String datos) {
        return new ReporteTXT(periodo, datos);
    }
}
