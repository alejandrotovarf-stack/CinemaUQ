package com.cinemauq.patterns.structural;

public interface ExportadorReporte {
    String exportar(String nombreArchivo, String datos);

    default String exportar(String nombreArchivo, String datos, ReporteGrafico grafico) {
        return exportar(nombreArchivo, datos);
    }
}
