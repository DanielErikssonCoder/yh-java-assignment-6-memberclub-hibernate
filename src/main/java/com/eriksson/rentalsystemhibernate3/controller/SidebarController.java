package com.eriksson.rentalsystemhibernate3.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SidebarController {

    @FXML private VBox sidebarRoot;

    @FXML private HBox btnDashboard, btnRentals, btnComputers, btnMembers, btnSettings;

    @FXML private Region indicatorDashboard, indicatorRentals, indicatorComputers, indicatorMembers, indicatorSettings;

    @FXML private Region dbStatusDot;

    private MainController mainController;
    private FadeTransition pulse;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    public void setSidebarDisabled(boolean disabled) {
        if (sidebarRoot != null) {
            sidebarRoot.setDisable(disabled);
        }
    }

    @FXML
    public void initialize() {

        resetState();

        // Activates dashboard UI and shows its indicator
        if (btnDashboard != null) {
            btnDashboard.getStyleClass().add("active");

            if (indicatorDashboard != null) {
                indicatorDashboard.setVisible(true);
            }
        }
        setupPulseAnimation();
    }

    /**
     * Creates continuous pulsing animation for database status indicator
     */
    private void setupPulseAnimation() {
        pulse = new FadeTransition(Duration.seconds(1.5), dbStatusDot);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.3);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    public void updateDatabaseStatus(boolean isOnline) {

        // Updates DB status dot color and pulse behavior
        if (isOnline) {
            dbStatusDot.setStyle("-fx-background-color: #3ECF8E; -fx-background-radius: 50;");

            if (pulse.getStatus() != Animation.Status.RUNNING) {
                pulse.play();
            }

        } else {
            dbStatusDot.setStyle("-fx-background-color: #FF4D4D; -fx-background-radius: 50;");
            pulse.stop();
            dbStatusDot.setOpacity(1.0);
        }
    }


    @FXML
    private void handleDashboard(MouseEvent event) {

        updateActiveState(btnDashboard, indicatorDashboard);

        if (mainController != null) {
            mainController.setCenterView("dashboard-view.fxml");
        }
    }

    @FXML
    private void handleRentals(MouseEvent event) {

        updateActiveState(btnRentals, indicatorRentals);

        if (mainController != null) {
            mainController.setCenterView("rental-view.fxml");
        }
    }

    @FXML
    private void handleComputers(MouseEvent event) {

        updateActiveState(btnComputers, indicatorComputers);

        if (mainController != null) {
            mainController.setCenterView("computer-view.fxml");
        }
    }

    @FXML
    private void handleMembers(MouseEvent event) {

        updateActiveState(btnMembers, indicatorMembers);

        if (mainController != null) {
            mainController.setCenterView("member-view.fxml");
        }
    }

    @FXML
    private void handleSettings(MouseEvent event) {

        updateActiveState(btnSettings, indicatorSettings);

        if (mainController != null) {
            mainController.setCenterView("settings-view.fxml");
        }
    }


    /**
     * Resets all views and activates chosen button and its indicator
     */
    private void updateActiveState(HBox btn, Region indicator) {

        resetState();

        if (btn != null) {
            btn.getStyleClass().add("active");
        }

        if (indicator != null) {
            indicator.setVisible(true);
        }
    }

    /**
     * Resets UI components to default inactive state
     */
    private void resetState() {

        HBox[] buttons = {btnDashboard, btnRentals, btnComputers, btnMembers, btnSettings};
        Region[] indicators = {indicatorDashboard, indicatorRentals, indicatorComputers, indicatorMembers, indicatorSettings};

        for (HBox b : buttons) {
            if (b != null) b.getStyleClass().remove("active");
        }
        for (Region r : indicators) {
            if (r != null) r.setVisible(false);
        }
    }
}