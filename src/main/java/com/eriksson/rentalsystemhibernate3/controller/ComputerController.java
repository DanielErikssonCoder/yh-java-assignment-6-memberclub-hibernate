package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.SessionFactory;

import java.io.IOException;

public class ComputerController {

    @FXML private VBox blurTarget;
    @FXML private TableView<Object> computerTable;
    @FXML private TableColumn<Object, Long> colId;
    @FXML private TableColumn<Object, String> colModel; // Visar 'name'
    @FXML private TableColumn<Object, String> colType;
    @FXML private TableColumn<Object, String> colSpecs;
    @FXML private TableColumn<Object, String> colHourlyPrice;
    @FXML private TableColumn<Object, String> colPrice; // Visar 'dailyPrice'
    @FXML private TableColumn<Object, String> colStatus;
    @FXML private TextField searchField;

    private GamingComputerService gamingService;
    private LaptopService laptopService;
    private WorkstationService workstationService;

    private final ObservableList<Object> masterData = FXCollections.observableArrayList();

    /**
     * Initializes services, table, data loading and search
     */
    @FXML
    public void initialize() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.gamingService = new GamingComputerService(new GamingComputerRepositoryImpl(sessionFactory));
        this.laptopService = new LaptopService(new LaptopRepositoryImpl(sessionFactory));
        this.workstationService = new WorkstationService(new WorkstationRepositoryImpl(sessionFactory));

        setupTable();
        loadComputers();
        setupSearch();
    }

    /**
     * Configures table columns to display device attributes
     */
    private void setupTable() {

        // Extracts device identifier for table column
        colId.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof GamingComputer c) return new SimpleObjectProperty<>(c.getComputerId());
            if (item instanceof Laptop l) return new SimpleObjectProperty<>(l.getComputerId());
            if (item instanceof Workstation w) return new SimpleObjectProperty<>(w.getComputerId());
            return null;
        });

        // Retrieves device name or placeholder when absent
        colModel.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof GamingComputer c) return new SimpleStringProperty(c.getName());
            if (item instanceof Laptop l) return new SimpleStringProperty(l.getName());
            if (item instanceof Workstation w) return new SimpleStringProperty(w.getName());
            return new SimpleStringProperty("-");
        });

        // Provides localized type label based on instance
        colType.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof GamingComputer) return new SimpleStringProperty("Gamingdator");
            if (item instanceof Laptop) return new SimpleStringProperty("Bärbar dator");
            if (item instanceof Workstation) return new SimpleStringProperty("Arbetsstation");
            return new SimpleStringProperty("Okänd");
        });

        colHourlyPrice.setCellValueFactory(cellData -> {
            double price = 0;
            Object item = cellData.getValue();

            // Selects hourly price from matching computer type
            if (item instanceof GamingComputer c) price = c.getHourlyPrice();
            else if (item instanceof Laptop l) price = l.getHourlyPrice();
            else if (item instanceof Workstation w) price = w.getHourlyPrice();
            return new SimpleStringProperty(String.format("%.2f kr", price));
        });

        colPrice.setCellValueFactory(cellData -> {
            double price = 0;
            Object item = cellData.getValue();

            // Computes daily price for each computer category
            if (item instanceof GamingComputer c) price = c.getDailyPrice();
            else if (item instanceof Laptop l) price = l.getDailyPrice();
            else if (item instanceof Workstation w) price = w.getDailyPrice();
            return new SimpleStringProperty(String.format("%.2f kr", price));
        });

        // Formats hardware specifications per computer category
        colSpecs.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            if (item instanceof GamingComputer c)
                return new SimpleStringProperty(c.getGraphicsCard() + " | " + c.getRam() + "GB RAM");

            if (item instanceof Laptop l)
                return new SimpleStringProperty(l.getScreenSize() + "\" | " + l.getRam() + "GB RAM");

            if (item instanceof Workstation w)
                return new SimpleStringProperty(w.getGpuConfig() + " | " + w.getRam() + "GB RAM");

            return new SimpleStringProperty("-");
        });

        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty("Tillgänglig"));
    }

    /**
     * Refreshes and populates computer list
     */
    private void loadComputers() {
        masterData.clear();
        masterData.addAll(gamingService.getAllGamingComputers());
        masterData.addAll(laptopService.getAllLaptops());
        masterData.addAll(workstationService.getAllWorkstations());
        computerTable.setItems(masterData);
    }

    public void refreshData() {
        loadComputers();
    }

    private void setupSearch() {
        FilteredList<Object> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            // Filters computer list to match search query
            filteredData.setPredicate(item -> {

                if (newVal == null || newVal.isEmpty()) return true;

                String filter = newVal.toLowerCase();
                String name = "";
                String id = "";

                // Extracts searchable name and ID from computer variants
                if (item instanceof GamingComputer c) {
                    name = c.getName();
                    id = String.valueOf(c.getComputerId());
                }
                else if (item instanceof Laptop l) {
                    name = l.getName();
                    id = String.valueOf(l.getComputerId());
                }
                else if (item instanceof Workstation w) {
                    name = w.getName();
                    id = String.valueOf(w.getComputerId());
                }

                return name.toLowerCase().contains(filter) || id.contains(filter);
            });
        });

        computerTable.setItems(filteredData);
    }

    @FXML
    private void handleAddComputer() {

        // Opens add‑computer dialog with blur and error handling
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eriksson/rentalsystemhibernate3/add-computer-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(null);

            String cssPath = "/style.css";

            var cssUrl = getClass().getResource(cssPath);

            // Applies hard‑coded background and border styling
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                root.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 12; -fx-border-color: #2e2e2e; -fx-border-radius: 12;");
            }

            AddComputerController controller = loader.getController();
            controller.setParentController(this);

            BoxBlur blur = new BoxBlur(5, 5, 3);
            ColorAdjust dim = new ColorAdjust(0, 0, -0.3, 0);
            dim.setInput(blur);

            if (blurTarget != null) {
                blurTarget.setEffect(dim);
            }

            stage.setScene(scene);
            stage.showAndWait();

            if (blurTarget != null) {
                blurTarget.setEffect(null);
            }

        } catch (IOException ex) {
            ex.printStackTrace(); // Bra för debugging
            AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.ERROR, "Laddningsfel", "Kunde inte öppna registreringsfönstret.");
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showStyledAlert(blurTarget, Alert.AlertType.ERROR, "Fel", "Ett oväntat fel uppstod: " + ex.getMessage());
        }
    }
}