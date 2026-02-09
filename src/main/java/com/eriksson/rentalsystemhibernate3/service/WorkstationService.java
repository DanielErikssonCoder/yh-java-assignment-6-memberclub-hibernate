package com.eriksson.rentalsystemhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.Workstation;
import com.eriksson.rentalsystemhibernate3.repo.WorkstationRepository;

import java.util.List;

public class WorkstationService {

    private final WorkstationRepository workstationRepository;

    public WorkstationService(WorkstationRepository workstationRepository) {
        this.workstationRepository = workstationRepository;
    }

    // Fetch all
    public List<Workstation> getAll() {
        return workstationRepository.findAll();
    }

    // Add with validation
    public void addWorkstation(String model,
                               String cpu,
                               int cpuCores,
                               int ramGb,
                               int supportedDisplays,
                               String ssd,
                               String gpu,
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

        if (cpuCores <= 0 || cpuCores <= 0) {
            throw new IllegalArgumentException("CPU cores saknas");
        }

        if (ramGb <= 0) {
            throw new IllegalArgumentException("RAM saknas");
        }

        if (supportedDisplays <= 0) {
            throw new IllegalArgumentException("Support för skärmar saknas");
        }

        if (ssd == null || ssd.isBlank()) {
            throw new IllegalArgumentException("SSD saknas");
        }

        if (gpu == null || gpu.isBlank()) {
            throw new IllegalArgumentException("GPU saknas");
        }

        Workstation workstation = new Workstation(model, cpu, cpuCores, ramGb, supportedDisplays, ssd, gpu, hourlyPrice, dailyPrice);

        workstationRepository.save(workstation);
    }

    public List<Workstation> getAllWorkstations() {
        return workstationRepository.findAll();
    }
}
