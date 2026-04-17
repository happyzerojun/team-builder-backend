package com.capstone.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testApi() {
        return "스웨거 연결 성공! 프론트엔드 팀원들, 여기서 API 테스트하시면 됩니다! 😎";
    }
}