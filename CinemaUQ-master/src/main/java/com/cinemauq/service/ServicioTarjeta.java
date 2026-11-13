package com.cinemauq.service;

import com.cinemauq.exception.AccesoNoAutorizadoException;
import com.cinemauq.model.Administrador;
import com.cinemauq.model.TarjetaVirtual;
import com.cinemauq.model.Usuario;

public class ServicioTarjeta {

    public void recargar(Usuario solicitante, TarjetaVirtual tarjeta, double valor) {
        if (!(solicitante instanceof Administrador)) {
            throw new AccesoNoAutorizadoException("Solo un administrador puede realizar recargas de tarjeta (RN-05).");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor a recargar debe ser mayor que cero.");
        }
        tarjeta.recargar(valor);
    }
}
