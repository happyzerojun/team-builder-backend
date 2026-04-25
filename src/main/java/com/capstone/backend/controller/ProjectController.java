package com.capstone.backend.controller;

import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 1. 전체 프로젝트 조회 (GET /api/projects)
    @GetMapping("")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // 2. 프로젝트 상세 조회 (GET /api/projects/{projectId})
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // 3. 프로젝트 생성 (POST /api/projects)
    @PostMapping("")
    public ResponseEntity<String> createProject() {
        return ResponseEntity.ok("프로젝트 생성 성공 (임시)");
    }

    // 4. 프로젝트 수정 (PUT /api/projects/{projectId})
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 수정 성공 (임시)");
    }

    // 5. 프로젝트 삭제 (DELETE /api/projects/{projectId})
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 삭제 성공 (임시)");
    }

    // 6. 프로젝트 팀원 목록
    @GetMapping("/{projectId}/members")
    public ResponseEntity<String> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 팀원 조회 (임시)");
    }

    // 7. 상태 변경
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<String> updateProjectStatus(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 상태 변경 (임시)");
    }
}