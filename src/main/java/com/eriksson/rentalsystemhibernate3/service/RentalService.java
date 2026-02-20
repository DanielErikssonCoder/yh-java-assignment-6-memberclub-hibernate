package com.eriksson.rentalsystemhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.exception.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class RentalService {

    private final RentalRepository rentalRepository;
    private final MemberRepository memberRepository;
    private final GamingComputerRepository gamingComputerRepository;
    private final LaptopRepository laptopRepository;
    private final WorkstationRepository workstationRepository;

    public RentalService(RentalRepository rentalRepository,
                         MemberRepository memberRepository,
                         GamingComputerRepository gamingComputerRepository,
                         LaptopRepository laptopRepository,
                         WorkstationRepository workstationRepository) {
        this.rentalRepository = rentalRepository;
        this.memberRepository = memberRepository;
        this.gamingComputerRepository = gamingComputerRepository;
        this.laptopRepository = laptopRepository;
        this.workstationRepository = workstationRepository;
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    /**
     * Creates a new rental after validating all constraints
     */
    public synchronized void createRental(Long memberId, Long computerId, RentalType type, int duration, boolean isDaily) {

        if (duration <= 0) {
            throw new InvalidRentalDataException("Varaktigheten måste vara minst 1.");
        }

        if (rentalRepository.isObjectRented(computerId, type)) {
            throw new ItemAlreadyRentedException("Enheten är redan uthyrd!");
        }

        Member member = memberRepository.findById(memberId);

        if (member == null) {
            throw new EntityNotFoundException("Medlem hittades ej.");
        }

        if (member.isBlocked()) {
            throw new MemberBlockedException("Medlemmen är blockerad och kan inte hyra nya enheter.");
        }

        // Counts active rentals for member to enforce quota using safe ID comparison
        long activeRentalsCount = rentalRepository.findAll().stream()
                .filter(r -> r.getMember() != null &&
                        Objects.equals(r.getMember().getId(), memberId) &&
                        r.getEndDate() == null)
                .count();

        if (activeRentalsCount >= 3) {
            throw new QuotaExceededException("Medlemmen har redan nått sin maxkvot på 3 aktiva lån.");
        }

        // Validates requested device exists per type and enforces availability
        switch (type) {

            case GAMING_COMPUTER -> {
                if (gamingComputerRepository.findById(computerId) == null) {
                    throw new EntityNotFoundException("Gamingdator med ID " + computerId + " saknas.");
                }
            }
            case LAPTOP -> {
                if (laptopRepository.findById(computerId) == null) {
                    throw new EntityNotFoundException("Laptop med ID " + computerId + " saknas.");
                }
            }
            case WORKSTATION -> {
                if (workstationRepository.findById(computerId) == null) {
                    throw new EntityNotFoundException("Workstation med ID " + computerId + " saknas.");
                }
            }
        }

        Rental rental = new Rental();
        rental.setMember(member);
        rental.setRentalType(type);
        rental.setRentalObjectId(computerId);

        LocalDateTime now = LocalDateTime.now();
        rental.setStartDate(now);

        if (isDaily) {
            rental.setEstimatedReturnDate(now.plusDays(duration));
        } else {
            rental.setEstimatedReturnDate(now.plusHours(duration));
        }

        rental.setDailyRate(isDaily);
        rental.setEndDate(null);
        rental.setTotalPrice(0.0);

        rentalRepository.save(rental);
    }

    /**
     * Calculate final price including late fee and updates and persists rental
     */
    public void returnRental(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId);

        if (rental == null) {
            throw new EntityNotFoundException("Uthyrning hittades ej.");
        }

        if (rental.getEndDate() != null) {
            throw new RentalAlreadyReturnedException("Denna uthyrning är redan avslutad.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = rental.getStartDate();

        double unitPrice = getUnitPrice(rental.getRentalType(), rental.getRentalObjectId(), rental.isDailyRate());

        long durationUsed;

        // Calculate rental length in days or hours, minimum one unit
        if (rental.isDailyRate()) {

            durationUsed = ChronoUnit.DAYS.between(start, now);

            if (durationUsed <= 0) {
                durationUsed = 1;
            }

        } else {

            durationUsed = ChronoUnit.HOURS.between(start, now);

            if (durationUsed <= 0) {
                durationUsed = 1;
            }
        }

        double finalPrice = durationUsed * unitPrice;

        if (now.isAfter(rental.getEstimatedReturnDate())) {
            finalPrice += 300.0;
        }

        rental.setEndDate(now);
        rental.setTotalPrice(finalPrice);

        rentalRepository.save(rental);
    }

    private double getUnitPrice(RentalType type, Long id, boolean isDaily) {

        // Selects appropriate device and returns its daily or hourly rate
        switch (type) {

            case GAMING_COMPUTER -> {
                GamingComputer gc = gamingComputerRepository.findById(id);
                return isDaily ? gc.getDailyPrice() : gc.getHourlyPrice();
            }

            case LAPTOP -> {
                Laptop l = laptopRepository.findById(id);
                return isDaily ? l.getDailyPrice() : l.getHourlyPrice();
            }

            case WORKSTATION -> {
                Workstation w = workstationRepository.findById(id);
                return isDaily ? w.getDailyPrice() : w.getHourlyPrice();
            }

            default -> throw new IllegalArgumentException("Okänd typ");
        }
    }
}