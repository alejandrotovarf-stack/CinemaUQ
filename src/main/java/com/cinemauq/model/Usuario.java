package com.cinemauq.model;

import java.io.Serializable;

public abstract class Usuario implements Serializable {

    protected String identificacion;
    protected String nombre;
    protected String correo;
    protected String clave;

    protected Usuario(String identificacion, String nombre, String correo, String clave) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getClave() { return clave; }

    public boolean validarClave(String claveIngresada) {
        return this.clave != null && this.clave.equals(claveIngresada);
    }

    public abstract String getRol();

    @Override
    public String toString() {
        return getRol() + "{" + identificacion + " - " + nombre + "}";
    }
}
