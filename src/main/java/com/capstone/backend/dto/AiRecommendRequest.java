package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 프로젝트 추천 요청 객체")
public class AiRecommendRequest {
    @Schema(description = "사용자의 질문 또는 희망하는 프로젝트 성격", example = "백엔드 공부하기 좋은 Spring Boot 프로젝트 추천해줘")
    private String prompt;
}