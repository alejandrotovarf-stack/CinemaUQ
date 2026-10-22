package com.cinemauq.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class Funcion {
    private String identificador;
    private Pelicula pelicula;
    private Sala sala;
    private LocalDate fecha;
    private LocalTime hora;
    private double precioBase;
    private boolean cancelada = false;
    private final Set<String> asientosOcupados = new HashSet<>();

    public Funcion(String identificador, Pelicula pelicula, Sala sala, LocalDate fecha, LocalTime hora, double precioBase) {
        this.identificador = identificador;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fecha = fecha;
        this.hora = hora;
        this.precioBase = precioBase;
    }

    public String getIdentificador() { return identificador; }
    public Pelicula getPelicula() { return pelicula; }
    public Sala getSala() { return sala; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public double getPrecioBase() { return precioBase; }
    public boolean isCancelada() { return cancelada; }
    public void setCancelada(boolean cancelada) { this.cancelada = cancelada; }

    public LocalDateTime getFechaHora() { return LocalDateTime.of(fecha, hora); }

    public boolean estaDisponible(Asiento asiento) {
        return !cancelada && !asientosOcupados.contains(asiento.getCodigo());
    }

    public void marcarOcupado(Asiento asiento) {
        asientosOcupados.add(asiento.getCodigo());
    }

    public void liberarAsiento(Asiento asiento) {
        asientosOcupados.remove(asiento.getCodigo());
    }

    @Override
    public String toString() {
        return pelicula.getTitulo() + " - " + sala.getIdentificador() + " - " + fecha + " " + hora;
    }
}
