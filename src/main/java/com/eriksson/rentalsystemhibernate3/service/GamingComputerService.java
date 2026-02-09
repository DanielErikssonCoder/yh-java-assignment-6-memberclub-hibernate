package com.eriksson.rentalsystemhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.GamingComputer;
import com.eriksson.rentalsystemhibernate3.repo.GamingComputerRepository;
import java.util.List;

public class GamingComputerService {

    private final GamingComputerRepository gamingComputerRepository;

    public GamingComputerService(GamingComputerRepository gamingComputerRepository) {
        this.gamingComputerRepository = gamingComputerRepository;
    }

    // Fetch all
    public List<GamingComputer> getAll() {
        return gamingComputerRepository.findAll();
    }

    // Add with validation
    public void addGamingComputer(String model, String cpu, int ram, String ssd, String gpu, double hourlyPrice, double dailyPrice) {

        if (hourlyPrice <= 0 || dailyPrice <= 0) {
            throw new IllegalArgumentException("Priset måste vara högre än 0");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Modellnamn saknas");
        }

        if (cpu == null || cpu.isBlank()) {
            throw new IllegalArgumentException("CPU saknas");
        }

        if (ram <= 0) {
            throw new IllegalArgumentException("RAM saknas");
        }

        if (ssd == null || ssd.isBlank()) {
            throw new IllegalArgumentException("SSD saknas");
        }

        if (gpu == null || gpu.isBlank()) {
            throw new IllegalArgumentException("Gpu saknas");
        }

        GamingComputer computer = new GamingComputer(model, cpu, ram, ssd, gpu, hourlyPrice, dailyPrice);

        gamingComputerRepository.save(computer);
    }

    public List<GamingComputer> getAllGamingComputers() {
        return gamingComputerRepository.findAll();
    }
}
