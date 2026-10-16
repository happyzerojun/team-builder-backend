package com.capstone.backend.security;

import com.capstone.backend.global.exception.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * 컨트롤러에서 "누가 요청했는가"를 꺼내는 유일한 통로.
 * 요청 본문·URL의 userId는 클라이언트가 바꿀 수 있으므로 신원 판단에 쓰지 않는다 (TS-05)
 * JwtFilter가 토큰의 subject(email)를 principal로 넣는다.
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static String email(Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
