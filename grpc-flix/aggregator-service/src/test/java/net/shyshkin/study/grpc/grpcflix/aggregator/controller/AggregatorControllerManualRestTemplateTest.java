package net.shyshkin.study.grpc.grpcflix.aggregator.controller;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Start movie-service and user-service before invoking this test")
//@Disabled("Only for manual testing")
class AggregatorControllerManualRestTemplateTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void getRecommendedMovies() {
        //given
        String loginId = "d.art.shyshkin";
        ParameterizedTypeReference<List<RecommendedMovie>> typeReference = new ParameterizedTypeReference<>() {
        };

        //when
        ResponseEntity<List<RecommendedMovie>> responseEntity = restTemplate.exchange("/user/{loginId}", HttpMethod.GET, null, typeReference, loginId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
                .hasSize(2)
                .anySatisfy(movie -> assertThat(movie)
                        .hasFieldOrPropertyWithValue("title", "Terminator")
                        .hasFieldOrPropertyWithValue("year", 1984)
                        .hasFieldOrPropertyWithValue("rating", 5.7)
                )
                .anySatisfy(movie -> assertThat(movie)
                        .hasFieldOrPropertyWithValue("title", "Terminator4")
                        .hasFieldOrPropertyWithValue("year", 1999)
                        .hasFieldOrPropertyWithValue("rating", 5.73)
                );
    }

    @Test
    void updateUserGenre() {
        //given
        String loginId = "arina.climb";
        UserGenre userGenre = new UserGenre();
        userGenre.setGenre("COMEDY");

        //when
        HttpEntity<UserGenre> requestEntity = new HttpEntity<>(userGenre);
        ResponseEntity<UserDto> responseEntity = restTemplate.exchange("/user/{loginId}", HttpMethod.PUT, requestEntity, UserDto.class, loginId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
                .hasFieldOrPropertyWithValue("name", "Arina Shyshkina")
                .hasFieldOrPropertyWithValue("genre", "COMEDY")
        ;
    }
}