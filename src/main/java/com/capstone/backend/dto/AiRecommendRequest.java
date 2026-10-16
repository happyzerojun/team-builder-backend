package com.capstone.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "AI 추천 요청 객체 (요청자는 JWT로 식별하며 본문에 userId를 받지 않는다)")
public class AiRecommendRequest {

    @NotBlank(message = "추천 조건을 입력해주세요.")
    @Size(max = 1000, message = "추천 조건은 1000자 이하로 입력해주세요.")
    @Schema(description = "사용자가 선택/입력한 조건 문자열", example = "React, 1~2개월, 프론트 위주")
    private String userPrompt;
}
