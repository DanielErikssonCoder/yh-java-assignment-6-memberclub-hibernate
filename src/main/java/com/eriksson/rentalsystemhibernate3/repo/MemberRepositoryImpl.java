package com.eriksson.rentalsystemhibernate3.repo;

import com.eriksson.rentalsystemhibernate3.entity.Member;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.List;

public class MemberRepositoryImpl implements MemberRepository {

    private final SessionFactory sessionFactory;

    public MemberRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Member member) {
        Transaction tx = null;

        // Persists member atomically and rolls back on failure
        try (Session session = sessionFactory.openSession()) {

            tx = session.beginTransaction();
            session.persist(member);
            tx.commit();

        } catch (Exception e) {

            if (tx != null) {
                tx.rollback();
            }

            throw e;
        }
    }

    @Override
    public void update(Member member) {

        Transaction tx = null;

        // Updates existing member via merge inside transactional block
        try (Session session = sessionFactory.openSession()) {

            tx = session.beginTransaction();
            session.merge(member);
            tx.commit();

        } catch (Exception e) {

            if (tx != null) {
                tx.rollback();
            }

            throw e;
        }
    }

    @Override
    public Member findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Member.class, id);
        }
    }

    @Override
    public List<Member> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Member", Member.class).list();
        }
    }

    @Override
    public void delete(Member member) {

        Transaction tx = null;

        // Deletes member merging if detached ensures atomic rollback on error
        try (Session session = sessionFactory.openSession()) {

            tx = session.beginTransaction();
            session.remove(session.contains(member) ? member : session.merge(member));
            tx.commit();

        } catch (Exception e) {

            if (tx != null) {
                tx.rollback();
            }

            throw e;
        }
    }
}