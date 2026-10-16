package com.capstone.backend.integration;

import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.service.ai.TechKeywordMatcher;
import com.capstone.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-20: 프로필 태그로 새로 생긴 기술 스택이 재시작 없이 AI 키워드 맵에 들어가는지 (MySQL) */
@DisplayName("기술 스택 키워드 맵 실행 중 갱신 (MySQL)")
class TechKeywordRefreshIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TechKeywordMatcher techKeywordMatcher;

    @Autowired
    private TechStackRepository techStackRepository;

    private void updateTags(User user, String tagsJson) throws Exception {
        mockMvc.perform(put("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"%s\", \"tags\": %s}".formatted(user.getName(), tagsJson)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프로필 저장으로 새 기술 스택이 생기면 커밋 후 바로 매칭된다")
    void newTechStackFromProfileIsMatchedWithoutRestart() throws Exception {
        User user = newUser("kw-user");
        String newStack = "zzstack" + UUID.randomUUID().toString().substring(0, 8);
        assertThat(techKeywordMatcher.match(newStack + " 쓰는 프로젝트")).isEmpty();

        try {
            updateTags(user, "[\"%s\"]".formatted(newStack));

            TechStack created = techStackRepository.findByName(newStack).orElseThrow();
            assertThat(techKeywordMatcher.match(newStack + " 쓰는 프로젝트")).contains(created.getId());
        } finally {
            updateTags(user, "[]");   // user_tech_stack 연결을 먼저 끊어야 기술 스택을 지울 수 있다
            techStackRepository.findByName(newStack).ifPresent(techStackRepository::delete);
            techKeywordMatcher.reload();
        }
    }
}
