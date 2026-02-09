package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.util.AlertHelper;
import com.eriksson.rentalsystemhibernate3.util.DataSeeder;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Optional;

public class SettingsController {

    @FXML private VBox blurTarget;
    @FXML private Label lblStatus, lblTotalMembers, lblActiveRentals;
    @FXML private Circle statusCircle;

    @FXML
    public void initialize() {
        refreshDiagnostics();
    }

    @FXML
    private void refreshDiagnostics() {
        boolean isOnline = HibernateUtil.checkConnection();

        // Updates UI status and colors based on database connectivity
        if (isOnline) {
            lblStatus.setText("ANSLUTEN");
            lblStatus.setTextFill(Color.web("#3ECF8E"));
            statusCircle.setFill(Color.web("#3ECF8E"));

            lblTotalMembers.setText("Aktiv");
            lblActiveRentals.setText("STABIL");
            lblActiveRentals.setTextFill(Color.web("#3ECF8E"));

        } else {
            lblStatus.setText("INGEN KONTAKT");
            lblStatus.setTextFill(Color.web("#FF4D4D"));
            statusCircle.setFill(Color.web("#FF4D4D"));

            lblTotalMembers.setText("---");
            lblActiveRentals.setText("KRITISK");
            lblActiveRentals.setTextFill(Color.web("#FF4D4D"));
        }
    }

    /**
     * Tests DB connection and shows styled alert accordingly
     */
    @FXML
    private void handleTestConnection() {

        refreshDiagnostics();

        if (HibernateUtil.checkConnection()) {
            AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.INFORMATION, "Systemcheck", "Anslutningen till MySQL-servern fungerar korrekt.");

        } else {
            AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.ERROR, "Anslutningsfel", "Kunde inte etablera kontakt med databasen.");
        }
    }


    /**
     * Resets database to default state after user confirmation
     */
    @FXML
    private void handleSeedData() {

        Optional<ButtonType> result = AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.CONFIRMATION,
                "Systemåterställning",
                "VARNING: Detta raderar all nuvarande data (medlemmar, uthyrningar, inventarie) och ersätter det med standardsortimentet. Vill du verkligen fortsätta?");

        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Resets database then refreshes diagnostics, informs user or reports error
            try {

                DataSeeder.forceSeed(HibernateUtil.getSessionFactory());

                refreshDiagnostics();

                AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.INFORMATION, "System återställt",
                        "Databasen har rensats och det kompletta standardsortimentet har laddats in.");

            } catch (Exception e) {
                AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.ERROR, "Systemfel",
                        "Ett oväntat fel uppstod vid återställningen: " + e.getMessage());
            }
        }
    }
}