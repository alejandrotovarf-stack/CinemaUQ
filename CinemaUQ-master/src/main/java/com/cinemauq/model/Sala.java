package com.cinemauq.model;

import java.util.ArrayList;
import java.util.List;

public class Sala {
    private String identificador;
    private int capacidad;
    private final List<Asiento> asientos = new ArrayList<>();

    public Sala(String identificador, int filas, int columnas) {
        this.identificador = identificador;
        this.capacidad = filas * columnas;
        char filaChar = 'A';
        for (int f = 0; f < filas; f++) {
            for (int c = 1; c <= columnas; c++) {
                asientos.add(new Asiento(String.valueOf(filaChar), c));
            }
            filaChar++;
        }
    }

    public String getIdentificador() { return identificador; }
    public int getCapacidad() { return capacidad; }
    public List<Asiento> getAsientos() { return asientos; }

    @Override
    public String toString() { return "Sala " + identificador + " (" + capacidad + " puestos)"; }
}
