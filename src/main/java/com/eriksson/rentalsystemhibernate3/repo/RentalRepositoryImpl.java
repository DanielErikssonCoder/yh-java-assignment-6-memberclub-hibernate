package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Rental;
import com.eriksson.rentalsystemhibernate3.entity.RentalType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class RentalRepositoryImpl implements RentalRepository {

    private final SessionFactory sessionFactory;

    public RentalRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Rental rental) {

        Transaction tx = null;

        // Persists or updates rental transactionally and rolls back on error
        try (Session session = sessionFactory.openSession()) {

            tx = session.beginTransaction();

            if (rental.getRentalId() == null) {
                session.persist(rental);

            } else {
                session.merge(rental);
            }

            tx.commit();

        } catch (Exception e) {

            if (tx != null) {
                tx.rollback();
            }

            throw e;
        }
    }

    @Override
    public Rental findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Rental.class, id);
        }
    }

    @Override
    public List<Rental> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Rental", Rental.class).list();
        }
    }

    @Override
    public boolean isObjectRented(Long objectId, RentalType type) {

        // Checks for active rental of specified object and type
        try (Session session = sessionFactory.openSession()) {

            String hql = "SELECT count(r) FROM Rental r WHERE r.rentalObjectId = :id AND r.rentalType = :type AND r.endDate IS NULL";

            Long count = session.createQuery(hql, Long.class)
                    .setParameter("id", objectId)
                    .setParameter("type", type)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }
}