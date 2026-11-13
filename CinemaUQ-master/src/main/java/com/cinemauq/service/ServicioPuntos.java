package com.cinemauq.service;

import com.cinemauq.exception.PuntosInsuficientesException;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.CuentaPuntos;
import com.cinemauq.patterns.creational.ConfiguracionCinema;

public class ServicioPuntos {

    public int calcularCostoEnPuntos(double valorARedimir) {
        return ConfiguracionCinema.getInstancia().calcularCostoEnPuntos(valorARedimir);
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
