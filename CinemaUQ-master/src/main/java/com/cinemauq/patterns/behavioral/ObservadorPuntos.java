package com.cinemauq.patterns.behavioral;

import com.cinemauq.model.Compra;
import com.cinemauq.patterns.creational.ConfiguracionCinema;

public class ObservadorPuntos implements ObservadorCompra {
    @Override
    public void actualizar(Compra compra) {
        if (!"CONFIRMADA".equals(compra.getEstado().getNombre())) return;
        ConfiguracionCinema config = ConfiguracionCinema.getInstancia();
        int puntos = config.calcularPuntosGanados(compra.getTotal());
        compra.getCliente().getCuentaPuntos().acumular(puntos, config.getDiasVigenciaPuntos(), compra);
        System.out.println("[ObservadorPuntos] +" + puntos + " puntos para " + compra.getCliente().getNombre());
    }
}
