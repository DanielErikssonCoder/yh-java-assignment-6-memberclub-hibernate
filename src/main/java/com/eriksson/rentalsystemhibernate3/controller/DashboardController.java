package com.eriksson.rentalsystemhibernate3.controller;

import com.eriksson.rentalsystemhibernate3.entity.Rental;
import com.eriksson.rentalsystemhibernate3.entity.RentalType;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DashboardController {

    @FXML private Label totalMembersLabel, activeRentalsLabel, revenueLabel;
    @FXML private Label gamingCountLabel, laptopCountLabel, workstationCountLabel;
    @FXML private ListView<String> recentActivityList;

    private MemberService memberService;
    private RentalService rentalService;
    private GamingComputerService gamingService;
    private LaptopService laptopService;
    private WorkstationService workstationService;

    @FXML
    public void initialize() {
        initServices();
        Platform.runLater(this::refreshDashboard);
    }

    /**
     * Initializes service layer with repository dependencies
     */
    private void initServices() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        RentalRepository rentalRepo = new RentalRepositoryImpl(sf);
        MemberRepository memberRepo = new MemberRepositoryImpl(sf);

        this.memberService = new MemberService(memberRepo, rentalRepo);
        this.rentalService = new RentalService(rentalRepo, memberRepo,
                new GamingComputerRepositoryImpl(sf), new LaptopRepositoryImpl(sf), new WorkstationRepositoryImpl(sf));
        this.gamingService = new GamingComputerService(new GamingComputerRepositoryImpl(sf));
        this.laptopService = new LaptopService(new LaptopRepositoryImpl(sf));
        this.workstationService = new WorkstationService(new WorkstationRepositoryImpl(sf));
    }

    private String formatRentalType(RentalType type) {
        if (type == null) return "";
        return switch (type) {
            case GAMING_COMPUTER -> "Gamingdator";
            case LAPTOP -> "Bärbar dator";
            case WORKSTATION -> "Arbetsstation";
        };
    }

    /**
     * Refreshes dashboard statistics and recent rental activities
     */
    private void refreshDashboard() {
        int totalMembers = memberService.getAllMembers().size();
        List<Rental> allRentals = rentalService.getAllRentals();
        long activeCount = allRentals.stream().filter(r -> r.getEndDate() == null).count();
        double totalRevenue = allRentals.stream().mapToDouble(Rental::getTotalPrice).sum();

        totalMembersLabel.setText(String.valueOf(totalMembers));
        activeRentalsLabel.setText(String.valueOf(activeCount));
        revenueLabel.setText(String.format("%.2f kr", totalRevenue));

        gamingCountLabel.setText(String.valueOf(gamingService.getAll().size()));
        laptopCountLabel.setText(String.valueOf(laptopService.getAll().size()));
        workstationCountLabel.setText(String.valueOf(workstationService.getAll().size()));

        recentActivityList.getItems().clear();

        // Selects latest eight rentals sorted by start date
        allRentals.stream()
                .sorted((r1, r2) -> r2.getStartDate().compareTo(r1.getStartDate()))
                .limit(8)
                .forEach(r -> {
                    String status = r.getEndDate() == null ? "NY UTHYRNING" : "RETURNERAD";
                    String name = r.getMember().getFirstName() + " " + r.getMember().getLastName();
                    String typeStr = formatRentalType(r.getRentalType());

                    recentActivityList.getItems().add(String.format("%-15s | %-20s | %s",
                            status, name, typeStr));
                });

        recentActivityList.setCellFactory(lv -> new ListCell<>() {
            @Override
                // Updates cell styling and text according to emptiness
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Updates cell styling and text according to emptiness
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #9BA1A6; -fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-padding: 15 10; -fx-background-color: transparent; -fx-border-color: #1E1E1E; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    @FXML
    private void onGoToInventory() {

        // Loads inventory view, replaces content area, reports loading failures
        try {

            String fxmlPath = "/com/eriksson.rentalsystemhibernate3/computer-view.fxml";

            URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                // Om den fortfarande inte hittas, prova relativt (eftersom controllern ligger i .controller)
                resource = getClass().getResource("../computer-view.fxml");
            }

            if (resource == null) {
                System.err.println("Kunde inte hitta computer-view.fxml. Kontrollera stavning och paket!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent inventoryView = loader.load();

            Scene scene = totalMembersLabel.getScene();

            Node contentArea = scene.lookup("#mainContent");

            // Replaces current pane or its container with inventory view
            if (contentArea instanceof Pane pane) {
                pane.getChildren().setAll(inventoryView);
            } else {

                VBox dashboardRoot = (VBox) totalMembersLabel.getParent().getParent().getParent();
                if (dashboardRoot.getParent() instanceof Pane container) {
                    container.getChildren().setAll(inventoryView);
                }
            }

        } catch (IOException e) {
            System.err.println("Fel vid växling av vy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}