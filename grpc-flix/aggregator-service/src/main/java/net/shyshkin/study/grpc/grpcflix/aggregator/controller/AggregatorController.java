package net.shyshkin.study.grpc.grpcflix.aggregator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserGenre;
import net.shyshkin.study.grpc.grpcflix.aggregator.service.UserMovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AggregatorController {

    private final UserMovieService userMovieService;

    @GetMapping("user/{loginId:.+}")
    public List<RecommendedMovie> getRecommendedMovies(@PathVariable String loginId) {
        log.debug("loginId: `{}`",loginId);
        return userMovieService.getRecommendedMovies(loginId);
    }

    @PutMapping("user/{loginId:.+}")
    public UserDto updateUserGenre(@PathVariable String loginId, @RequestBody UserGenre userGenre) {
        return userMovieService.updateUserGenre(loginId, userGenre.getGenre());
    }


}
