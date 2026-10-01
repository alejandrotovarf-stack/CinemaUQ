package com.cinemauq.patterns.creational;

public class GeneradorReportePDF extends GeneradorReporte {
    @Override
    protected Reporte crearReporte(String periodo, String datos) {
        return new ReportePDF(periodo, datos);
    }
}
