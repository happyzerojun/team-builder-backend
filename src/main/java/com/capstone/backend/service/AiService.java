package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.service.ai.TechKeywordMatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    // 키워드 맵은 DataInitializer 이후(ApplicationReadyEvent)에 적재된다 (TS-10)
    private final TechKeywordMatcher techKeywordMatcher;
    private final ObjectMapper objectMapper;

    // 타임아웃 없는 new RestTemplate()은 Gemini가 응답하지 않으면 요청 스레드가 무기한 대기했다 (TS-03)
    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);   // ms
        factory.setReadTimeout(15_000);     // LLM 응답 생성 시간을 고려
        return new RestTemplate(factory);
    }
    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Transactional(readOnly = true)
    public AiRecommendResponse getRecommendation(String email, String userPrompt) {
        // 1. 요청자 확인 (JWT의 email). 탈퇴 등으로 없으면 401
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요."));

        // 2. 동적 기술 스택 매칭
        // 가장 긴 키워드부터 매칭한다. 이전에는 HashMap 순회 순서의 첫 매칭이라
        // "javascript" 요청에 "java"가 걸리는 등 결과가 비결정적이었다 (TS-03)
        Long targetTechStackId = techKeywordMatcher.match(userPrompt).orElse(null);

        // 3. 데이터 로딩 (🔥 Fallback 로직 강화: 빈 깡통 방지)
        List<Project> projects = new ArrayList<>();
        if (targetTechStackId != null) {
            projects = projectRepository.findByTechStackId(targetTechStackId, PageRequest.of(0, 50)).getContent();
        }

        // 검색된 기술 스택 프로젝트가 없거나, 스택 지정을 안했다면 최신 프로젝트 50개를 불러와 AI에게 맡김
        if (projects.isEmpty()) {
            // 정렬이 없으면 "최신 50건"이 보장되지 않는다 (TS-03)
            projects = projectRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        }

        if (projects.isEmpty()) {
            return AiRecommendResponse.builder().recommendations(new ArrayList<>()).build();
        }

        // 4. 프로젝트 데이터 문자열 변환 (NullPointerException 방지)
        String projectData = projects.stream().map(p -> {
            String stacks = p.getProjectTechStacks().isEmpty() ? "지정안됨" :
                    p.getProjectTechStacks().stream().map(pts -> pts.getTechStack().getName()).collect(Collectors.joining(", "));
            return String.format("ID: %d | 제목: %s | 스택: %s | 기간: %s | 내용: %s",
                    p.getId(), p.getTitle(), stacks, p.getTerm(), p.getContent());
        }).collect(Collectors.joining("\n"));

        // 5. 프롬프트 생성 (🔥 사용자의 목적/상황 분석 지시 강화)
        String finalPrompt = String.format("""
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
            5. 절대로 인사말이나 다른 텍스트를 출력하지 말고, 오직 아래 예시와 같은 JSON 배열(`[ ]`) 형식으로만 반환하세요.
            
            [응답 형식 예시]
            [{"project_id": 1, "matching_score": 95, "reason": "Node.js 기술을 사용하며, 대기업에서 선호하는 트래픽 처리 경험을 쌓을 수 있는 프로젝트입니다."}]
            """, userPrompt, projectData);

        return processAiResponse(finalPrompt);
    }

    private AiRecommendResponse processAiResponse(String finalPrompt) {
        try {
            String aiJsonResponse = callGeminiApi(finalPrompt);
            if (aiJsonResponse == null || aiJsonResponse.isEmpty()) {
                return AiRecommendResponse.builder().recommendations(new ArrayList<>()).build();
            }

            // 🔥 [핵심 수정] AI가 헛소리를 섞어 보내도 JSON 배열 [ ... ] 부분만 완벽하게 추출
            int startIndex = aiJsonResponse.indexOf('[');
            int endIndex = aiJsonResponse.lastIndexOf(']');
            if (startIndex != -1 && endIndex != -1) {
                aiJsonResponse = aiJsonResponse.substring(startIndex, endIndex + 1);
            } else {
                return AiRecommendResponse.builder().recommendations(new ArrayList<>()).build(); // JSON이 아예 없으면 빈 응답
            }

            List<AiResultDto> aiResults = objectMapper.readValue(aiJsonResponse, new TypeReference<List<AiResultDto>>() {});
            List<Long> recommendedIds = aiResults.stream().map(AiResultDto::getProject_id).filter(Objects::nonNull).collect(Collectors.toList());
            List<Project> fullProjects = projectRepository.findAllById(recommendedIds);
            Map<Long, Project> projectMap = fullProjects.stream().collect(Collectors.toMap(Project::getId, p -> p));

            List<RecommendationDto> recommendations = aiResults.stream()
                    .map(ai -> {
                        Project realProject = projectMap.get(ai.getProject_id());
                        return (realProject == null) ? null : RecommendationDto.builder()
                                .project_id(realProject.getId())
                                .title(realProject.getTitle())
                                .matching_score(ai.getMatching_score())
                                .reason(ai.getReason())
                                .build();
                    })
                    .filter(Objects::nonNull).collect(Collectors.toList());

            return AiRecommendResponse.builder().recommendations(recommendations).build();
        } catch (Exception e) {
            log.error("AI 추천 에러 또는 JSON 파싱 에러: ", e);
            return AiRecommendResponse.builder().recommendations(new ArrayList<>()).build();
        }
    }

    private String callGeminiApi(String prompt) {
        // API 키를 URL 쿼리스트링에 두면 접근 로그·프록시 로그에 남을 수 있어 헤더로 보낸다 (TS-03)
        String url = apiUrl;
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json") // Gemini가 JSON을 내뱉도록 유도
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); set("x-goog-api-key", apiKey); }});
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        try {
            return objectMapper.readTree(response.getBody()).path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (Exception e) {
            return "[]";
        }
    }

    @lombok.Getter @lombok.Setter
    public static class AiResultDto {
        private Long project_id;
        private int matching_score;
        private String reason;
    }
}