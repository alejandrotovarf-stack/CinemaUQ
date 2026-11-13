package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.exception.AsientoNoDisponibleException;
import com.cinemauq.exception.SaldoInsuficienteException;
import com.cinemauq.exception.TarjetaInactivaException;
import com.cinemauq.model.*;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CompraWizardView extends BorderPane {

    private static final String[] NOMBRES_PASOS = {"Función", "Asientos", "Confitería", "Pago"};

    private static final String RUTA_IMG_CONFITERIA = "/images/confiteria/";
    private static final double IMG_W = 170;
    private static final double IMG_H = 115;

    private final AppContext ctx;
    private final Pelicula pelicula;

    private Funcion funcionSeleccionada;
    private final Set<Asiento> asientosSeleccionados = new LinkedHashSet<>();
    private final Map<ItemConfiteria, Integer> confiteriaSeleccionada = new LinkedHashMap<>();

    private final Label pasoLabel = new Label();
    private final HBox stepper = new HBox(8);
    private final StackPane stepArea = new StackPane();

    public CompraWizardView(AppContext ctx, Pelicula pelicula) {
        this.ctx = ctx;
        this.pelicula = pelicula;
        getStyleClass().add("view-root");
        setPadding(new Insets(24, 32, 0, 32));
        buildHeader();
        setCenter(stepArea);
        mostrarPasoFunciones();
    }

    private void buildHeader() {
        Button volver = UiKit.enlace("← Volver a cartelera");
        volver.setPadding(new Insets(6, 0, 6, 0));
        volver.setOnAction(e -> ctx.mostrarVista(new CarteleraView(ctx)));

        HBox titulo = UiKit.tituloCompacto(pelicula.getTitulo(), null);
        pasoLabel.getStyleClass().add("view-subtitle");

        VBox header = new VBox(6, volver, titulo, stepper, pasoLabel);
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 12, 0));
    }

    private void marcarPaso(int actual, String descripcion) {
        pasoLabel.setText(descripcion);
        stepper.getChildren().clear();
        for (int i = 0; i < NOMBRES_PASOS.length; i++) {
            Label pill = new Label((i + 1) + "  " + NOMBRES_PASOS[i]);
            pill.getStyleClass().add("step-pill");
            if (i + 1 == actual) pill.getStyleClass().add("step-active");
            else if (i + 1 < actual) pill.getStyleClass().add("step-done");
            stepper.getChildren().add(pill);
        }
    }

    private ScrollPane envolver(Region contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        return scroll;
    }

    private void mostrarPasoFunciones() {
        marcarPaso(1, "Elige función (sala, fecha y hora)");
        VBox lista = new VBox(12);
        lista.setPadding(new Insets(12, 12, 24, 12));

        List<Funcion> funciones = ctx.getDataStore().getFuncionesDePelicula(pelicula);
        if (funciones.isEmpty()) {
            Label vacio = new Label("No hay funciones programadas para esta película.");
            vacio.getStyleClass().add("empty-state");
            lista.getChildren().add(vacio);
        }
        for (Funcion f : funciones) {
            HBox fila = new HBox(16);
            fila.setPadding(new Insets(16, 20, 16, 20));
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("funcion-row");

            Label info = new Label(f.getFecha().format(DateTimeFormatter.ofPattern("EEE dd/MM")) + "  ·  " + f.getHora()
                    + "  ·  " + f.getSala().getIdentificador() + "  ·  $" + (int) f.getPrecioBase());
            info.getStyleClass().add("card-title");

            Button elegir = new Button("Elegir");
            elegir.getStyleClass().add("btn-primary");
            elegir.setOnAction(e -> {
                funcionSeleccionada = f;
                asientosSeleccionados.clear();
                mostrarPasoAsientos();
            });
            fila.getChildren().addAll(info, UiKit.espaciador(), elegir);
            UiEffects.attachButtonHover(fila);
            lista.getChildren().add(fila);
        }
        stepArea.getChildren().setAll(envolver(lista));
    }

    private void mostrarPasoAsientos() {
        marcarPaso(2, "Selecciona tus asientos — " + funcionSeleccionada);

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(760);
        panel.getStyleClass().add("seat-panel");

        Label pantalla = new Label("P A N T A L L A");
        pantalla.getStyleClass().add("pantalla-label");
        pantalla.setMaxWidth(Double.MAX_VALUE);
        pantalla.setAlignment(Pos.CENTER);

        Map<String, List<Asiento>> porFila = new LinkedHashMap<>();
        for (Asiento a : funcionSeleccionada.getSala().getAsientos()) {
            porFila.computeIfAbsent(a.getFila(), k -> new ArrayList<>()).add(a);
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        int fila = 0;
        for (Map.Entry<String, List<Asiento>> entry : porFila.entrySet()) {
            Label letra = new Label(entry.getKey());
            letra.getStyleClass().add("fila-label");
            grid.add(letra, 0, fila);
            int col = 1;
            for (Asiento asiento : entry.getValue()) {
                ToggleButton btnAsiento = new ToggleButton(String.valueOf(asiento.getNumero()));
                btnAsiento.getStyleClass().add("seat-btn");
                boolean disponible = funcionSeleccionada.estaDisponible(asiento);
                if (!disponible) {
                    btnAsiento.getStyleClass().add("seat-occupied");
                    btnAsiento.setDisable(true);
                } else {
                    btnAsiento.getStyleClass().add("seat-free");
                }
                btnAsiento.setOnAction(e -> {
                    if (btnAsiento.isSelected()) {
                        asientosSeleccionados.add(asiento);
                        btnAsiento.getStyleClass().remove("seat-free");
                        btnAsiento.getStyleClass().add("seat-selected");
                    } else {
                        asientosSeleccionados.remove(asiento);
                        btnAsiento.getStyleClass().remove("seat-selected");
                        btnAsiento.getStyleClass().add("seat-free");
                    }
                });
                grid.add(btnAsiento, col, fila);
                col++;
            }
            fila++;
        }

        HBox leyenda = new HBox(20,
                leyendaItem("seat-free", "Disponible"),
                leyendaItem("seat-selected", "Seleccionado"),
                leyendaItem("seat-occupied", "Ocupado"));
        leyenda.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(pantalla, grid, leyenda);

        Button continuar = UiKit.primario("Continuar →");
        continuar.setOnAction(e -> {
            if (asientosSeleccionados.isEmpty()) {
                ctx.mostrarError("Selección requerida", "Debes seleccionar al menos un asiento.");
                return;
            }
            mostrarPasoConfiteria();
        });
        Button atras = UiKit.secundario("← Cambiar función");
        atras.setOnAction(e -> mostrarPasoFunciones());

        HBox acciones = new HBox(12, atras, continuar);
        acciones.setAlignment(Pos.CENTER);

        VBox contenedor = new VBox(20, panel, acciones);
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(12, 12, 28, 12));
        stepArea.getChildren().setAll(envolver(contenedor));
    }

    private HBox leyendaItem(String claseCss, String texto) {
        Region box = new Region();
        box.setPrefSize(18, 18);
        box.setMinSize(18, 18);
        box.getStyleClass().add(claseCss);
        Label label = new Label(texto);
        label.getStyleClass().add("card-meta");
        HBox item = new HBox(8, box, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private void mostrarPasoConfiteria() {
        marcarPaso(3, "Agrega confitería (opcional)");

        VBox contenido = new VBox(14);
        contenido.setPadding(new Insets(12, 12, 28, 12));

        FlowPane productosPane = new FlowPane(16, 16);
        productosPane.setPadding(new Insets(8, 4, 12, 4));
        for (ProductoConfiteria p : ctx.getDataStore().getProductos()) {
            if (p.isDisponible()) productosPane.getChildren().add(crearTarjetaConfiteria(p));
        }

        FlowPane combosPane = new FlowPane(16, 16);
        combosPane.setPadding(new Insets(8, 4, 12, 4));
        for (Combo c : ctx.getDataStore().getCombos()) {
            combosPane.getChildren().add(crearTarjetaConfiteria(c));
        }

        Button continuar = UiKit.primario("Continuar →");
        continuar.setOnAction(e -> mostrarPasoResumen());
        Button atras = UiKit.secundario("← Cambiar asientos");
        atras.setOnAction(e -> mostrarPasoAsientos());
        Button saltar = UiKit.enlace("Omitir confitería");
        saltar.setOnAction(e -> mostrarPasoResumen());

        HBox acciones = new HBox(12, atras, saltar, UiKit.espaciador(), continuar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        contenido.getChildren().addAll(UiKit.seccion("Productos"), productosPane,
                UiKit.seccion("Combos"), combosPane, acciones);
        stepArea.getChildren().setAll(envolver(contenido));
    }

    private VBox crearTarjetaConfiteria(ItemConfiteria item) {
        Label nombre = new Label(item.getNombre());
        nombre.getStyleClass().add("card-title");
        nombre.setWrapText(true);
        nombre.setMinHeight(40);
        Label precio = new Label("$" + (int) item.getPrecio());
        precio.getStyleClass().add("card-meta");

        Label cantidadLabel = new Label(String.valueOf(confiteriaSeleccionada.getOrDefault(item, 0)));
        cantidadLabel.getStyleClass().add("qty-label");
        Button menos = new Button("−");
        Button mas = new Button("+");
        menos.getStyleClass().add("btn-qty");
        mas.getStyleClass().add("btn-qty");

        mas.setOnAction(e -> {
            confiteriaSeleccionada.merge(item, 1, Integer::sum);
            cantidadLabel.setText(String.valueOf(confiteriaSeleccionada.get(item)));
        });
        menos.setOnAction(e -> {
            int actual = confiteriaSeleccionada.getOrDefault(item, 0);
            if (actual > 0) {
                confiteriaSeleccionada.put(item, actual - 1);
                cantidadLabel.setText(String.valueOf(actual - 1));
            }
        });

        HBox controles = new HBox(10, menos, cantidadLabel, mas);
        controles.setAlignment(Pos.CENTER);

        VBox card = new VBox(8, imagenConfiteria(item), nombre, precio, controles);
        card.setPadding(new Insets(12, 12, 16, 12));
        card.setPrefWidth(IMG_W + 24);
        card.setMinWidth(IMG_W + 24);
        card.getStyleClass().add("confiteria-card");
        UiEffects.attachCardHover(card);
        return card;
    }

    private javafx.scene.Node imagenConfiteria(ItemConfiteria item) {
        String clave = claveImagen(item.getNombre());
        if (clave != null) {
            URL url = getClass().getResource(RUTA_IMG_CONFITERIA + clave + ".png");
            if (url != null) return UiKit.poster(url.toExternalForm(), IMG_W, IMG_H, 10);
        }
        Region vacio = new Region();
        vacio.setPrefSize(IMG_W, IMG_H);
        vacio.setMinSize(IMG_W, IMG_H);
        vacio.setMaxSize(IMG_W, IMG_H);
        vacio.getStyleClass().add("confiteria-img-empty");
        return vacio;
    }

    private static String claveImagen(String nombre) {
        if (nombre == null) return null;
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
        if (n.contains("combo")) {
            if (n.contains("familiar")) return "combo_familiar";
            if (n.contains("pareja")) return "combo_pareja";
            if (n.contains("kids")) return "combo_kids";
            return "combo_parceros";
        }
        if (n.contains("crispetas") || n.contains("palomita") || n.contains("popcorn")) return "crispetas";
        if (n.contains("gaseosa") || n.contains("refresco") || n.contains("bebida") || n.contains("soda")) return "gaseosa";
        if (n.contains("perro") || n.contains("hot dog") || n.contains("hotdog")) return "perro";
        if (n.contains("nacho")) return "nachos";
        if (n.contains("chocolat") || n.contains("dulce") || n.contains("candy")) return "chocolatina";
        if(n.contains("salchipapas")) return "salchipapas";
        if(n.contains("Gomitas")||n.contains("gomitas")) return "gomitas";
        if(n.contains("pizza")||n.contains("Pizza")) return "pizza";
        if(n.contains("ice")||n.contains("Ice")) return "ice";


        return null;
    }

    private void mostrarPasoResumen() {
        marcarPaso(4, "Confirma y paga");

        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();

        VBox resumen = new VBox(8);
        resumen.setPadding(new Insets(20));
        resumen.setMaxWidth(620);
        resumen.getStyleClass().add("resumen-box");

        Label tituloEntradas = new Label("Entradas (" + asientosSeleccionados.size() + ")");
        tituloEntradas.getStyleClass().add("section-sub");
        resumen.getChildren().add(tituloEntradas);
        double subtotalEntradas = 0;
        for (Asiento a : asientosSeleccionados) {
            resumen.getChildren().add(lineaResumen(a.getCodigo() + "  ·  $" + (int) funcionSeleccionada.getPrecioBase()));
            subtotalEntradas += funcionSeleccionada.getPrecioBase();
        }

        List<ItemConfiteria> itemsFinal = new ArrayList<>();
        double subtotalConfiteria = 0;
        if (!confiteriaSeleccionada.isEmpty()) {
            Label tituloConf = new Label("Confitería");
            tituloConf.getStyleClass().add("section-sub");
            tituloConf.setPadding(new Insets(10, 0, 0, 0));
            resumen.getChildren().add(tituloConf);
            for (Map.Entry<ItemConfiteria, Integer> entry : confiteriaSeleccionada.entrySet()) {
                if (entry.getValue() <= 0) continue;
                for (int i = 0; i < entry.getValue(); i++) itemsFinal.add(entry.getKey());
                double sub = entry.getKey().getPrecio() * entry.getValue();
                subtotalConfiteria += sub;
                resumen.getChildren().add(lineaResumen(entry.getValue() + " x " + entry.getKey().getNombre() + "  ·  $" + (int) sub));
            }
        }

        double subtotal = subtotalEntradas + subtotalConfiteria;

        TextField codigoPromo = new TextField();
        codigoPromo.setPromptText("Código de promoción (ej: CINEUQ10)");
        codigoPromo.setMaxWidth(620);

        Label totalLabel = new Label();
        totalLabel.getStyleClass().add("total-label");

        Runnable actualizarTotal = () -> {
            double descuento = "CINEUQ10".equalsIgnoreCase(codigoPromo.getText().trim()) ? 0.10 : 0.0;
            double total = subtotal - (subtotal * descuento);
            totalLabel.setText("Total a pagar: $" + String.format("%,.0f", total));
        };
        actualizarTotal.run();
        codigoPromo.textProperty().addListener((obs, o, n) -> actualizarTotal.run());

        Label saldoLabel = new Label("Saldo disponible en tu tarjeta: $" + String.format("%,.0f", cliente.getTarjetaVirtual().getSaldo()));
        saldoLabel.getStyleClass().add("card-meta");

        Button pagar = UiKit.primario("Pagar con Tarjeta Virtual");
        pagar.setOnAction(e -> ejecutarPago(cliente, itemsFinal, codigoPromo.getText().trim()));

        Button atras = UiKit.secundario("← Cambiar confitería");
        atras.setOnAction(e -> mostrarPasoConfiteria());

        HBox acciones = new HBox(12, atras, pagar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        VBox contenedor = new VBox(16, resumen, codigoPromo, totalLabel, saldoLabel, acciones);
        contenedor.setPadding(new Insets(12, 12, 28, 12));
        stepArea.getChildren().setAll(envolver(contenedor));
    }

    private Label lineaResumen(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("card-meta");
        return l;
    }

    private void ejecutarPago(Cliente cliente, List<ItemConfiteria> itemsFinal, String codigoPromo) {
        String promoNombre = null;
        double promoPct = 0.0;
        if ("CINEUQ10".equalsIgnoreCase(codigoPromo)) {
            promoNombre = "CINEUQ10";
            promoPct = 0.10;
        }
        try {
            Compra compra = ctx.getServicioCompra().iniciarProcesoCompra(
                    cliente, funcionSeleccionada, new ArrayList<>(asientosSeleccionados), itemsFinal, promoNombre, promoPct);

            boolean ok = ctx.getServicioCompra().procesarPagoYConfirmar(compra);
            if (ok) {
                ctx.getDataStore().registrarCompra(compra);
                ctx.mostrarInfo("¡Compra confirmada!",
                        "Compra " + compra.getId() + " confirmada.\nTotal pagado: $" + String.format("%,.0f", compra.getTotal())
                                + "\nSaldo restante: $" + String.format("%,.0f", cliente.getTarjetaVirtual().getSaldo())
                                + "\nPuntos acumulados: " + cliente.getCuentaPuntos().getSaldoPuntos() + " (total)");
                ctx.mostrarVista(new CarteleraView(ctx));
            } else {
                ctx.mostrarError("Pago rechazado", "No fue posible procesar el pago.");
            }
        } catch (AsientoNoDisponibleException ex) {
            ctx.mostrarError("Asiento no disponible", ex.getMessage());
            mostrarPasoAsientos();
        } catch (SaldoInsuficienteException | TarjetaInactivaException ex) {
            ctx.mostrarError("No se pudo pagar", ex.getMessage());
        } catch (RuntimeException ex) {
            ctx.mostrarError("Error inesperado", ex.getMessage());
        }
    }
}
