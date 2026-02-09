package com.eriksson.rentalsystemhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.Laptop;
import com.eriksson.rentalsystemhibernate3.repo.LaptopRepository;

import java.util.List;

public class LaptopService {

    private final LaptopRepository laptopRepository;

    public LaptopService(LaptopRepository laptopRepository) {
        this.laptopRepository = laptopRepository;
    }

    // Fetch all
    public List<Laptop> getAll() {
        return laptopRepository.findAll();
    }

    // Add with validation
    public void addLaptop (String model,
                           String cpu,
                           int ramGb,
                           String ssd,
                           double screenSize,
                           int batteryLifeHours,
                           boolean hasTouchScreen,
                           double hourlyPrice,
                           double dailyPrice) {

        if (hourlyPrice <= 0 || dailyPrice <= 0) {
            throw new IllegalArgumentException("Priset måste vara högre än 0");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Modellnamn saknas");
        }

        if (cpu == null || cpu.isBlank()) {
            throw new IllegalArgumentException("CPU saknas");
        }

        if (ramGb <= 0) {
            throw new IllegalArgumentException("RAM saknas");
        }

        if (ssd == null || ssd.isBlank()) {
            throw new IllegalArgumentException("SSD saknas");
        }

        if (screenSize <= 0) {
            throw new IllegalArgumentException("Skärmstorlek saknas");
        }

        if (batteryLifeHours <= 0) {
            throw new IllegalArgumentException("Batteritid saknas");
        }

        Laptop laptop = new Laptop(model, cpu, ramGb, ssd, screenSize, batteryLifeHours, hasTouchScreen, hourlyPrice, dailyPrice);

        laptopRepository.save(laptop);
    }

    public List<Laptop> getAllLaptops() {
        return laptopRepository.findAll();
    }
}
