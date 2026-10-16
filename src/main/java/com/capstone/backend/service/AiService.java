package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.service.ai.AiCandidateReader;
import com.capstone.backend.service.ai.AiResult;
import com.capstone.backend.service.ai.GeminiClient;
import com.capstone.backend.service.ai.ProjectCandidate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 프로젝트 추천 흐름 조립.
 *
 * 이 클래스에는 의도적으로 @Transactional이 없다. 이전에는 메서드 전체가 @Transactional(readOnly = true)라
 * Gemini 응답(최대 15초)을 기다리는 동안 DB 커넥션을 쥐고 있었고, 동시 요청이 몰리면 커넥션 풀(기본 10개)이
 * 고갈돼 AI와 무관한 API까지 대기했다. 이제 DB 구간은 AiCandidateReader의 짧은 트랜잭션 두 개뿐이다 (TS-12)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiCandidateReader candidateReader;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public AiRecommendResponse getRecommendation(String email, String userPrompt) {
        // 1. 후보 조회 (트랜잭션 1, 바로 커밋되고 커넥션 반환)
        List<ProjectCandidate> candidates = candidateReader.loadCandidates(email, userPrompt);
        if (candidates.isEmpty()) {
            return empty();
        }
        Set<Long> candidateIds = candidates.stream().map(ProjectCandidate::id).collect(Collectors.toUnmodifiableSet());

        // 2. Gemini 호출 (트랜잭션 없음)
        List<AiResult> aiResults = askGemini(buildPrompt(userPrompt, candidates));
        if (aiResults.isEmpty()) {
            return empty();
        }

        // 3. 후보 집합으로 거르고 DB 재조회 (트랜잭션 2)
        List<RecommendationDto> recommendations = candidateReader.resolve(aiResults, candidateIds);
        return AiRecommendResponse.builder().recommendations(recommendations).build();
    }

    private List<AiResult> askGemini(String prompt) {
        try {
            return parse(objectMapper, geminiClient.generate(prompt));
        } catch (Exception e) {
            // API 오류·타임아웃·파싱 실패는 500 대신 빈 추천으로 (화면이 깨지지 않게)
            log.error("AI 추천 에러 또는 JSON 파싱 에러", e);
            return List.of();
        }
    }

    /** 응답에 설명 문장이 섞여도 첫 '['부터 마지막 ']'까지만 파싱한다 */
    static List<AiResult> parse(ObjectMapper objectMapper, String text) throws Exception {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start == -1 || end <= start) {
            return List.of();
        }
        List<AiResult> results = objectMapper.readValue(text.substring(start, end + 1), new TypeReference<List<AiResult>>() {});
        return results == null ? List.of() : results;
    }

    static String buildPrompt(String userPrompt, List<ProjectCandidate> candidates) {
        String projectData = candidates.stream().map(ProjectCandidate::toPromptLine).collect(Collectors.joining("\n"));
        return String.format("""
            당신은 최고의 개발자 커리어 매칭 AI입니다. [사용자 요청]에 맞춰 [프로젝트 목록] 중 최적의 프로젝트를 최대 3개 추천하세요.
            
            [사용자 요청]
            %s
            
            [프로젝트 목록]
            %s
            
            [미션]
            1. 사용자가 언급한 기술 스택(예: Node.js)이나 요구사항(예: 쇼핑몰, 대기업 지원용)에 가장 부합하는 프로젝트를 분석하세요.
            2. 완벽히 일치하는 스택이 없더라도, 사용자의 '목적(예: 대기업 취업)'이나 '상황'에 가장 도움이 될 만한 프로젝트를 찾아내세요.
            3. 매칭 점수(matching_score)는 일치도에 따라 0~100 사이로 평가하세요.
            4. reason(추천 이유)에는 "왜 이 프로젝트가 사용자의 목적(쇼핑몰, 대기업 등)에 도움이 되는지" 구체적으로 적어주세요.
            5. project_id는 반드시 [프로젝트 목록]에 있는 ID 중에서만 고르세요.
            6. 절대로 인사말이나 다른 텍스트를 출력하지 말고, 오직 아래 예시와 같은 JSON 배열(`[ ]`) 형식으로만 반환하세요.
            
            [응답 형식 예시]
            [{"project_id": 1, "matching_score": 95, "reason": "Node.js 기술을 사용하며, 대기업에서 선호하는 트래픽 처리 경험을 쌓을 수 있는 프로젝트입니다."}]
            """, userPrompt, projectData);
    }

    private static AiRecommendResponse empty() {
        return AiRecommendResponse.builder().recommendations(List.of()).build();
    }
}
