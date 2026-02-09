package com.eriksson.rentalsystemhibernate3;

import com.eriksson.rentalsystemhibernate3.entity.RentalType;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import org.hibernate.SessionFactory;

public class Main {

    /**
     * Initializes services, runs operations, handles exceptions, shuts down
     */
    static void main(String[] args) {

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        MemberRepository memberRepo = new MemberRepositoryImpl(sessionFactory);
        GamingComputerRepository gamingRepo = new GamingComputerRepositoryImpl(sessionFactory);
        LaptopRepository laptopRepo = new LaptopRepositoryImpl(sessionFactory);
        WorkstationRepository workstationRepo = new WorkstationRepositoryImpl(sessionFactory);
        RentalRepository rentalRepo = new RentalRepositoryImpl(sessionFactory);

        MemberService memberService = new MemberService(memberRepo, rentalRepo);
        GamingComputerService gamingComputerService = new GamingComputerService(gamingRepo);
        LaptopService laptopService = new LaptopService(laptopRepo);
        WorkstationService workstationService = new WorkstationService(workstationRepo);

        // Orchestrates test execution with error handling and cleanup
        RentalService rentalService = new RentalService(rentalRepo, memberRepo, gamingRepo, laptopRepo, workstationRepo);

        // Orchestrates test flow, handles exceptions, ensures shutdown
        try {
            System.out.println("--- Startar Test ---");

            // Attempts to register new member
            try {
                memberService.createMember("Daniel", "Eriksson", "danieleriksson026@gmail.com");
                System.out.println("Medlem skapad: Daniel Eriksson");
            } catch (Exception e) {
                System.out.println("Medlem kunde inte skapas (kanske finns den redan): " + e.getMessage());
            }

            // Adds gaming computer
            try {
                gamingComputerService.addGamingComputer("ASUS ROG", "AMD Ryzen 9 9950x3D", 64, "4TB", "RTX 5090", 50.0, 500.0);
                System.out.println("Dator skapad: ASUS ROG");
            } catch (Exception e) {
                System.out.println("Dator kunde inte skapas: " + e.getMessage());
            }

            // Attempts to rent gaming PC for three days
            try {
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 3, true);
                System.out.println("Uthyrning genomförd (3 dagar)!");

            } catch (Exception e) {
                System.out.println("Kunde inte hyra ut: " + e.getMessage());
            }

            System.out.println("\n--- ALLA UTHYRNINGAR I DATABASEN ---");

            // Iterates rentals and prints summary details
            rentalService.getAllRentals().forEach(r -> {

                // Displays rental ID and renter's full name
                System.out.println("ID: " + r.getRentalId() + " | Hyrestagare: " + r.getMember().getFirstName() + " " + r.getMember().getLastName());
                System.out.println("Objekt: " + r.getRentalType() + " (ID: " + r.getRentalObjectId() + ")");
                System.out.println("Totalpris: " + r.getTotalPrice() + " kr");
                System.out.println("Status: " + (r.getEndDate() == null ? "Aktiv" : "Avslutad (" + r.getEndDate() + ")"));
                System.out.println("------------------------------------");
            });

        } catch (Exception e) {
            System.err.println("Ett oväntat fel uppstod i Main: " + e.getMessage());

            e.printStackTrace();

        } finally {
            HibernateUtil.shutdown();
        }
    }
}