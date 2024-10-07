package io.clh.bookstore.book;

import io.clh.bookstore.author.AuthorServiceImp;
import io.clh.bookstore.bookstore.Book;
import io.clh.bookstore.bookstore.BookServiceGrpc;
import io.clh.bookstore.entities.Entities;
import io.clh.bookstore.untils.GrpcEntitiesToModels;
import io.clh.bookstore.untils.ModelsToGrpcEntities;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.clh.bookstore.untils.ModelsToGrpcEntities.BookModelToGrpc;

@RequiredArgsConstructor
public class BookServiceGrpcImp extends BookServiceGrpc.BookServiceImplBase {

    private final BookServiceImpService bookServiceImp;
    private final AuthorServiceImp authorServiceImp;

    @Override
    public void createBook(Book.CreateBookRequest request, StreamObserver<Book.CreateBookResponse> responseObserver) {
        try {
            GrpcEntitiesToModels converter = new GrpcEntitiesToModels();
            io.clh.models.Book book = converter.convertFromBookProto(request.getBook(), authorServiceImp);
            io.clh.models.Book createdBook = bookServiceImp.createBook(book);
            Entities.Book grpcBook = BookModelToGrpc(createdBook);
            Book.CreateBookResponse response = Book.CreateBookResponse.newBuilder().setBook(grpcBook).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getBookById(Book.GetBookByIdRequest request, StreamObserver<Book.GetBookByIdResponse> responseObserver) {
        try {
            io.clh.models.Book bookById = bookServiceImp.getBookById(request.getId());
            if (bookById == null) {
                responseObserver.onError(new Throwable("Book not found with ID: " + request.getId()));
                return;
            }
            Entities.Book grpc = BookModelToGrpc(bookById);

            Book.GetBookByIdResponse response = Book.GetBookByIdResponse.newBuilder().setBook(grpc).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void getAllBooks(Book.GetAllBooksRequest request, StreamObserver<Entities.Book> responseObserver) {
        try {
            int limitPages = request.getPage() == 0 ? 1 : request.getPage();
            List<io.clh.models.Book> books = bookServiceImp.getAllBooks(limitPages);
            books.stream().map(ModelsToGrpcEntities::BookModelToGrpc).forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateBook(Book.UpdateBookRequest request, StreamObserver<Book.UpdateBookResponse> responseObserver) {
        try {
            GrpcEntitiesToModels converter = new GrpcEntitiesToModels();
            io.clh.models.Book book = converter.convertFromBookProto(request.getBook(), authorServiceImp);

            io.clh.models.Book updatedBook = bookServiceImp.updateBook(book);

            Entities.Book grpcBook = BookModelToGrpc(updatedBook);
            Book.UpdateBookResponse updateBookResponse = Book.UpdateBookResponse.newBuilder().setBook(grpcBook).build();

            responseObserver.onNext(updateBookResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteBook(Book.DeleteBookRequest request, StreamObserver<Book.DeleteBookResponse> responseObserver) {
        try {
            io.clh.models.Book deletedBookById = bookServiceImp.deleteBookById(request.getBook().getBookId());
            if (deletedBookById == null) {
                responseObserver.onError(new Throwable("Book not able to delete with ID: " + request.getBook().getBookId()));
                return;
            }

            Entities.Book grpcBook = BookModelToGrpc(deletedBookById);
            Book.DeleteBookResponse deleteBookResponse = Book.DeleteBookResponse.newBuilder().setBook(grpcBook).build();

            responseObserver.onNext(deleteBookResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
