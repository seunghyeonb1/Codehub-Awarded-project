package io.clh.bookstore.categories;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryServiceImpHibernateTest {
    private static SessionFactory sessionFactory;
    private static Session session;

    @Container
    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest").withDatabaseName("testdb").withUsername("test").withPassword("test");

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
    public void CreateCategory() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);

        Set<Book> emptyBooks = Set.of();

        Category category = new Category();
        category.setName("Novel");
        category.setDescription("Test");
        category.setBooks(emptyBooks);

        Transaction tx = session.beginTransaction();
        Category addedCategory = categoryServiceImp.AddCategory(category);
        tx.commit();

        Assertions.assertEquals(addedCategory.getCategory_id(), 1);
    }

    @Test
    @Order(2)
    public void GetAllCategories() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);

        Transaction tx = session.beginTransaction();
        List<Category> categories = categoryServiceImp.GetAllCategories();
        tx.commit();

        Optional<Long> allCategories = Optional.ofNullable(categories.get(0).getCategory_id());

        Assertions.assertFalse(categories.isEmpty());
        Assertions.assertEquals(allCategories.get(), 1);
    }

    @Test()
    @Order(3)
    public void UpdateCategory() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);
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

        Category category = new Category();
        category.setCategory_id(1L);
        category.setName("Novel");
        category.setDescription("Test");
        category.setBooks(Set.of(book));

        book.setCategory(category);

        tx = session.beginTransaction();
        bookServiceImp.createBook(book);
        tx.commit();


        Category updateCategory = categoryServiceImp.UpdateCategory(category);
        Set<Book> books = updateCategory.getBooks();

        List<Book> bookList = books.stream().toList();

        Assertions.assertTrue(!bookList.isEmpty());
        Assertions.assertEquals(bookList.size(), 1);
    }

    @Test
    @Order(4)
    public void GetAllBooksByCategory() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);
        List<Book> books = categoryServiceImp.GetAllBooksByCategory(1);

        Assertions.assertFalse(books.isEmpty(), "Books list should not be empty");
        Assertions.assertEquals(books.get(0).getTitle(), "Test Book");
    }

    @Test
    @Order(5)
    public void GetCategoryById() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);

        Category category = categoryServiceImp.GetCategoryById(1L);

        Assertions.assertTrue(category.getCategory_id() > 0);
        Assertions.assertEquals(category.getName(), "Novel");
    }

    @Test
    @Order(6)
    public void DeleteCategoryById() {
        CategoryServiceImpService categoryServiceImp = new CategoryServiceImpService(sessionFactory);

        Category category = categoryServiceImp.DeleteCategory(1);

        Assertions.assertTrue(category.getCategory_id() > 0);
        Assertions.assertEquals(category.getName(), "Novel");
    }
}