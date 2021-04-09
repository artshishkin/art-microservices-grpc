package net.shyshkin.study.grpc.grpcflix.movie.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.shyshkin.study.grpc.grpcflix.common.Genre;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchRequest;
import net.shyshkin.study.grpc.grpcflix.movie.MovieSearchResponse;
import net.shyshkin.study.grpc.grpcflix.movie.MovieServiceGrpc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class MovieServiceTest {

    private static MovieServiceGrpc.MovieServiceBlockingStub blockingStub;
    private static ManagedChannel managedChannel;

    @BeforeAll
    static void beforeAll() {
        managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 7575)
                .usePlaintext()
                .build();

        blockingStub = MovieServiceGrpc.newBlockingStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        managedChannel.shutdown();
    }

    @Test
    void getMovies_present() {
        //given
        Genre genre = Genre.ACTION;
        MovieSearchRequest request = MovieSearchRequest.newBuilder()
                .setGenre(genre)
                .build();

        //when
        MovieSearchResponse response = blockingStub.getMovies(request);

        //then
        assertThat(response.getMovieList())
                .hasSize(2)
                .allSatisfy(movie -> assertThat(movie.getTitle()).containsIgnoringCase("terminator"))
                .anySatisfy(movie -> assertAll(
                        () -> assertThat(movie.getTitle()).isEqualTo("Terminator4"),
                        () -> assertThat(movie.getRating()).isEqualTo(5.73),
                        () -> assertThat(movie.getYear()).isEqualTo(1999)
                ));
    }

    @Test
    void getMovies_absent() {
        //given
        Genre genre = Genre.CRIME;
        MovieSearchRequest request = MovieSearchRequest.newBuilder()
                .setGenre(genre)
                .build();

        //when
        MovieSearchResponse response = blockingStub.getMovies(request);

        //then
        assertThat(response.getMovieList())
                .isEmpty();
    }
}