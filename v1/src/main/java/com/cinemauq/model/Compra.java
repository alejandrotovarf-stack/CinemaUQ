package com.cinemauq.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;







public class Compra {

    private final Cliente cliente;
    private final LocalDateTime fecha;
    private final List<Entrada> entradas;
    private final List<ItemConfiteria> itemsConfiteria;
    private final double subtotal;
    private final double total;
    private final TarjetaVirtual tarjetaUtilizada;
    private EstadoCompra estado;

    
    private Compra(Builder builder) {
        this.cliente = builder.cliente;
        this.entradas = builder.entradas;
        this.itemsConfiteria = builder.itemsConfiteria;
        this.tarjetaUtilizada = builder.tarjeta;
        this.fecha = LocalDateTime.now();
        this.subtotal = calcularSubtotal();
        this.total = subtotal - (subtotal * builder.descuentoPromocion);
        this.estado = EstadoCompra.PENDIENTE;
    }

    private double calcularSubtotal() {
        double total = 0;
        for (Entrada e : entradas) total += e.getPrecioBase();
        for (ItemConfiteria i : itemsConfiteria) total += i.getPrecio();
        return total;
    }

    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public List<Entrada> getEntradas() { return Collections.unmodifiableList(entradas); }
    public List<ItemConfiteria> getItemsConfiteria() { return Collections.unmodifiableList(itemsConfiteria); }
    public double getSubtotal() { return subtotal; }
    public double getTotal() { return total; }
    public TarjetaVirtual getTarjetaUtilizada() { return tarjetaUtilizada; }
    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Compra{cliente=" + cliente.getNombre() + ", entradas=" + entradas.size()
                + ", confiteria=" + itemsConfiteria.size() + ", total=$" + total + ", estado=" + estado + "}";
    }

    
    
    
    




    public static class Builder {
        private Cliente cliente;
        private final List<Entrada> entradas = new ArrayList<>();
        private final List<ItemConfiteria> itemsConfiteria = new ArrayList<>();
        private double descuentoPromocion = 0.0; 
        private TarjetaVirtual tarjeta;

        public Builder conCliente(Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder conEntradas(List<Entrada> entradas) {
            this.entradas.addAll(entradas);
            return this;
        }

        public Builder conEntrada(Entrada entrada) {
            this.entradas.add(entrada);
            return this;
        }

        public Builder conConfiteria(List<ItemConfiteria> items) {
            this.itemsConfiteria.addAll(items);
            return this;
        }

        public Builder conPromocion(double porcentajeDescuento) {
            this.descuentoPromocion = porcentajeDescuento;
            return this;
        }

        public Builder conTarjeta(TarjetaVirtual tarjeta) {
            this.tarjeta = tarjeta;
            return this;
        }

        
        public Compra build() {
            if (cliente == null) {
                throw new IllegalStateException("La compra requiere un cliente.");
            }
            if (entradas.isEmpty()) {
                throw new IllegalStateException("La compra requiere al menos una entrada.");
            }
            return new Compra(this);
        }
    }
}
