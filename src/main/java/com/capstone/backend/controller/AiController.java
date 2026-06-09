package com.capstone.backend.controller;

import com.capstone.backend.dto.AiRecommendRequest;
import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/recommend")
    @Operation(summary = "맞춤형 프로젝트 추천", description = "사용자의 프롬프트를 분석하여 적합한 프로젝트를 추천합니다.")
    public ResponseEntity<AiRecommendResponse> recommendProject(
            @RequestBody AiRecommendRequest request,
            Authentication authentication) {
        String prompt = request.getUserPrompt();
        log.info("AI recommendation request from {}", authentication.getName());

        AiRecommendResponse response = aiService.getRecommendation(authentication.getName(), prompt);

        return ResponseEntity.ok(response);
    }
}
