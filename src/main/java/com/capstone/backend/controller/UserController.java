package com.capstone.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/user/me")
    public String me(Authentication authentication) {
        return "로그인 사용자: " + authentication.getName();
    }
}