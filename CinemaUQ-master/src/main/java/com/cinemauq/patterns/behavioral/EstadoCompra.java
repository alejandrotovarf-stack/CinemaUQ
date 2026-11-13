package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;

public interface EstadoCompra {
    void procesar(Compra compra);
    void cancelar(Compra compra);
    String getNombre();
}
