package com.cinemauq.service;

import com.cinemauq.exception.PuntosInsuficientesException;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.Compra;
import com.cinemauq.model.CuentaPuntos;
import com.cinemauq.model.EstadoCompra;
import com.cinemauq.patterns.creational.ConfiguracionCinema;

public class ServicioPuntos {

    public int calcularCostoEnPuntos(double valorARedimir) {
        return ConfiguracionCinema.getInstancia().calcularCostoEnPuntos(valorARedimir);
    }

    /** Acumula en la cuenta del cliente los puntos ganados por una compra confirmada. */
    public void acumularPorCompra(Compra compra) {
        if (compra.getEstado() != EstadoCompra.CONFIRMADA) return;
        ConfiguracionCinema config = ConfiguracionCinema.getInstancia();
        int puntos = config.calcularPuntosGanados(compra.getTotal());
        compra.getCliente().getCuentaPuntos().acumular(puntos, config.getDiasVigenciaPuntos(), compra);
        System.out.println("[ServicioPuntos] +" + puntos + " puntos para " + compra.getCliente().getNombre());
    }

    public void redimir(Cliente cliente, double valorARedimir) {
        CuentaPuntos cuenta = cliente.getCuentaPuntos();
        int costo = calcularCostoEnPuntos(valorARedimir);
        if (cuenta.getSaldoPuntos() < costo) {
            throw new PuntosInsuficientesException("Saldo de puntos insuficiente. Se requieren " + costo + " puntos.");
        }
        boolean ok = cuenta.consumir(costo);
        if (!ok) {
            throw new PuntosInsuficientesException("No fue posible completar la redencion de puntos.");
        }
    }
}
