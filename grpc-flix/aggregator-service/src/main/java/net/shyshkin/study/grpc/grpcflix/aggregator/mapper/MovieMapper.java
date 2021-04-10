package net.shyshkin.study.grpc.grpcflix.aggregator.mapper;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.movie.MovieDto;
import org.mapstruct.Mapper;

@Mapper
public interface MovieMapper {

    RecommendedMovie toRecommendedMovie(MovieDto movieDto);

}
