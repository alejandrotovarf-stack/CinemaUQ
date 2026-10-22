package com.cinemauq.patterns.structural;

import java.util.List;

public record ReporteGrafico(
        String titulo,
        String periodo,
        String generado,
        List<Indicador> indicadores,
        String tituloGrafico,
        String ejeValores,
        boolean monetario,
        List<Punto> puntos,
        String detalle) {

    public record Indicador(String nombre, String valor) {}

    public record Punto(String etiqueta, double valor, String textoValor) {}
}
