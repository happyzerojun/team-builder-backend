package com.capstone.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    // 4. 프로젝트 확정 팀원 목록 조회 (GET /api/project/{projectId}/members)
    @GetMapping("/{projectId}/members")
    public ResponseEntity<String> getProjectMembers(@PathVariable Long projectId) {
        // TODO: projectId에 해당하는 프로젝트의 '승인된(ACCEPTED)' 팀원 목록 조회 로직 추가
        return ResponseEntity.ok("프로젝트 " + projectId + "의 확정 팀원 목록 조회 성공 (API 연결 테스트)");
    }

    // 5. 프로젝트 모집 상태 변경 (PATCH /api/project/{projectId}/status)
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<String> updateProjectStatus(@PathVariable Long projectId) {
        // TODO: projectId에 해당하는 프로젝트의 모집 상태(모집중, 완료 등) 변경 로직 추가
        return ResponseEntity.ok("프로젝트 " + projectId + " 모집 상태 변경 완료 (API 연결 테스트)");
    }
}