package com.capstone.backend.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiAuthenticationFilterTest {

    private final JwtUtil jwtUtil = new JwtUtil("mysecretkeymysecretkeymysecretkey", 3600000);
    private final JwtFilter jwtFilter = new JwtFilter(jwtUtil);
    private final ApiAuthenticationFilter apiAuthenticationFilter =
            new ApiAuthenticationFilter(new ObjectMapper());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void protectedPostApiRejectsRequestWithoutToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        runFilterChain(request, response);

        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"인증이 필요합니다.\"}", response.getContentAsString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void protectedAiApiRejectsRequestWithInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ai/recommend");
        request.addHeader("Authorization", "Bearer invalid.token.value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        runFilterChain(request, response);

        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"유효한 인증 토큰이 필요합니다.\"}", response.getContentAsString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void protectedApiPassesWithValidToken() throws ServletException, IOException {
        String token = jwtUtil.createToken("user@example.com");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        runFilterChain(request, response);

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void nonProtectedApiSkipsAuthenticationFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tech-stacks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        runFilterChain(request, response);

        assertEquals(200, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void runFilterChain(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        jwtFilter.doFilter(request, response, (req, res) ->
                apiAuthenticationFilter.doFilter(req, res, new MockFilterChain()));
    }
}
