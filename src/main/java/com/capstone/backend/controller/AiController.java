package com.capstone.backend.controller;

import com.capstone.backend.dto.AiRecommendRequest;
import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.security.AuthenticatedUser;
import com.capstone.backend.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Tag(name = "AI Recommendation", description = "AI 기반 프로젝트 추천 API")
public class AiController {

    private final AiService aiService;

    /**
     * 이전에는 본문의 userId를 쓰고, 없으면 2L(특정 사용자)로 대체했다.
     * 비로그인 요청이 남의 계정으로 처리되고 Gemini 비용도 그 계정 이름으로 쓰였다.
     * 이제 JWT의 사용자만 사용하고, 비로그인은 SecurityConfig에서 401 (TS-05)
     */
    @PostMapping("/recommend")
    @Operation(summary = "맞춤형 프로젝트 추천", description = "사용자의 프롬프트를 분석하여 적합한 프로젝트를 추천합니다. 로그인 필요.")
    public ResponseEntity<AiRecommendResponse> recommendProject(
            @Valid @RequestBody AiRecommendRequest request,
            Authentication authentication) {

        String email = AuthenticatedUser.email(authentication);
        // 프롬프트 원문은 개인정보가 섞일 수 있어 길이만 남긴다
        log.info("AI 추천 요청 user={} promptLength={}", email, request.getUserPrompt().length());

        return ResponseEntity.ok(aiService.getRecommendation(email, request.getUserPrompt()));
    }
}
