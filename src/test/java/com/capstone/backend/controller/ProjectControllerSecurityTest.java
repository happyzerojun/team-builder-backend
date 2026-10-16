package com.capstone.backend.controller;

import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.capstone.backend.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-05: 기본 닫힘 인가 규칙과 토큰 사용자 식별 */
@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
@DisplayName("모집글 API 인가")
class ProjectControllerSecurityTest {

    private static final String VALID_BODY = """
            {"title": "제목", "content": "내용", "region": "온라인", "term": 3, "leader_id": 999}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private String bearer(String email) {
        return "Bearer " + jwtUtil.createToken(email);
    }

    private static ProjectResponseDto dto() {
        return new ProjectResponseDto(1L, "제목", "내용", "온라인", "모집중", 7L, "작성자", "3", null, false, List.of(), List.of());
    }

    @Test
    @DisplayName("목록·상세·팀원 조회는 비로그인도 200")
    void publicReadsArePermitted() throws Exception {
        when(projectService.getAllProjects(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto())));
        when(projectService.getProjectById(1L)).thenReturn(dto());
        when(projectService.getProjectMembers(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
        mockMvc.perform(get("/api/projects/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/projects/1/members")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("생성·수정·삭제·상태변경·팀원관리는 비로그인 401(JSON)")
    void writesRequireLogin() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(put("/api/projects/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/projects/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/projects/1/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"진행중\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/projects/1/members/2")).andExpect(status().isUnauthorized());

        verify(projectService, never()).createProject(anyString(), any());
        verify(projectService, never()).deleteProject(anyString(), anyLong());
    }

    @Test
    @DisplayName("내가 쓴 글(GET /me)은 비로그인 401, 로그인하면 토큰의 사용자로 조회 (TS-17)")
    void myProjectsRequireLoginAndUseTokenUser() throws Exception {
        mockMvc.perform(get("/api/projects/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        verify(projectService, never()).getMyProjects(anyString(), any(Pageable.class));

        when(projectService.getMyProjects(eq("me@example.com"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto())));
        mockMvc.perform(get("/api/projects/me").header("Authorization", bearer("me@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].project_id").value(1));
        verify(projectService).getMyProjects(eq("me@example.com"), any(Pageable.class));
        // "/me"가 "/{projectId}"로 잘못 매칭되면 숫자 변환 실패(400)나 상세 조회가 호출된다
        verify(projectService, never()).getProjectById(anyLong());
    }

    @Test
    @DisplayName("작성자는 본문의 leader_id가 아니라 토큰의 사용자")
    void createUsesTokenUser() throws Exception {
        when(projectService.createProject(eq("me@example.com"), any(ProjectRequestDto.class))).thenReturn(dto());

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer("me@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(projectService).createProject(eq("me@example.com"), any(ProjectRequestDto.class));
    }

    @Test
    @DisplayName("필수 값이 비면 서비스 호출 전에 400")
    void invalidBodyIs400() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer("me@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\", \"content\": \"내용\", \"region\": \"온라인\", \"term\": 3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(projectService, never()).createProject(anyString(), any());
    }

    @Test
    @DisplayName("작성자가 아니면 403(JSON)")
    void nonOwnerIs403() throws Exception {
        when(projectService.updateProject(eq("other@example.com"), eq(1L), any(ProjectRequestDto.class)))
                .thenThrow(new ForbiddenException("모집글 작성자만 할 수 있습니다."));

        mockMvc.perform(put("/api/projects/1")
                        .header("Authorization", bearer("other@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("위조·만료 토큰은 인증으로 인정하지 않는다")
    void invalidTokenIs401() throws Exception {
        mockMvc.perform(delete("/api/projects/1").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }
}
