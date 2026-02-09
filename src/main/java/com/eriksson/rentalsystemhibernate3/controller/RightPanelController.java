package com.eriksson.rentalsystemhibernate3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

// Service & Entity imports
import com.eriksson.rentalsystemhibernate3.service.RentalService;

// Repository imports (Alla behövs för RentalService)
import com.eriksson.rentalsystemhibernate3.repo.RentalRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.MemberRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.GamingComputerRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.LaptopRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.WorkstationRepositoryImpl;

// Repositories interfaces
import com.eriksson.rentalsystemhibernate3.repo.RentalRepository;
import com.eriksson.rentalsystemhibernate3.repo.MemberRepository;
import com.eriksson.rentalsystemhibernate3.repo.GamingComputerRepository;
import com.eriksson.rentalsystemhibernate3.repo.LaptopRepository;
import com.eriksson.rentalsystemhibernate3.repo.WorkstationRepository;

// Hibernate imports
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import org.hibernate.SessionFactory;

public class RightPanelController {

    @FXML private Label lblActiveRentals;
    @FXML private Label lblTotalComputers;
    @FXML private VBox rentalsListContainer;

    private RentalService rentalService;

    /**
     * Initializes repositories, loads stats, populates rental list
     */
    @FXML
    public void initialize() {

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        RentalRepository rentalRepo = new RentalRepositoryImpl(sessionFactory);
        MemberRepository memberRepo = new MemberRepositoryImpl(sessionFactory);
        GamingComputerRepository gamingRepo = new GamingComputerRepositoryImpl(sessionFactory);
        LaptopRepository laptopRepo = new LaptopRepositoryImpl(sessionFactory);
        WorkstationRepository workstationRepo = new WorkstationRepositoryImpl(sessionFactory);

        this.rentalService = new RentalService(
                rentalRepo,
                memberRepo,
                gamingRepo,
                laptopRepo,
                workstationRepo
        );

        loadStats();
    }

    /**
     * Updates UI stats and lists current rentals
     */
    private void loadStats() {

        lblActiveRentals.setText("12");
        lblTotalComputers.setText("45");

        rentalsListContainer.getChildren().clear();
        addRentalRow("Alice W.", "Gaming PC 01", true);
        addRentalRow("Bob S.", "Laptop Dell", true);
        addRentalRow("Charlie", "Workstation X", false);
    }

    /**
     * Creates visual rental entry with avatar, details, and status indicator
     */
    private void addRentalRow(String name, String device, boolean active) {

        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("list-item");

        Circle avatar = new Circle(16);
        avatar.setFill(javafx.scene.paint.Color.web("#1A2230"));
        avatar.setStroke(javafx.scene.paint.Color.web("rgba(255,255,255,0.1)"));

        VBox texts = new VBox();
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: 600; -fx-text-fill: white;");
        Label devLbl = new Label(device);
        devLbl.getStyleClass().add("text-muted");
        texts.getChildren().addAll(nameLbl, devLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Circle status = new Circle(4);
        status.getStyleClass().add(active ? "status-dot-active" : "status-dot-inactive");

        row.getChildren().addAll(avatar, new Region() {{ setPrefWidth(12); }}, texts, spacer, status);
        rentalsListContainer.getChildren().add(row);
    }

    @FXML
    private void handleNewRental() {
        System.out.println("Öppnar dialog för ny uthyrning");
    }
}