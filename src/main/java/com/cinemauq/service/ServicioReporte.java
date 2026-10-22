package com.cinemauq.service;

import com.cinemauq.model.Compra;
import com.cinemauq.model.Entrada;
import com.cinemauq.model.Funcion;
import com.cinemauq.model.Reembolso;
import com.cinemauq.patterns.creational.*;
import com.cinemauq.patterns.structural.AdaptadorPDF;
import com.cinemauq.patterns.structural.AdaptadorTXT;
import com.cinemauq.patterns.structural.ExportadorReporte;
import com.cinemauq.patterns.structural.ReporteGrafico;
import com.cinemauq.repository.DataStore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ServicioReporte {

    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";

    private final DataStore dataStore;

    public ServicioReporte(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public enum TipoReporte { VENTAS, OCUPACION, CONFITERIA, CANCELACIONES, PUNTOS }

    public record OcupacionFuncion(Funcion funcion, int capacidad, int vendidos, int disponibles, double porcentaje) {}

    public record PuntoGrafico(String etiqueta, double valor, String textoValor) {}

    private boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private boolean esConfirmada(Compra compra) {
        return ESTADO_CONFIRMADA.equals(compra.getEstado().getNombre());
    }

    private List<Compra> comprasEnRango(LocalDate desde, LocalDate hasta) {
        return dataStore.getCompras().stream()
                .filter(c -> enRango(c.getFecha().toLocalDate(), desde, hasta))
                .toList();
    }

    private List<Reembolso> reembolsosEnRango(LocalDate desde, LocalDate hasta) {
        return dataStore.getReembolsos().stream()
                .filter(r -> enRango(r.getFecha().toLocalDate(), desde, hasta))
                .toList();
    }

    public List<OcupacionFuncion> calcularOcupacion(LocalDate desde, LocalDate hasta) {
        Map<String, Set<String>> asientosVendidos = new HashMap<>();
        for (Compra compra : dataStore.getCompras()) {
            if (!esConfirmada(compra)) continue;
            for (Entrada entrada : compra.getEntradas()) {
                asientosVendidos
                        .computeIfAbsent(entrada.getFuncion().getIdentificador(), k -> new HashSet<>())
                        .add(entrada.getAsiento().getCodigo());
            }
        }

        List<Funcion> funciones = dataStore.getFunciones().stream()
                .filter(f -> enRango(f.getFecha(), desde, hasta))
                .sorted(Comparator.comparing(Funcion::getFechaHora).thenComparing(Funcion::getIdentificador))
                .toList();

        List<OcupacionFuncion> resultado = new ArrayList<>();
        for (Funcion f : funciones) {
            int capacidad = f.getSala().getCapacidad();
            int vendidos = asientosVendidos.getOrDefault(f.getIdentificador(), Set.of()).size();
            int disponibles = f.isCancelada() ? 0 : Math.max(0, capacidad - vendidos);
            double porcentaje = capacidad > 0 ? vendidos * 100.0 / capacidad : 0.0;
            resultado.add(new OcupacionFuncion(f, capacidad, vendidos, disponibles, porcentaje));
        }
        return resultado;
    }

    public String construirDatos(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder();
        List<Compra> comprasFiltradas = comprasEnRango(desde, hasta);

        switch (tipo) {
            case VENTAS -> {
                double total = 0;
                sb.append("Reporte de Ventas\n");
                if (comprasFiltradas.isEmpty()) {
                    sb.append("No hay compras registradas en el periodo seleccionado.\n");
                }
                for (Compra c : comprasFiltradas) {
                    sb.append(String.format("%s | %s | %s | $%.0f | %s%n",
                            c.getId(), c.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            c.getCliente().getNombre(), c.getTotal(), c.getEstado().getNombre()));
                    if (esConfirmada(c)) total += c.getTotal();
                }
                sb.append(String.format("%nTOTAL VENDIDO (confirmadas): $%.0f%n", total));
                sb.append("Numero de compras en el periodo: ").append(comprasFiltradas.size());
            }
            case OCUPACION -> {
                sb.append("Reporte de Ocupacion de Funciones\n");
                List<OcupacionFuncion> ocupaciones = calcularOcupacion(desde, hasta);
                if (ocupaciones.isEmpty()) {
                    sb.append("No hay funciones programadas en el periodo seleccionado.");
                } else {
                    int totalVendidos = 0;
                    int totalCapacidad = 0;
                    for (OcupacionFuncion o : ocupaciones) {
                        Funcion f = o.funcion();
                        sb.append(String.format("%s | %s | %s %s | %s | Capacidad: %d | Vendidos: %d | Disponibles: %d | Ocupacion: %.1f%%%s%n",
                                f.getIdentificador(), f.getPelicula().getTitulo(), f.getFecha(), f.getHora(),
                                f.getSala().getIdentificador(), o.capacidad(), o.vendidos(), o.disponibles(), o.porcentaje(),
                                f.isCancelada() ? " | FUNCION CANCELADA" : ""));
                        if (!f.isCancelada()) {
                            totalVendidos += o.vendidos();
                            totalCapacidad += o.capacidad();
                        }
                    }
                    double global = totalCapacidad > 0 ? totalVendidos * 100.0 / totalCapacidad : 0.0;
                    sb.append(String.format("%nFunciones en el periodo: %d | Asientos vendidos: %d de %d (funciones activas) | Ocupacion global: %.1f%%",
                            ocupaciones.size(), totalVendidos, totalCapacidad, global));
                }
            }
            case CONFITERIA -> {
                sb.append("Reporte de Confiteria / Combos vendidos (compras confirmadas)\n");
                int totalItems = 0;
                double totalValor = 0;
                for (Compra c : comprasFiltradas) {
                    if (!esConfirmada(c)) continue;
                    for (var item : c.getItemsConfiteria()) {
                        sb.append(String.format("%s | %s | $%.0f%n", c.getId(), item.getNombre(), item.getPrecio()));
                        totalItems++;
                        totalValor += item.getPrecio();
                    }
                }
                if (totalItems == 0) {
                    sb.append("No hay ventas de confiteria en el periodo seleccionado.\n");
                }
                sb.append(String.format("%nTotal items vendidos: %d | Valor total: $%.0f", totalItems, totalValor));
            }
            case CANCELACIONES -> {
                sb.append("Reporte de Cancelaciones y Reembolsos\n");
                double totalDevuelto = 0;
                List<Reembolso> reembolsos = reembolsosEnRango(desde, hasta);
                if (reembolsos.isEmpty()) {
                    sb.append("No hay cancelaciones ni reembolsos en el periodo seleccionado.\n");
                }
                for (Reembolso r : reembolsos) {
                    sb.append(r.toString()).append("\n");
                    totalDevuelto += r.getValorDevuelto();
                }
                sb.append(String.format("%nTotal devuelto: $%.0f", totalDevuelto));
            }
            case PUNTOS -> {
                sb.append("Reporte de Puntos CinemaUQ\n");
                if (dataStore.getClientes().isEmpty()) {
                    sb.append("No hay clientes registrados.");
                }
                dataStore.getClientes().forEach(cl ->
                        sb.append(String.format("%s | Saldo actual: %d pts%n", cl.getNombre(), cl.getCuentaPuntos().getSaldoPuntos())));
            }
        }
        return sb.toString();
    }

    public List<PuntoGrafico> construirSerie(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        List<PuntoGrafico> puntos = new ArrayList<>();
        Set<String> usadas = new HashSet<>();

        switch (tipo) {
            case VENTAS -> {
                TreeMap<LocalDate, Double> porDia = new TreeMap<>();
                for (Compra c : comprasEnRango(desde, hasta)) {
                    if (esConfirmada(c)) porDia.merge(c.getFecha().toLocalDate(), c.getTotal(), Double::sum);
                }
                DateTimeFormatter fmt = formatoDia(desde, hasta);
                porDia.forEach((dia, valor) ->
                        puntos.add(new PuntoGrafico(etiquetaUnica(usadas, dia.format(fmt)), valor, dinero(valor))));
            }
            case OCUPACION -> {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                for (OcupacionFuncion o : calcularOcupacion(desde, hasta)) {
                    Funcion f = o.funcion();
                    String etiqueta = f.getIdentificador() + " · " + abreviar(f.getPelicula().getTitulo(), 24)
                            + " · " + f.getFecha().format(fmt) + " " + f.getHora();
                    String texto = f.isCancelada()
                            ? "Cancelada"
                            : String.format("%d de %d (%.0f%%)", o.vendidos(), o.capacidad(), o.porcentaje());
                    puntos.add(new PuntoGrafico(etiquetaUnica(usadas, etiqueta), o.vendidos(), texto));
                }
            }
            case CONFITERIA -> {
                TreeMap<String, Integer> porProducto = new TreeMap<>();
                for (Compra c : comprasEnRango(desde, hasta)) {
                    if (!esConfirmada(c)) continue;
                    c.getItemsConfiteria().forEach(item -> porProducto.merge(item.getNombre(), 1, Integer::sum));
                }
                porProducto.forEach((nombre, unidades) ->
                        puntos.add(new PuntoGrafico(etiquetaUnica(usadas, nombre), unidades, unidades + " und.")));
            }
            case CANCELACIONES -> {
                TreeMap<LocalDate, Double> porDia = new TreeMap<>();
                for (Reembolso r : reembolsosEnRango(desde, hasta)) {
                    porDia.merge(r.getFecha().toLocalDate(), r.getValorDevuelto(), Double::sum);
                }
                DateTimeFormatter fmt = formatoDia(desde, hasta);
                porDia.forEach((dia, valor) ->
                        puntos.add(new PuntoGrafico(etiquetaUnica(usadas, dia.format(fmt)), valor, dinero(valor))));
            }
            case PUNTOS -> {
                dataStore.getClientes().forEach(cl -> {
                    int saldo = cl.getCuentaPuntos().getSaldoPuntos();
                    puntos.add(new PuntoGrafico(etiquetaUnica(usadas, cl.getNombre()), saldo, saldo + " pts"));
                });
            }
        }
        return puntos;
    }

    private DateTimeFormatter formatoDia(LocalDate desde, LocalDate hasta) {
        return DateTimeFormatter.ofPattern(desde.getYear() == hasta.getYear() ? "dd/MM" : "dd/MM/yy");
    }

    private String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }

    private String abreviar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1).trim() + "…";
    }

    private String etiquetaUnica(Set<String> usadas, String base) {
        String etiqueta = base;
        int n = 2;
        while (!usadas.add(etiqueta)) {
            etiqueta = base + " (" + n++ + ")";
        }
        return etiqueta;
    }

    private String tituloReporte(TipoReporte tipo) {
        return switch (tipo) {
            case VENTAS -> "Ventas generales";
            case OCUPACION -> "Ocupación de salas";
            case CONFITERIA -> "Ventas de confitería";
            case CANCELACIONES -> "Cancelaciones y reembolsos";
            case PUNTOS -> "Puntos CinemaUQ";
        };
    }

    private String tituloGrafico(TipoReporte tipo) {
        return switch (tipo) {
            case VENTAS -> "Ventas confirmadas por día";
            case OCUPACION -> "Ocupación por función (asientos vendidos)";
            case CONFITERIA -> "Unidades vendidas por producto/combo";
            case CANCELACIONES -> "Valor reembolsado por día";
            case PUNTOS -> "Saldo de puntos vigente por cliente";
        };
    }

    private String ejeGrafico(TipoReporte tipo) {
        return switch (tipo) {
            case VENTAS -> "Ventas ($)";
            case OCUPACION -> "Asientos vendidos";
            case CONFITERIA -> "Unidades vendidas";
            case CANCELACIONES -> "Valor reembolsado ($)";
            case PUNTOS -> "Puntos";
        };
    }

    private boolean esMonetario(TipoReporte tipo) {
        return tipo == TipoReporte.VENTAS || tipo == TipoReporte.CANCELACIONES;
    }

    private List<ReporteGrafico.Indicador> construirIndicadores(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        List<ReporteGrafico.Indicador> ind = new ArrayList<>();
        switch (tipo) {
            case VENTAS -> {
                List<Compra> compras = comprasEnRango(desde, hasta);
                double total = 0;
                int confirmadas = 0;
                for (Compra c : compras) {
                    if (esConfirmada(c)) {
                        total += c.getTotal();
                        confirmadas++;
                    }
                }
                ind.add(new ReporteGrafico.Indicador("Total vendido (confirmadas)", dinero(total)));
                ind.add(new ReporteGrafico.Indicador("Compras en el periodo", String.valueOf(compras.size())));
                ind.add(new ReporteGrafico.Indicador("Compras confirmadas", String.valueOf(confirmadas)));
            }
            case OCUPACION -> {
                List<OcupacionFuncion> ocupaciones = calcularOcupacion(desde, hasta);
                int vendidos = 0;
                int capacidad = 0;
                for (OcupacionFuncion o : ocupaciones) {
                    if (!o.funcion().isCancelada()) {
                        vendidos += o.vendidos();
                        capacidad += o.capacidad();
                    }
                }
                double global = capacidad > 0 ? vendidos * 100.0 / capacidad : 0.0;
                ind.add(new ReporteGrafico.Indicador("Funciones en el periodo", String.valueOf(ocupaciones.size())));
                ind.add(new ReporteGrafico.Indicador("Asientos vendidos", String.valueOf(vendidos)));
                ind.add(new ReporteGrafico.Indicador("Capacidad (funciones activas)", String.valueOf(capacidad)));
                ind.add(new ReporteGrafico.Indicador("Ocupación global", String.format("%.1f%%", global)));
            }
            case CONFITERIA -> {
                int items = 0;
                double valor = 0;
                for (Compra c : comprasEnRango(desde, hasta)) {
                    if (!esConfirmada(c)) continue;
                    for (var item : c.getItemsConfiteria()) {
                        items++;
                        valor += item.getPrecio();
                    }
                }
                ind.add(new ReporteGrafico.Indicador("Ítems vendidos", String.valueOf(items)));
                ind.add(new ReporteGrafico.Indicador("Valor total", dinero(valor)));
            }
            case CANCELACIONES -> {
                List<Reembolso> reembolsos = reembolsosEnRango(desde, hasta);
                double devuelto = 0;
                for (Reembolso r : reembolsos) devuelto += r.getValorDevuelto();
                ind.add(new ReporteGrafico.Indicador("Reembolsos en el periodo", String.valueOf(reembolsos.size())));
                ind.add(new ReporteGrafico.Indicador("Total devuelto", dinero(devuelto)));
            }
            case PUNTOS -> {
                long totalPuntos = 0;
                for (var cl : dataStore.getClientes()) totalPuntos += cl.getCuentaPuntos().getSaldoPuntos();
                ind.add(new ReporteGrafico.Indicador("Clientes registrados", String.valueOf(dataStore.getClientes().size())));
                ind.add(new ReporteGrafico.Indicador("Puntos vigentes (total)", totalPuntos + " pts"));
            }
        }
        return ind;
    }

    private String formatoValorSerie(TipoReporte tipo, double valor) {
        return String.format(esMonetario(tipo) ? "$%,.0f" : "%,.0f", valor);
    }

    private String seccionTextoIndicadores(List<ReporteGrafico.Indicador> indicadores) {
        StringBuilder sb = new StringBuilder();
        sb.append("INDICADORES\n-----------\n");
        for (ReporteGrafico.Indicador i : indicadores) {
            sb.append(i.nombre()).append(": ").append(i.valor()).append("\n");
        }
        return sb.toString();
    }

    private String seccionTextoSerie(TipoReporte tipo, List<PuntoGrafico> puntos) {
        StringBuilder sb = new StringBuilder();
        String titulo = "DATOS DEL GRÁFICO: " + tituloGrafico(tipo);
        sb.append(titulo).append("\n").append("-".repeat(titulo.length())).append("\n");
        if (puntos.isEmpty()) {
            sb.append("No hay datos para el periodo seleccionado.\n");
            return sb.toString();
        }
        int anchoEtiqueta = "Categoría".length();
        int anchoValor = ejeGrafico(tipo).length();
        double total = 0;
        for (PuntoGrafico p : puntos) {
            anchoEtiqueta = Math.max(anchoEtiqueta, p.etiqueta().length());
            anchoValor = Math.max(anchoValor, formatoValorSerie(tipo, p.valor()).length());
            total += p.valor();
        }
        anchoValor = Math.max(anchoValor, formatoValorSerie(tipo, total).length());
        String formato = "%-" + anchoEtiqueta + "s  %" + anchoValor + "s  %s%n";
        sb.append(String.format(formato, "Categoría", ejeGrafico(tipo), "Detalle"));
        sb.append("-".repeat(anchoEtiqueta + anchoValor + 12)).append("\n");
        for (PuntoGrafico p : puntos) {
            sb.append(String.format(formato, p.etiqueta(), formatoValorSerie(tipo, p.valor()), p.textoValor()));
        }
        sb.append("-".repeat(anchoEtiqueta + anchoValor + 12)).append("\n");
        sb.append(String.format(formato, "TOTAL", formatoValorSerie(tipo, total), ""));
        return sb.toString();
    }

    public String generarYExportar(TipoReporte tipo, LocalDate desde, LocalDate hasta, boolean formatoPDF) {
        String datos = construirDatos(tipo, desde, hasta).replace("\r\n", "\n");
        String periodo = desde + " a " + hasta;
        List<PuntoGrafico> serie = construirSerie(tipo, desde, hasta);
        List<ReporteGrafico.Indicador> indicadores = construirIndicadores(tipo, desde, hasta);

        GeneradorReporte generador = formatoPDF ? new GeneradorReportePDF() : new GeneradorReporteTXT();
        String contenidoReporte = formatoPDF
                ? datos
                : datos + "\n\n" + seccionTextoIndicadores(indicadores).replace("\r\n", "\n")
                        + "\n" + seccionTextoSerie(tipo, serie).replace("\r\n", "\n");
        Reporte reporte = generador.generar(periodo, contenidoReporte);

        ExportadorReporte exportador = formatoPDF ? new AdaptadorPDF() : new AdaptadorTXT();
        String nombreArchivo = tipo.name().toLowerCase() + "_" + LocalDate.now();

        if (!formatoPDF) {
            return exportador.exportar(nombreArchivo, reporte.generarContenido());
        }

        List<ReporteGrafico.Punto> puntos = new ArrayList<>();
        for (PuntoGrafico p : serie) puntos.add(new ReporteGrafico.Punto(p.etiqueta(), p.valor(), p.textoValor()));
        ReporteGrafico grafico = new ReporteGrafico(
                tituloReporte(tipo), reporte.getPeriodo(),
                reporte.getFechaGeneracion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                indicadores, tituloGrafico(tipo), ejeGrafico(tipo), esMonetario(tipo), puntos, reporte.getDatos());
        return exportador.exportar(nombreArchivo, reporte.generarContenido(), grafico);
    }
}
