package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 프로젝트 추천 응답 객체")
public class AiRecommendResponse {

    @Schema(description = "추천 프로젝트 리스트")
    private List<RecommendationDto> recommendations;
}