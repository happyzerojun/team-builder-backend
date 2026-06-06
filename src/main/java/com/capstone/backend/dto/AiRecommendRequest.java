package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "AI 추천 요청 객체")
public class AiRecommendRequest {
    @Schema(description = "현재 로그인한 유저의 ID", example = "2")
    private Long userId; // 🔥 추가!

    @Schema(description = "사용자가 선택/입력한 조건 문자열", example = "React, 1~2개월, 프론트 위주")
    private String userPrompt;
}