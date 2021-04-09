package net.shyshkin.study.grpc.grpcflix.movie.repository;

import net.shyshkin.study.grpc.grpcflix.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Set<Movie> findAllByGenre(String genre);
}
