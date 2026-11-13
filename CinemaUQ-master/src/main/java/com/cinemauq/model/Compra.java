package com.cinemauq.model;

import com.cinemauq.patterns.behavioral.CompraSubject;
import com.cinemauq.patterns.behavioral.EstadoCompra;
import com.cinemauq.patterns.behavioral.EstadoPendiente;
import com.cinemauq.patterns.behavioral.ObservadorCompra;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Compra implements CompraSubject {

    private final String id;
    private final Cliente cliente;
    private final LocalDateTime fecha;
    private final List<Entrada> entradas;
    private final List<ItemConfiteria> itemsConfiteria;
    private final Promocion promocionAplicada;
    private final double subtotal;
    private final double total;
    private final TarjetaVirtual tarjetaUtilizada;
    private EstadoCompra estado;
    private final List<ObservadorCompra> observadores = new ArrayList<>();

    private Compra(Builder builder) {
        this.id = "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.cliente = builder.cliente;
        this.entradas = builder.entradas;
        this.itemsConfiteria = builder.itemsConfiteria;
        this.tarjetaUtilizada = builder.tarjeta;
        this.fecha = LocalDateTime.now();

        double sub = 0;
        for (Entrada e : entradas) sub += e.getPrecioBase();
        for (ItemConfiteria i : itemsConfiteria) sub += i.getPrecio();
        this.subtotal = sub;

        PrecioCalculable calculable = new PrecioBase(sub);
        if (builder.nombrePromocion != null && builder.porcentajeDescuento > 0) {
            this.promocionAplicada = new Promocion(calculable, builder.nombrePromocion, builder.porcentajeDescuento);
            calculable = this.promocionAplicada;
        } else {
            this.promocionAplicada = null;
        }
        this.total = calculable.calcularPrecio();
        this.estado = new EstadoPendiente();
    }

    public String getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public List<Entrada> getEntradas() { return Collections.unmodifiableList(entradas); }
    public List<ItemConfiteria> getItemsConfiteria() { return Collections.unmodifiableList(itemsConfiteria); }
    public Promocion getPromocionAplicada() { return promocionAplicada; }
    public double getSubtotal() { return subtotal; }
    public double getTotal() { return total; }
    public TarjetaVirtual getTarjetaUtilizada() { return tarjetaUtilizada; }
    public EstadoCompra getEstado() { return estado; }

    public void cambiarEstado(EstadoCompra nuevoEstado) { this.estado = nuevoEstado; }

    public void confirmar() { estado.procesar(this); }
    public void cancelarCompra() { estado.cancelar(this); }

    @Override
    public void adjuntarObservador(ObservadorCompra observador) { observadores.add(observador); }

    @Override
    public void notificarObservadores() {
        for (ObservadorCompra obs : observadores) obs.actualizar(this);
    }

    @Override
    public String toString() {
        return id + " | " + cliente.getNombre() + " | " + entradas.size() + " entrada(s) | total=$" + String.format("%.0f", total) + " | " + estado.getNombre();
    }

    public static class Builder {
        private Cliente cliente;
        private final List<Entrada> entradas = new ArrayList<>();
        private final List<ItemConfiteria> itemsConfiteria = new ArrayList<>();
        private String nombrePromocion;
        private double porcentajeDescuento = 0.0;
        private TarjetaVirtual tarjeta;

        public Builder conCliente(Cliente cliente) { this.cliente = cliente; return this; }

        public Builder conEntradas(List<Entrada> entradas) { this.entradas.addAll(entradas); return this; }

        public Builder conConfiteria(List<ItemConfiteria> items) { this.itemsConfiteria.addAll(items); return this; }

        public Builder conPromocion(String nombre, double porcentajeDescuento) {
            this.nombrePromocion = nombre;
            this.porcentajeDescuento = porcentajeDescuento;
            return this;
        }

        public Builder conTarjeta(TarjetaVirtual tarjeta) { this.tarjeta = tarjeta; return this; }

        public Compra build() {
            if (cliente == null) throw new IllegalStateException("La compra requiere un cliente.");
            if (entradas.isEmpty()) throw new IllegalStateException("La compra requiere al menos una entrada.");
            if (tarjeta == null) throw new IllegalStateException("La compra requiere una tarjeta virtual.");
            return new Compra(this);
        }
    }
}
