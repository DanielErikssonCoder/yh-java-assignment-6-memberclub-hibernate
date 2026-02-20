package com.eriksson.rentalsystemhibernate3.util;

import com.eriksson.rentalsystemhibernate3.entity.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import java.io.InputStream;
import java.util.Properties;


/**
 * This is a utility class that are responsible for creating, providing and shutting down the Hibernate {@link SessionFactory}.
 * <p>
 * The session factory is built once at startup from a {@code hibernate.properties} file located on the classpath.
 * <p>
 * If the properties file cannot be found or an error occurs during configuration, the application falls back to an
 * offline mode where no persistent storage is available.
 * <p>
 * All public methods are static and thread‑safe. The class itself cannot be instantiated.
 */
public class HibernateUtil {

    // The name of the property file that stores the Hibernate configuration
    private static final String PROPERTIES_FILE = "hibernate.properties";

    /*
        Singleton instance shared across the application. Reads hibernate properties, opens a connection to MySQL,
        checks tables, creates missing tables (if any)
     */
    private static SessionFactory SESSION_FACTORY = buildSessionFactory();


    // Prevents external instantiation of HibernateUtil
    private HibernateUtil() {}

    /**
     * Provides thread‑safe access to shared SessionFactory (SessionFactory is used to create database-sessions)
     */
    public static SessionFactory getSessionFactory() {

        // Recreate the SessionFactory when it hasn't been initialized or has already been closed.
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

    /**
     * Helper method for creating a Hibernate object, which is the central component in the Hibernate-ORM framework,
     * which are needed to open database sessions
     */
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

            // An empty configuration-object is created and inserts our properties
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

            /*
                Using the populated Configuration together with the ServiceRegistry,
                we can now construct the final SessionFactory
             */
            return configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception e) {

            System.err.println("KRITISKT: Databasen kunde inte nås vid uppstart. Programmet körs i offline-läge.");

            return null;
        }
    }

    /**
     * Checks whether a connection to the database is currently available
     */
    public static boolean checkConnection() {

        // Verifies DB reachability via test query
        try {

            // Obtain (or rebuild) the shared SessionFactory instance
            SessionFactory sf = getSessionFactory();

            // If we couldn't create a SessionFactory or it's already closed, the DB is unreachable
            if (sf == null || sf.isClosed()) {
                return false;
            }

            // Open a new session for the duration of this check. It will be automatically closed
            try (org.hibernate.Session session = sf.openSession()) {
                session.createNativeQuery("SELECT 1").getSingleResult();
                return true;
            }

        } catch (Exception e) {

            // Any exception indicates that the DB cannot be reached at the moment
            return false;
        }
    }
}