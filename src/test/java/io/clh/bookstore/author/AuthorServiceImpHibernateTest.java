package io.clh.bookstore.author;

import io.clh.models.Author;
import io.clh.models.Book;
import io.clh.models.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorServiceImpHibernateTest {
    private static SessionFactory sessionFactory;
    private static Session session;

    @Container
    private static final PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>("postgres:latest").withDatabaseName("testdb").withUsername("test").withPassword("test");

    @BeforeAll
    public static void setUp() throws IOException, URISyntaxException {
        postgresqlContainer.start();

        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.url", postgresqlContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgresqlContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgresqlContainer.getPassword());
        configuration.setImplicitNamingStrategy(new org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl());
        configuration.addAnnotatedClass(Author.class);
        configuration.addAnnotatedClass(Book.class);
        configuration.addAnnotatedClass(Category.class);

        sessionFactory = configuration.buildSessionFactory();
        session = sessionFactory.openSession();

        Path path = Paths.get(ClassLoader.getSystemResource("setup.sql").toURI());
        String sql = new String(Files.readAllBytes(path));
        try (Connection conn = DriverManager.getConnection(postgresqlContainer.getJdbcUrl(), postgresqlContainer.getUsername(), postgresqlContainer.getPassword()); Statement stmt = conn.createStatement()) {
            String[] statements = sql.split(";");
            for (String statement : statements) {
                stmt.execute(statement.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    @AfterAll
    public static void tearDown() {
        if (session != null) {
            session.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }

        postgresqlContainer.stop();
    }

    @Test
    @Order(1)
    public void createAuthor() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        Book book = new Book();

        Set<Book> emptySet = Set.of();


        Author author = new Author(1L, "username", "my biblio", "",
                emptySet
        );

        session.beginTransaction();
        authorServiceImp.addAuthor(author);
        Author retrievedAuthor = authorServiceImp.getAuthorById(author.getAuthor_id());
        session.getTransaction().commit();

        Assertions.assertNotNull(retrievedAuthor);
        Assertions.assertEquals("username", new String(retrievedAuthor.getName()));
        assertEquals("my biblio", retrievedAuthor.getBiography());
    }


    @Test
    @Order(2)
    public void getAuthorByIdShouldNotBeEmpty() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        Author authorById1 = authorServiceImp.getAuthorById(1L);

        Assertions.assertTrue(!authorById1.getName().isEmpty());
    }


    @Test
    @Order(2)
    public void getAllAuthorsShouldNotBeEmpty() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        List<Author> authors = authorServiceImp.getAllAuthors(1);

        assertFalse(authors.isEmpty(), "The list of authors should not be empty");
    }

    @Test
    @Order(3)
    public void setAuthorImageUrlAvatar() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        Author author = authorServiceImp.setUrlAvatar("https://0.gravatar.com/avatar/1b4e9e532c9fbb9e7eec83c0a2cb8884bfb996017696c7a419c0ec92b870a35b?size=256", 1L);

        Assertions.assertNotNull(author);
    }
}