package com.cinemauq.patterns.structural;

public class AdaptadorPDF implements ExportadorReporte {
    private final LibreriaPDFExterna libreria = new LibreriaPDFExterna();

    @Override
    public String exportar(String nombreArchivo, String datos) {
        String ruta = "reportes/" + nombreArchivo + ".pdf";
        libreria.renderPDFStream(ruta, datos);
        return ruta;
    }

    @Override
    public String exportar(String nombreArchivo, String datos, ReporteGrafico grafico) {
        if (grafico == null) return exportar(nombreArchivo, datos);
        String ruta = "reportes/" + nombreArchivo + ".pdf";
        libreria.renderPDFStream(ruta, grafico);
        return ruta;
    }
}
