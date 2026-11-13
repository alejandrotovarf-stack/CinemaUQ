package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;

public class EstadoPendiente implements EstadoCompra {
    @Override
    public void procesar(Compra compra) {
        compra.cambiarEstado(new EstadoConfirmada());
        compra.notificarObservadores();
    }

    @Override
    public void cancelar(Compra compra) {
        compra.cambiarEstado(new EstadoCancelada());
    }

    @Override
    public String getNombre() { return "PENDIENTE"; }
}
