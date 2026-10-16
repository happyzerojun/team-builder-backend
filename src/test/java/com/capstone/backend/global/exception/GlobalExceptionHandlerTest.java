package com.capstone.backend.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 에러 응답에 code가 채워지고, 기존 message 필드도 유지되는지 (Spring 컨텍스트 없이 standalone) */
@DisplayName("공통 에러 응답 code")
class GlobalExceptionHandlerTest {

    @RestController
    static class ThrowingController {
        @GetMapping("/not-found")
        void notFound() { throw new NotFoundException("프로젝트를 찾을 수 없습니다."); }

        @GetMapping("/forbidden")
        void forbidden() { throw new ForbiddenException("모집글 작성자만 할 수 있습니다."); }

        @GetMapping("/conflict")
        void conflict() { throw new IllegalStateException("정원이 찼습니다."); }

        @GetMapping("/unauthorized")
        void unauthorized() { throw new UnauthorizedException("로그인이 필요합니다."); }

        @GetMapping("/bad")
        void bad() { throw new IllegalArgumentException("잘못된 값"); }

        @GetMapping("/boom")
        void boom() { throw new RuntimeException("SQL syntax error near 'user'"); }

        @PostMapping("/body")
        void body(@RequestBody Map<String, Object> body) { }
    }

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void notFoundIs404() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다."));
    }

    @Test
    void forbiddenIs403() throws Exception {
        mockMvc.perform(get("/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void illegalStateIs409Conflict() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void unauthorizedIs401() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void illegalArgumentIs400ValidationFailed() throws Exception {
        mockMvc.perform(get("/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void malformedJsonIs400InsteadOf500() throws Exception {
        mockMvc.perform(post("/body").contentType(MediaType.APPLICATION_JSON).content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void unexpectedExceptionHidesInternalMessage() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }
}
