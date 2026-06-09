package com.capstone.backend.controller;

import com.capstone.backend.dto.*;
import com.capstone.backend.entity.User;
import com.capstone.backend.service.AuthService;
import com.capstone.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Set<String> SUPPORTED_OAUTH_PROVIDERS = Set.of("google", "kakao", "naver");

    private final AuthService authService;
    private final UserService userService;

    // =========================
    // 회원가입
    // =========================
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // =========================
    // 로그인
    // =========================
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new TokenResponse(authService.login(request)));
    }

    // =========================
    // 서버 테스트
    // =========================
    @GetMapping("/test")
    public String test() {
        return "server running";
    }

    // =========================
    // 🔥 로그인 유저 정보 (핵심)
    // =========================
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {

        Map<String, Object> res = new HashMap<>();

        // 🔴 1. 로그인 안 된 경우 (500 방지 핵심)
        if (auth == null ||
                !auth.isAuthenticated() ||
                auth.getName() == null ||
                auth.getName().equals("anonymousUser")) {

            res.put("userId", 0);
            res.put("email", null);
            res.put("name", null);

            return ResponseEntity.ok(res);
        }

        // 🔵 2. 로그인 된 경우
        User user = userService.findByEmail(auth.getName());

        // 🔴 user null 방어
        if (user == null) {
            res.put("userId", 0);
            res.put("email", null);
            res.put("name", null);

            return ResponseEntity.ok(res);
        }

        res.put("userId", user.getId());
        res.put("email", user.getEmail());
        res.put("name", user.getName());

        return ResponseEntity.ok(res);
    }

    // =========================
    // OAuth2 URL
    // =========================
    @GetMapping("/oauth2/url/{provider}")
    public ResponseEntity<String> getOauth2AuthorizationUrl(@PathVariable String provider) {
        String normalizedProvider = provider.toLowerCase();
        if (!SUPPORTED_OAUTH_PROVIDERS.contains(normalizedProvider)) {
            return ResponseEntity.badRequest().body("지원하지 않는 OAuth 제공자입니다.");
        }
        return ResponseEntity.ok("/oauth2/authorization/" + normalizedProvider);
    }
}
