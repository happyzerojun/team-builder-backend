package com.capstone.backend.controller;

import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.dto.ProjectStatusUpdateRequestDto;
import com.capstone.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
// 👇 페이징 처리를 위해 새로 추가된 임포트문들
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 1. 전체 프로젝트 조회 (페이징 적용 후: GET /api/projects?page=0&size=10)
    // 기본값으로 10개씩, 생성일(createdAt) 기준 최신순(DESC)으로 가져오도록 설정했습니다.
    @GetMapping("")
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    // 2. 프로젝트 상세 조회 (GET /api/projects/{projectId})
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // 3. 프로젝트 생성 (POST /api/projects)
    @PostMapping("")
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody ProjectRequestDto request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(projectService.createProject(request, authentication.getName()));
    }

    // 4. 프로젝트 수정 (PUT /api/projects/{projectId})
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request, authentication.getName()));
    }

    // 5. 프로젝트 삭제 (DELETE /api/projects/{projectId})
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId, Authentication authentication) {
        projectService.deleteProject(projectId, authentication.getName());
        return ResponseEntity.noContent().build();
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
            @RequestBody ProjectStatusUpdateRequestDto request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, request.getStatus(), authentication.getName()));
    }

    // 8. 팀원 제외 (DELETE /api/projects/{projectId}/members/{memberId})
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<String> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            Authentication authentication) {
        projectService.removeProjectMember(projectId, memberId, authentication.getName());
        return ResponseEntity.ok("팀원 제외 성공");
    }

    // 팀원 추가 (POST /api/projects/{projectId}/members)
    @PostMapping("/{projectId}/members")
    public ResponseEntity<String> addProjectMember(
            @PathVariable Long projectId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        projectService.addProjectMember(projectId, body.get("user_id"), authentication.getName());
        return ResponseEntity.ok("팀원 추가 성공");
    }
}
