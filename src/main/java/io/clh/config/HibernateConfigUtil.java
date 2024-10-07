package io.clh.config;

import io.clh.models.Author;
import io.clh.models.Book;
import io.clh.models.Category;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Properties;

public class HibernateConfigUtil {
    private static final String POSTGRES_URL = System.getenv("POSTGRES_URL") != null ?
            System.getenv("POSTGRES_URL") :
            "jdbc:postgresql://localhost:5432/your_db_name";

    private static final String POSTGRES_USER = System.getenv("POSTGRES_USER") != null ?
            System.getenv("POSTGRES_USER") :
            "your_username";

    private static final String POSTGRES_PASS = System.getenv("POSTGRES_PASS") != null ?
            System.getenv("POSTGRES_PASS") :
            "your_password";

    public static SessionFactory createSessionFactory() {
        Configuration configuration = new Configuration();

        Properties settings = new Properties();
        settings.put(Environment.DRIVER, "org.postgresql.Driver");
        settings.put(Environment.URL, POSTGRES_URL);
        settings.put(Environment.USER, POSTGRES_USER);
        settings.put(Environment.PASS, POSTGRES_PASS);
        settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.HBM2DDL_AUTO, "update");

        configuration.setProperties(settings);

        // Add annotated classes
        configuration.addAnnotatedClass(Author.class);
        configuration.addAnnotatedClass(Book.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.setImplicitNamingStrategy(new org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl());

        return configuration.buildSessionFactory();
    }
}
