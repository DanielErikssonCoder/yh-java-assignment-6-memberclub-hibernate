package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.exception.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RentalController {

    @FXML private VBox rootPane;
    @FXML private ComboBox<Member> memberComboBox;
    @FXML private ComboBox<RentalType> typeComboBox;
    @FXML private ComboBox<Object> computerComboBox;
    @FXML private TextField durationField;
    @FXML private Label totalCostLabel;
    @FXML private RadioButton radioHours, radioDays;
    @FXML private ToggleGroup pricingGroup;
    @FXML private Button rentButton;

    @FXML private TableView<Rental> rentalTable;
    @FXML private TableColumn<Rental, Long> colRentalId;
    @FXML private TableColumn<Rental, String> colMemberId, colStartDate, colEndDate, colTotalPrice;
    @FXML private TableColumn<Rental, Long> colObjectId;
    @FXML private TableColumn<Rental, String> colType;
    @FXML private TableColumn<Rental, Void> colActions;

    private RentalService rentalService;
    private MemberService memberService;
    private GamingComputerService gamingService;
    private LaptopService laptopService;
    private WorkstationService workstationService;

    private final ObservableList<Rental> masterData = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initializes services, configures UI, and sets focus
     */
    @FXML
    public void initialize() {
        initServices();
        setupTable();
        loadFormData();

        applyThemeStyles(memberComboBox, typeComboBox, computerComboBox, durationField);
        applyPrimaryButtonStyle(rentButton);

        memberComboBox.setCellFactory(lv -> createMemberCell());
        memberComboBox.setButtonCell(createMemberCell());
        typeComboBox.setCellFactory(lv -> createTypeCell());
        typeComboBox.setButtonCell(createTypeCell());
        computerComboBox.setCellFactory(lv -> createComputerCell());
        computerComboBox.setButtonCell(createComputerCell());

        durationField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalCost());
        computerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateTotalCost());
        radioHours.selectedProperty().addListener((obs, oldVal, newVal) -> updateTotalCost());
        radioDays.selectedProperty().addListener((obs, oldVal, newVal) -> updateTotalCost());

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            loadAvailableComputers(newVal);
            updateTotalCost();
        });

        Platform.runLater(() -> rentalTable.requestFocus());
    }

    /**
     * Initializes all domain services using Hibernate session
     */
    private void initServices() {

        SessionFactory sf = HibernateUtil.getSessionFactory();

        // Instantiates services using Hibernate repositories
        this.rentalService = new RentalService(new RentalRepositoryImpl(sf), new MemberRepositoryImpl(sf), new GamingComputerRepositoryImpl(sf), new LaptopRepositoryImpl(sf), new WorkstationRepositoryImpl(sf));
        this.memberService = new MemberService(new MemberRepositoryImpl(sf), new RentalRepositoryImpl(sf));
        this.gamingService = new GamingComputerService(new GamingComputerRepositoryImpl(sf));
        this.laptopService = new LaptopService(new LaptopRepositoryImpl(sf));
        this.workstationService = new WorkstationService(new WorkstationRepositoryImpl(sf));
    }

    private void updateTotalCost() {

        // Computes rental fee from selection, resets on bad input
        try {

            Object selectedComputer = computerComboBox.getValue();
            String durationText = durationField.getText().replace(",", ".");

            if (selectedComputer == null || durationText.isEmpty()) {
                totalCostLabel.setText("0.00 kr");
                return;
            }

            double duration = Double.parseDouble(durationText);
            double price = 0;

            // Chooses daily or hourly rate per computer type
            if (selectedComputer instanceof GamingComputer c) {
                price = radioDays.isSelected() ? c.getDailyPrice() : c.getHourlyPrice();

            }
            else if (selectedComputer instanceof Laptop l) {
                price = radioDays.isSelected() ? l.getDailyPrice() : l.getHourlyPrice();

            }
            else if (selectedComputer instanceof Workstation w) {
                price = radioDays.isSelected() ? w.getDailyPrice() : w.getHourlyPrice();
            }

            totalCostLabel.setText(String.format("%.2f kr", price * duration));

        } catch (NumberFormatException e) {
            totalCostLabel.setText("0.00 kr");
        }
    }

    /**
     * Loads and filters active computer inventory
     */
    private void loadAvailableComputers(RentalType type) {

        computerComboBox.getItems().clear();

        if (type == null) {
            return;
        }

        ObservableList<Object> allComputers = FXCollections.observableArrayList();

        // Adds all computers of the chosen rental type to the selection list
        switch (type) {
            case GAMING_COMPUTER -> allComputers.addAll(gamingService.getAllGamingComputers());
            case LAPTOP -> allComputers.addAll(laptopService.getAllLaptops());
            case WORKSTATION -> allComputers.addAll(workstationService.getAllWorkstations());
        }

        // Filters active rentals of selected type to identify current usage
        List<Rental> activeRentals = rentalService.getAllRentals().stream()
                .filter(r -> r.getEndDate() == null)
                .filter(r -> r.getRentalType() == type)
                .toList();

        Set<Long> rentedIds = activeRentals.stream()
                .map(Rental::getRentalObjectId)
                .collect(Collectors.toSet());

        List<Object> availableComputers = allComputers.stream()
                .filter(c -> !rentedIds.contains(getComputerId(c)))
                .toList();

        computerComboBox.setItems(FXCollections.observableArrayList(availableComputers));
    }

    /**
     * Maps various computer objects to their unique identifiers
     */
    private Long getComputerId(Object computer) {

        if (computer instanceof GamingComputer c) {
            return c.getComputerId();
        }

        if (computer instanceof Laptop l) {
            return l.getComputerId();
        }

        if (computer instanceof Workstation w) {
            return w.getComputerId();
        }

        return -1L;
    }

    /**
     * Configures rental table columns, actions, and refreshes data
     */
    private void setupTable() {

        colRentalId.setCellValueFactory(new PropertyValueFactory<>("rentalId"));

        // Displays full member name in table column
        colMemberId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMember().getFirstName() + " " + d.getValue().getMember().getLastName()));
        colObjectId.setCellValueFactory(new PropertyValueFactory<>("rentalObjectId"));
        colType.setCellValueFactory(d -> new SimpleStringProperty(formatRentalType(d.getValue().getRentalType())));

        colStartDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().format(timeFormatter)));

        // Displays rental end date or ongoing status
        colEndDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate() != null ? d.getValue().getEndDate().format(timeFormatter) : "Pågående"));
        colTotalPrice.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f kr", d.getValue().getTotalPrice())));

        addReturnButton();
        refreshTable();
    }

    private void addReturnButton() {

        // Defines action column with conditional button or status
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Returnera");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3ECF8E; -fx-border-color: #3ECF8E; -fx-cursor: hand;");

                // Processes return, updates data, and shows receipt or alerts
                btn.setOnAction(e -> {
                    Rental r = getTableView().getItems().get(getIndex());
                    if (r == null) {
                        return;
                    }

                    // Processes return, refreshes table, and displays receipt or error
                    try {
                        rentalService.returnRental(r.getRentalId());
                        refreshTable();

                        // Uppdatera listan med lediga datorer (om vi råkar ha rätt typ vald)
                        if (typeComboBox.getValue() == r.getRentalType()) {
                            loadAvailableComputers(r.getRentalType());
                        }

                        // Retrieves updated rental from master data set by ID
                        Rental updatedRental = masterData.stream()
                                .filter(rental -> rental.getRentalId().equals(r.getRentalId()))
                                .findFirst()
                                .orElse(null);

                        if (updatedRental != null) {
                            showReturnReceipt(updatedRental);
                        }

                    } catch (RentalAlreadyReturnedException ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.INFORMATION, "Information", ex.getMessage());

                    } catch (EntityNotFoundException ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Fel", ex.getMessage());

                    } catch (Exception ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Systemfel", "Kunde inte returnera: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(null);
                setText(null);
                setStyle("");

                // Conditionally renders cell content based on rental presence and status
                if (!empty) {

                    Rental r = getTableView().getItems().get(getIndex());

                    if (r != null) {

                        // Displays status indicator for concluded rentals, shows action button otherwise
                        if (r.getEndDate() != null) {

                            setText("Avslutad");
                            setStyle("-fx-text-fill: #9BA1A6; -fx-alignment: CENTER; -fx-font-style: italic;");

                        } else {
                            setGraphic(btn);
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void handleRent() {

        // Processes rental request validates input creates booking handles errors
        try {
            Member m = memberComboBox.getValue();
            RentalType type = typeComboBox.getValue();
            Object computer = computerComboBox.getValue();
            String durationStr = durationField.getText();

            // Checks for missing data warns user
            if (m == null || type == null || computer == null || durationStr.isEmpty()) {
                AlertHelper.showStyledAlert(rootPane, Alert.AlertType.WARNING, "Indata saknas", "Vänligen fyll i alla fält för att kunna hyra ut utrustning.");
                return;
            }

            int duration;

            try {
                duration = Integer.parseInt(durationStr);

            } catch (NumberFormatException nfe) {
                AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Felaktig indata", "Varaktigheten måste anges som ett heltal.");
                return;
            }

            long objId = getComputerId(computer);
            boolean isDaily = radioDays.isSelected();

            rentalService.createRental(m.getMemberId(), objId, type, duration, isDaily);

            showBookingReceipt(m, type, objId, duration, isDaily);

            refreshTable();
            loadAvailableComputers(type);
            clearFields();

        } catch (ItemAlreadyRentedException | MemberBlockedException | QuotaExceededException | InvalidRentalDataException | EntityNotFoundException ex) {
            AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Uthyrning misslyckades", ex.getMessage());

        } catch (Exception ex) {
            AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Systemfel", "Ett oväntat fel uppstod: " + ex.getMessage());
        }
    }

    private String formatRentalType(RentalType type) {

        if (type == null) {
            return "";
        }

        return switch (type) {
            case GAMING_COMPUTER -> "Gamingdator";
            case LAPTOP -> "Bärbar dator";
            case WORKSTATION -> "Arbetsstation";
        };
    }

    /**
     * Populates member and rental type selectors for UI
     */
    private void loadFormData() {
        if (memberService != null) memberComboBox.setItems(FXCollections.observableArrayList(memberService.getAllMembers()));
        typeComboBox.setItems(FXCollections.observableArrayList(RentalType.values()));
    }

    private ListCell<RentalType> createTypeCell() {

        return new ListCell<>() {

            // Sets cell text and style for rental type items
            @Override protected void updateItem(RentalType item, boolean empty) {

                super.updateItem(item, empty);

                setText(empty || item == null ? null : formatRentalType(item));

                setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-padding: 8 12;");
            }
        };
    }

    private ListCell<Member> createMemberCell() {

        return new ListCell<>() {

            // Updates cell text with member name and styles it
            @Override protected void updateItem(Member item, boolean empty) {

                super.updateItem(item, empty);

                setText(empty || item == null ? null : item.getFirstName() + " " + item.getLastName());

                setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-padding: 8 12;");
            }
        };
    }

    private ListCell<Object> createComputerCell() {

        return new ListCell<>() {

            // Updates cell content with styled computer info
            @Override protected void updateItem(Object item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                }

                else {

                    // Sets cell label according to computer subtype
                    if (item instanceof GamingComputer c) {
                        setText(c.getName() + " (ID: " + c.getComputerId() + ")");
                    }

                    else if (item instanceof Laptop l) {
                        setText(l.getName() + " (ID: " + l.getComputerId() + ")");
                    }

                    else if (item instanceof Workstation w) {
                        setText(w.getName() + " (ID: " + w.getComputerId() + ")");
                    }
                }
                setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-padding: 8 12;");
            }
        };
    }

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Displays modal receipt summarizing rental return
     */
    private void showReturnReceipt(Rental r) {
        Dialog<Void> dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        dialogPane.setStyle("-fx-background-color: #171717; -fx-border-color: #3ECF8E; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: transparent;");

        content.setOnMousePressed(event -> { xOffset = event.getSceneX(); yOffset = event.getSceneY(); });

        // Enables draggable window movement via mouse drag
        content.setOnMouseDragged(event -> { stage.setX(event.getScreenX() - xOffset); stage.setY(event.getScreenY() - yOffset); });

        Label title = new Label("WIGELLS RENTAL");
        title.setStyle("-fx-text-fill: #3ECF8E; -fx-font-size: 20px; -fx-font-weight: 900; -fx-letter-spacing: 3px;");
        Label subTitle = new Label("SLUTGILTIGT BETALNINGSKVITTO");
        subTitle.setStyle("-fx-text-fill: #9BA1A6; -fx-font-size: 10px; -fx-font-weight: bold;");

        Separator sep1 = new Separator(); sep1.setStyle("-fx-opacity: 0.1; -fx-padding: 10 0;");

        long timeUsed;
        String unit;

        // Calculates rental duration and selects appropriate unit
        if (r.isDailyRate()) {
            timeUsed = ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate());

            if (timeUsed == 0) {
                timeUsed = 1;
            }

            unit = "DYGN";

        } else {
            timeUsed = ChronoUnit.HOURS.between(r.getStartDate(), r.getEndDate());

            if (timeUsed == 0) {
                timeUsed = 1;
            }

            unit = "TIMMAR";
        }

        GridPane details = new GridPane();
        details.setHgap(40); details.setVgap(12); details.setAlignment(Pos.CENTER);

        String lStyle = "-fx-text-fill: #60676C; -fx-font-size: 11px; -fx-font-weight: bold;";
        String vStyle = "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 13px;";

        addReceiptRow(details, 0, "KVITTO NR", "RET-" + r.getRentalId(), lStyle, vStyle);
        addReceiptRow(details, 1, "MEDLEM", r.getMember().getFirstName() + " " + r.getMember().getLastName(), lStyle, vStyle);
        addReceiptRow(details, 2, "UTLÅNAD", r.getStartDate().format(timeFormatter), lStyle, vStyle);
        addReceiptRow(details, 3, "ÅTERLÄMNAD", r.getEndDate().format(timeFormatter), lStyle, vStyle);
        addReceiptRow(details, 4, "FAKTISK TID", timeUsed + " " + unit, lStyle, vStyle);

        // Adds overdue fee label to receipt when returned late
        if (r.getEndDate().isAfter(r.getEstimatedReturnDate())) {
            Label penaltyLabel = new Label("INKL. FÖRSENINGSAVGIFT");
            penaltyLabel.setStyle("-fx-text-fill: #FF5555; -fx-font-size: 10px; -fx-font-weight: bold;");
            details.add(penaltyLabel, 1, 5);
        }

        Separator sep2 = new Separator(); sep2.setStyle("-fx-opacity: 0.1; -fx-padding: 10 0;");

        Label totalValue = new Label(String.format("%.2f kr", r.getTotalPrice()));
        totalValue.setStyle("-fx-text-fill: #3ECF8E; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label totalLabel = new Label("BELOPP ATT DRAS FRÅN KORT");
        totalLabel.setStyle("-fx-text-fill: #9BA1A6; -fx-font-size: 9px;");

        Button closeBtn = new Button("BEKRÄFTA BETALNING");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle("-fx-background-color: #3ECF8E; -fx-text-fill: #000000; -fx-font-weight: 800; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        content.getChildren().addAll(title, subTitle, sep1, details, sep2, totalLabel, totalValue, new Region(), closeBtn);
        dialogPane.setContent(content);
        dialog.showAndWait();
    }

    /**
     * Displays booking confirmation dialog with styled receipt details
     */
    private void showBookingReceipt(Member m, RentalType t, long id, int dur, boolean isDaily) {

        String unit = isDaily ? "dygn" : "timmar";
        String dateStr = LocalDateTime.now().format(timeFormatter);

        Dialog<Void> dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        dialogPane.setStyle("-fx-background-color: #171717; -fx-border-color: #3ECF8E; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: transparent;");

        content.setOnMousePressed(event -> { xOffset = event.getSceneX(); yOffset = event.getSceneY(); });

        // Updates stage position during mouse drag
        content.setOnMouseDragged(event -> { stage.setX(event.getScreenX() - xOffset); stage.setY(event.getScreenY() - yOffset); });

        Label title = new Label("WIGELLS RENTAL");
        title.setStyle("-fx-text-fill: #3ECF8E; -fx-font-size: 20px; -fx-font-weight: 900; -fx-letter-spacing: 3px;");

        Label subTitle = new Label("BOKNINGSBEKRÄFTELSE (EJ BETALNING)");
        subTitle.setStyle("-fx-text-fill: #9BA1A6; -fx-font-size: 10px; -fx-font-weight: bold;");

        Separator sep1 = new Separator(); sep1.setStyle("-fx-opacity: 0.1; -fx-padding: 10 0;");

        GridPane details = new GridPane();
        details.setHgap(40); details.setVgap(12); details.setAlignment(Pos.CENTER);

        String lStyle = "-fx-text-fill: #60676C; -fx-font-size: 11px; -fx-font-weight: bold;";
        String vStyle = "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 13px;";

        addReceiptRow(details, 0, "BOKNINGS-ID", "PRE-" + System.currentTimeMillis() % 10000, lStyle, vStyle);
        addReceiptRow(details, 1, "DATUM", dateStr, lStyle, vStyle);
        addReceiptRow(details, 2, "MEDLEM", m.getFirstName().toUpperCase() + " " + m.getLastName().toUpperCase(), lStyle, vStyle);
        addReceiptRow(details, 3, "OBJEKT", formatRentalType(t).toUpperCase(), lStyle, vStyle);
        addReceiptRow(details, 4, "OBJEKT ID", "#" + id, lStyle, vStyle);
        addReceiptRow(details, 5, "BOKAD TID", dur + " " + unit.toUpperCase(), lStyle, vStyle);

        Separator sep2 = new Separator(); sep2.setStyle("-fx-opacity: 0.1; -fx-padding: 10 0;");

        Label totalValue = new Label(totalCostLabel.getText());
        totalValue.setStyle("-fx-text-fill: #3ECF8E; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label totalLabel = new Label("ESTIMERAD KOSTNAD");
        totalLabel.setStyle("-fx-text-fill: #9BA1A6; -fx-font-size: 9px;");

        Button closeBtn = new Button("BEKRÄFTA BOKNING");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle("-fx-background-color: #3ECF8E; -fx-text-fill: #000000; -fx-font-weight: 800; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        content.getChildren().addAll(title, subTitle, sep1, details, sep2, totalLabel, totalValue, new Region(), closeBtn);
        dialogPane.setContent(content);
        dialog.showAndWait();
    }

    /**
     * Adds styled label pair as new receipt row
     */
    private void addReceiptRow(GridPane grid, int row, String label, String value, String lStyle, String vStyle) {
        Label l = new Label(label); l.setStyle(lStyle);
        Label v = new Label(value); v.setStyle(vStyle);
        grid.add(l, 0, row); grid.add(v, 1, row);
    }

    private void applyThemeStyles(Control... controls) {

        for (Control c : controls) {
            c.setStyle("-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-border-color: #393939; -fx-border-radius: 6;");
        }
    }

    private void applyPrimaryButtonStyle(Button b) {
        b.setStyle("-fx-background-color: #3ECF8E; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
    }

    private void refreshTable() {
        masterData.setAll(rentalService.getAllRentals());
        rentalTable.setItems(masterData);
        rentalTable.refresh();
    }

    /**
     * Resets input fields and cost display
     */
    private void clearFields() {
        durationField.clear(); memberComboBox.setValue(null); typeComboBox.setValue(null); computerComboBox.setValue(null); totalCostLabel.setText("0.00 kr");
    }
}