package io.clh.bookstore.books;

import io.clh.bookstore.author.AuthorServiceImp;
import io.clh.bookstore.book.BookServiceImpService;
import io.clh.models.Author;
import io.clh.models.Book;
import io.clh.models.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookServiceImpHibernateTest {
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
    public void createBook() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        BookServiceImpService bookServiceImp = new BookServiceImpService(sessionFactory, authorServiceImp);

        Author author = new Author();
        author.setName("Author Name");
        author.setBiography("A brief bio");
        author.setAvatar_url("http://example.com/avatar.jpg");

        Transaction tx = session.beginTransaction();
        authorServiceImp.addAuthor(author);
        tx.commit();

        Book book = new Book();
        book.setTitle("Test Book");
        book.setDescription("A test book description");
        book.setIsbn("1234567890");
        book.setPrice(19.99);
        book.setStockQuantity(100);
        book.setAvatar_url("http://example.com/image.png");
        book.setPublicationDate(new Date(System.currentTimeMillis()));
        book.setAuthors(Set.of(author));

        tx = session.beginTransaction();
        bookServiceImp.createBook(book);
        tx.commit();

        Book retrievedBook = bookServiceImp.getBookById(1L);

        Assertions.assertTrue(retrievedBook.getBook_id() > 0);
        Assertions.assertFalse(retrievedBook.getAuthors().isEmpty());
    }


    @Test
    @Order(2)
    public void GetBookByIdNotEmpty() {
        AuthorServiceImp authorServiceImp = new AuthorServiceImp(sessionFactory);
        BookServiceImpService bookServiceImp = new BookServiceImpService(sessionFactory, authorServiceImp);

        Transaction tx = session.beginTransaction();
        Book retrievedBook = bookServiceImp.getBookById(1L);
        tx.commit();

        Assertions.assertTrue(retrievedBook.getBook_id() > 0);
    }

    @Test
    @Order(3)
    public void updateBook() {
        BookServiceImpService bookServiceImp = new BookServiceImpService(sessionFactory, new AuthorServiceImp(sessionFactory));

        Transaction tx = session.beginTransaction();
        Book bookToUpdate = bookServiceImp.getBookById(1L);
        bookToUpdate.setTitle("Updated Test Book Title");
        bookServiceImp.updateBook(bookToUpdate);
        tx.commit();

        Book updatedBook = bookServiceImp.getBookById(1L);

        Assertions.assertEquals("Updated Test Book Title", updatedBook.getTitle());
    }

    @Test
    @Order(4)
    public void deleteBook() {
        BookServiceImpService bookServiceImp = new BookServiceImpService(sessionFactory, new AuthorServiceImp(sessionFactory));
        Transaction tx = session.beginTransaction();
        Book bookToDelete = bookServiceImp.deleteBookById(1L);
        tx.commit();

        Assertions.assertTrue(bookToDelete.getBook_id() > 0);
    }

    @Test
    @Order(5)
    public void BookHasBeenRemoved() {
        BookServiceImpService bookServiceImp = new BookServiceImpService(sessionFactory, new AuthorServiceImp(sessionFactory));

        Transaction tx = session.beginTransaction();
        Book removedBook = bookServiceImp.getBookById(1L);
        tx.commit();

        Assertions.assertNull(removedBook);
    }
}
