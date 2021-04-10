package net.shyshkin.study.grpc.grpcflix.aggregator.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import net.shyshkin.study.grpc.grpcflix.aggregator.mapper.MovieMapper;
import net.shyshkin.study.grpc.grpcflix.aggregator.mapper.UserMapper;
import net.shyshkin.study.grpc.grpcflix.common.Genre;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchRequest;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchResponse;
import net.shyshkin.study.grpc.grpcflix.movie.MovieServiceGrpc;
import net.shyshkin.study.grpc.grpcflix.user.UserGenreUpdateRequest;
import net.shyshkin.study.grpc.grpcflix.user.UserResponse;
import net.shyshkin.study.grpc.grpcflix.user.UserSearchRequest;
import net.shyshkin.study.grpc.grpcflix.user.UserServiceGrpc;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrpcUserMovieService implements UserMovieService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    @GrpcClient("movie-service")
    private MovieServiceGrpc.MovieServiceBlockingStub movieServiceBlockingStub;

    private final MovieMapper movieMapper;
    private final UserMapper userMapper;

    @Override
    public List<RecommendedMovie> getRecommendedMovies(String userId) {

        UserSearchRequest userSearchRequest = UserSearchRequest.newBuilder()
                .setLoginId(userId)
                .build();
        UserResponse userGenre = userServiceBlockingStub.getUserGenre(userSearchRequest);

        MovieSearchRequest movieRequest = MovieSearchRequest.newBuilder()
                .setGenre(userGenre.getGenre())
                .build();

        MovieSearchResponse movieSearchResponse = movieServiceBlockingStub.getMovies(movieRequest);

        return movieSearchResponse.getMovieList()
                .stream()
                .map(movieMapper::toRecommendedMovie)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUserGenre(String userId, String genre) {

        UserGenreUpdateRequest request = UserGenreUpdateRequest.newBuilder()
                .setLoginId(userId)
                .setGenre(Genre.valueOf(genre.toUpperCase()))
                .build();

        UserResponse userResponse = userServiceBlockingStub.updateUserGenre(request);

        return userMapper.toUserDto(userResponse);
    }
}
