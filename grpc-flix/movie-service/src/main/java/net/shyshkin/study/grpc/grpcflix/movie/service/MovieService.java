package net.shyshkin.study.grpc.grpcflix.movie.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import net.shyshkin.study.grpc.grpcflix.common.Genre;
import net.shyshkin.study.grpc.grpcflix.movie.MovieDto;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchRequest;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchResponse;
import net.shyshkin.study.grpc.grpcflix.movie.MovieServiceGrpc;
import net.shyshkin.study.grpc.grpcflix.movie.entity.Movie;
import net.shyshkin.study.grpc.grpcflix.movie.repository.MovieRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class MovieService extends MovieServiceGrpc.MovieServiceImplBase {

    private final MovieRepository movieRepository;

    @Override
    public void getMovies(MovieSearchRequest request, StreamObserver<MovieSearchResponse> responseObserver) {

        Genre genre = request.getGenre();

        Set<Movie> movies = movieRepository.findAllByGenre(genre.toString().toUpperCase());

        List<MovieDto> movieDtoList = movies.stream()
                .map(movie -> MovieDto.newBuilder()
                        .setRating(movie.getRating())
                        .setTitle(movie.getTitle())
                        .setYear(movie.getYear())
                        .build())
                .collect(Collectors.toList());

        MovieSearchResponse response = MovieSearchResponse.newBuilder()
                .addAllMovie(movieDtoList)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
