package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.exception.AccesoNoAutorizadoException;
import com.cinemauq.model.*;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminView extends VBox {

    private final AppContext ctx;

    public AdminView(AppContext ctx) {
        this.ctx = ctx;
        setSpacing(15);
        setPadding(new Insets(18, 10, 8, 32));
        getStyleClass().add("view-root");

        HBox titulo = UiKit.tituloBrutalista("Panel de", "administración");

        ReportesView reportes = new ReportesView(ctx, true);
        Tab tabReportes = new Tab("Reportes", envolverEnScroll(reportes));
        tabReportes.setOnSelectionChanged(e -> {
            if (tabReportes.isSelected()) reportes.refrescar();
        });

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Películas", buildPeliculasTab()),
                new Tab("Funciones", buildFuncionesTab()),
                new Tab("Confitería", buildConfiteriaTab()),
                new Tab("Clientes y Tarjetas", buildClientesTab()),
                tabReportes
        );
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(titulo, tabs);
    }

    private ScrollPane envolverEnScroll(javafx.scene.layout.Region contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        return scroll;
    }

    private VBox buildPeliculasTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(4, 0, 0, 0));

        ListView<String> lista = new ListView<>();
        refrescarListaPeliculas(lista);
        VBox.setVgrow(lista, Priority.ALWAYS);

        TextField titulo = new TextField(); titulo.setPromptText("Título"); titulo.setPrefWidth(200);
        TextField genero = new TextField(); genero.setPromptText("Género"); genero.setPrefWidth(140);
        TextField duracion = new TextField(); duracion.setPromptText("Duración (min)"); duracion.setPrefWidth(130);
        TextField clasificacion = new TextField(); clasificacion.setPromptText("Clasificación"); clasificacion.setPrefWidth(130);
        TextField poster = new TextField(); poster.setPromptText("URL del póster"); poster.setPrefWidth(260);

        Button agregar = UiKit.primario("Agregar película");
        agregar.setOnAction(e -> {
            try {
                int dur = Integer.parseInt(duracion.getText().trim());
                Pelicula p = new Pelicula("P" + System.currentTimeMillis() % 10000, titulo.getText().trim(),
                        genero.getText().trim(), dur, clasificacion.getText().trim(), "", poster.getText().trim());
                ctx.getDataStore().getPeliculas().add(p);
                refrescarListaPeliculas(lista);
                titulo.clear(); genero.clear(); duracion.clear(); clasificacion.clear(); poster.clear();
            } catch (Exception ex) {
                ctx.mostrarError("Datos inválidos", "Revisa los campos ingresados (duración debe ser numérica).");
            }
        });

        FlowPane form = new FlowPane(12, 12, titulo, genero, duracion, clasificacion, poster, agregar);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(16, 20, 16, 20));
        form.getStyleClass().add("form-card");

        box.getChildren().addAll(UiKit.seccion("Catálogo de películas"), lista, form);
        return box;
    }

    private void refrescarListaPeliculas(ListView<String> lista) {
        lista.setItems(FXCollections.observableArrayList());
        for (Pelicula p : ctx.getDataStore().getPeliculas()) {
            lista.getItems().add(p.getTitulo() + " · " + p.getGenero() + " · " + p.getDuracionMinutos() + " min · " + p.getClasificacion());
        }
    }

    private VBox buildFuncionesTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(4, 0, 0, 0));

        VBox lista = new VBox(12);
        lista.setPadding(new Insets(12, 10, 24, 10));
        refrescarFunciones(lista);
        ScrollPane scroll = envolverEnScroll(lista);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        box.getChildren().addAll(
                UiKit.seccion("Funciones programadas"),
                UiKit.subtitulo("Cancelar una función dispara el reembolso del 100%."),
                scroll);
        return box;
    }

    private void refrescarFunciones(VBox contenedor) {
        contenedor.getChildren().clear();
        for (Funcion f : ctx.getDataStore().getFunciones()) {
            HBox fila = new HBox(14);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(14, 20, 14, 20));
            fila.getStyleClass().add("funcion-row");
            if (f.isCancelada()) fila.getStyleClass().add("row-cancelled");

            Label info = new Label(f.getIdentificador() + " · " + f.getPelicula().getTitulo() + " · " + f.getSala().getIdentificador()
                    + " · " + f.getFecha() + " " + f.getHora() + (f.isCancelada() ? "  [CANCELADA]" : ""));
            info.getStyleClass().add("card-title");

            Button cancelarBtn = new Button("Cancelar función");
            cancelarBtn.getStyleClass().add("btn-danger");
            cancelarBtn.setDisable(f.isCancelada());
            cancelarBtn.setOnAction(e -> cancelarFuncion(f, contenedor));

            fila.getChildren().addAll(info, UiKit.espaciador(), cancelarBtn);
            UiEffects.attachButtonHover(fila);
            contenedor.getChildren().add(fila);
        }
    }

    private void cancelarFuncion(Funcion funcion, VBox contenedor) {
        boolean confirmar = ctx.confirmar("Cancelar función",
                "¿Confirmas cancelar la función " + funcion.getIdentificador() + "? Se reembolsará el 100% a todas las compras asociadas (RN-09).");
        if (!confirmar) return;

        funcion.setCancelada(true);
        int reembolsadas = 0;
        for (Compra compra : ctx.getDataStore().getCompras()) {
            boolean perteneceAFuncion = compra.getEntradas().stream().anyMatch(en -> en.getFuncion() == funcion);
            if (perteneceAFuncion && "CONFIRMADA".equals(compra.getEstado().getNombre())) {
                try {
                    var reembolso = ctx.getServicioCancelacion().cancelarPorFuncionCancelada(compra);
                    ctx.getDataStore().registrarReembolso(reembolso);
                    reembolsadas++;
                } catch (RuntimeException ignored) { }
            }
        }
        ctx.mostrarInfo("Función cancelada", "Se canceló la función y se reembolsaron " + reembolsadas + " compra(s) al 100%.");
        refrescarFunciones(contenedor);
    }

    private VBox buildConfiteriaTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(4, 0, 0, 0));

        ListView<String> lista = new ListView<>();
        for (ProductoConfiteria p : ctx.getDataStore().getProductos()) {
            lista.getItems().add(p.getCodigo() + " · " + p.getNombre() + " · $" + (int) p.getPrecio() + " · " + (p.isDisponible() ? "Disponible" : "No disponible"));
        }
        for (Combo c : ctx.getDataStore().getCombos()) {
            lista.getItems().add("[COMBO] " + c.getNombre() + " · $" + (int) c.getPrecio() + " (" + c.getComponentes().size() + " productos)");
        }
        VBox.setVgrow(lista, Priority.ALWAYS);

        box.getChildren().addAll(
                UiKit.seccion("Productos y combos"),
                UiKit.subtitulo("Solo los productos disponibles pueden venderse"),
                lista);
        return box;
    }

    private VBox buildClientesTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(4, 0, 0, 0));

        VBox lista = new VBox(12);
        lista.setPadding(new Insets(4, 4, 20, 4));
        refrescarClientes(lista);
        ScrollPane scroll = envolverEnScroll(lista);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        box.getChildren().addAll(UiKit.seccion("Clientes registrados"), scroll);
        return box;
    }

    private void refrescarClientes(VBox contenedor) {
        contenedor.getChildren().clear();
        Administrador admin = (Administrador) ctx.getGestorSesion().getUsuarioActual();

        for (Cliente c : ctx.getDataStore().getClientes()) {
            HBox fila = new HBox(14);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(14, 20, 14, 20));
            fila.getStyleClass().add("funcion-row");

            Label info = new Label(c.getNombre() + " · " + c.getCorreo() + " · Saldo: $"
                    + String.format("%,.0f", c.getTarjetaVirtual().getSaldo()) + " · " + c.getCuentaPuntos().getSaldoPuntos() + " pts");
            info.getStyleClass().add("card-title");

            TextField valor = new TextField();
            valor.setPromptText("Valor a recargar");
            valor.setPrefWidth(160);

            Button recargarBtn = UiKit.primario("Recargar");
            recargarBtn.setOnAction(e -> {
                try {
                    double monto = Double.parseDouble(valor.getText().trim());
                    ctx.getServicioTarjeta().recargar(admin, c.getTarjetaVirtual(), monto);
                    ctx.mostrarInfo("Recarga exitosa", "Se recargaron $" + String.format("%,.0f", monto) + " a " + c.getNombre());
                    valor.clear();
                    refrescarClientes(contenedor);
                } catch (NumberFormatException nfe) {
                    ctx.mostrarError("Valor inválido", "Ingresa un número válido.");
                } catch (AccesoNoAutorizadoException aex) {
                    ctx.mostrarError("No autorizado", aex.getMessage());
                }
            });

            fila.getChildren().addAll(info, UiKit.espaciador(), valor, recargarBtn);
            contenedor.getChildren().add(fila);
        }
    }
}
