package com.cinemauq.patterns.creational;

public final class ConfiguracionCinema {

    private static volatile ConfiguracionCinema instancia;

    private double valorPuntoRedencion;
    private double montoPorPunto;
    private int puntosPorMonto;
    private int diasVigenciaPuntos;

    private ConfiguracionCinema() {
        this.valorPuntoRedencion = 100.0;
        this.montoPorPunto = 10000.0;
        this.puntosPorMonto = 1;
        this.diasVigenciaPuntos = 365;
    }

    public static ConfiguracionCinema getInstancia() {
        if (instancia == null) {
            synchronized (ConfiguracionCinema.class) {
                if (instancia == null) {
                    instancia = new ConfiguracionCinema();
                }
            }
        }
        return instancia;
    }

    public double getValorPuntoRedencion() { return valorPuntoRedencion; }
    public double getMontoPorPunto() { return montoPorPunto; }
    public int getPuntosPorMonto() { return puntosPorMonto; }
    public int getDiasVigenciaPuntos() { return diasVigenciaPuntos; }

    public void setValorPuntoRedencion(double valorPuntoRedencion) { this.valorPuntoRedencion = valorPuntoRedencion; }
    public void setMontoPorPunto(double montoPorPunto) { this.montoPorPunto = montoPorPunto; }
    public void setPuntosPorMonto(int puntosPorMonto) { this.puntosPorMonto = puntosPorMonto; }
    public void setDiasVigenciaPuntos(int diasVigenciaPuntos) { this.diasVigenciaPuntos = diasVigenciaPuntos; }

    public int calcularPuntosGanados(double valorPagado) {
        return (int) Math.floor((valorPagado / montoPorPunto) * puntosPorMonto);
    }

    public int calcularCostoEnPuntos(double valor) {
        return (int) Math.ceil(valor / valorPuntoRedencion);
    }

    public String getReglasPuntos() {
        return String.format(
                "1 punto CinemaUQ por cada $%.0f pagados | 1 punto = $%.0f en redenciones | Vigencia: %d dias",
                montoPorPunto, valorPuntoRedencion, diasVigenciaPuntos);
    }
}
