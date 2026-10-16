package com.capstone.backend.integration;

import com.capstone.backend.entity.User;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 모집글 수정 API가 기술 스택(TS-18)을 실제로 반영하는지 (MySQL) */
@DisplayName("모집글 수정 (MySQL)")
class ProjectUpdateIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TechStackRepository techStackRepository;

    private long stackId(String name) {
        return techStackRepository.findByName(name).orElseThrow().getId();   // DataInitializer 초기 데이터
    }

    private long createProject(User leader, String extraJson) throws Exception {
        String created = mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "수정 테스트", "content": "내용", "region": "온라인", "term": 3 %s}
                                """.formatted(extraJson)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long projectId = ((Number) JsonPath.read(created, "$.project_id")).longValue();
        trackProject(projectId);
        return projectId;
    }

    private ResultActions update(User leader, long projectId, String extraJson) throws Exception {
        return mockMvc.perform(put("/api/projects/" + projectId)
                .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "수정한 제목", "content": "내용", "region": "온라인", "term": 3 %s}
                        """.formatted(extraJson)));
    }

    private List<String> stackNames(long projectId) throws Exception {
        String body = mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.techStacks[*].name");
    }

    @Test
    @DisplayName("기술 스택: 생성 응답에 포함되고, 수정하면 교체·빈 목록이면 해제·필드가 없으면 유지")
    void techStacksAreReplacedOnUpdate() throws Exception {
        User leader = newUser("upd-stack");
        long react = stackId("React");
        long java = stackId("Java");
        long docker = stackId("Docker");

        long projectId = createProject(leader, ", \"techStackIds\": [%d, %d]".formatted(react, java));
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Java");

        // 교체: React 유지, Java 제거, Docker 추가 (중복 ID는 한 번만)
        update(leader, projectId, ", \"techStackIds\": [%d, %d, %d]".formatted(react, docker, docker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정한 제목"))
                .andExpect(jsonPath("$.techStacks.length()").value(2));
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Docker");

        // techStackIds 필드 없음 → 기존 유지
        update(leader, projectId, "").andExpect(status().isOk());
        assertThat(stackNames(projectId)).containsExactlyInAnyOrder("React", "Docker");

        // 빈 목록 → 모두 해제
        update(leader, projectId, ", \"techStackIds\": []").andExpect(status().isOk());
        assertThat(stackNames(projectId)).isEmpty();
    }

    @Test
    @DisplayName("생성 응답에도 저장한 기술 스택이 들어 있다")
    void createResponseIncludesTechStacks() throws Exception {
        User leader = newUser("upd-create");
        mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "생성 응답", "content": "내용", "region": "온라인", "term": 3, "techStackIds": [%d]}
                                """.formatted(stackId("Spring Boot"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.techStacks[0].name").value("Spring Boot"))
                .andDo(result -> trackProject(((Number) JsonPath.read(
                        result.getResponse().getContentAsString(), "$.project_id")).longValue()));
    }
}
