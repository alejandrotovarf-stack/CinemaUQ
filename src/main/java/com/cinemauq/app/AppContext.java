package com.cinemauq.app;

import com.cinemauq.repository.DataStore;
import com.cinemauq.service.*;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Optional;

public class AppContext {

    private final DataStore dataStore = new DataStore();
    private final GestorSesion gestorSesion = new GestorSesion();
    private final ServicioPuntos servicioPuntos = new ServicioPuntos();
    private final ServicioCompraFacade servicioCompra = new ServicioCompraFacade(servicioPuntos);
    private final ServicioTarjeta servicioTarjeta = new ServicioTarjeta();
    private final ServicioCancelacion servicioCancelacion = new ServicioCancelacion();
    private final ServicioReporte servicioReporte = new ServicioReporte(dataStore);

    private StackPane contentArea;
    private Runnable onSessionChange;

    public DataStore getDataStore() { return dataStore; }
    public GestorSesion getGestorSesion() { return gestorSesion; }
    public ServicioCompraFacade getServicioCompra() { return servicioCompra; }
    public ServicioTarjeta getServicioTarjeta() { return servicioTarjeta; }
    public ServicioPuntos getServicioPuntos() { return servicioPuntos; }
    public ServicioCancelacion getServicioCancelacion() { return servicioCancelacion; }
    public ServicioReporte getServicioReporte() { return servicioReporte; }

    public void setContentArea(StackPane contentArea) { this.contentArea = contentArea; }
    public void setOnSessionChange(Runnable onSessionChange) { this.onSessionChange = onSessionChange; }
    public void notificarCambioSesion() { if (onSessionChange != null) onSessionChange.run(); }

    public void mostrarVista(Node vista) {
        if (contentArea == null) return;
        contentArea.getChildren().setAll(vista);
        FadeTransition ft = new FadeTransition(Duration.millis(220), vista);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void estilizar(Alert alert) {
        var css = getClass().getResource("/css/cinemauq.css");
        if (css != null) alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    public void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        estilizar(alert);
        alert.showAndWait();
    }

    public void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        estilizar(alert);
        alert.showAndWait();
    }

    public boolean confirmar(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        estilizar(alert);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.YES;
    }
}
