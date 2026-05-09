package com.capstone.backend.controller;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService; // 비즈니스 로직을 처리할 Service 클래스

    /**
     * 1. 프로젝트 지원 (POST /api/application)
     */
    @PostMapping
    public ResponseEntity<ApplicationResponseDto> apply(@RequestBody ApplicationRequestDto.Create requestDto) {
        // 실제로는 세션이나 JWT 토큰에서 로그인한 유저 정보(applicant_id)를 가져오는 것이 보안상 안전합니다.
        ApplicationResponseDto response = applicationService.createApplication(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 지원 취소 (DELETE /api/application/{applicationId})
     */
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> cancelApplication(@PathVariable Long applicationId) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 3. 내 지원 내역 조회 (GET /api/application/user/{userId})
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(@PathVariable Long userId) {
        List<ApplicationResponseDto> responses = applicationService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 4. 프로젝트별 지원자 목록 조회 (GET /api/application/project/{projectId})
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ApplicationResponseDto>> getProjectApplications(@PathVariable Long projectId) {
        List<ApplicationResponseDto> responses = applicationService.getApplicationsByProjectId(projectId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 5. 지원서 상태 변경 (수락/거절) (PATCH /api/application/{applicationId})
     */
    @PatchMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody ApplicationRequestDto.UpdateStatus requestDto) {

        ApplicationResponseDto response = applicationService.updateStatus(applicationId, requestDto.getStatus());
        return ResponseEntity.ok(response);
    }
}