package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.service.ai.AiCandidateReader;
import com.capstone.backend.service.ai.AiResult;
import com.capstone.backend.service.ai.GeminiClient;
import com.capstone.backend.service.ai.ProjectCandidate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.SocketTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-12: AI 추천 흐름 = 후보 조회(트랜잭션) → Gemini(트랜잭션 밖) → 결과 재조회(트랜잭션) */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI 추천 흐름")
class AiServiceTest {

    @Mock
    private AiCandidateReader candidateReader;

    @Mock
    private GeminiClient geminiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiService service() {
        return new AiService(candidateReader, geminiClient, objectMapper);
    }

    private static ProjectCandidate candidate(long id) {
        return new ProjectCandidate(id, "글" + id, "Java", "3", "내용");
    }

    @Test
    @DisplayName("후보가 없으면 Gemini를 호출하지 않는다")
    void noCandidatesSkipsGemini() throws Exception {
        when(candidateReader.loadCandidates("me@example.com", "React")).thenReturn(List.of());

        AiRecommendResponse response = service().getRecommendation("me@example.com", "React");

        assertThat(response.getRecommendations()).isEmpty();
        verify(geminiClient, never()).generate(anyString());
    }

    @Test
    @DisplayName("Gemini 타임아웃·오류는 500 대신 빈 추천")
    void geminiFailureReturnsEmpty() throws Exception {
        when(candidateReader.loadCandidates(anyString(), anyString())).thenReturn(List.of(candidate(1L)));
        when(geminiClient.generate(anyString())).thenThrow(new SocketTimeoutException("Read timed out"));

        AiRecommendResponse response = service().getRecommendation("me@example.com", "React");

        assertThat(response.getRecommendations()).isEmpty();
        verify(candidateReader, never()).resolve(any());
    }

    @Test
    @DisplayName("Gemini 응답은 재조회 단계로 넘겨 DB 값으로 확정한다")
    void passesAiResultsToResolve() throws Exception {
        when(candidateReader.loadCandidates(anyString(), anyString())).thenReturn(List.of(candidate(1L)));
        when(geminiClient.generate(anyString()))
                .thenReturn("[{\"project_id\": 1, \"matching_score\": 88, \"reason\": \"좋아요\"}]");
        List<RecommendationDto> resolved = List.of(RecommendationDto.builder().project_id(1L).title("글1").matching_score(88).build());
        when(candidateReader.resolve(any())).thenReturn(resolved);

        AiRecommendResponse response = service().getRecommendation("me@example.com", "Java");

        assertThat(response.getRecommendations()).isEqualTo(resolved);
    }

    @Test
    @DisplayName("응답에 설명 문장이 섞여도 JSON 배열만 파싱한다")
    void parseIgnoresSurroundingText() throws Exception {
        List<AiResult> results = AiService.parse(objectMapper,
                "추천 결과입니다:\n[{\"project_id\": 7, \"matching_score\": 70, \"reason\": \"r\"}]\n감사합니다");

        assertThat(results).containsExactly(new AiResult(7L, 70, "r"));
        assertThat(AiService.parse(objectMapper, "죄송합니다")).isEmpty();
        assertThat(AiService.parse(objectMapper, null)).isEmpty();
    }
}
