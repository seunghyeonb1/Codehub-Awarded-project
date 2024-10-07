package io.clh.bookstore.author;

import io.clh.models.Author;

import java.util.List;

// AuthorService
public interface AuthorService {
    Author addAuthor(Author author);

    Author getAuthorById(Long id);

    List<Author> getAllAuthors(int page);

    Author setUrlAvatar(String url, Long id) throws IllegalAccessException;
}