package com.cinemauq.model;

import java.util.Objects;

public class Pelicula {
    private String identificador;
    private String titulo;
    private String genero;
    private int duracionMinutos;
    private String clasificacion;
    private String sinopsis;
    private String urlPoster;

    public Pelicula(String identificador, String titulo, String genero, int duracionMinutos,
                     String clasificacion, String sinopsis, String urlPoster) {
        this.identificador = identificador;
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.clasificacion = clasificacion;
        this.sinopsis = sinopsis;
        this.urlPoster = urlPoster;
    }

    public String getIdentificador() { return identificador; }
    public String getTitulo() { return titulo; }
    public String getGenero() { return genero; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public String getClasificacion() { return clasificacion; }
    public String getSinopsis() { return sinopsis; }
    public String getUrlPoster() { return urlPoster; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pelicula)) return false;
        Pelicula pelicula = (Pelicula) o;
        return Objects.equals(identificador, pelicula.identificador);
    }

    @Override
    public int hashCode() { return Objects.hash(identificador); }

    @Override
    public String toString() { return titulo; }
}
