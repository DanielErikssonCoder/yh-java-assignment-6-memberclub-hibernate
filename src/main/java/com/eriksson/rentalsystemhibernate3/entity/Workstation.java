package com.eriksson.rentalsystemhibernate3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workstations")
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "computer_generator")
    @SequenceGenerator(name = "computer_generator", sequenceName = "computer_seq", allocationSize = 1)
    @Column(name = "computer_id")
    private Long computerId;

    @Column(nullable = false)
    private String name;
    private String processor;
    private int ram;
    private int storage; // GB
    private int monitorCount;
    private String raidConfig;
    private String gpuConfig;

    @Column(name = "hourly_price")
    private double hourlyPrice;

    @Column(name = "daily_price")
    private double dailyPrice;

    public Workstation() {}

    public Workstation(String name, String processor, int ram, int storage, int monitorCount, String raidConfig, String gpuConfig, double hourlyPrice, double dailyPrice) {
        this.name = name;
        this.processor = processor;
        this.ram = ram;
        this.storage = storage;
        this.monitorCount = monitorCount;
        this.raidConfig = raidConfig;
        this.gpuConfig = gpuConfig;
        this.hourlyPrice = hourlyPrice;
        this.dailyPrice = dailyPrice;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProcessor() { return processor; }
    public void setProcessor(String processor) { this.processor = processor; }
    public int getRam() { return ram; }
    public void setRam(int ram) { this.ram = ram; }
    public int getStorage() { return storage; }
    public void setStorage(int storage) { this.storage = storage; }
    public int getMonitorCount() { return monitorCount; }
    public void setMonitorCount(int monitorCount) { this.monitorCount = monitorCount; }
    public String getRaidConfig() { return raidConfig; }
    public void setRaidConfig(String raidConfig) { this.raidConfig = raidConfig; }
    public String getGpuConfig() { return gpuConfig; }
    public void setGpuConfig(String gpuConfig) { this.gpuConfig = gpuConfig; }
    public double getHourlyPrice() { return hourlyPrice; }
    public void setHourlyPrice(double hourlyPrice) { this.hourlyPrice = hourlyPrice; }
    public double getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(double dailyPrice) { this.dailyPrice = dailyPrice; }
}