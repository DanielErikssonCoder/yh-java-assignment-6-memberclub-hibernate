package com.eriksson.rentalsystemhibernate3.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class AlertHelper {

    /**
     * Displays a transparent styled alert and returns user choice
     */
    public static Optional<ButtonType> showStyledAlert(Node ownerNode, Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);

        DialogPane dp = alert.getDialogPane();

        dp.setStyle("-fx-background-color: #171717; " +
                "-fx-border-color: #3ECF8E; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;");

        dp.setMinHeight(Region.USE_PREF_SIZE);
        dp.setMaxWidth(450);

        Label contentLabel = (Label) dp.lookup(".label.content");

        // Formats alert content label for readable styled text
        if (contentLabel != null) {
            contentLabel.setWrapText(true);
            contentLabel.setPrefWidth(400);
            contentLabel.setMaxHeight(Double.MAX_VALUE);
            contentLabel.setStyle("-fx-text-fill: #EDEDED; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 20 15 15 15;");
        }

        Button okButton = (Button) dp.lookupButton(ButtonType.OK);
        if (okButton != null) {
            stylePrimaryButton(okButton);
        }

        Button cancelButton = (Button) dp.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            styleSecondaryButton(cancelButton);
        }

        Label headerLabel = new Label(title.toUpperCase());
        headerLabel.setStyle("-fx-text-fill: #3ECF8E; -fx-font-weight: bold; -fx-font-size: 12px; -fx-letter-spacing: 1px;");
        VBox topBox = new VBox(headerLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(15, 0, 0, 20));
        dp.setHeader(topBox);

        // Blur & Dim
        BoxBlur blur = new BoxBlur(5, 5, 3);
        ColorAdjust dim = new ColorAdjust(0, 0, -0.3, 0);
        dim.setInput(blur);

        if (ownerNode != null) {
            ownerNode.setEffect(dim);
        }

        stage.sizeToScene();

        Optional<ButtonType> result = alert.showAndWait();

        if (ownerNode != null) {
            ownerNode.setEffect(null);
        }

        return result;
    }

    /**
     * Sets primary button to bold green style with hover effect
     */
    private static void stylePrimaryButton(Button btn) {
        String base = "-fx-background-color: #3ECF8E; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;";
        String hover = "-fx-background-color: #6EE7B7; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 20;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    /**
     * Applies transparent secondary button with hover darkening
     */
    private static void styleSecondaryButton(Button btn) {
        String base = "-fx-background-color: transparent; -fx-text-fill: #9BA1A6; -fx-cursor: hand; -fx-border-color: #333; -fx-border-radius: 5; -fx-padding: 8 20;";
        String hover = "-fx-background-color: #222; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: #444; -fx-border-radius: 5; -fx-padding: 8 20;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}