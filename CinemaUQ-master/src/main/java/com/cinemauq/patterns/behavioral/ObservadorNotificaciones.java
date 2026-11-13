package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;

import java.util.ArrayList;
import java.util.List;

public class ObservadorNotificaciones implements ObservadorCompra {
    private final List<String> bandeja = new ArrayList<>();

    @Override
    public void actualizar(Compra compra) {
        String mensaje = "Tu compra " + compra.getId() + " ahora esta " + compra.getEstado().getNombre();
        bandeja.add(mensaje);
        System.out.println("[ObservadorNotificaciones] " + mensaje);
    }

    public List<String> getBandeja() { return bandeja; }
}
