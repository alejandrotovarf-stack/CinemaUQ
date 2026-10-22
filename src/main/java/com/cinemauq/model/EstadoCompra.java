package com.cinemauq.model;

/**
 * Estado del ciclo de vida de una {@link Compra}.
 * Es un simple valor enumerado: las transiciones se validan directamente en {@link Compra}.
 */
public enum EstadoCompra {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA;

    public String getNombre() { return name(); }
}
