package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {

    @Schema(description = "프로젝트 ID", example = "3")
    private Long project_id;

    @Schema(description = "프로젝트 제목")
    private String title;

    @Schema(description = "매칭 점수")
    private int matching_score;

    @Schema(description = "추천 이유")
    private String reason;
}