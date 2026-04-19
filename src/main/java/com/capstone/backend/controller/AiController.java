package com.capstone.backend.controller;

import com.capstone.backend.dto.AiRecommendRequest;
import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.service.AiService; // 추가!
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor; // 추가!
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor // 추가! (의존성 주입을 위해 필요)
@RequestMapping("/api/ai")
@Tag(name = "AI Recommendation", description = "AI 기반 프로젝트 추천 API")
public class AiController {

    private final AiService aiService; // 추가!

    @PostMapping("/recommend")
    @Operation(summary = "맞춤형 프로젝트 추천", description = "사용자의 프롬프트를 분석하여 적합한 프로젝트를 추천합니다.")
    public ResponseEntity<AiRecommendResponse> recommendProject(@RequestBody AiRecommendRequest request) {
        log.info("수연님이(프론트에서) 보낸 질문: {}", request.getPrompt());

        // 🔥 더미 데이터 지우고 진짜 AI 엔진 호출!
        AiRecommendResponse realResponse = aiService.getRecommendation(request.getPrompt());

        return ResponseEntity.ok(realResponse);
    }
}