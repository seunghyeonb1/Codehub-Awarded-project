package io.clh.bookstore.categories;


import io.clh.models.Book;
import io.clh.models.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class CategoryServiceImpService implements CategoryService {
    private final SessionFactory sessionFactory;

    public CategoryServiceImpService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Book> GetAllBooksByCategory(Integer categoryId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Book> cq = cb.createQuery(Book.class);
            Root<Book> root = cq.from(Book.class);
            cq.where(cb.equal(root.get("category").get("category_id"), categoryId));

            TypedQuery<Book> query = session.createQuery(cq);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving books by category", e);
        }
    }


    @Override
    public Category AddCategory(Category category) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            if (!category.getBooks().isEmpty()) {
                category.getBooks().forEach(book -> {
                    book.setCategory(category);
                    session.saveOrUpdate(book);
                });
            }

            session.save(category);
            transaction.commit();

            return category;
        } catch (RuntimeException e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public Category DeleteCategory(Integer categoryId) {
        Transaction transaction = null;
        Category categoryById = GetCategoryById(Long.valueOf(categoryId));

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            if (categoryById != null) {
                categoryById.getBooks().forEach(book -> {
                    book.setCategory(null);
                    session.saveOrUpdate(book);
                });
                session.delete(categoryById);
            }

            transaction.commit();
            return categoryById;
        } catch (RuntimeException e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }


    @Override
    public List<Category> GetAllCategories() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Category> cq = cb.createQuery(Category.class);
            Root<Category> rootEntry = cq.from(Category.class);
            CriteriaQuery<Category> all = cq.select(rootEntry);

            TypedQuery<Category> allQuery = session.createQuery(all);
            return allQuery.getResultList();
        }
    }

    @Override
    public Category UpdateCategory(Category category) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.update(category);
            transaction.commit();
            return category;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public Category GetCategoryById(Long categoryId) {
        try (Session session = sessionFactory.openSession()) {
            String jpql = "SELECT c FROM Category c LEFT JOIN FETCH c.books WHERE c.category_id = :categoryId";
            return session.createQuery(jpql, Category.class)
                    .setParameter("categoryId", categoryId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
