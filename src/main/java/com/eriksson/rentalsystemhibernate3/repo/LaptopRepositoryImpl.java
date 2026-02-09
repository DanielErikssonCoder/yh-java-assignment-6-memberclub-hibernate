package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Laptop;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class LaptopRepositoryImpl  implements LaptopRepository {

    private final SessionFactory sessionFactory;

    public LaptopRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Persists laptop entity inside transactional session
     */
    @Override
    public void save(Laptop laptop) {

        try (Session session = sessionFactory.openSession()) {

            var tx = session.beginTransaction();
            session.persist(laptop);
            tx.commit();
        }
    }

    @Override
    public Laptop findById(Long id) {

        try (Session session = sessionFactory.openSession()) {

            return session.get(Laptop.class, id);
        }
    }

    @Override
    public List<Laptop> findAll() {

        try (Session session = sessionFactory.openSession()) {

            return session.createQuery("FROM Laptop", Laptop.class).list();
        }
    }
}
