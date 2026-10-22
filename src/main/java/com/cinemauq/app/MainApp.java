package com.cinemauq.app;

import com.cinemauq.model.Usuario;
import com.cinemauq.view.*;
import com.cinemauq.view.util.UiKit;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final AppContext ctx = new AppContext();
    private final List<Button> navButtons = new ArrayList<>();
    private StackPane rootStack;

    @Override
    public void start(Stage stage) {

        cargarFuente("/fonts/Inter-Regular.ttf");
        cargarFuente("/fonts/Inter-Bold.ttf");
        cargarFuente("/fonts/Anton-Regular.ttf");


        rootStack = new StackPane();
        rootStack.getStyleClass().add("app-background");

        mostrarLogin();

        Scene scene = new Scene(rootStack, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/css/cinemauq.css").toExternalForm());

        stage.setTitle("CinemaUQ");
        cargarIcono(stage, "/images/logo-cinemauq.png");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.show();
    }

    private void cargarIcono(Stage stage, String ruta) {
        try (InputStream in = getClass().getResourceAsStream(ruta)) {
            if (in == null) { System.out.println("NO ENCONTRADA: " + ruta); return; }
            stage.getIcons().add(new Image(in));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarFuente(String ruta) {
        try (InputStream in = getClass().getResourceAsStream(ruta)) {
            if (in == null) { System.out.println("NO ENCONTRADA: " + ruta); return; }
            Font f = Font.loadFont(in, 14);
            System.out.println(ruta + " -> familia: " + (f == null ? "FALLÓ" : f.getFamily()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarLogin() {
        LoginView login = new LoginView(ctx, this::mostrarShellPrincipal);
        rootStack.getChildren().setAll(login);
    }

    private void mostrarShellPrincipal() {
        BorderPane shellRoot = new BorderPane();
        shellRoot.getStyleClass().add("shell-root");

        shellRoot.setTop(buildHeader());
        shellRoot.setLeft(buildSidebar());

        StackPane content = new StackPane();
        content.getStyleClass().add("content-area");
        shellRoot.setCenter(content);
        ctx.setContentArea(content);

        rootStack.getChildren().setAll(shellRoot);
        navegarHome();
    }

    private HBox buildHeader() {
        HBox header = new HBox(UiKit.logo(null));
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("header-bar");
        header.setViewOrder(-1);
        return header;
    }

    private VBox buildSidebar() {
        navButtons.clear();

        VBox nav = new VBox(6);
        nav.setPadding(new Insets(24, 14, 20, 14));
        nav.getStyleClass().add("sidebar");
        nav.setPrefWidth(230);
        nav.setMinWidth(230);

        if (ctx.getGestorSesion().esCliente()) {
            nav.getChildren().addAll(
                    navButton("Cartelera", this::navegarHome),
                    navButton("Mi tarjeta", () -> ctx.mostrarVista(new TarjetaView(ctx))),
                    navButton("Mis puntos", () -> ctx.mostrarVista(new PuntosView(ctx))),
                    navButton("Mis compras", () -> ctx.mostrarVista(new HistorialView(ctx)))
            );
        } else {
            nav.getChildren().addAll(
                    navButton("Panel administrativo", this::navegarHome)
            );
        }

        Region empuje = new Region();
        VBox.setVgrow(empuje, Priority.ALWAYS);

        Usuario usuario = ctx.getGestorSesion().getUsuarioActual();
        Label quien = new Label(usuario.getNombre() + "  ·  " + usuario.getRol());
        quien.getStyleClass().add("sidebar-user");
        quien.setPadding(new Insets(0, 0, 4, 16));

        Button logout = new Button("Cerrar sesión");
        logout.getStyleClass().addAll("nav-button", "nav-logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setOnAction(e -> {
            ctx.getGestorSesion().cerrarSesion();
            mostrarLogin();
        });

        nav.getChildren().addAll(empuje, quien, logout);
        return nav;
    }

    private Button navButton(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            marcarActivo(btn);
            accion.run();
        });
        navButtons.add(btn);
        return btn;
    }

    private void marcarActivo(Button activo) {
        for (Button b : navButtons) b.getStyleClass().remove("nav-active");
        activo.getStyleClass().add("nav-active");
    }

    private void navegarHome() {
        if (!navButtons.isEmpty()) marcarActivo(navButtons.get(0));
        if (ctx.getGestorSesion().esCliente()) {
            ctx.mostrarVista(new CarteleraView(ctx));
        } else {
            ctx.mostrarVista(new AdminView(ctx));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
