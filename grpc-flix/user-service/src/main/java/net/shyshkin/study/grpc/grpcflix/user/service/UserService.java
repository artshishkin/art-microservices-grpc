package net.shyshkin.study.grpc.grpcflix.user.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import net.shyshkin.study.grpc.grpcflix.common.Genre;
import net.shyshkin.study.grpc.grpcflix.user.UserGenreUpdateRequest;
import net.shyshkin.study.grpc.grpcflix.user.UserResponse;
import net.shyshkin.study.grpc.grpcflix.user.UserSearchRequest;
import net.shyshkin.study.grpc.grpcflix.user.UserServiceGrpc;
import net.shyshkin.study.grpc.grpcflix.user.entity.User;
import net.shyshkin.study.grpc.grpcflix.user.repository.UserRepository;

import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void getUserGenre(UserSearchRequest request, StreamObserver<UserResponse> responseObserver) {

        String loginId = request.getLoginId();
        Optional<User> userOptional = userRepository.findById(loginId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Genre genre = Genre.valueOf(user.getGenre().toUpperCase());
            UserResponse userResponse = UserResponse.newBuilder()
                    .setName(user.getName())
                    .setLoginId(user.getLogin())
                    .setGenre(genre)
                    .build();
            responseObserver.onNext(userResponse);
            responseObserver.onCompleted();
        } else {
            Status status = Status.FAILED_PRECONDITION.withDescription(String.format("User with id `%s` not found", loginId));
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void updateUserGenre(UserGenreUpdateRequest request, StreamObserver<UserResponse> responseObserver) {

        String loginId = request.getLoginId();
        Optional<User> userOptional = userRepository.findById(loginId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Genre genre = request.getGenre();
            user.setGenre(genre.toString());
            userRepository.save(user);
            UserResponse userResponse = UserResponse.newBuilder()
                    .setName(user.getName())
                    .setLoginId(user.getLogin())
                    .setGenre(genre)
                    .build();
            responseObserver.onNext(userResponse);
            responseObserver.onCompleted();
        } else {
            Status status = Status.FAILED_PRECONDITION.withDescription(String.format("User with id `%s` not found", loginId));
            responseObserver.onError(status.asRuntimeException());
        }
    }
}
