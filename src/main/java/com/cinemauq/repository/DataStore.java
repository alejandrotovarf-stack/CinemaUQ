package com.cinemauq.repository;

import com.cinemauq.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    private final List<Pelicula> peliculas = new ArrayList<>();
    private final List<Sala> salas = new ArrayList<>();
    private final List<Funcion> funciones = new ArrayList<>();
    private final List<ProductoConfiteria> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Administrador> administradores = new ArrayList<>();
    private final List<Compra> compras = new ArrayList<>();
    private final List<Reembolso> reembolsos = new ArrayList<>();

    public DataStore() {
        seed();
    }

    private void seed() {
        Pelicula p1 = new Pelicula("P01", "Intersellar", "Ciencia Ficción", 209, "5+",
                "Una tripulación descubre una señal que podría cambiar el destino de la humanidad.",
                "https://resizing.flixster.com/-XZAfHZM39UwaGJIFWKAE8fS0ak=/v3/t/assets/p10543523_p_v8_as.jpg");
        Pelicula p2 = new Pelicula("P02", "Un sueño posible", "Deporte/Drama", 129, "T",
                "Una familia adopta a un joven de las calles y lo ayuda a aprovechar todo su potencial como jugador de fútbol americano. ",
                "https://m.media-amazon.com/images/S/pv-target-images/e56b1d15eb0162016caefeb57ea589386c8900a290ccc9eab5d22a525251ec57._UR2000,3000_.jpg");
        Pelicula p3 = new Pelicula("P03", "Son como niños", "Comedia", 102, "T",
                "Un grupo de amigos y excompañeros descubren que envejecer no siempre significa madurar cuando se reúnen para honrar la memoria de su entrenador de baloncesto.",
                "https://m.media-amazon.com/images/S/pv-target-images/7b86f2a7e80941d97c265348873a82c73450c0286ed1d1eeb7d86440ec707de5.jpg");
        Pelicula p4 = new Pelicula("P04", "Spider-Man: Brand New Day", "Accion", 150, "T",
                "Un joven descubre que puede comunicarse con criaturas legendarias.",
                "https://m.media-amazon.com/images/M/MV5BZjJhYjA3NzMtMWExNC00OTMwLWIyY2YtOGIzODhlYWI0ZWY1XkEyXkFqcGc@._V1_.jpg");
        Pelicula p5 = new Pelicula("P05", "Focus", "Acción", 105, "15+",
                "Un estafador veterano apoya a una joven y atractiva mujer",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQtbwTumQkXy0z_zynQZnqXUsjDde9IE95pTn01Iq8Ox0wliX6TdWy-uWsA&s=10");
        Pelicula p6 = new Pelicula("P06","El lobo de wall street","Comedio/Suspenso",180,"15+",
                "El corredor de bolsa neoyorquino Jordan Belfort, que fundó la empresa de corretaje Stratton Oakmont cuando aún no había cumplido los 20 años, adquiere hábitos de excesos y corrupción.",
        "https://es.web.img3.acsta.net/pictures/210/615/21061596_20131129121836144.jpg");
        Pelicula p7 = new Pelicula("P07","La bella y la bestia","Animado",84,"T",
                "Una doncella francesa toma el lugar de su padre secuestrado en el castillo encantado de un príncipe embrujado",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQSZbFxBUO2GFk8MdCjswMFEkaqlHqRygOZajfnGiymQ&s=10");
        Pelicula p8 = new Pelicula("P08","Jorge el curioso","Infantil/Aventura",86,"T",
                "El Hombre del Sombrero Amarillo intenta ayudar a un mono curioso a salir de sus problemas luego de una serie de contratiempos.",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTLvbzt8vga2MjeGv7GlPYuTjBaKEhM4S-LMgEY-oqgDQ&s=10");
        peliculas.add(p1); peliculas.add(p2); peliculas.add(p3); peliculas.add(p4); peliculas.add(p5); peliculas.add(p6); peliculas.add(p7); peliculas.add(p8);

        Sala s1 = new Sala("Sala 1", 6, 10);
        Sala s2 = new Sala("Sala 2", 5, 8);
        Sala s3 = new Sala("Sala 3 VIP", 4, 6);
        Sala s4 = new Sala("Sala 4 Pro",10,10);
        salas.add(s1); salas.add(s2); salas.add(s3); salas.add(s4);

        int idFuncion = 1;
        LocalTime[] horarios = { LocalTime.of(14, 30), LocalTime.of(17, 0), LocalTime.of(20, 0) };
        Pelicula[] cartelera = { p1, p2, p3, p4, p5, p6, p7, p8 };
        Sala[] todasLasSalas = { s1, s2, s3, s4 };
        for (int dia = 0; dia < 3; dia++) {
            for (int i = 0; i < cartelera.length; i++) {
                Funcion f = new Funcion("F" + String.format("%03d", idFuncion++),
                        cartelera[i], todasLasSalas[i % todasLasSalas.length],
                        LocalDate.now().plusDays(dia), horarios[i % horarios.length],
                        i == 4 ? 22000 : (dia == 2 ? 20000 : 16000));
                funciones.add(f);
            }
        }

        productos.add(new ProductoConfiteria("PR01", "Crispetas Grandes", "Maíz pira con mantequilla", 12000, true));
        productos.add(new ProductoConfiteria("PR02", "Gaseosa Grande", "500 ml a elección", 8000, true));
        productos.add(new ProductoConfiteria("PR03", "Perro Caliente", "Clásico con papas fritas", 14000, true));
        productos.add(new ProductoConfiteria("PR04", "Nachos con Queso", "Con queso cheddar derretido", 13000, true));
        productos.add(new ProductoConfiteria("PR05", "Chocolatina", "Snack dulce", 5000, true));
        productos.add(new ProductoConfiteria("PR06","Salchipapas","Deliciosas salchipapas,con queso cheddar derretido por encima",20000,true));
        productos.add(new ProductoConfiteria("PR07","Gomitas","Gomitas de gusanitos",7000,true));
        productos.add(new ProductoConfiteria("PR08","Pizza","Pizza de peperoni",22000,true));
        productos.add(new ProductoConfiteria("PR09","Ice","granizado tradicional",12000,true));


        Combo comboPareja = new Combo("Combo Pareja", 5000);
        comboPareja.agregarItem(productos.get(0));
        comboPareja.agregarItem(productos.get(1));
        comboPareja.agregarItem(productos.get(1));
        combos.add(comboPareja);

        Combo comboFamiliar = new Combo("Combo Familiar", 9000);
        comboFamiliar.agregarItem(productos.get(0));
        comboFamiliar.agregarItem(productos.get(0));
        comboFamiliar.agregarItem(productos.get(1));
        comboFamiliar.agregarItem(productos.get(1));
        comboFamiliar.agregarItem(productos.get(2));
        combos.add(comboFamiliar);

        Combo comboParceros = new Combo("Combo Parceros",10000);
        comboParceros.agregarItem(productos.get(0));
        comboParceros.agregarItem(productos.get(1));
        comboParceros.agregarItem(productos.get(1));
        comboParceros.agregarItem(productos.get(5));
        comboParceros.agregarItem(productos.get(5));
        combos.add(comboParceros);


        Combo comboKids = new Combo("Combo Kids", 10000);
        comboKids.agregarItem(productos.get(2));
        comboKids.agregarItem(productos.get(4));
        comboKids.agregarItem(productos.get(6));
        comboKids.agregarItem(productos.get(8));
        combos.add(comboKids);






        Cliente clienteDemo = new Cliente("CC-1001", "Emiliano Tovar", "emiliano@cinemauq.com", "2014");
        TarjetaVirtual tarjetaDemo = new TarjetaVirtual("TV-0001");
        tarjetaDemo.recargar(15000000);
        clienteDemo.setTarjetaVirtual(tarjetaDemo);
        clientes.add(clienteDemo);

        Cliente clienteDemo2 = new Cliente("CC-1002", "Alejandro Tovar", "alejandro@cinemauq.com", "2008");
        TarjetaVirtual tarjetaDemo2 = new TarjetaVirtual("TV-0002");
        tarjetaDemo2.recargar(5000000);
        clienteDemo2.setTarjetaVirtual(tarjetaDemo2);
        clientes.add(clienteDemo2);

        administradores.add(new Administrador("CC-9001", "Maria Jose Gutierrez", "majito.admin@cinemauq.com", "2010", "EMP-01"));
    }

    public List<Pelicula> getPeliculas() { return peliculas; }
    public List<Sala> getSalas() { return salas; }
    public List<Funcion> getFunciones() { return funciones; }
    public List<ProductoConfiteria> getProductos() { return productos; }
    public List<Combo> getCombos() { return combos; }
    public List<Cliente> getClientes() { return clientes; }
    public List<Administrador> getAdministradores() { return administradores; }
    public List<Compra> getCompras() { return compras; }
    public List<Reembolso> getReembolsos() { return reembolsos; }

    public List<Funcion> getFuncionesDePelicula(Pelicula pelicula) {
        List<Funcion> resultado = new ArrayList<>();
        for (Funcion f : funciones) {
            if (f.getPelicula().equals(pelicula) && !f.isCancelada()) resultado.add(f);
        }
        return resultado;
    }

    public Cliente autenticarCliente(String correo, String clave) {
        for (Cliente c : clientes) {
            if (c.getCorreo().equalsIgnoreCase(correo) && c.validarClave(clave)) return c;
        }
        return null;
    }

    public Administrador autenticarAdministrador(String correo, String clave) {
        for (Administrador a : administradores) {
            if (a.getCorreo().equalsIgnoreCase(correo) && a.validarClave(clave)) return a;
        }
        return null;
    }

    public boolean existeCorreo(String correo) {
        for (Cliente c : clientes) if (c.getCorreo().equalsIgnoreCase(correo)) return true;
        for (Administrador a : administradores) if (a.getCorreo().equalsIgnoreCase(correo)) return true;
        return false;
    }

    public void registrarCompra(Compra compra) {
        compras.add(compra);
    }

    public void registrarReembolso(Reembolso reembolso) {
        reembolsos.add(reembolso);
    }

    public void registrarCliente(Cliente cliente, double saldoInicial) {
        TarjetaVirtual tarjeta = new TarjetaVirtual("TV-" + String.format("%04d", clientes.size() + 1));
        if (saldoInicial > 0) tarjeta.recargar(saldoInicial);
        cliente.setTarjetaVirtual(tarjeta);
        clientes.add(cliente);
    }
}
