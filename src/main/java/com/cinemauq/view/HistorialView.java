package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.exception.CancelacionNoPermitidaException;
import com.cinemauq.exception.OperacionYaReembolsadaException;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.Compra;
import com.cinemauq.model.Reembolso;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;

public class HistorialView extends VBox {

    private final AppContext ctx;
    private final VBox lista = new VBox(14);

    public HistorialView(AppContext ctx) {
        this.ctx = ctx;
        setSpacing(8);
        setPadding(new Insets(28, 32, 0, 32));
        getStyleClass().add("view-root");

        HBox titulo = UiKit.tituloBrutalista("Mis", "compras");
        Label subtitulo = UiKit.subtitulo("Consulta tus compras y solicita cancelaciones");

        lista.setPadding(new Insets(20, 12, 28, 12));
        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titulo, subtitulo, scroll);
        refrescar();
    }

    private void refrescar() {
        lista.getChildren().clear();
        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();
        var compras = new ArrayList<>(cliente.getHistorialCompras());
        Collections.reverse(compras);

        if (compras.isEmpty()) {
            Label vacio = new Label("Todavía no has realizado compras.");
            vacio.getStyleClass().add("empty-state");
            lista.getChildren().add(vacio);
            return;
        }

        for (Compra c : compras) {
            lista.getChildren().add(crearFila(c));
        }
    }

    private VBox crearFila(Compra compra) {
        Label encabezado = new Label(compra.getId() + "  ·  " + compra.getFecha().toLocalDate());
        encabezado.getStyleClass().add("card-title");

        Label detalle = new Label(compra.getEntradas().size() + " entrada(s) · " + compra.getItemsConfiteria().size()
                + " ítem(s) de confitería · Total: $" + String.format("%,.0f", compra.getTotal()));
        detalle.getStyleClass().add("card-meta");

        String nombreEstado = compra.getEstado().getNombre();
        Label estado = new Label(nombreEstado);
        estado.getStyleClass().addAll("badge", "badge-" + nombreEstado.toLowerCase());

        VBox textos = new VBox(6, encabezado, detalle);
        HBox fila = new HBox(14, textos, UiKit.espaciador(), estado);
        fila.setAlignment(Pos.CENTER_LEFT);

        if ("CONFIRMADA".equals(nombreEstado)) {
            Button cancelar = new Button("Solicitar cancelación");
            cancelar.getStyleClass().add("btn-danger");
            cancelar.setOnAction(e -> solicitarCancelacion(compra));
            fila.getChildren().add(cancelar);
        }

        VBox card = new VBox(fila);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.getStyleClass().add("compra-card");
        if ("CANCELADA".equals(nombreEstado)) card.getStyleClass().add("row-cancelled");
        UiEffects.attachButtonHover(card);
        return card;
    }

    private void solicitarCancelacion(Compra compra) {
        boolean confirmar = ctx.confirmar("Cancelar compra",
                "¿Deseas solicitar la cancelación de la compra " + compra.getId() + "? El reembolso dependerá del tiempo restante para la función.");
        if (!confirmar) return;
        try {
            Reembolso reembolso = ctx.getServicioCancelacion().solicitarCancelacion(compra);
            ctx.getDataStore().registrarReembolso(reembolso);
            ctx.mostrarInfo("Cancelación procesada",
                    String.format("Se devolvió el %.0f%% del valor pagado: $%,.0f a tu tarjeta virtual.",
                            reembolso.getPorcentajeAplicado() * 100, reembolso.getValorDevuelto()));
            refrescar();
        } catch (CancelacionNoPermitidaException | OperacionYaReembolsadaException ex) {
            ctx.mostrarError("No se pudo cancelar", ex.getMessage());
        }
    }
}
