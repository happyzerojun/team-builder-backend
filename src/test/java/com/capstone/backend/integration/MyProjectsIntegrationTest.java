package com.capstone.backend.integration;

import com.capstone.backend.entity.User;
import com.capstone.backend.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-17: 마이페이지 "내가 만든 프로젝트"가 전체 목록 첫 페이지에 의존하지 않는지 */
@DisplayName("내가 쓴 모집글 GET /api/projects/me (MySQL)")
class MyProjectsIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("작성자 본인의 글만, 전체 목록 첫 페이지 밖의 글까지 페이지로 모두 조회")
    void returnsOnlyMyProjectsWithPaging() throws Exception {
        User me = newUser("mine-me");
        User other = newUser("mine-other");
        for (int i = 0; i < 12; i++) {
            newProject(me, "내 글 " + i);
        }
        newProject(other, "남의 글 1");
        newProject(other, "남의 글 2");   // 가장 최신 글은 남의 글

        String first = mockMvc.perform(get("/api/projects/me").param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].title").value("내 글 11"))   // 기본 최신순
                .andReturn().getResponse().getContentAsString();
        List<Number> leaderIds = JsonPath.read(first, "$.content[*].leader_id");
        assertThat(leaderIds).allSatisfy(id -> assertThat(id.longValue()).isEqualTo(me.getId()));

        mockMvc.perform(get("/api/projects/me").param("page", "2").param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, bearer(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[1].title").value("내 글 0"));

        mockMvc.perform(get("/api/projects/me").header(HttpHeaders.AUTHORIZATION, bearer(other)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("비로그인은 401, 글이 없으면 빈 페이지")
    void requiresLoginAndHandlesEmpty() throws Exception {
        mockMvc.perform(get("/api/projects/me"))
                .andExpect(status().isUnauthorized());

        User nobody = newUser("mine-empty");
        mockMvc.perform(get("/api/projects/me").header(HttpHeaders.AUTHORIZATION, bearer(nobody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
