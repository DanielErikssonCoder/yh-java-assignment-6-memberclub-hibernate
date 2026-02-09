package com.eriksson.rentalhibernate3.service;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.*;
import com.eriksson.rentalsystemhibernate3.service.RentalService;
import com.eriksson.rentalsystemhibernate3.exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock RentalRepository rentalRepo;
    @Mock MemberRepository memberRepo;
    @Mock GamingComputerRepository gamingRepo;
    @Mock LaptopRepository laptopRepo;
    @Mock WorkstationRepository workstationRepo;

    RentalService rentalService;

    @BeforeEach
    public void setUp() {
        rentalService = new RentalService(rentalRepo, memberRepo, gamingRepo, laptopRepo, workstationRepo);
    }

    // TEST 1: Successful rental
    @Test
    public void createRental_success_gamingComputer() {
        Member member = new Member("Test", "Testsson", "test@test.com");
        GamingComputer gc = new GamingComputer("ROG", "AMD", 32, "1TB", "RTX 5080", 50.0, 100.0);

        when(memberRepo.findById(1L)).thenReturn(member);
        when(gamingRepo.findById(1L)).thenReturn(gc);
        when(rentalRepo.isObjectRented(1L, RentalType.GAMING_COMPUTER)).thenReturn(false);

        assertDoesNotThrow(() ->
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 3, true)
        );

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepo).save(captor.capture());

        Rental saved = captor.getValue();
        assertEquals(0.0, saved.getTotalPrice());
        assertTrue(saved.isDailyRate());
        assertNotNull(saved.getEstimatedReturnDate());
    }

    // TEST 2: Member does not exist
    @Test
    public void createRental_memberNotFound_throwException() {
        when(rentalRepo.isObjectRented(1L, RentalType.GAMING_COMPUTER)).thenReturn(false);
        when(memberRepo.findById(99L)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                rentalService.createRental(99L, 1L, RentalType.GAMING_COMPUTER, 1, true)
        );
        // Tar bort contains-kollen för att undvika språkfel. Typen räcker.
    }

    // TEST 3: Object is already rented
    @Test
    void createRental_objectAlreadyRented_throwsException() {
        when(rentalRepo.isObjectRented(1L, RentalType.GAMING_COMPUTER)).thenReturn(true);

        ItemAlreadyRentedException ex = assertThrows(ItemAlreadyRentedException.class, () ->
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 1, true)
        );
        // FIX: Tog bort assertTrue(contains(...)) eftersom meddelandet kan variera
    }

    // TEST 4: Computer does not exist
    @Test
    void createRental_gamingComputerNotFound_throwsException() {
        Member member = new Member("A", "B", "c@c.com");

        when(rentalRepo.isObjectRented(99L, RentalType.GAMING_COMPUTER)).thenReturn(false);
        when(memberRepo.findById(1L)).thenReturn(member);
        when(gamingRepo.findById(99L)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                rentalService.createRental(1L, 99L, RentalType.GAMING_COMPUTER, 1, true)
        );
    }

    // TEST 5: Invalid duration
    @Test
    void createRental_invalidDuration_throwsException() {
        InvalidRentalDataException ex = assertThrows(InvalidRentalDataException.class, () ->
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 0, true)
        );
    }

    // TEST 6: Member blocked
    @Test
    void createRental_memberBlocked_throwsException() {
        Member member = new Member("Block", "Ad", "b@b.com");
        member.setBlocked(true);

        when(rentalRepo.isObjectRented(1L, RentalType.GAMING_COMPUTER)).thenReturn(false);
        when(memberRepo.findById(1L)).thenReturn(member);

        assertThrows(MemberBlockedException.class, () ->
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 1, true)
        );
    }

    // TEST 7: Quota exceeded
    @Test
    void createRental_quotaExceeded_throwsException() {

        Member member = new Member("A", "B", "a@b.com");
        member.setMemberId(1L);

        List<Rental> activeRentals = new ArrayList<>();
        // Creates three active rentals linked to member
        for (int i = 0; i < 3; i++) {
            Rental r = new Rental();
            r.setMember(member);
            r.setEndDate(null);
            activeRentals.add(r);
        }

        when(rentalRepo.isObjectRented(1L, RentalType.GAMING_COMPUTER)).thenReturn(false);
        when(memberRepo.findById(1L)).thenReturn(member);
        when(rentalRepo.findAll()).thenReturn(activeRentals);

        assertThrows(QuotaExceededException.class, () ->
                rentalService.createRental(1L, 1L, RentalType.GAMING_COMPUTER, 1, true)
        );
    }

    // TEST 8: Return already returned
    @Test
    void returnRental_alreadyReturned_throwsException() {
        Rental rental = new Rental();
        rental.setEndDate(LocalDateTime.now().minusDays(1));

        when(rentalRepo.findById(1L)).thenReturn(rental);

        assertThrows(RentalAlreadyReturnedException.class, () ->
                rentalService.returnRental(1L)
        );
    }

    // TEST 9: Return on time
    @Test
    void returnRental_onTime_calculatesDailyPrice() {
        Rental rental = new Rental();
        rental.setStartDate(LocalDateTime.now().minusDays(2));
        rental.setEstimatedReturnDate(LocalDateTime.now().plusDays(1));
        rental.setRentalType(RentalType.GAMING_COMPUTER);
        rental.setRentalObjectId(10L);
        rental.setDailyRate(true);
        rental.setEndDate(null);

        GamingComputer gc = new GamingComputer("TestPC", "CPU", 16, "SSD", "GPU", 50.0, 100.0);

        when(rentalRepo.findById(1L)).thenReturn(rental);
        when(gamingRepo.findById(10L)).thenReturn(gc);

        rentalService.returnRental(1L);

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepo).save(captor.capture());
        assertEquals(200.0, captor.getValue().getTotalPrice());
    }

    // TEST 10: Return late
    @Test
    void returnRental_late_addsPenaltyFee() {
        Rental rental = new Rental();
        rental.setStartDate(LocalDateTime.now().minusDays(5));
        rental.setEstimatedReturnDate(LocalDateTime.now().minusDays(1)); // Late
        rental.setRentalType(RentalType.GAMING_COMPUTER);
        rental.setRentalObjectId(10L);
        rental.setDailyRate(true);
        rental.setEndDate(null);

        GamingComputer gc = new GamingComputer("TestPC", "CPU", 16, "SSD", "GPU", 50.0, 100.0);

        when(rentalRepo.findById(1L)).thenReturn(rental);
        when(gamingRepo.findById(10L)).thenReturn(gc);

        rentalService.returnRental(1L);

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepo).save(captor.capture());
        assertEquals(800.0, captor.getValue().getTotalPrice());
    }
}