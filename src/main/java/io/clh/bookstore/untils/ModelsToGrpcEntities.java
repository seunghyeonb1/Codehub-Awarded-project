package io.clh.bookstore.untils;

import com.google.protobuf.Timestamp;
import io.clh.bookstore.entities.Entities;
import io.clh.models.Author;
import io.clh.models.Book;
import io.clh.models.Category;

import java.util.List;
import java.util.stream.Collectors;

public class ModelsToGrpcEntities {
    public static Entities.Book BookModelToGrpc(Book book) {
        Timestamp timestamp = null;
        if (book.getPublicationDate() != null) {
            long millis = book.getPublicationDate().getTime();
            timestamp = Timestamp.newBuilder().setSeconds(millis / 1000).setNanos((int) ((millis % 1000) * 1000000)).build();
        }

        Entities.Book.Builder bookBuilder = Entities.Book.newBuilder()
                .setBookId(book.getBook_id())
                .setPrice(book.getPrice())
                .setIsbn(book.getIsbn())
                .setDescription(book.getDescription())
                .setTitle(book.getTitle())
                .setStockQuantity(book.getStockQuantity());

        if (book.getAvatar_url() != null) {
            bookBuilder.setAvatarUrl(book.getAvatar_url());
        }

        if (timestamp != null) {
            bookBuilder.setPublicationDate(timestamp);
        }

        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            List<Long> authorIds = book.getAuthors().stream().map(Author::getAuthor_id).map(Long::valueOf).collect(Collectors.toList());

            bookBuilder.addAllAuthorIds(authorIds);
        }

        return bookBuilder.build();
    }

    public static Entities.Category CategoryModelToCategoryGrpc(Category category) {
        Entities.Category.Builder builder = Entities.Category.newBuilder();

        builder.setId(category.getCategory_id());
        builder.setName(category.getName());
        builder.setDescription(category.getDescription());
        List<Entities.Book> list = category.getBooks().stream().map(ModelsToGrpcEntities::BookModelToGrpc).toList();
        builder.addAllBooks(list);

        return builder.build();
    }

    public static Entities.AuthorEntity AuthorEntityModelToAuthorGrpc(Author author) {

        Entities.AuthorEntity.Builder builder = Entities.AuthorEntity.newBuilder();

        builder.setAuthorId(author.getAuthor_id());
        builder.setName(author.getName());
        builder.setBiography(author.getBiography());
        builder.setAvatarUrl(author.getAvatar_url());

        return builder.build();
    }
}