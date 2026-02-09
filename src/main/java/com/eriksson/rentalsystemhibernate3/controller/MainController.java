package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.DataSeeder;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.WindowResizer;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.hibernate.SessionFactory;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    @FXML private SidebarController sidebarController;
    @FXML private VBox offlineOverlay;

    private MemberService memberService;
    private GamingComputerService gamingService;
    private LaptopService laptopService;
    private WorkstationService workstationService;

    /**
     * Sets up services, data, sidebar, DB checker and loads default view
     */
    @FXML
    public void initialize() {
        setupServices();
        seedData();

        if (sidebarController != null) {
            sidebarController.setMainController(this);
            startDatabaseStatusChecker();
        }

        setCenterView("dashboard-view.fxml");

        Platform.runLater(() -> {

            // Configures window minimum size and enables resizability
            if (rootPane != null && rootPane.getScene() != null) {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setMinWidth(1280);
                stage.setMinHeight(800);
                WindowResizer.addResizability(stage, rootPane);
            }
        });
    }

    /**
     * Initiates recurring database status polling
     */
    private void startDatabaseStatusChecker() {
        refreshDbStatus();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            refreshDbStatus();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Updates sidebar and overlay based on database connectivity
     */
    @FXML
    public void refreshDbStatus() {
        boolean isOnline = HibernateUtil.checkConnection();
        if (sidebarController != null) {
            sidebarController.updateDatabaseStatus(isOnline);
            sidebarController.setSidebarDisabled(!isOnline);
        }
        if (offlineOverlay != null) {
            offlineOverlay.setVisible(!isOnline);
        }
    }

    private void setupServices() {

        // Initializes services with Hibernate and logs init errors
        try {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            if (sessionFactory == null) return;

            this.memberService = new MemberService(new MemberRepositoryImpl(sessionFactory), new RentalRepositoryImpl(sessionFactory));
            this.gamingService = new GamingComputerService(new GamingComputerRepositoryImpl(sessionFactory));
            this.laptopService = new LaptopService(new LaptopRepositoryImpl(sessionFactory));
            this.workstationService = new WorkstationService(new WorkstationRepositoryImpl(sessionFactory));
        } catch (Exception e) {
            System.err.println("Fel vid initiering av tjänster.");
        }
    }

    private void seedData() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf != null) {
            DataSeeder.seedIfEmpty(sf);
        }
    }

    public void setCenterView(String fxmlFile) {

        // Loads view, clears content, applies fade‑in and slide‑up animations
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eriksson/rentalsystemhibernate3/" + fxmlFile));
            Parent newView = loader.load();
            newView.setOpacity(0);
            newView.setTranslateY(15);

            // Replaces center view with animated transition
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(newView);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newView);
                fadeIn.setToValue(1);
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), newView);
                slideUp.setToY(0);
                fadeIn.play();
                slideUp.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleMinimize() {

        // Iconifies application window when minimize clicked
        if (rootPane.getScene() != null) ((Stage) rootPane.getScene().getWindow()).setIconified(true);
    }

    @FXML private void handleMaximize() {

        // Toggles application window between maximized and normal states
        if (rootPane.getScene() != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML private void handleClose() {
        HibernateUtil.shutdown();
        Platform.exit();
        System.exit(0);
    }

    @FXML private void handleTitleBarClick(javafx.scene.input.MouseEvent event) {

        // Maximizes window when primary mouse double‑clicked on title bar
        if (event.getButton().equals(javafx.scene.input.MouseButton.PRIMARY) && event.getClickCount() == 2) handleMaximize();
    }
}