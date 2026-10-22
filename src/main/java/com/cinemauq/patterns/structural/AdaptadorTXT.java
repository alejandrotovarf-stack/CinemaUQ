package com.cinemauq.patterns.structural;

public class AdaptadorTXT implements ExportadorReporte {
    private final ApiGeneradorTXTExterno api = new ApiGeneradorTXTExterno();

    @Override
    public String exportar(String nombreArchivo, String datos) {
        String ruta = "reportes/" + nombreArchivo + ".txt";
        api.writeTextFile(ruta, datos);
        return ruta;
    }
}
