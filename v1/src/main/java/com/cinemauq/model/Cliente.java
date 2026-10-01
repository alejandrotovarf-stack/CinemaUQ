package com.cinemauq.model;

import java.util.ArrayList;
import java.util.List;





public class Cliente extends Usuario {

    private TarjetaVirtual tarjetaVirtual;
    private CuentaPuntos cuentaPuntos;
    private final List<Compra> historialCompras = new ArrayList<>();

    public Cliente(String identificacion, String nombre, String correo, String clave) {
        super(identificacion, nombre, correo, clave);
        this.cuentaPuntos = new CuentaPuntos(this);
    }

    public TarjetaVirtual getTarjetaVirtual() { return tarjetaVirtual; }
    public void setTarjetaVirtual(TarjetaVirtual tarjetaVirtual) { this.tarjetaVirtual = tarjetaVirtual; }

    public CuentaPuntos getCuentaPuntos() { return cuentaPuntos; }

    public List<Compra> getHistorialCompras() { return historialCompras; }

    public void registrarCompra(Compra compra) {
        historialCompras.add(compra);
    }

    
    public void consultarCartelera() {
        System.out.println(">> " + nombre + " esta consultando la cartelera disponible.");
    }

    @Override
    public String getRol() { return "CLIENTE"; }
}
