package com.cinemauq.view.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public final class UiKit {

    private UiKit() {}

    public static HBox logo(String styleClass) {
        Label cinema = new Label("cinema");
        cinema.getStyleClass().addAll("logo-text", "logo-white");
        Label uq = new Label("UQ");
        uq.getStyleClass().addAll("logo-text", "logo-red");
        HBox box = new HBox(0, cinema, uq);
        box.setAlignment(Pos.CENTER);
        if (styleClass != null) box.getStyleClass().add(styleClass);
        return box;
    }

    public static HBox tituloBrutalista(String blanco, String rojo) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.BASELINE_LEFT);
        box.getStyleClass().add("brutal-title-box");
        if (blanco != null && !blanco.isBlank()) {
            Label l = new Label(blanco.toUpperCase());
            l.getStyleClass().addAll("brutal-title", "brutal-white");
            box.getChildren().add(l);
        }
        if (rojo != null && !rojo.isBlank()) {
            Label l = new Label(rojo.toUpperCase());
            l.getStyleClass().addAll("brutal-title", "brutal-red");
            box.getChildren().add(l);
        }
        return box;
    }

    public static HBox tituloCompacto(String blanco, String rojo) {
        HBox box = tituloBrutalista(blanco, rojo);
        box.getStyleClass().add("brutal-sm");
        return box;
    }

    public static ImageView poster(String url, double w, double h, double radio) {
        ImageView iv = new ImageView();
        if (url != null && !url.isBlank()) {
            try {
                iv.setImage(new Image(url, w, h, false, true, true));
            } catch (IllegalArgumentException ignored) { }
        }
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(radio * 2);
        clip.setArcHeight(radio * 2);
        iv.setClip(clip);
        return iv;
    }

    public static Label seccion(String texto) {
        Label l = new Label(texto.toUpperCase());
        l.getStyleClass().add("section-title");
        return l;
    }

    public static Label subtitulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("view-subtitle");
        l.setWrapText(true);
        return l;
    }

    public static Button primario(String texto) {
        Button b = new Button(texto);
        b.getStyleClass().add("btn-primary");
        UiEffects.attachButtonHover(b);
        return b;
    }

    public static Button secundario(String texto) {
        Button b = new Button(texto);
        b.getStyleClass().add("btn-secondary");
        UiEffects.attachButtonHover(b);
        return b;
    }

    public static Button peligro(String texto) {
        Button b = new Button(texto);
        b.getStyleClass().add("btn-danger");
        UiEffects.attachButtonHover(b);
        return b;
    }

    public static Button enlace(String texto) {
        Button b = new Button(texto);
        b.getStyleClass().add("btn-link");
        return b;
    }

    public static Region espaciador() {
        Region r = new Region();
        HBox.setHgrow(r, javafx.scene.layout.Priority.ALWAYS);
        return r;
    }
}
