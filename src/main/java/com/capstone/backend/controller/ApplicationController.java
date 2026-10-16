package com.capstone.backend.controller;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.security.AuthenticatedUser;
import com.capstone.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 모든 엔드포인트 로그인 필요. 요청자는 JWT에서 꺼내고 소유권은 서비스에서 검사한다 (TS-05) */
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /** 1. 프로젝트 지원 (본문의 applicant_id는 무시하고 토큰의 사용자로 지원) */
    @PostMapping
    public ResponseEntity<ApplicationResponseDto> apply(@RequestBody ApplicationRequestDto.Create requestDto,
                                                        Authentication authentication) {
        ApplicationResponseDto response = applicationService.createApplication(AuthenticatedUser.email(authentication), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 2. 지원 취소: 지원자 본인만 */
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> cancelApplication(@PathVariable Long applicationId, Authentication authentication) {
        applicationService.deleteApplication(AuthenticatedUser.email(authentication), applicationId);
        return ResponseEntity.noContent().build();
    }

    /** 3. 내 지원 내역 */
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(AuthenticatedUser.email(authentication)));
    }

    /** 3-1. 기존 경로 호환: 본인 userId만 허용(다르면 403) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsByUser(@PathVariable Long userId,
                                                                              Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplicationsByUserId(AuthenticatedUser.email(authentication), userId));
    }

    /** 4. 프로젝트별 지원서 목록: 팀장은 전체, 팀원은 수락된 지원서만 */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ApplicationResponseDto>> getProjectApplications(@PathVariable Long projectId,
                                                                               Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplicationsByProjectId(AuthenticatedUser.email(authentication), projectId));
    }

    /** 5. 지원서 상태 변경(수락/거절): 모집글 작성자만 */
    @PatchMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody ApplicationRequestDto.UpdateStatus requestDto,
            Authentication authentication) {
        return ResponseEntity.ok(applicationService.updateStatus(
                AuthenticatedUser.email(authentication), applicationId, requestDto.getStatus()));
    }
}
