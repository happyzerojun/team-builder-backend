package com.capstone.backend.integration;

import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.support.IntegrationTestSupport;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("TS-12: Gemini를 기다리는 동안 트랜잭션도, OSIV EntityManager도 열려 있지 않다")
    void geminiIsCalledOutsideTransaction() throws Exception {
        User me = newUser("ai-tx");
        Project project = newProject(me, "AI 트랜잭션 테스트 글");

        AtomicBoolean transactionActiveDuringGemini = new AtomicBoolean(true);
        AtomicBoolean entityManagerBoundDuringGemini = new AtomicBoolean(true);
        when(geminiClient.generate(anyString())).thenAnswer(invocation -> {
            transactionActiveDuringGemini.set(TransactionSynchronizationManager.isActualTransactionActive());
            // OSIV가 켜져 있으면 요청 내내 EntityManager(와 그 커넥션)가 스레드에 바인딩돼 있다
            entityManagerBoundDuringGemini.set(TransactionSynchronizationManager.hasResource(entityManagerFactory));
            // 두 번째 항목은 후보에 없는(존재하지 않는) ID: 응답에서 빠져야 한다 (TS-11)
            return "[{\"project_id\": " + project.getId() + ", \"matching_score\": 90, \"reason\": \"테스트\"},"
                    + " {\"project_id\": 987654321, \"matching_score\": 99, \"reason\": \"환각\"}]";
        });

        mockMvc.perform(post("/api/ai/recommend")
                        .header(HttpHeaders.AUTHORIZATION, bearer(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userPrompt\": \"아무 조건 없이 추천해줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].project_id").value(project.getId()))
                .andExpect(jsonPath("$.recommendations[0].title").value("AI 트랜잭션 테스트 글"))
                .andExpect(jsonPath("$.recommendations.length()").value(1));

        assertThat(transactionActiveDuringGemini.get()).isFalse();
        assertThat(entityManagerBoundDuringGemini.get())
                .as("OSIV off: Gemini 대기 중에는 EntityManager·DB 커넥션을 쥐고 있지 않다")
                .isFalse();
    }
}
