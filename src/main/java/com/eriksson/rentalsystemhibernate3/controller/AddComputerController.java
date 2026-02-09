package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.RentalType;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.SessionFactory;

/**
 * Controller for the pop-up window to add new computers.
 * Handles dynamic UI updates for all computer types and persists data.
 */
public class AddComputerController {

    @FXML private ComboBox<RentalType> typeComboBox;
    @FXML private TextField modelField, cpuField, ramField, ssdField, gpuField, screenField, hourlyPriceField, dailyPriceField;

    @FXML private TextField coresField, displaysField, batteryField;
    @FXML private CheckBox touchCheckBox;

    @FXML private VBox gpuBox, screenBox, coresBox, displaysBox, batteryBox, touchBox;
    @FXML private Button saveButton;
    @FXML private VBox rootNode; // Denna används nu för blur-effekten

    private GamingComputerService gamingService;
    private LaptopService laptopService;
    private WorkstationService workstationService;
    private ComputerController parentController;

    /**
     * Initializes services, configures UI, sets listeners
     */
    @FXML
    public void initialize() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.gamingService = new GamingComputerService(new GamingComputerRepositoryImpl(sessionFactory));
        this.laptopService = new LaptopService(new LaptopRepositoryImpl(sessionFactory));
        this.workstationService = new WorkstationService(new WorkstationRepositoryImpl(sessionFactory));

        typeComboBox.setItems(FXCollections.observableArrayList(RentalType.values()));
        typeComboBox.setCellFactory(lv -> createCategoryListCell());
        typeComboBox.setButtonCell(createCategoryListCell());

        configureInputStyles(typeComboBox, modelField, cpuField, ramField, ssdField,
                gpuField, screenField, hourlyPriceField, dailyPriceField,
                coresField, displaysField, batteryField, touchCheckBox);

        configurePrimaryButton(saveButton);

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldVisibility(newVal);

            // Adjusts visible fields and resizes window on selection
            if (typeComboBox.getScene() != null) {
                Stage stage = (Stage) typeComboBox.getScene().getWindow();
                if (stage != null) stage.sizeToScene();
            }
        });

        updateFieldVisibility(null);

        Platform.runLater(() -> {
            if (rootNode != null) rootNode.requestFocus();
        });
    }

    private void configureInputStyles(Control... controls) {
        String baseStyle = "-fx-background-color: #0F0F0F; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #393939; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-prompt-text-fill: #555555; " +
                "-fx-highlight-fill: #3ECF8E; " +
                "-fx-highlight-text-fill: black;";

        for (Control c : controls) {

            c.setStyle(baseStyle);

            // Switches control styling between focused and unfocused states
            c.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    c.setStyle(baseStyle + "-fx-border-color: #3ECF8E; -fx-background-color: #050505;");
                } else {
                    c.setStyle(baseStyle);
                }
            });
        }
    }

    /**
     * Sets primary button styling; defines hover color changes
     */
    private void configurePrimaryButton(Button button) {
        if (button == null) return;
        String baseStyle = "-fx-background-color: #3ECF8E; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;";
        String hoverStyle = "-fx-background-color: #6EE7B7; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;";
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }

    private ListCell<RentalType> createCategoryListCell() {

        return new ListCell<>() {

            @Override
            protected void updateItem(RentalType item, boolean empty) {
                super.updateItem(item, empty);

                // Sets cell content and interactive styling based on rental type
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #0F0F0F;");
                } else {
                    switch (item) {
                        case GAMING_COMPUTER -> setText("Gamingdator");
                        case LAPTOP -> setText("Bärbar dator");
                        case WORKSTATION -> setText("Arbetsstation");
                    }
                    setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-padding: 8 12;");
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #262626; -fx-text-fill: #3ECF8E; -fx-padding: 8 12;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-padding: 8 12;"));
                }
            }
        };
    }

    /**
     * Adjusts UI element visibility per device type
     */
    private void updateFieldVisibility(RentalType type) {
        boolean isGaming = (type == RentalType.GAMING_COMPUTER);
        boolean isLaptop = (type == RentalType.LAPTOP);
        boolean isWorkstation = (type == RentalType.WORKSTATION);

        gpuBox.setVisible(isGaming || isWorkstation);
        gpuBox.setManaged(isGaming || isWorkstation);

        screenBox.setVisible(isLaptop);
        screenBox.setManaged(isLaptop);
        batteryBox.setVisible(isLaptop);
        batteryBox.setManaged(isLaptop);
        touchBox.setVisible(isLaptop);
        touchBox.setManaged(isLaptop);

        coresBox.setVisible(isWorkstation);
        coresBox.setManaged(isWorkstation);
        displaysBox.setVisible(isWorkstation);
        displaysBox.setManaged(isWorkstation);
    }

    @FXML
    private void handleSave() {

        // Saves selected computer, handles validation and errors
        try {
            RentalType selectedType = typeComboBox.getValue();
            if (selectedType == null) {
                AlertHelper.showStyledAlert(rootNode, Alert.AlertType.WARNING, "Kategori saknas", "Vänligen välj en kategori för datorn.");
                return;
            }

            String model = modelField.getText();
            String cpu = cpuField.getText();
            String ssd = ssdField.getText();

            int ram;
            double hourly, daily;

            // Validates and parses numeric input values
            try {
                ram = Integer.parseInt(ramField.getText());
                hourly = Double.parseDouble(hourlyPriceField.getText().replace(",", "."));
                daily = Double.parseDouble(dailyPriceField.getText().replace(",", "."));
            } catch (NumberFormatException ex) {
                AlertHelper.showStyledAlert(rootNode, Alert.AlertType.ERROR, "Formatfel", "RAM och priser måste vara numeriska värden.");
                return;
            }

            // Adds chosen computer variant using parsed specifications
            switch (selectedType) {
                case GAMING_COMPUTER -> gamingService.addGamingComputer(model, cpu, ram, ssd, gpuField.getText(), hourly, daily);
                case LAPTOP -> {
                    double screenSize = Double.parseDouble(screenField.getText().replace(",", "."));
                    int battery = Integer.parseInt(batteryField.getText());
                    boolean hasTouch = touchCheckBox.isSelected();
                    laptopService.addLaptop(model, cpu, ram, ssd, screenSize, battery, hasTouch, hourly, daily);
                }
                case WORKSTATION -> {
                    int cores = Integer.parseInt(coresField.getText());
                    int displays = Integer.parseInt(displaysField.getText());
                    workstationService.addWorkstation(model, cpu, cores, ram, displays, ssd, gpuField.getText(), hourly, daily);
                }
            }

            if (parentController != null) parentController.refreshData();
            handleCancel();

        } catch (IllegalArgumentException ex) {
            AlertHelper.showStyledAlert(rootNode, Alert.AlertType.ERROR, "Valideringsfel", ex.getMessage());
        } catch (Exception ex) {
            AlertHelper.showStyledAlert(rootNode, Alert.AlertType.ERROR, "Systemfel", "Kunde inte spara datorn: " + ex.getMessage());
        }
    }

    public void setParentController(ComputerController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleCancel() { ((Stage) typeComboBox.getScene().getWindow()).close(); }
}