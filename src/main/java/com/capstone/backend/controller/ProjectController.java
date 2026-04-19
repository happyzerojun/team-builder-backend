package com.capstone.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
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

    // 1. 전체 프로젝트 조회 (GET /api/project)
    @GetMapping("")
    public ResponseEntity<String> getAllProjects() {
        return ResponseEntity.ok("전체 프로젝트 조회 성공 (API 연결 테스트)");
    }

    // 2. 프로젝트 상세 조회 (GET /api/project/{projectId})
    @GetMapping("/{projectId}")
    public ResponseEntity<String> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 상세 조회 성공 (API 연결 테스트)");
    }

    // 3. 프로젝트 생성 (POST /api/project)
    @PostMapping("")
    public ResponseEntity<String> createProject() {
        return ResponseEntity.ok("프로젝트 생성 성공 (API 연결 테스트)");
    }

    // 4. 프로젝트 수정 (PUT /api/project/{projectId})
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 수정 성공 (API 연결 테스트)");
    }

    // 5. 프로젝트 삭제 (DELETE /api/project/{projectId})
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 삭제 성공 (API 연결 테스트)");
    }
}