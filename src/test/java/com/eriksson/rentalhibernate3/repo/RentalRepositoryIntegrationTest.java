package com.eriksson.rentalhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.*;
import com.eriksson.rentalsystemhibernate3.repo.MemberRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.repo.RentalRepositoryImpl;
import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// This is an Integration Test.
// Unlike Unit Tests (which use mocks), this test starts a REAL Hibernate session
// and talks to a real (in-memory H2) database.
class RentalRepositoryIntegrationTest {

    private static SessionFactory sessionFactory;
    private RentalRepositoryImpl rentalRepo;
    private MemberRepositoryImpl memberRepo;

    // @BeforeAll runs ONCE before any test methods in this class.
    @BeforeAll
    static void init() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    // @AfterAll runs ONCE after all tests are finished.
    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    // @BeforeEach runs before EVERY single test.
    @BeforeEach
    void setUp() {
        rentalRepo = new RentalRepositoryImpl(sessionFactory);
        memberRepo = new MemberRepositoryImpl(sessionFactory);

        // CLEANUP
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rental").executeUpdate();
            session.createMutationQuery("DELETE FROM Member").executeUpdate();
            session.getTransaction().commit();
        }
    }

    // TEST 1: Verify we can save a Rental and find it again.
    @Test
    void saveAndFindRental_shouldPersistToDatabase() {

        // 1. Create and save a Member first.
        Member m = new Member("Test", "Integrationsson", "test@integration.se");
        memberRepo.save(m);

        // 2. Create and save a Rental (Historical rental, returned)
        // Constructor: Member, Type, ObjectId, Start, End, Price, Estimated, IsDaily
        Rental rental = new Rental(
                m,
                RentalType.LAPTOP,
                10L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(2), // Returned 2 days ago
                500.0,
                LocalDateTime.now().minusDays(2), // Estimated return
                true
        );
        rentalRepo.save(rental);

        // 3. Verify
        assertNotNull(rental.getRentalId(), "Rental should have an ID after save");
        assertFalse(rentalRepo.findAll().isEmpty(), "Database should not be empty");
    }

    // TEST 2: Verify our custom "isObjectRented" logic works with real SQL.
    @Test
    void isObjectRented_shouldReturnTrueIfActiveRentalExists() {
        // 1. Setup data
        Member m = new Member("Check", "Rented", "check@rented.se");
        memberRepo.save(m);

        // Create an ACTIVE rental (endDate is null)
        Rental activeRental = new Rental(
                m,
                RentalType.GAMING_COMPUTER,
                55L,
                LocalDateTime.now(),
                null, // Active!
                0.0,
                LocalDateTime.now().plusDays(1),
                true
        );
        rentalRepo.save(activeRental);

        // 2. Test Positive Case:
        // Check if ID 55 is considered rented. Should be TRUE.
        boolean isRented = rentalRepo.isObjectRented(55L, RentalType.GAMING_COMPUTER);
        assertTrue(isRented, "Object 55 should be marked as rented");

        // 3. Test Negative Case:
        // Check an ID that we haven't rented (999L). Should be FALSE.
        boolean isOtherRented = rentalRepo.isObjectRented(999L, RentalType.GAMING_COMPUTER);
        assertFalse(isOtherRented, "Object 999 should NOT be marked as rented");
    }
}