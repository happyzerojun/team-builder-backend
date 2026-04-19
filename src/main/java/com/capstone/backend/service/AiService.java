package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.repository.ProjectRepository; // 프로젝트 레포지토리 추가
import com.capstone.backend.entity.Project; // 프로젝트 엔티티 추가
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository; // 🔥 DB 조회를 위한 레포지토리 주입

    public AiRecommendResponse getRecommendation(String userPrompt) {
        // 1. DB에서 현재 등록된 모든 프로젝트 모집글 가져오기
        List<Project> projects = projectRepository.findAll();

        // 2. AI에게 전달할 '데이터 리스트' 문자열 생성 (지역 정보 추가!)
        String projectData = projects.stream()
                .map(p -> String.format("- 제목: %s, 설명: %s, 지역: %s, 모집기간: %s",
                        p.getTitle(), p.getContent(), p.getRegion(), p.getTerm()))
                .collect(Collectors.joining("\n"));

        // 3. DB 상황에 따른 동적 프롬프트 설계
        String systemInstruction = projects.isEmpty()
                ? "현재 DB에 등록된 프로젝트가 없으니, 사용자의 질문에 맞는 새로운 프로젝트 아이디어를 하나 기획해서 추천해줘."
                : "아래 제공된 [현재 등록된 프로젝트 목록] 중에서 사용자의 질문과 가장 잘 어울리는 프로젝트를 하나 골라서 추천해줘. 만약 딱 맞는 게 없다면 가장 유사한 것을 추천해줘.";

        String finalPrompt = String.format("""
                너는 '팀 빌더' 플랫폼의 전문 매칭 AI야.
                
                [현재 등록된 프로젝트 목록]
                %s
                
                [지시 사항]
                1. %s
                2. 반드시 아래 JSON 형식으로만 답변할 것 (부가 설명 금지).
                
                [출력 형식]
                {
                    "title": "선택한 프로젝트 제목",
                    "description": "추천 이유를 포함한 프로젝트 설명",
                    "techStack": "해당 프로젝트의 기술 스택",
                    "experienceLevel": "초보, 중급, 고수 중 적합한 난이도"
                }
                
                사용자 질문: %s
                """, projectData, systemInstruction, userPrompt);

        // --- 이후 로직(RestTemplate 요청 및 JSON 파싱)은 아까와 동일 ---
        return callGeminiApi(finalPrompt);
    }

    // API 호출 로직 분리 (가독성을 위해)
    private AiRecommendResponse callGeminiApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = apiUrl + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, requestBody, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiAnswer = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // AI 답변이 JSON 마크다운(```json ... ```)을 포함할 경우를 대비한 정제
            String cleanJson = aiAnswer.replaceAll("```json|```", "").trim();
            return objectMapper.readValue(cleanJson, AiRecommendResponse.class);

        } catch (Exception e) {
            log.error("AI 추천 중 오류 발생: ", e);
            return AiRecommendResponse.builder().title("추천 엔진 일시 정지").build();
        }
    }
}