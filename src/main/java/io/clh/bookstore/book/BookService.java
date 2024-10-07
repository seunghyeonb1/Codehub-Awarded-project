package io.clh.bookstore.book;

import io.clh.models.Book;

import java.util.List;
import java.util.Set;

public interface BookService {
    Book createBook(Book book);

    List<Book> getAllBooks(int page);

    Book updateBook(Book book);

    Book getBookById(Long bookId);

    Set<Book> findBooksByAuthorId(Long authorId);

    Book deleteBookById(Long bookId);
}
