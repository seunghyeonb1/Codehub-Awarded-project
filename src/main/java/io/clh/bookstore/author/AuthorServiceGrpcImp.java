package io.clh.bookstore.author;

import io.clh.bookstore.book.BookServiceImpService;
import io.clh.bookstore.entities.Entities;
import io.clh.bookstore.untils.ModelsToGrpcEntities;
import io.clh.models.Book;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.clh.bookstore.untils.GrpcEntitiesToModels.AuthorGrpcToAuthorModel;
import static io.clh.bookstore.untils.ModelsToGrpcEntities.AuthorEntityModelToAuthorGrpc;

@RequiredArgsConstructor
public class AuthorServiceGrpcImp extends AuthorServiceGrpc.AuthorServiceImplBase {
    private final AuthorServiceImp authorServiceImp;
    private final BookServiceImpService bookServiceImp;

    @Override
    public void createAuthor(Author.CreateAuthorRequest request, StreamObserver<Entities.AuthorEntity> responseObserver) {
        try {
            io.clh.models.Author author = authorServiceImp.addAuthor(AuthorGrpcToAuthorModel(request.getAuthor()));
            Entities.AuthorEntity response = AuthorEntityModelToAuthorGrpc(author);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAllAuthors(Author.GetAllAuthorsRequest request, StreamObserver<Entities.AuthorEntity> responseObserver) {
        try {
            int limitPages = request.getPage() == 0 ? 1 : request.getPage();

            List<io.clh.models.Author> allAuthors = authorServiceImp.getAllAuthors(limitPages);

            allAuthors.stream()
                    .map(ModelsToGrpcEntities::AuthorEntityModelToAuthorGrpc)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAuthorById(Author.AuthorByIdRequest request, StreamObserver<Author.GetAuthorByIdResponse> responseObserver) {
        try {
            long authorId = request.getAuthorId();

            io.clh.models.Author authorById = authorServiceImp.getAuthorById(authorId);
            List<Book> booksByAuthorId = bookServiceImp.findBooksByAuthorId(authorById.getAuthor_id()).stream().toList();

            Entities.AuthorEntity authorEntity = AuthorEntityModelToAuthorGrpc(authorById);
            List<Entities.Book> collect = booksByAuthorId.stream().map(ModelsToGrpcEntities::BookModelToGrpc).toList();

            Author.GetAuthorByIdResponse response = Author.GetAuthorByIdResponse.newBuilder()
                    .setAuthor(authorEntity)
                    .addAllBooks(collect).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void setAuthorAvatarUrlById(Author.AuthorAvatarUrlRequest request, StreamObserver<Entities.AuthorEntity> responseObserver) {
        try {
            long authorId = request.getAuthorId();
            String avatarUrl = request.getAvatarUrl();

            io.clh.models.Author author = authorServiceImp.setUrlAvatar(avatarUrl, authorId);
            Entities.AuthorEntity response = AuthorEntityModelToAuthorGrpc(author);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());

        }

    }
}
