package com.capstone.backend.security;

import com.capstone.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 필터 단계(컨트롤러 도달 전)에서 막힌 요청도 GlobalExceptionHandler와 같은
 * {"code": "...", "message": "..."} 형식으로 응답한다.
 * 이전 HttpStatusEntryPoint는 본문 없이 401만 내려 프론트가 사유를 알 수 없었다.
 */
public final class SecurityErrorResponder {

    private SecurityErrorResponder() {
    }

    public static AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                write(response, ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    public static AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) ->
                write(response, ErrorCode.FORBIDDEN, "권한이 없습니다.");
    }

    static void write(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        response.setStatus(code.status().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + code.name() + "\",\"message\":\"" + escape(message) + "\"}");
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
