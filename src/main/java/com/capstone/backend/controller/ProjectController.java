package com.capstone.backend.controller;

import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.dto.ProjectStatusUpdateRequestDto;
import com.capstone.backend.security.AuthenticatedUser;
import com.capstone.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GET(목록·상세·팀원)은 비로그인 공개, 나머지는 로그인 + 모집글 작성자만 (SecurityConfig, ProjectService)
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 1. 전체 프로젝트 조회 (GET /api/projects?page=0&size=10), 기본 최신순
    @GetMapping("")
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    // 2. 프로젝트 상세 조회
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    // 3. 프로젝트 생성: 작성자는 토큰의 사용자
    @PostMapping("")
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto request,
                                                            Authentication authentication) {
        return ResponseEntity.ok(projectService.createProject(AuthenticatedUser.email(authentication), request));
    }

    // 4. 프로젝트 수정: 작성자만
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProject(AuthenticatedUser.email(authentication), projectId, request));
    }

    // 5. 프로젝트 삭제: 작성자만 (이전에는 실제로 지우지 않는 임시 응답이었다)
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId, Authentication authentication) {
        projectService.deleteProject(AuthenticatedUser.email(authentication), projectId);
        return ResponseEntity.noContent().build();
    }

    // 6. 프로젝트 팀원 목록 (공개)
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<MemberResponseDto>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    // 7. 상태 변경: 작성자만, 모집중 → 진행중 → 종료됨
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponseDto> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectStatusUpdateRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProjectStatus(
                AuthenticatedUser.email(authentication), projectId, request.getStatus()));
    }

    // 8. 팀원 제외: 작성자만
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<String> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            Authentication authentication) {
        projectService.removeProjectMember(AuthenticatedUser.email(authentication), projectId, memberId);
        return ResponseEntity.ok("팀원 제외 성공");
    }

    // 9. 팀원 추가: 작성자만 (작성자 본인은 글 생성 시 자동 등록되므로 프론트가 따로 부르지 않는다)
    @PostMapping("/{projectId}/members")
    public ResponseEntity<String> addProjectMember(
            @PathVariable Long projectId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        projectService.addProjectMember(AuthenticatedUser.email(authentication), projectId, body.get("user_id"));
        return ResponseEntity.ok("팀원 추가 성공");
    }
}
