package io.clh.bookstore.author;

import io.clh.models.Author;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;


@RequiredArgsConstructor
public class AuthorServiceImp implements AuthorService {
    private final SessionFactory sessionFactory;

    @Override
    public Author addAuthor(Author author) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(author);
            transaction.commit();

            return author;
        } catch (RuntimeException e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public Author getAuthorById(Long id) {
        Session session = sessionFactory.openSession();
        Author author = session.get(Author.class, id);
        session.close();
        return author;
    }


    @Override
    public List<Author> getAllAuthors(int page) {
        final int pageSize = 50;
        try (Session session = sessionFactory.openSession()) {
            int firstResult = (page - 1) * pageSize;

            return session.createQuery("from Author", Author.class)
                    .setFirstResult(firstResult)
                    .setMaxResults(pageSize)
                    .list();
        } catch (RuntimeException e) {
            throw e;
        }
    }


    @Override
    public Author setUrlAvatar(String url, Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Author author = session.get(Author.class, id);
            if (author == null) {
                throw new IllegalArgumentException(String.format("User with id %s not found", id));
            }

            author.setAvatar_url(url);
            session.update(author);
            transaction.commit();

            return author;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

}
