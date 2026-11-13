package com.cinemauq.service;

import com.cinemauq.model.Usuario;

public class GestorSesion {
    private Usuario usuarioActual;

    public void iniciarSesion(Usuario usuario) { this.usuarioActual = usuario; }
    public void cerrarSesion() { this.usuarioActual = null; }
    public Usuario getUsuarioActual() { return usuarioActual; }
    public boolean haySesionActiva() { return usuarioActual != null; }
    public boolean esAdministrador() { return usuarioActual != null && "ADMINISTRADOR".equals(usuarioActual.getRol()); }
    public boolean esCliente() { return usuarioActual != null && "CLIENTE".equals(usuarioActual.getRol()); }
}
