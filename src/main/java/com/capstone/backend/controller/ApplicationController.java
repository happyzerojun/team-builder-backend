package com.capstone.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    // 1. 특정 유저의 지원 내역 조회 (GET /api/application/user/{userId})
    @GetMapping("/user/{userId}")
    public ResponseEntity<String> getUserApplications(@PathVariable Long userId) {
        // TODO: userId로 해당 유저가 지원한 내역 목록(Application) 조회 로직 추가
        return ResponseEntity.ok("유저 " + userId + "의 지원 내역 조회 성공 (API 연결 테스트)");
    }

    // 2. 특정 프로젝트의 지원자 목록 조회 (GET /api/application/project/{projectId})
    @GetMapping("/project/{projectId}")
    public ResponseEntity<String> getProjectApplications(@PathVariable Long projectId) {
        // TODO: projectId로 해당 프로젝트에 들어온 지원서 목록 조회 로직 추가
        return ResponseEntity.ok("프로젝트 " + projectId + "의 지원자 목록 조회 성공 (API 연결 테스트)");
    }

    // 3. 지원서 승인/거절 상태 변경 (PATCH /api/application/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateApplicationStatus(@PathVariable Long id) {
        // TODO: id(지원서 PK)에 해당하는 지원서의 상태(status) 업데이트 로직 추가
        return ResponseEntity.ok("지원서 " + id + " 상태 변경 완료 (API 연결 테스트)");
    }
}