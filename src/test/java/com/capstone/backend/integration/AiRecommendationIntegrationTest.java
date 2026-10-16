package com.capstone.backend.integration;

import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AI 추천 통합 (MySQL)")
class AiRecommendationIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("TS-12: Gemini를 기다리는 동안 트랜잭션이 열려 있지 않다")
    void geminiIsCalledOutsideTransaction() throws Exception {
        User me = newUser("ai-tx");
        Project project = newProject(me, "AI 트랜잭션 테스트 글");

        AtomicBoolean transactionActiveDuringGemini = new AtomicBoolean(true);
        when(geminiClient.generate(anyString())).thenAnswer(invocation -> {
            transactionActiveDuringGemini.set(TransactionSynchronizationManager.isActualTransactionActive());
            return "[{\"project_id\": " + project.getId() + ", \"matching_score\": 90, \"reason\": \"테스트\"}]";
        });

        mockMvc.perform(post("/api/ai/recommend")
                        .header(HttpHeaders.AUTHORIZATION, bearer(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userPrompt\": \"아무 조건 없이 추천해줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].project_id").value(project.getId()))
                .andExpect(jsonPath("$.recommendations[0].title").value("AI 트랜잭션 테스트 글"));

        assertThat(transactionActiveDuringGemini.get()).isFalse();
    }
}
