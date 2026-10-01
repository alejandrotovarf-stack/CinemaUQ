package com.cinemauq.patterns.creational;










public abstract class GeneradorReporte {

    
    public final Reporte generar(String periodo, String datos) {
        Reporte reporte = crearReporte(periodo, datos);
        System.out.println(">> Reporte generado con " + getClass().getSimpleName());
        return reporte;
    }

    
    protected abstract Reporte crearReporte(String periodo, String datos);
}
