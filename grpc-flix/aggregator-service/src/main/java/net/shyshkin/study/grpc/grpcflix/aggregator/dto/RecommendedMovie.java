package net.shyshkin.study.grpc.grpcflix.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedMovie {

    private String title;
    private Integer year;
    private Double rating;

}
