package com.capstone.backend.controller;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.capstone.backend.service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-05: 지원서 API는 로그인 필수이고, 지원자는 본문이 아닌 토큰으로 정해진다 */
@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
@DisplayName("지원서 API 인가")
class ApplicationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Test
    @DisplayName("비로그인 지원·조회는 401")
    void anonymousIs401() throws Exception {
        mockMvc.perform(post("/api/application").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"project_id\": 1, \"applicant_id\": 2}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/application/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/application/project/1")).andExpect(status().isUnauthorized());

        verify(applicationService, never()).createApplication(anyString(), any());
    }

    @Test
    @DisplayName("본문에 남의 applicant_id를 넣어도 토큰의 사용자로 지원된다")
    void applyUsesTokenUser() throws Exception {
        when(applicationService.createApplication(eq("me@example.com"), any()))
                .thenReturn(ApplicationResponseDto.builder().applicationId(1L).build());

        mockMvc.perform(post("/api/application")
                        .header("Authorization", "Bearer " + jwtUtil.createToken("me@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"project_id\": 1, \"applicant_id\": 999, \"support_role\": \"백엔드\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<ApplicationRequestDto.Create> captor = ArgumentCaptor.forClass(ApplicationRequestDto.Create.class);
        verify(applicationService).createApplication(eq("me@example.com"), captor.capture());
        assertThat(captor.getValue().getProject_id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("내 지원 내역은 토큰의 사용자 기준")
    void myApplicationsUsesTokenUser() throws Exception {
        when(applicationService.getMyApplications("me@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/application/me")
                        .header("Authorization", "Bearer " + jwtUtil.createToken("me@example.com")))
                .andExpect(status().isOk());
    }
}
