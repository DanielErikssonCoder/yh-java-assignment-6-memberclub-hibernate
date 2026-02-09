package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.GamingComputer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class GamingComputerRepositoryImpl implements GamingComputerRepository {

    private final SessionFactory sessionFactory;

    public GamingComputerRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Persists new gaming computer using transactional session
     */
    @Override
    public void save(GamingComputer gameComputer) {

        try (Session session = sessionFactory.openSession()) {

            var tx = session.beginTransaction();
            session.persist(gameComputer);
            tx.commit();
        }
    }

    @Override
    public GamingComputer findById(Long id) {

        try (Session session = sessionFactory.openSession()) {

            return session.get(GamingComputer.class, id);
        }
    }

    @Override
    public List<GamingComputer> findAll() {

        try (Session session = sessionFactory.openSession()) {

            return session.createQuery("FROM GamingComputer", GamingComputer.class).list();
        }
    }
}
