package com.capstone.backend.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini generateContent 호출만 담당하는 HTTP 클라이언트.
 * DB 트랜잭션과 섞이지 않도록 서비스에서 분리했고, 테스트에서는 이 빈만 목으로 바꾼다.
 */
@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;

    public GeminiClient(ObjectMapper objectMapper,
                        @Value("${gemini.api.url}") String apiUrl,
                        @Value("${gemini.api.key}") String apiKey) {
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.restTemplate = createRestTemplate();
    }

    // 타임아웃 없는 new RestTemplate()은 Gemini가 응답하지 않으면 요청 스레드가 무기한 대기했다 (TS-03)
    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);   // ms
        factory.setReadTimeout(15_000);     // LLM 응답 생성 시간을 고려
        return new RestTemplate(factory);
    }

    /** 모델이 생성한 텍스트. 응답 구조가 예상과 다르면 빈 문자열 */
    public String generate(String prompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json") // JSON 응답 유도
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // API 키를 URL 쿼리스트링에 두면 접근 로그·프록시 로그에 남을 수 있어 헤더로 보낸다 (TS-03)
        headers.set("x-goog-api-key", apiKey);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, new HttpEntity<>(requestBody, headers), String.class);
        if (response.getBody() == null) {
            return "";
        }
        return objectMapper.readTree(response.getBody())
                .path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
    }
}
