package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Workstation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class WorkstationRepositoryImpl implements WorkstationRepository {

    private final SessionFactory sessionFactory;

    public WorkstationRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Persists workstation in transactional session
     */
    @Override
    public void save(Workstation workstation) {

        try (Session session = sessionFactory.openSession()) {

            var tx = session.beginTransaction();
            session.persist(workstation);
            tx.commit();
        }
    }

    @Override
    public Workstation findById(Long id) {

        try (Session session = sessionFactory.openSession()) {

            return session.get(Workstation.class, id);
        }
    }

    @Override
    public List<Workstation> findAll() {

        try (Session session = sessionFactory.openSession()) {

            return session.createQuery("FROM Workstation", Workstation.class).list();
        }
    }
}
