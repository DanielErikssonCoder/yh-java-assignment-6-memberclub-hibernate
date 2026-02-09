package com.eriksson.rentalsystemhibernate3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Long rentalId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_rentals_member")
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false)
    private RentalType rentalType;

    @Column(name = "rental_object_id", nullable = false)
    private Long rentalObjectId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "estimated_return_date")
    private LocalDateTime estimatedReturnDate;

    @Column(name = "is_daily_rate")
    private boolean isDailyRate;

    public Rental() {}

    public Rental(Member member, RentalType rentalType, Long rentalObjectId, LocalDateTime startDate, LocalDateTime endDate, Double totalPrice, LocalDateTime estimatedReturnDate, boolean isDailyRate) {
        this.member = member;
        this.rentalType = rentalType;
        this.rentalObjectId = rentalObjectId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.estimatedReturnDate = estimatedReturnDate;
        this.isDailyRate = isDailyRate;
    }

    public Long getRentalId() {
        return rentalId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public RentalType getRentalType() {
        return rentalType;
    }

    public void setRentalType(RentalType rentalType) {
        this.rentalType = rentalType;
    }

    public Long getRentalObjectId() {
        return rentalObjectId;
    }

    public void setRentalObjectId(Long rentalObjectId) {
        this.rentalObjectId = rentalObjectId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getEstimatedReturnDate() {
        return estimatedReturnDate;
    }

    public void setEstimatedReturnDate(LocalDateTime estimatedReturnDate) {
        this.estimatedReturnDate = estimatedReturnDate;
    }

    public boolean isDailyRate() {
        return isDailyRate;
    }

    public void setDailyRate(boolean dailyRate) {
        isDailyRate = dailyRate;
    }
}