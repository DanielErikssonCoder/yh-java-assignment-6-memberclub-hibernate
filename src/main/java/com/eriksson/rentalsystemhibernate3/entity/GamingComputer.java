package com.eriksson.rentalsystemhibernate3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gaming_computers")
public class GamingComputer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "computer_generator")
    @SequenceGenerator(name = "computer_generator", sequenceName = "computer_seq", allocationSize = 1)
    @Column(name = "computer_id")
    private Long computerId;

    @Column(nullable = false)
    private String name;
    private String processor;
    private int ram;
    private String storage;
    private String graphicsCard;

    @Column(name = "hourly_price")
    private double hourlyPrice;

    @Column(name = "daily_price")
    private double dailyPrice;

    public GamingComputer() {}

    public GamingComputer(String name, String processor, int ram, String storage, String graphicsCard, double hourlyPrice, double dailyPrice) {
        this.name = name;
        this.processor = processor;
        this.ram = ram;
        this.storage = storage;
        this.graphicsCard = graphicsCard;
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
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getGraphicsCard() { return graphicsCard; }
    public void setGraphicsCard(String graphicsCard) { this.graphicsCard = graphicsCard; }
    public double getHourlyPrice() { return hourlyPrice; }
    public void setHourlyPrice(double hourlyPrice) { this.hourlyPrice = hourlyPrice; }
    public double getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(double dailyPrice) { this.dailyPrice = dailyPrice; }
}