package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.Member;
import com.eriksson.rentalsystemhibernate3.exception.EntityNotFoundException;
import com.eriksson.rentalsystemhibernate3.exception.InvalidMemberDataException;
import com.eriksson.rentalsystemhibernate3.exception.MemberAlreadyExistsException;
import com.eriksson.rentalsystemhibernate3.exception.MemberHasActiveRentalsException;
import com.eriksson.rentalsystemhibernate3.repo.MemberRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.RentalRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.service.MemberService;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.hibernate.SessionFactory;

public class MemberController {

    @FXML private VBox rootPane;
    @FXML private TextField txtFirstName, txtLastName, txtEmail, searchField;
    @FXML private Button registerButton;
    @FXML private TableView<Member> memberTable;
    @FXML private TableColumn<Member, Long> colId;
    @FXML private TableColumn<Member, String> colFirstName, colLastName, colEmail;
    @FXML private TableColumn<Member, Void> colActions;

    private MemberService memberService;
    private final ObservableList<Member> masterData = FXCollections.observableArrayList();

    private Member selectedMemberForEdit = null;

    /**
     * Initializes UI components, configures table, loads data
     */
    @FXML
    public void initialize() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        memberService = new MemberService(
                new MemberRepositoryImpl(sessionFactory),
                new RentalRepositoryImpl(sessionFactory)
        );

        colId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        configureInputStyles(txtFirstName, txtLastName, txtEmail, searchField);
        configurePrimaryButton(registerButton);

        addActionsButtons();
        refreshTable();
        setupSearch();

        Platform.runLater(() -> memberTable.requestFocus());
    }

    private void configureInputStyles(Control... controls) {
        String baseStyle = "-fx-background-color: #0F0F0F; -fx-text-fill: white; -fx-border-color: #393939; -fx-border-radius: 6; -fx-background-radius: 6;";
        for (Control c : controls) {

            c.setStyle(baseStyle);

            // Updates control border color on focus
            c.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) c.setStyle(baseStyle + "-fx-border-color: #3ECF8E;");
                else c.setStyle(baseStyle);
            });
        }
    }

    /**
     * Sets primary button style and hover behavior
     */
    private void configurePrimaryButton(Button button) {
        String baseStyle = "-fx-background-color: #3ECF8E; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(baseStyle + "-fx-background-color: #6EE7B7;"));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }

    private void setupSearch() {
        FilteredList<Member> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            // Filters member list by search input, ignoring case
            filteredData.setPredicate(m -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }

                String low = newVal.toLowerCase();

                return m.getFirstName().toLowerCase().contains(low) ||
                        m.getLastName().toLowerCase().contains(low) ||
                        m.getEmail().toLowerCase().contains(low);
            });
        });
        memberTable.setItems(filteredData);
    }

    @FXML
    private void onAddMember() {

        // Validates mandatory input and alerts user, and aborts action
        if(txtFirstName.getText().isBlank() || txtLastName.getText().isBlank() || txtEmail.getText().isBlank()) {
            AlertHelper.showStyledAlert(rootPane, Alert.AlertType.WARNING, "Indata saknas", "Alla fält måste fyllas i.");
            return;
        }

        // Validates input, persists member, handles errors
        try {

            // Creates or updates member, then refreshes UI
            if (selectedMemberForEdit == null) {

                memberService.createMember(txtFirstName.getText(), txtLastName.getText(), txtEmail.getText());

            } else {

                memberService.updateMember(selectedMemberForEdit.getMemberId(), txtFirstName.getText(), txtLastName.getText(), txtEmail.getText());
                selectedMemberForEdit = null;
                registerButton.setText("Registrera Medlem");
            }

            clearFields();
            refreshTable();

        } catch (InvalidMemberDataException | MemberAlreadyExistsException ex) {
            AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Fel vid sparning", ex.getMessage());

        } catch (Exception ex) {
            AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Systemfel", "Ett oväntat fel uppstod: " + ex.getMessage());
        }
    }

    private void addActionsButtons() {

        // Creates editable and deletable action cells in member table
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Ändra");
            private final Button btnDelete = new Button("Ta bort");
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #3ECF8E; -fx-cursor: hand; -fx-font-weight: bold;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4545; -fx-cursor: hand;");

                // Retrieves selected member and initiates edit mode
                btnEdit.setOnAction(e -> {
                    Member m = getTableView().getItems().get(getIndex());
                    prepareEdit(m);
                });

                btnDelete.setOnAction(e -> {
                    Member m = getTableView().getItems().get(getIndex());

                    // Deletes selected member, displays alerts for not‑found or active rentals
                    try {
                        memberService.deleteMember(m.getMemberId());
                        refreshTable();

                    } catch (EntityNotFoundException ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.WARNING, "Hittades ej", ex.getMessage());

                    } catch (MemberHasActiveRentalsException ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Kan inte radera", ex.getMessage());

                    } catch (Exception ex) {
                        AlertHelper.showStyledAlert(rootPane, Alert.AlertType.ERROR, "Fel", "Kunde inte radera: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);

                } else {
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Populates edit form for selected member and updates UI elements
     */
    private void prepareEdit(Member m) {
        selectedMemberForEdit = m;
        txtFirstName.setText(m.getFirstName());
        txtLastName.setText(m.getLastName());
        txtEmail.setText(m.getEmail());
        registerButton.setText("Spara ändringar");
        txtFirstName.requestFocus();
    }

    private void refreshTable() {
        masterData.setAll(memberService.getAllMembers());
        memberTable.setItems(masterData);
    }

    @FXML private void clearFields() {
        selectedMemberForEdit = null;
        registerButton.setText("Registrera Medlem");
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
    }
}