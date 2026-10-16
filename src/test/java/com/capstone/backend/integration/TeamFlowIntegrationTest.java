package com.capstone.backend.integration;

import com.capstone.backend.entity.User;
import com.capstone.backend.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 화면 흐름 그대로 API를 끝까지 호출한다: 글 작성 → 지원 → 수락 → 진행 → 종료 → 리뷰 → 삭제.
 * - TS-12: OSIV를 끈 상태에서 컨트롤러·DTO 변환 중 LazyInitializationException(500)이 나지 않는지
 * - TS-05: 토큰 사용자 기준 식별과 소유권 검사
 * - TS-06: 포지션 정원
 */
@DisplayName("팀 빌딩 전체 흐름 (MySQL, OSIV off)")
class TeamFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void fullTeamBuildingFlow() throws Exception {
        User leader = newUser("flow-leader");
        User applicant = newUser("flow-applicant");
        User latecomer = newUser("flow-latecomer");
        User outsider = newUser("flow-outsider");

        // 1. 글 작성: 본문의 leader_id(남의 ID)는 무시되고 토큰의 사용자가 작성자
        String created = mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "흐름 테스트", "content": "내용", "region": "온라인", "term": 2,
                                 "leader_id": %d, "positions": [{"roleName": "백엔드", "requiredCount": 1}]}
                                """.formatted(outsider.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leader_id").value(leader.getId()))
                .andExpect(jsonPath("$.status").value("모집중"))
                .andExpect(jsonPath("$.positions[0].requiredCount").value(1))
                .andReturn().getResponse().getContentAsString();
        long projectId = ((Number) JsonPath.read(created, "$.project_id")).longValue();
        trackProject(projectId);

        // 2. 공개 조회(비로그인): 목록·상세·팀원(작성자가 팀장으로 자동 등록)
        mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author_name").value("flow-leader"));
        mockMvc.perform(get("/api/projects/" + projectId + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("팀장"));

        // 3. 지원: 본문 applicant_id는 무시
        String applied = mockMvc.perform(post("/api/application")
                        .header(HttpHeaders.AUTHORIZATION, bearer(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"project_id": %d, "applicant_id": %d, "support_role": "백엔드", "message": "지원합니다"}
                                """.formatted(projectId, outsider.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicantId").value(applicant.getId()))
                .andReturn().getResponse().getContentAsString();
        long applicationId = ((Number) JsonPath.read(applied, "$.applicationId")).longValue();

        mockMvc.perform(post("/api/application")
                        .header(HttpHeaders.AUTHORIZATION, bearer(latecomer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"project_id\": %d, \"support_role\": \"백엔드\"}".formatted(projectId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/application/me").header(HttpHeaders.AUTHORIZATION, bearer(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectTitle").value("흐름 테스트"))
                .andExpect(jsonPath("$[0].projectStatus").value("모집중"))
                .andExpect(jsonPath("$[0].projectRegion").value("온라인"));

        // 4. 지원서 목록·수락은 작성자만
        mockMvc.perform(get("/api/application/project/" + projectId).header(HttpHeaders.AUTHORIZATION, bearer(outsider)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/application/project/" + projectId).header(HttpHeaders.AUTHORIZATION, bearer(leader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(patch("/api/application/" + applicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(outsider))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"ACCEPTED\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/application/" + applicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 정원 1명이 찼으므로 두 번째 지원자 수락은 409
        String latecomerApps = mockMvc.perform(get("/api/application/me").header(HttpHeaders.AUTHORIZATION, bearer(latecomer)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long latecomerApplicationId = ((Number) JsonPath.read(latecomerApps, "$[0].applicationId")).longValue();
        mockMvc.perform(patch("/api/application/" + latecomerApplicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"ACCEPTED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(jsonPath("$.positions[0].currentCount").value(1));

        // 팀원은 수락된 지원서만 본다
        mockMvc.perform(get("/api/application/project/" + projectId).header(HttpHeaders.AUTHORIZATION, bearer(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 5. 진행 → 종료 (작성자만, 한 단계씩)
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(applicant))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"진행중\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"진행중\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("진행중"));
        mockMvc.perform(patch("/api/projects/" + projectId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leader))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"종료됨\"}"))
                .andExpect(status().isOk());

        // 6. 리뷰: 본문 reviewer_id는 무시, 팀원끼리만
        mockMvc.perform(post("/api/review/bulk")
                        .header(HttpHeaders.AUTHORIZATION, bearer(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"project_id": %d, "reviewer_id": %d, "reviewee_id": %d, "rating": 5, "comment": "최고"}]
                                """.formatted(projectId, outsider.getId(), leader.getId())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/review/received/me").header(HttpHeaders.AUTHORIZATION, bearer(leader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewer_id").value(applicant.getId()));
        mockMvc.perform(get("/api/review/project/" + projectId + "/me").header(HttpHeaders.AUTHORIZATION, bearer(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 7. 프로필(지연 로딩 컬렉션 userTechStacks)
        mockMvc.perform(get("/api/users/me/profile").header(HttpHeaders.AUTHORIZATION, bearer(leader)))
                .andExpect(status().isOk());

        // 8. 삭제: 작성자만, 리뷰·지원서·포지션까지 정리
        mockMvc.perform(delete("/api/projects/" + projectId).header(HttpHeaders.AUTHORIZATION, bearer(applicant)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/projects/" + projectId).header(HttpHeaders.AUTHORIZATION, bearer(leader)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
