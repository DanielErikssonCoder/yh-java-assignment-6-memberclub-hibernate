package com.eriksson.rentalsystemhibernate3.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class MapController {

    @FXML
    private Pane mapContainer;

    @FXML
    public void initialize() {
        animateNodes();
    }

    private void animateNodes() {

        for (Node node : mapContainer.getChildren()) {

            // Animates circles with looping scale and fade
            if (node instanceof Circle) {

                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(2000), node);
                scaleTransition.setByX(0.1);
                scaleTransition.setByY(0.1);
                scaleTransition.setCycleCount(Animation.INDEFINITE);
                scaleTransition.setAutoReverse(true);
                scaleTransition.play();

                FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500 + Math.random() * 1000), node);
                fadeTransition.setFromValue(0.6);
                fadeTransition.setToValue(1.0);
                fadeTransition.setCycleCount(Animation.INDEFINITE);
                fadeTransition.setAutoReverse(true);
                fadeTransition.play();
            }
        }
    }
}