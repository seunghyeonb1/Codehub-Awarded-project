package io.clh.bookstore.categories;

import com.google.protobuf.Empty;
import io.clh.bookstore.author.AuthorServiceImp;
import io.clh.bookstore.book.BookServiceImpService;
import io.clh.bookstore.category.Category;
import io.clh.bookstore.category.CategoryServiceGrpc;
import io.clh.bookstore.entities.Entities;
import io.clh.bookstore.untils.GrpcEntitiesToModels;
import io.clh.bookstore.untils.ModelsToGrpcEntities;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import static io.clh.bookstore.untils.ModelsToGrpcEntities.CategoryModelToCategoryGrpc;

@RequiredArgsConstructor
public class CategoryServiceGrpcImp extends CategoryServiceGrpc.CategoryServiceImplBase {
    private final CategoryServiceImpService categoryServiceImp;
    private final AuthorServiceImp authorServiceImp;
    private final BookServiceImpService bookServiceImp;

    @Override
    public void getAllBooksByCategory(Category.GetAllBooksByCategoryRequest request, StreamObserver<Entities.Book> responseObserver) {
        try {
            categoryServiceImp.GetAllBooksByCategory(request.getCategoryId()).stream().map(ModelsToGrpcEntities::BookModelToGrpc).forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void getAllCategories(Empty request, StreamObserver<Entities.Category> responseObserver) {
        try {
            categoryServiceImp.GetAllCategories().stream().map(ModelsToGrpcEntities::CategoryModelToCategoryGrpc).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void addCategory(Entities.Category request, StreamObserver<Entities.Category> responseObserver) {
        try {
            GrpcEntitiesToModels converter = new GrpcEntitiesToModels();
            io.clh.models.Category category = converter.CategoryGrpcToCategoryModel(request, authorServiceImp, bookServiceImp);
            io.clh.models.Category createdCategory = categoryServiceImp.AddCategory(category);
            Entities.Category grpc = CategoryModelToCategoryGrpc(createdCategory);

            responseObserver.onNext(grpc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void deleteCategory(Category.DeleteCategoryRequest request, StreamObserver<Entities.Category> responseObserver) {
        try {
            io.clh.models.Category deletedCategory = categoryServiceImp.DeleteCategory(request.getCategoryId());
            Entities.Category grpc = CategoryModelToCategoryGrpc(deletedCategory);

            responseObserver.onNext(grpc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void updateCategory(Entities.Category request, StreamObserver<Entities.Category> responseObserver) {
        try {
            GrpcEntitiesToModels converter = new GrpcEntitiesToModels();
            io.clh.models.Category category = converter.CategoryGrpcToCategoryModel(request, authorServiceImp, bookServiceImp);

            io.clh.models.Category updatedCategory = categoryServiceImp.UpdateCategory(category);
            Entities.Category grpc = CategoryModelToCategoryGrpc(updatedCategory);

            responseObserver.onNext(grpc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void getCategoryById(Category.GetCategoryByIdRequest request, StreamObserver<Entities.Category> responseObserver) {
        try {
            io.clh.models.Category updatedCategory = categoryServiceImp.GetCategoryById(request.getCategoryId());
            Entities.Category grpc = CategoryModelToCategoryGrpc(updatedCategory);

            responseObserver.onNext(grpc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}
