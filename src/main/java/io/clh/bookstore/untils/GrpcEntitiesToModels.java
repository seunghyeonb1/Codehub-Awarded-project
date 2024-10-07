package io.clh.bookstore.untils;

import com.google.protobuf.Timestamp;
import io.clh.bookstore.author.AuthorService;
import io.clh.bookstore.book.BookService;
import io.clh.bookstore.entities.Entities;
import io.clh.models.Author;
import io.clh.models.Book;
import io.clh.models.Category;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GrpcEntitiesToModels {
    public static Author AuthorGrpcToAuthorModel(Entities.AuthorEntity authorEntity) {
        return Author.builder()
                .avatar_url(authorEntity.getAvatarUrl())
                .biography(authorEntity.getBiography())
                .name(authorEntity.getName())
                .build();
    }

    public Book convertFromBookProto(Entities.Book bookProto, AuthorService authorService) {
        Date date = null;

        if (bookProto.hasPublicationDate()) {
            Timestamp ts = bookProto.getPublicationDate();
            long millis = ts.getSeconds() * 1000 + ts.getNanos() / 1000000;
            date = new Date(millis);
        }

        Set<Author> authors = new HashSet<>();
        for (long authorId : bookProto.getAuthorIdsList()) {
            Author author = authorService.getAuthorById(authorId);
            if (author != null) {
                authors.add(author);
            }
        }

        return Book.builder()
                .book_id(bookProto.getBookId())
                .title(bookProto.getTitle())
                .description(bookProto.getDescription())
                .isbn(bookProto.getIsbn())
                .price(bookProto.getPrice())
                .stockQuantity(bookProto.getStockQuantity())
                .avatar_url(bookProto.getAvatarUrl())
                .publicationDate(date)
                .authors(authors)
                .build();
    }

    public Category CategoryGrpcToCategoryModel(Entities.Category category, AuthorService authorService, BookService bookService) {
        GrpcEntitiesToModels converter = new GrpcEntitiesToModels();
        List<Book> list = category.getBooksList().stream()
                .map(book -> converter.convertFromBookProto(book, authorService)) // Convert from proto to Book
                .map(book -> bookService.getBookById(book.getBook_id())) // Fetch the book from the service
                .filter(Objects::nonNull)
                .toList();

        Set<Book> booksSet = new HashSet<>(list);


        return Category.builder()
                .category_id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .books(booksSet)
                .build();
    }
}