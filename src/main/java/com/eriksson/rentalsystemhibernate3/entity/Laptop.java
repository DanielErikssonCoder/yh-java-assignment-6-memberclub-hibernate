package com.eriksson.rentalsystemhibernate3.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "laptops")
public class Laptop {

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
    private double screenSize;
    private int batteryLife;
    private boolean touchScreen;

    @Column(name = "hourly_price")
    private double hourlyPrice;

    @Column(name = "daily_price")
    private double dailyPrice;

    public Laptop() {}

    public Laptop(String name, String processor, int ram, String storage, double screenSize, int batteryLife, boolean touchScreen, double hourlyPrice, double dailyPrice) {
        this.name = name;
        this.processor = processor;
        this.ram = ram;
        this.storage = storage;
        this.screenSize = screenSize;
        this.batteryLife = batteryLife;
        this.touchScreen = touchScreen;
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
    public double getScreenSize() { return screenSize; }
    public void setScreenSize(double screenSize) { this.screenSize = screenSize; }
    public int getBatteryLife() { return batteryLife; }
    public void setBatteryLife(int batteryLife) { this.batteryLife = batteryLife; }
    public boolean isTouchScreen() { return touchScreen; }
    public void setTouchScreen(boolean touchScreen) { this.touchScreen = touchScreen; }
    public double getHourlyPrice() { return hourlyPrice; }
    public void setHourlyPrice(double hourlyPrice) { this.hourlyPrice = hourlyPrice; }
    public double getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(double dailyPrice) { this.dailyPrice = dailyPrice; }
}