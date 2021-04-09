package net.shyshkin.study.grpc.grpcflix.movie.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movie {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    private Long id;
    private String title;
    private Integer year;
    private Double rating;
    private String genre;
}
