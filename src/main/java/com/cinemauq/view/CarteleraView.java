package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.Pelicula;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class CarteleraView extends VBox {

    private static final double POSTER_W = 208;
    private static final double POSTER_H = 302;
    private static final double GAP_MIN = 12;
    private static final double GAP_MAX = 40;

    private final AppContext ctx;

    public CarteleraView(AppContext ctx) {
        this.ctx = ctx;
        setSpacing(8);
        setPadding(new Insets(28, 32, 0, 32));
        getStyleClass().add("view-root");
        build();
    }

    private void build() {
        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();
        cliente.consultarCartelera();

        HBox titulo = UiKit.tituloBrutalista("Cartelera", "cinemaUQ");
        Label subtitulo = UiKit.subtitulo("Elige una película para ver sus funciones disponibles");

        FlowPane grid = new FlowPane();
        grid.setHgap(24);
        grid.setVgap(24);
        grid.setPadding(new Insets(24, 16, 32, 16));

        for (Pelicula p : ctx.getDataStore().getPeliculas()) {
            grid.getChildren().add(crearTarjeta(p));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.viewportBoundsProperty().addListener((obs, viejo, nuevo) -> repartirColumnas(grid, nuevo.getWidth()));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titulo, subtitulo, scroll);
    }

    private void repartirColumnas(FlowPane grid, double anchoViewport) {
        double ancho = anchoViewport - grid.getPadding().getLeft() - grid.getPadding().getRight() - 1;
        double tarjeta = POSTER_W + 24;
        int columnas = Math.max(1, (int) Math.floor((ancho + GAP_MIN) / (tarjeta + GAP_MIN)));
        double gap = columnas > 1 ? (ancho - columnas * tarjeta) / (columnas - 1) : GAP_MIN;
        grid.setHgap(Math.max(GAP_MIN, Math.min(gap, GAP_MAX)));
    }

    private VBox crearTarjeta(Pelicula p) {
        Label titulo = new Label(p.getTitulo());
        titulo.getStyleClass().add("card-title");
        titulo.setWrapText(true);
        titulo.setMinHeight(40);
        titulo.setAlignment(Pos.TOP_LEFT);

        Label meta = new Label(p.getGenero() + " · " + p.getDuracionMinutos() + " min · " + p.getClasificacion());
        meta.getStyleClass().add("card-meta");

        Button ver = new Button("Ver funciones");
        ver.getStyleClass().add("btn-primary");
        ver.setMaxWidth(Double.MAX_VALUE);
        ver.setMouseTransparent(true);
        ver.setFocusTraversable(false);

        VBox card = new VBox(8, UiKit.poster(p.getUrlPoster(), POSTER_W, POSTER_H, 10), titulo, meta, ver);
        card.setPadding(new Insets(12));
        card.setPrefWidth(POSTER_W + 24);
        card.setMaxWidth(POSTER_W + 24);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("movie-card");
        UiEffects.attachCardHover(card);

        card.setOnMouseClicked(e -> ctx.mostrarVista(new CompraWizardView(ctx, p)));
        return card;
    }
}