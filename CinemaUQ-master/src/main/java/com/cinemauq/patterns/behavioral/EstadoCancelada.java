package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;

public class EstadoCancelada implements EstadoCompra {
    @Override
    public void procesar(Compra compra) {
        throw new IllegalStateException("No se puede procesar una compra cancelada.");
    }

    @Override
    public void cancelar(Compra compra) {
        throw new IllegalStateException("La compra ya se encuentra cancelada (protege RN-11).");
    }

    @Override
    public String getNombre() { return "CANCELADA"; }
}
