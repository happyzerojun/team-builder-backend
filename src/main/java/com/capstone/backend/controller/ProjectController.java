package com.capstone.backend.controller;

import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.service.ProjectService;
import com.capstone.backend.dto.MemberResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.capstone.backend.dto.ProjectStatusUpdateRequestDto;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody ProjectRequestDto request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    // 4. 프로젝트 수정 (PUT /api/projects/{projectId})
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
        @PathVariable Long projectId,
        @RequestBody ProjectRequestDto request) {
    return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    // 5. 프로젝트 삭제 (DELETE /api/projects/{projectId})
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        return ResponseEntity.ok("프로젝트 " + projectId + " 삭제 성공 (임시)");
    }

    // 6. 프로젝트 팀원 목록
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<MemberResponseDto>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    // 7. 상태 변경
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponseDto> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectStatusUpdateRequestDto request
    ) {
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, request.getStatus()));
    }

    // 8. 팀원 제외 (DELETE /api/projects/{projectId}/members/{memberId})
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<String> removeProjectMember(
        @PathVariable Long projectId,
        @PathVariable Long memberId) {
            projectService.removeProjectMember(projectId, memberId);
            return ResponseEntity.ok("팀원 제외 성공");
    }

    // 팀원 추가 (POST /api/projects/{projectId}/members)
    @PostMapping("/{projectId}/members")
    public ResponseEntity<String> addProjectMember(
        @PathVariable Long projectId,
        @RequestBody Map<String, Long> body) {
            projectService.addProjectMember(projectId, body.get("user_id"));
            return ResponseEntity.ok("팀원 추가 성공");
    }

}