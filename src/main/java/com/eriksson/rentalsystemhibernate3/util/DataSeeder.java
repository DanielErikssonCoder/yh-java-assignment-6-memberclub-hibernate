package com.eriksson.rentalsystemhibernate3.util;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class DataSeeder {

    /**
     * Checks member list; triggers data seeding if empty
     */
    public static void seedIfEmpty(SessionFactory sessionFactory) {
        MemberService memberService = new MemberService(
                new MemberRepositoryImpl(sessionFactory),
                new RentalRepositoryImpl(sessionFactory)
        );

        if (memberService.getAllMembers().isEmpty()) {
            forceSeed(sessionFactory);
        }
    }

    /**
     * Resets database then populates members and computers
     */
    public static void forceSeed(SessionFactory sessionFactory) {

        // Clears all rental and computer data and commits cleanup
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.createMutationQuery("delete from Rental").executeUpdate();
            session.createMutationQuery("delete from GamingComputer").executeUpdate();
            session.createMutationQuery("delete from Laptop").executeUpdate();
            session.createMutationQuery("delete from Workstation").executeUpdate();
            session.createMutationQuery("delete from Member").executeUpdate();
            tx.commit();
        }

        MemberRepository memberRepo = new MemberRepositoryImpl(sessionFactory);
        RentalRepository rentalRepo = new RentalRepositoryImpl(sessionFactory);

        MemberService ms = new MemberService(memberRepo, rentalRepo);
        GamingComputerService gs = new GamingComputerService(new GamingComputerRepositoryImpl(sessionFactory));
        LaptopService ls = new LaptopService(new LaptopRepositoryImpl(sessionFactory));
        WorkstationService ws = new WorkstationService(new WorkstationRepositoryImpl(sessionFactory));

        seedMembers(ms);
        seedComputers(gs, ls, ws);

        // Uncomment for test rentals below
        // seedRentals(memberRepo, rentalRepo, gs, ls);

        System.out.println("DataSeeder: Systemet har återställts med komplett sortiment.");
    }

    /**
     * Creates initial member profiles for system startup
     */
    private static void seedMembers(MemberService ms) {
        ms.createMember("Daniel", "Eriksson", "daniel@eriksson.se");
        ms.createMember("Anna", "Svensson", "anna@telia.se");
        ms.createMember("Mikael", "Nordström", "micke@gmail.com");
        ms.createMember("Sofia", "Larsson", "sofia.l@outlook.com");
        ms.createMember("Erik", "Bergqvist", "erik.b@företaget.se");
        ms.createMember("Lina", "Holm", "lina.h@test.se");
    }

    /**
     * Adds diverse devices to inventory
     */
    private static void seedComputers(GamingComputerService gs, LaptopService ls, WorkstationService ws) {

        // Gaming Computers
        gs.addGamingComputer("ASUS ROG Swift Extreme", "i9-14900K", 64, "2TB SSD", "RTX 4090", 120, 950);
        gs.addGamingComputer("Alienware Aurora R16", "Ryzen 9 7950X", 32, "1TB SSD", "RTX 4080 Super", 95, 750);
        gs.addGamingComputer("MSI MPG Infinite X2", "i7-14700KF", 32, "2TB NVMe", "RTX 4070 Ti", 75, 550);
        gs.addGamingComputer("HP Omen 45L", "Ryzen 7 7800X3D", 32, "1TB NVMe", "RTX 4080", 85, 680);
        gs.addGamingComputer("Corsair One i300", "i9-12900K", 32, "2TB SSD", "RTX 3080 Ti", 70, 500);

        // Laptops
        ls.addLaptop("MacBook Pro 16", "M3 Max", 48, "1TB SSD", 16.2, 22, false, 95, 800);
        ls.addLaptop("Dell XPS 17 Touch", "i9-13900H", 32, "1TB SSD", 17.0, 10, true, 85, 650);
        ls.addLaptop("Lenovo ThinkPad X1 Carbon", "i7-1365U", 16, "512GB SSD", 14.0, 15, false, 55, 450);
        ls.addLaptop("Razer Blade 14", "Ryzen 9 7940HS", 16, "1TB SSD", 14.0, 8, false, 80, 620);
        ls.addLaptop("ASUS Zenbook Duo", "i9-13900H", 32, "2TB SSD", 14.5, 9, true, 75, 580);

        // Workstations
        ws.addWorkstation("HP Z8 Fury G5", "Intel Xeon w9", 56, 256, 4, "8TB RAID", "Dual RTX A6000", 350, 2500);
        ws.addWorkstation("Lenovo ThinkStation P920", "Threadripper PRO", 32, 128, 4, "4TB NVMe", "RTX A6000", 220, 1800);
        ws.addWorkstation("Dell Precision 7960", "Intel Xeon w7", 24, 64, 3, "2TB NVMe", "RTX A4500", 180, 1400);
        ws.addWorkstation("Mac Studio Ultimate", "M2 Ultra", 24, 128, 5, "2TB SSD", "76-core GPU", 250, 2000);
    }

    /**
     * Initializes member rentals with current and historic usage scenarios for gaming computers and laptops
     */
    private static void seedRentals(MemberRepository memberRepo, RentalRepository rentalRepo, GamingComputerService gs, LaptopService ls) {

        List<Member> members = memberRepo.findAll();

        if (members.isEmpty()) {
            return;
        }

        List<GamingComputer> gcs = gs.getAllGamingComputers();
        List<Laptop> laps = ls.getAllLaptops();

        if (!gcs.isEmpty()) {

            // Creates and persists a gaming‑computer rental for first member
            Rental r1 = new Rental(
                    members.get(0),
                    RentalType.GAMING_COMPUTER,
                    gcs.get(0).getComputerId(),
                    LocalDateTime.now().minusDays(1),
                    null,
                    0.0,
                    LocalDateTime.now().plusDays(1),
                    true
            );

            rentalRepo.save(r1);
        }

        if (gcs.size() > 1) {

            // Registers gaming‑computer rental with historic dates
            Rental r2 = new Rental(
                    members.get(1),
                    RentalType.GAMING_COMPUTER,
                    gcs.get(1).getComputerId(),
                    LocalDateTime.now().minusDays(5),
                    null,
                    0.0,
                    LocalDateTime.now().minusDays(1),
                    true
            );

            rentalRepo.save(r2);
        }

        if (!laps.isEmpty()) {

            // Records laptop rental with recent usage
            Rental r3 = new Rental(
                    members.get(2),
                    RentalType.LAPTOP,
                    laps.get(0).getComputerId(),
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(8),
                    1600.0,
                    LocalDateTime.now().minusDays(8),
                    true
            );

            rentalRepo.save(r3);
        }
    }
}