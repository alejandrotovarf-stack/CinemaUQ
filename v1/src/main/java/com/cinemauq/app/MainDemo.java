package com.cinemauq.app;

import com.cinemauq.model.*;
import com.cinemauq.patterns.creational.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;









public class MainDemo {

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println(" CINEMAUQ - ENTREGA PARCIAL (PATRONES CREACIONALES)");
        System.out.println("======================================================\n");

        
        
        
        System.out.println("--- 1) Patron SINGLETON: ConfiguracionCinema ---");
        ConfiguracionCinema config1 = ConfiguracionCinema.getInstancia();
        ConfiguracionCinema config2 = ConfiguracionCinema.getInstancia();
        System.out.println("Reglas de puntos: " + config1.getReglasPuntos());
        System.out.println("¿Misma instancia? " + (config1 == config2));
        System.out.println();

        
        
        
        Cliente cliente = new Cliente("CC-1001", "Laura Gómez", "laura@correo.com", "1234");
        TarjetaVirtual tarjeta = new TarjetaVirtual("TV-0001");
        tarjeta.recargar(100000);
        cliente.setTarjetaVirtual(tarjeta);

        Pelicula pelicula = new Pelicula("P01", "Dune: Parte Tres", "Ciencia Ficción", 165,
                "12+", "La saga continúa en Arrakis.", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba");
        Sala sala = new Sala("Sala 3", 5, 8);
        Funcion funcion = new Funcion("F01", pelicula, sala, LocalDate.now().plusDays(1), LocalTime.of(19, 30), 18000);

        Asiento asientoA1 = sala.getAsientos().get(0);
        Asiento asientoA2 = sala.getAsientos().get(1);
        funcion.marcarOcupado(asientoA1);
        funcion.marcarOcupado(asientoA2);

        Entrada entrada1 = new Entrada(funcion, asientoA1);
        Entrada entrada2 = new Entrada(funcion, asientoA2);

        ProductoConfiteria combo = new ProductoConfiteria("CB01", "Combo Pareja", "2 crispetas + 2 gaseosas", 25000, true);

        
        
        
        System.out.println("--- 2) Patron BUILDER: Compra.Builder ---");
        Compra compra = new Compra.Builder()
                .conCliente(cliente)
                .conEntradas(List.of(entrada1, entrada2))
                .conConfiteria(List.of(combo))
                .conPromocion(0.10) 
                .conTarjeta(tarjeta)
                .build();

        System.out.println(compra);
        System.out.println("Subtotal: $" + compra.getSubtotal());
        System.out.println("Total con promoción aplicada: $" + compra.getTotal());

        boolean pagoExitoso = tarjeta.debitar(compra.getTotal());
        if (pagoExitoso) {
            compra.setEstado(EstadoCompra.CONFIRMADA);
            cliente.registrarCompra(compra);
            int puntosGanados = config1.calcularPuntosGanados(compra.getTotal());
            cliente.getCuentaPuntos().acumular(puntosGanados);
            System.out.println("Pago exitoso. Saldo restante: $" + tarjeta.getSaldo());
            System.out.println("Puntos CinemaUQ acumulados: " + puntosGanados);
        } else {
            System.out.println("Pago rechazado: saldo insuficiente o tarjeta inactiva.");
        }
        System.out.println();

        
        try {
            new Compra.Builder().conCliente(cliente).build();
        } catch (IllegalStateException ex) {
            System.out.println("Validación del Builder funcionando correctamente -> " + ex.getMessage());
        }
        System.out.println();

        
        
        
        System.out.println("--- 3) Patron FACTORY METHOD: GeneradorReporte ---");
        String datosReporte = "Compras del día: 1 | Total vendido: $" + compra.getTotal();

        GeneradorReporte generadorTxt = new GeneradorReporteTXT();
        Reporte reporteTxt = generadorTxt.generar(LocalDate.now().toString(), datosReporte);
        System.out.println(reporteTxt.generarContenido());
        System.out.println();

        GeneradorReporte generadorPdf = new GeneradorReportePDF();
        Reporte reportePdf = generadorPdf.generar(LocalDate.now().toString(), datosReporte);
        System.out.println(reportePdf.generarContenido());

        System.out.println("\n======================================================");
        System.out.println(" FIN DE LA DEMOSTRACIÓN - ENTREGA PARCIAL");
        System.out.println("======================================================");
    }
}
