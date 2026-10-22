package com.cinemauq.view.util;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class UiEffects {

    private UiEffects() {}

    public static void attachCardHover(Node node) {
        attach(node, 1.035, -6, 26, 0.55);
    }

    public static void attachButtonHover(Node node) {
        attach(node, 1.02, -2, 16, 0.45);
    }

    private static void attach(Node node, double scaleTo, double translateYTo, double shadowRadius, double shadowOpacity) {
        DropShadow shadowReposo = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.35), 12, 0, 0, 6);
        DropShadow shadowHover = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, shadowOpacity), shadowRadius, 0, 0, 10);
        shadowHover.setColor(Color.rgb(0, 0, 0, shadowOpacity));
        node.setEffect(shadowReposo);

        ScaleTransition growth = new ScaleTransition(Duration.millis(160), node);
        growth.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition lift = new TranslateTransition(Duration.millis(160), node);
        lift.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            growth.stop();
            lift.stop();
            growth.setToX(scaleTo);
            growth.setToY(scaleTo);
            lift.setToY(translateYTo);
            node.setEffect(shadowHover);
            growth.playFromStart();
            lift.playFromStart();
        });

        node.setOnMouseExited(e -> {
            growth.stop();
            lift.stop();
            growth.setToX(1.0);
            growth.setToY(1.0);
            lift.setToY(0);
            growth.playFromStart();
            lift.playFromStart();
            node.setEffect(shadowReposo);
        });
    }
}
