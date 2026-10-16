package com.capstone.backend.controller;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.capstone.backend.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TS-05: AI 추천은 JWT의 사용자로만 동작한다 (예전 userId 기본값 2L 제거) */
@WebMvcTest(AiController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
class AiControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AiService aiService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Test
    void anonymousRequestIs401AndDoesNotCallAi() throws Exception {
        mockMvc.perform(post("/api/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 2, \"userPrompt\": \"React 프로젝트\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verify(aiService, never()).getRecommendation(anyString(), anyString());
    }

    @Test
    void usesEmailFromTokenAndIgnoresBodyUserId() throws Exception {
        String token = jwtUtil.createToken("me@example.com");
        when(aiService.getRecommendation(eq("me@example.com"), anyString()))
                .thenReturn(AiRecommendResponse.builder().recommendations(List.of()).build());

        mockMvc.perform(post("/api/ai/recommend")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 2, \"userPrompt\": \"React 프로젝트\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray());

        verify(aiService).getRecommendation("me@example.com", "React 프로젝트");
    }

    @Test
    void blankPromptIs400() throws Exception {
        String token = jwtUtil.createToken("me@example.com");
        mockMvc.perform(post("/api/ai/recommend")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userPrompt\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
