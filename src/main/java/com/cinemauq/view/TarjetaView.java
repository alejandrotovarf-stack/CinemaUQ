package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.MovimientoTarjeta;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;

public class TarjetaView extends VBox {

    public TarjetaView(AppContext ctx) {
        setSpacing(16);
        setPadding(new Insets(28, 32, 28, 32));
        getStyleClass().add("view-root");

        Cliente cliente = (Cliente) ctx.getGestorSesion().getUsuarioActual();

        HBox titulo = UiKit.tituloBrutalista("Mi tarjeta", "virtual");

        Label numero = new Label(cliente.getTarjetaVirtual().getNumeroTarjeta());
        numero.getStyleClass().add("card-number");
        Label saldo = new Label("$" + String.format("%,.0f", cliente.getTarjetaVirtual().getSaldo()));
        saldo.getStyleClass().add("card-balance");
        Label estado = new Label(cliente.getTarjetaVirtual().isActiva() ? "ACTIVA" : "INACTIVA");
        estado.getStyleClass().add("card-status");

        VBox saldoBox = new VBox(10, numero, saldo, estado);
        saldoBox.setAlignment(Pos.TOP_LEFT);
        saldoBox.setPadding(new Insets(26, 30, 26, 30));
        saldoBox.setPrefSize(420, 200);
        saldoBox.setMaxSize(420, 200);
        saldoBox.getStyleClass().add("saldo-card");
        UiEffects.attachCardHover(saldoBox);

        VBox tarjetaWrap = new VBox(saldoBox);
        tarjetaWrap.setPadding(new Insets(10, 0, 10, 0));
        tarjetaWrap.setAlignment(Pos.CENTER_LEFT);

        ListView<String> lista = new ListView<>();
        var movimientos = cliente.getTarjetaVirtual().getMovimientos();
        if (movimientos.isEmpty()) {
            lista.getItems().add("Aún no tienes movimientos.");
        } else {
            var copia = new ArrayList<>(movimientos);
            Collections.reverse(copia);
            for (MovimientoTarjeta m : copia) lista.getItems().add(m.toString());
        }
        VBox.setVgrow(lista, Priority.ALWAYS);

        Label nota = UiKit.subtitulo("Las recargas de saldo son realizadas exclusivamente por el administrador de CinemaUQ.");

        getChildren().addAll(titulo, tarjetaWrap, UiKit.seccion("Movimientos"), lista, nota);
    }
}
