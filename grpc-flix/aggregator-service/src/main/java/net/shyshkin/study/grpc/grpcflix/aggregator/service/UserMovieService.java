package net.shyshkin.study.grpc.grpcflix.aggregator.service;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMovieService {

    public List<RecommendedMovie> getRecommendedMovies(String userId) {
        throw new RuntimeException("NOT IMPLEMENTED YET");
    }

    public UserDto updateUserGenre(String userId, String genre) {
        throw new RuntimeException("NOT IMPLEMENTED YET");
    }
}
