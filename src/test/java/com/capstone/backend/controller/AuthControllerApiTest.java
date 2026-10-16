package com.capstone.backend.controller;

import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.capstone.backend.service.AuthService;
import com.capstone.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    // AuthController가 UserService를 주입받도록 바뀐 뒤 테스트에 반영되지 않아 컨텍스트 로딩이 실패했다 (TS-07)
    @MockBean
    private UserService userService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Test
    void signupReturnsCreated() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(User.builder().build());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "pw1234",
                                  "name": "New User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    void loginReturnsAccessTokenJson() throws Exception {
        String token = jwtUtil.createToken("user@example.com");
        when(authService.login(argThat(request ->
                "user@example.com".equals(request.getEmail()) &&
                        "pw1234".equals(request.getPassword())))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "pw1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token));
    }

    // /me 응답이 문자열(email)에서 JSON(userId, email, name)으로 바뀐 뒤 테스트가 갱신되지 않았다 (TS-07)
    @Test
    void meReturnsGuestJsonWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(0))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void meReturnsUserJsonWithValidToken() throws Exception {
        String token = jwtUtil.createToken("user@example.com");
        when(userService.findByEmail("user@example.com"))
                .thenReturn(User.builder().email("user@example.com").name("Tester").build());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("Tester"));
    }

    @Test
    void signupReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": "123",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
