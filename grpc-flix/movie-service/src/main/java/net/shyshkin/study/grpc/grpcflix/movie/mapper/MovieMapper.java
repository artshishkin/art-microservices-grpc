package net.shyshkin.study.grpc.grpcflix.movie.mapper;

import net.shyshkin.study.grpc.grpcflix.movie.MovieDto;
import net.shyshkin.study.grpc.grpcflix.movie.entity.Movie;
import org.mapstruct.Mapper;

@Mapper
public interface MovieMapper {

    MovieDto toDto(Movie movie);

}
