package com.cinemauq.patterns.creational;

import java.time.LocalDateTime;

public abstract class Reporte {
    protected String periodo;
    protected String datos;
    protected LocalDateTime fechaGeneracion;

    public Reporte(String periodo, String datos) {
        this.periodo = periodo;
        this.datos = datos;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public String getPeriodo() { return periodo; }
    public String getDatos() { return datos; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }

    public abstract String generarContenido();
}
