package com.cinemauq.patterns.behavioral;

public interface CompraSubject {
    void adjuntarObservador(ObservadorCompra observador);
    void notificarObservadores();
}
