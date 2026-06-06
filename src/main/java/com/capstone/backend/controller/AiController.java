package com.capstone.backend.controller;

import com.capstone.backend.dto.AiRecommendRequest;
import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // 프론트엔드 통신 허용
@Tag(name = "AI Recommendation", description = "AI 기반 프로젝트 추천 API")
public class AiController {

    private final AiService aiService;

    @PostMapping("/recommend")
    @Operation(summary = "맞춤형 프로젝트 추천", description = "사용자의 프롬프트를 분석하여 적합한 프로젝트를 추천합니다.")
    public ResponseEntity<AiRecommendResponse> recommendProject(
            @RequestBody AiRecommendRequest request) {

        // 🔥 DTO에서 프론트엔드가 보낸 userId를 바로 꺼내서 사용합니다.
        Long userId = request.getUserId();

        // 프론트엔드에서 실수로 안 보냈을 경우를 대비한 안전장치 (나중에 에러 처리로 바꿔도 됩니다)
        if (userId == null) {
            userId = 2L;
        }

        String prompt = request.getUserPrompt();
        log.info("사용자 ID: {} 의 요청: {}", userId, prompt);

        AiRecommendResponse response = aiService.getRecommendation(userId, prompt);

        return ResponseEntity.ok(response);
    }
}