package com.eriksson.rentalsystemhibernate3.util;

import com.eriksson.rentalsystemhibernate3.entity.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.InputStream;
import java.util.Properties;

public class HibernateUtil {

    private static final String PROPERTIES_FILE = "hibernate.properties";

    private static SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {}

    /**
     * Provides thread‑safe access to shared SessionFactory
     */
    public static SessionFactory getSessionFactory() {

        if (SESSION_FACTORY == null || SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY = buildSessionFactory();
        }

        return SESSION_FACTORY;
    }

    /**
     * Shuts down active session factory resource
     */
    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }

    private static SessionFactory buildSessionFactory() {

        // Builds SessionFactory with annotations and logs error for offline mode
        try {
            Properties properties = new Properties();

            // Loads config properties from resource and returns null if absent
            try (InputStream in = HibernateUtil.class
                    .getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILE)) {

                if (in == null) {
                    System.err.println("Kunde inte hitta " + PROPERTIES_FILE);
                    return null;
                }

                properties.load(in);
            }

            Configuration configuration = new Configuration();
            configuration.setProperties(properties);
            configuration.addAnnotatedClass(Member.class);
            configuration.addAnnotatedClass(Rental.class);
            configuration.addAnnotatedClass(GamingComputer.class);
            configuration.addAnnotatedClass(Laptop.class);
            configuration.addAnnotatedClass(Workstation.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            return configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception e) {

            System.err.println("CRITICAL: Databasen kunde inte nås vid uppstart. Appen körs i offline-läge.");

            return null;
        }
    }

    public static boolean checkConnection() {

        // Verifies DB reachability via test query
        try {

            SessionFactory sf = getSessionFactory();

            if (sf == null || sf.isClosed()) {
                return false;
            }

            try (org.hibernate.Session session = sf.openSession()) {
                session.createNativeQuery("SELECT 1").getSingleResult();
                return true;
            }

        } catch (Exception e) {

            return false;
        }
    }
}