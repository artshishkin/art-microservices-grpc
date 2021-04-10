package net.shyshkin.study.grpc.grpcflix.aggregator.controller;

import net.shyshkin.study.grpc.grpcflix.aggregator.dto.RecommendedMovie;
import net.shyshkin.study.grpc.grpcflix.aggregator.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("AggregatorControllerManualMockMvcTest - Start movie-service and user-service before invoking this test")
@Disabled("Only for manual testing")
class AggregatorControllerManualMockMvcTest {

    @Autowired
    AggregatorController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getRecommendedMovies() throws Exception {
        //given
        String loginId = "d.art.shyshkin";

        //when
        mockMvc.perform(get("/user/{loginId}", loginId))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))

                .andExpect(jsonPath("$.[0].title", equalTo("Terminator")))
                .andExpect(jsonPath("$.[0].year", equalTo(1984)))
                .andExpect(jsonPath("$.[0].rating", equalTo(5.7)))

                .andExpect(jsonPath("$.[1].title", equalTo("Terminator4")))
                .andExpect(jsonPath("$.[1].year", equalTo(1999)))
                .andExpect(jsonPath("$.[1].rating", equalTo(5.73)))
        ;
    }

    @Test
    void getRecommendedMovies_usingRecommendedMovie() throws Exception {
        //given
        String loginId = "d.art.shyshkin";
        List<RecommendedMovie> movies = List.of(
                RecommendedMovie.builder()
                        .title("Terminator")
                        .year(1984)
                        .rating(5.7)
                        .build(),
                RecommendedMovie.builder()
                        .title("Terminator4")
                        .year(1999)
                        .rating(5.73)
                        .build());

        //when
        mockMvc.perform(get("/user/{loginId}", loginId))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))

                .andExpect(jsonPath("$.[0]", equalTo(movies.get(0)), RecommendedMovie.class))
                .andExpect(jsonPath("$.[1]", equalTo(movies.get(1)), RecommendedMovie.class))
        ;
    }

    @Test
    void updateUserGenre() throws Exception {
        //given
        String loginId = "arina.climb";
        String genreJson = "{\"genre\":\"COMEDY\"}";

        UserDto userDto = new UserDto();
        userDto.setGenre("COMEDY");
        userDto.setName("Arina Shyshkina");

        //when
        mockMvc.perform(put("/user/{loginId}", loginId)
                .contentType(APPLICATION_JSON)
                .content(genreJson))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", equalTo(userDto), UserDto.class));
        ;
    }
}