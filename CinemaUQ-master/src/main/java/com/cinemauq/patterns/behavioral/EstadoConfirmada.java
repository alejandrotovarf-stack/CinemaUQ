package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;

public class EstadoConfirmada implements EstadoCompra {
    @Override
    public void procesar(Compra compra) {
        System.out.println("La compra " + compra.getId() + " ya se encuentra confirmada.");
    }

    @Override
    public void cancelar(Compra compra) {
        compra.cambiarEstado(new EstadoCancelada());
    }

    @Override
    public String getNombre() { return "CONFIRMADA"; }
}
