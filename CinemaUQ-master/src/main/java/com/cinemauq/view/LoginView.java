package com.cinemauq.view;

import com.cinemauq.app.AppContext;
import com.cinemauq.model.Administrador;
import com.cinemauq.model.Cliente;
import com.cinemauq.model.Pelicula;
import com.cinemauq.view.util.UiEffects;
import com.cinemauq.view.util.UiKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LoginView extends BorderPane {

    private static final String DEMO_CLIENTE_CORREO = "emiliano@cinemauq.com";
    private static final String DEMO_CLIENTE_CLAVE = "2014";
    private static final String DEMO_ADMIN_CORREO = "majito.admin@cinemauq.com";
    private static final String DEMO_ADMIN_CLAVE = "2010";

    private static final double ALTO_TIRA = 250;
    private static final double RESERVA_DEBAJO_DEL_TITULO = ALTO_TIRA + 28 + 20;
    private static final String[] FAMILIAS_BRUTALISTAS = {"Anton", "Impact", "Arial Black"};
    private final AppContext ctx;
    private final Runnable onLoginExitoso;
    private HBox cuerpo;

    private final Label heading = new Label();
    private final HBox segmentado = new HBox(4);
    private final ToggleButton tglUsuario = new ToggleButton("Usuario");
    private final ToggleButton tglAdmin = new ToggleButton("Admin");
    private final VBox formArea = new VBox(14);
    private final TextField correo = new TextField();
    private final PasswordField clave = new PasswordField();
    private final Label mensaje = new Label();
    private final Label hint = new Label();

    public LoginView(AppContext ctx, Runnable onLoginExitoso) {
        this.ctx = ctx;
        this.onLoginExitoso = onLoginExitoso;
        getStyleClass().add("login-root");
        build();
    }

    private void build() {
        HBox header = new HBox(UiKit.logo("logo-large"));
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18));
        header.getStyleClass().add("header-bar");
        header.setViewOrder(-1);
        setTop(header);

        cuerpo = new HBox(40);
        cuerpo.setPadding(new Insets(32, 40, 32, 40));
        cuerpo.setAlignment(Pos.CENTER_LEFT);
        cuerpo.getChildren().addAll(buildCard(), buildHero());
        setCenter(cuerpo);

        mostrarAcceso();
    }

    private VBox buildCard() {
        heading.getStyleClass().add("card-heading");

        ToggleGroup grupo = new ToggleGroup();
        for (ToggleButton t : new ToggleButton[]{tglUsuario, tglAdmin}) {
            t.setToggleGroup(grupo);
            t.getStyleClass().add("segmented-btn");
            t.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(t, Priority.ALWAYS);
        }
        tglUsuario.setSelected(true);
        grupo.selectedToggleProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) { viejo.setSelected(true); return; }
            aplicarDemo();
        });
        segmentado.getChildren().addAll(tglUsuario, tglAdmin);
        segmentado.getStyleClass().add("segmented");

        VBox card = new VBox(20, heading, segmentado, formArea);
        card.setPadding(new Insets(32));
        card.setPrefWidth(380);
        card.setMinWidth(380);
        card.setMaxWidth(380);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("login-card");
        return card;
    }

    private void aplicarDemo() {
        boolean admin = tglAdmin.isSelected();
        correo.setText(admin ? DEMO_ADMIN_CORREO : DEMO_CLIENTE_CORREO);
        clave.setText(admin ? DEMO_ADMIN_CLAVE : DEMO_CLIENTE_CLAVE);
        hint.setText("Demo: " + (admin ? DEMO_ADMIN_CORREO + " / " + DEMO_ADMIN_CLAVE
                : DEMO_CLIENTE_CORREO + " / " + DEMO_CLIENTE_CLAVE));
        mensaje.setText("");
    }

    private void mostrarAcceso() {
        heading.setText("Iniciar sesión");
        segmentado.setVisible(true);
        segmentado.setManaged(true);

        correo.setPromptText("Correo");
        clave.setPromptText("Contraseña");
        mensaje.getStyleClass().setAll("label", "login-error");
        hint.getStyleClass().setAll("label", "login-hint");
        aplicarDemo();

        Button entrar = UiKit.primario("Iniciar sesión");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setOnAction(e -> autenticar());
        clave.setOnAction(e -> entrar.fire());

        Button registrarse = UiKit.enlace("¿No tienes cuenta? Regístrate");
        registrarse.setOnAction(e -> mostrarRegistro());

        VBox links = new VBox(2, hint, registrarse);
        links.setAlignment(Pos.CENTER);

        formArea.getChildren().setAll(correo, clave, entrar, mensaje, links);
    }

    private void autenticar() {
        if (tglAdmin.isSelected()) {
            Administrador admin = ctx.getDataStore().autenticarAdministrador(correo.getText().trim(), clave.getText());
            if (admin == null) { mensaje.setText("Credenciales inválidas."); return; }
            ctx.getGestorSesion().iniciarSesion(admin);
        } else {
            Cliente cliente = ctx.getDataStore().autenticarCliente(correo.getText().trim(), clave.getText());
            if (cliente == null) { mensaje.setText("Credenciales inválidas."); return; }
            ctx.getGestorSesion().iniciarSesion(cliente);
        }
        onLoginExitoso.run();
    }

    private void mostrarRegistro() {
        heading.setText("Crear cuenta");
        segmentado.setVisible(false);
        segmentado.setManaged(false);

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre completo");
        TextField identificacion = new TextField();
        identificacion.setPromptText("Identificación");
        TextField correoReg = new TextField();
        correoReg.setPromptText("Correo");
        PasswordField claveReg = new PasswordField();
        claveReg.setPromptText("Contraseña");

        Label error = new Label();
        error.getStyleClass().add("login-error");

        Button crear = UiKit.primario("Crear cuenta");
        crear.setMaxWidth(Double.MAX_VALUE);
        crear.setOnAction(e -> {
            if (nombre.getText().isBlank() || correoReg.getText().isBlank() || claveReg.getText().isBlank()) {
                error.setText("Completa todos los campos.");
                return;
            }
            if (ctx.getDataStore().existeCorreo(correoReg.getText().trim())) {
                error.setText("Ese correo ya está registrado.");
                return;
            }
            Cliente nuevo = new Cliente(identificacion.getText().trim(), nombre.getText().trim(),
                    correoReg.getText().trim(), claveReg.getText());
            ctx.getDataStore().registrarCliente(nuevo, 30000);
            ctx.getGestorSesion().iniciarSesion(nuevo);
            ctx.mostrarInfo("Bienvenido", "Cuenta creada con saldo inicial de $30.000 en tu tarjeta virtual.");
            onLoginExitoso.run();
        });

        Button volver = UiKit.enlace("← Ya tengo cuenta");
        volver.setOnAction(e -> mostrarAcceso());

        VBox links = new VBox(volver);
        links.setAlignment(Pos.CENTER);

        formArea.getChildren().setAll(nombre, identificacion, correoReg, claveReg, crear, error, links);
    }

    private VBox buildHero() {
        Label l1 = new Label("¡BIENVENIDO A");
        l1.getStyleClass().add("hero-line-white");
        Label l2 = new Label("CINEMA UQ!");
        l2.getStyleClass().add("hero-line-red");
        Label sub = new Label("Más que cine, emociones.");
        sub.getStyleClass().add("hero-sub");
        VBox titulos = new VBox(l1, l2);
        titulos.setFillWidth(false);

        HBox fila = new HBox(20);
        fila.setPadding(new Insets(22, 20, 21, 8));
        int n = 0;
        for (Pelicula p : ctx.getDataStore().getPeliculas()) {
            if (n++ >= 10) break;
            fila.getChildren().add(crearMiniPoster(p));
        }

        ScrollPane tira = new ScrollPane(fila);
        tira.setFitToHeight(true);
        tira.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tira.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tira.setPannable(true);
        tira.getStyleClass().add("transparent-scroll");
        tira.setMinHeight(250);
        tira.setPrefHeight(250);

        VBox hero = new VBox(10, titulos, sub, tira);
        hero.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hero, Priority.ALWAYS);
        hero.setMinWidth(0);

        Runnable ajustar = () -> ajustarTitulos(l1, l2, titulos, hero.getWidth(), cuerpo.getHeight());
        hero.widthProperty().addListener((o, a, b) -> ajustar.run());
        cuerpo.heightProperty().addListener((o, a, b) -> ajustar.run());
        return hero;
    }

    private static String familiaBrutalista() {
        var instaladas = Font.getFamilies();
        for (String f : FAMILIAS_BRUTALISTAS) if (instaladas.contains(f)) return f;
        return null;
    }

    private void ajustarTitulos(Label l1, Label l2, VBox contenedor, double anchoHero, double altoCuerpo) {
        double ancho = anchoHero - 6;
        double alto = altoCuerpo - 64 - RESERVA_DEBAJO_DEL_TITULO;
        if (ancho <= 0 || alto <= 0) return;

        String familia = familiaBrutalista();
        Font medidor = familia != null ? Font.font(familia, FontWeight.NORMAL, 100)
                                       : Font.font("Arial", FontWeight.BOLD, 100);
        Text m1 = new Text(l1.getText()); m1.setFont(medidor);
        Text m2 = new Text(l2.getText()); m2.setFont(medidor);

        double s1 = ancho / m1.getLayoutBounds().getWidth() * 100;
        double s2 = ancho / m2.getLayoutBounds().getWidth() * 100;
        double solape = 0.20;
        double altoTotal = s1 * m1.getLayoutBounds().getHeight() / 100
                         + s2 * m2.getLayoutBounds().getHeight() / 100 - s2 * solape;
        if (altoTotal > alto) {
            double f = alto / altoTotal;
            s1 *= f; s2 *= f;
        }
        s1 = Math.max(s1, 28); s2 = Math.max(s2, 36);

        String base = familia != null ? "-fx-font-family: '" + familia + "'; -fx-font-weight: normal;"
                                      : "-fx-font-family: 'Arial'; -fx-font-weight: bold;";
        l1.setStyle(base + " -fx-font-size: " + Math.floor(s1) + "px;");
        l2.setStyle(base + " -fx-font-size: " + Math.floor(s2) + "px;");
        contenedor.setSpacing(-Math.floor(s2) * solape);
    }

    private StackPane crearMiniPoster(Pelicula p) {
        StackPane poster = new StackPane(UiKit.poster(p.getUrlPoster(), 140, 200, 12));

        Label badge = new Label(p.getClasificacion());
        badge.getStyleClass().add("poster-badge");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(8));
        poster.getChildren().add(badge);

        poster.setMaxSize(900, 1000);
        poster.getStyleClass().add("poster-card");
        UiEffects.attachCardHover(poster);
        return poster;
    }
}
