package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.exception.PuntosInsuficientesException;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.MovimientoPuntos;
import com.cinemauq.patterns.creational.ConfiguracionCinema;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;

public class PuntosView extends VBox {

    private final AppContext ctx;
    private final Label saldoLabel = new Label();
    private final ListView<String> movimientosList = new ListView<>();

    public PuntosView(AppContext ctx) {
        this.ctx = ctx;
        setSpacing(16);
        setPadding(new Insets(28, 32, 28, 32));
        getStyleClass().add("view-root");

        HBox titulo = UiKit.tituloBrutalista("Mis puntos", "cinemaUQ");

        Label reglas = UiKit.subtitulo(ConfiguracionCinema.getInstancia().getReglasPuntos());

        saldoLabel.getStyleClass().add("points-big");

        TextField valorRedimir = new TextField();
        valorRedimir.setPromptText("Valor en $ que deseas redimir (ej: 5000)");
        HBox.setHgrow(valorRedimir, Priority.ALWAYS);
        Button redimirBtn = UiKit.primario("Redimir por saldo");
        redimirBtn.setOnAction(e -> redimir(valorRedimir));

        HBox redimirBox = new HBox(12, valorRedimir, redimirBtn);
        redimirBox.setAlignment(Pos.CENTER_LEFT);

        Label ayuda = new Label("Los puntos redimidos se convierten en saldo disponible en tu tarjeta virtual.");
        ayuda.getStyleClass().add("card-meta");
        ayuda.setWrapText(true);

        VBox panel = new VBox(12, saldoLabel, redimirBox, ayuda);
        panel.setPadding(new Insets(22, 24, 22, 24));
        panel.getStyleClass().add("form-card");

        VBox.setVgrow(movimientosList, Priority.ALWAYS);

        getChildren().addAll(titulo, reglas, panel, UiKit.seccion("Movimientos"), movimientosList);
        refrescar();
    }

    private void redimir(TextField valorField) {
        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();
        double valor;
        try {
            valor = Double.parseDouble(valorField.getText().trim());
            if (valor <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            ctx.mostrarError("Valor inválido", "Ingresa un valor numérico mayor que cero.");
            return;
        }
        try {
            int costo = ctx.getServicioPuntos().calcularCostoEnPuntos(valor);
            ctx.getServicioPuntos().redimir(cliente, valor);
            cliente.getTarjetaVirtual().acreditar(valor);
            ctx.mostrarInfo("Redención exitosa", "Redimiste " + costo + " puntos por $" + String.format("%,.0f", valor) + " de saldo.");
            valorField.clear();
            refrescar();
        } catch (PuntosInsuficientesException ex) {
            ctx.mostrarError("Puntos insuficientes", ex.getMessage());
        }
    }

    private void refrescar() {
        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();
        saldoLabel.setText(cliente.getCuentaPuntos().getSaldoPuntos() + " PTS");

        movimientosList.getItems().clear();
        var movimientos = new ArrayList<>(cliente.getCuentaPuntos().getMovimientos());
        Collections.reverse(movimientos);
        if (movimientos.isEmpty()) {
            movimientosList.getItems().add("Aún no tienes movimientos de puntos.");
        } else {
            for (MovimientoPuntos m : movimientos) movimientosList.getItems().add(m.toString());
        }
    }
}
