package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, Long> dynamicTechLookupMap = new ConcurrentHashMap<>();

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initTechLookupMap() {
        List<TechStack> allStacks = techStackRepository.findAll();
        for (TechStack stack : allStacks) {
            Long id = stack.getId();
            if (stack.getName() != null) dynamicTechLookupMap.put(stack.getName().toLowerCase().replace(" ", ""), id);
            if (stack.getKeywords() != null) {
                for (String kw : stack.getKeywords().split(",")) {
                    dynamicTechLookupMap.put(kw.trim().toLowerCase().replace(" ", ""), id);
                }
            }
        }
        log.info("🔥 [AiService] 키워드 맵 로드 완료. 총 {}개", dynamicTechLookupMap.size());
    }

    @Transactional(readOnly = true)
    public AiRecommendResponse getRecommendation(String email, String userPrompt) {
        // 1. 유저 정보 조회
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2. 동적 기술 스택 매칭
        Long targetTechStackId = dynamicTechLookupMap.entrySet().stream()
                .filter(entry -> userPrompt.toLowerCase().contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);

        // 3. 데이터 로딩 (🔥 Fallback 로직 강화: 빈 깡통 방지)
        List<Project> projects = new ArrayList<>();
        if (targetTechStackId != null) {
            projects = projectRepository.findByTechStackId(targetTechStackId, PageRequest.of(0, 50)).getContent();
        }

        // 검색된 기술 스택 프로젝트가 없거나, 스택 지정을 안했다면 최신 프로젝트 50개를 불러와 AI에게 맡김
        if (projects.isEmpty()) {
            projects = projectRepository.findAll(PageRequest.of(0, 50)).getContent();
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
        String url = apiUrl + "?key=" + apiKey;
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json") // Gemini가 JSON을 내뱉도록 유도
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
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
