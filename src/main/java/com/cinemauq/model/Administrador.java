package com.cinemauq.model;

public class Administrador extends Usuario {

    private String codigoEmpleado;

    public Administrador(String identificacion, String nombre, String correo, String clave, String codigoEmpleado) {
        super(identificacion, nombre, correo, clave);
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getCodigoEmpleado() { return codigoEmpleado; }

    @Override
    public String getRol() { return "ADMINISTRADOR"; }
}
