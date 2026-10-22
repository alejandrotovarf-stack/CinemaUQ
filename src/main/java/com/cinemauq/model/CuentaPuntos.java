package com.cinemauq.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CuentaPuntos {
    private final Cliente titular;
    private final List<MovimientoPuntos> movimientos = new ArrayList<>();

    public CuentaPuntos(Cliente titular) {
        this.titular = titular;
    }

    public List<MovimientoPuntos> getMovimientos() { return movimientos; }

    public int getSaldoPuntos() {
        LocalDate hoy = LocalDate.now();
        return movimientos.stream()
                .filter(m -> m.getTipo() == MovimientoPuntos.Tipo.ACUMULACION)
                .filter(m -> !m.estaVencido(hoy))
                .mapToInt(MovimientoPuntos::getCantidad)
                .sum();
    }

    public void acumular(int cantidad, int diasVigencia, Compra origen) {
        if (cantidad <= 0) return;
        movimientos.add(new MovimientoPuntos(MovimientoPuntos.Tipo.ACUMULACION, cantidad, LocalDate.now(), diasVigencia, origen));
    }

    public boolean consumir(int cantidad) {
        if (cantidad > getSaldoPuntos()) return false;
        LocalDate hoy = LocalDate.now();
        List<MovimientoPuntos> lotesOrdenados = new ArrayList<>();
        for (MovimientoPuntos m : movimientos) {
            if (m.getTipo() == MovimientoPuntos.Tipo.ACUMULACION && !m.estaVencido(hoy) && m.getCantidad() > 0) {
                lotesOrdenados.add(m);
            }
        }
        lotesOrdenados.sort(Comparator.comparing(MovimientoPuntos::getFechaVencimiento));

        int restante = cantidad;
        for (MovimientoPuntos lote : lotesOrdenados) {
            if (restante <= 0) break;
            restante -= lote.consumir(restante);
        }
        movimientos.add(new MovimientoPuntos(MovimientoPuntos.Tipo.REDENCION, cantidad, hoy, 0, null));
        return true;
    }

    public void reversarPorCompra(Compra compra) {
        LocalDate hoy = LocalDate.now();
        int totalReversado = 0;
        for (MovimientoPuntos m : movimientos) {
            if (m.getTipo() == MovimientoPuntos.Tipo.ACUMULACION && m.getCompraOrigen() == compra) {
                totalReversado += m.consumir(m.getCantidad());
            }
        }
        if (totalReversado > 0) {
            movimientos.add(new MovimientoPuntos(MovimientoPuntos.Tipo.REVERSION, totalReversado, hoy, 0, compra));
        }
    }
}
