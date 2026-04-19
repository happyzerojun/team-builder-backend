package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "AI 프로젝트 추천 응답 객체")
public class AiRecommendResponse {
    @Schema(description = "추천 프로젝트 제목", example = "Spring Boot API 마스터 클래스")
    private String title;

    @Schema(description = "프로젝트 상세 설명", example = "REST API 설계부터 배포까지 학습하는 프로젝트입니다.")
    private String description;

    @Schema(description = "권장 기술 스택", example = "Spring Boot, JPA, MySQL")
    private String techStack;

    @Schema(description = "추천 난이도", example = "초보")
    private String experienceLevel;
}