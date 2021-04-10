package net.shyshkin.study.grpc.grpcflix.aggregator.service;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;

import java.util.List;

public interface UserMovieService {
    List<RecommendedMovie> getRecommendedMovies(String userId);

    UserDto updateUserGenre(String userId, String genre);
}
