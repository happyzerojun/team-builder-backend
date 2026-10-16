package com.capstone.backend.controller;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.security.AuthenticatedUser;
import com.capstone.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 모든 엔드포인트 로그인 필요. 경로의 사용자 ID는 본인일 때만 허용 (TS-05) */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 특정 프로젝트에서 내가 작성한 리뷰 조회
    @GetMapping("/project/{projectId}/me")
    public ResponseEntity<List<ReviewResponseDto>> getProjectMyReviews(@PathVariable Long projectId,
                                                                       Authentication authentication) {
        return ResponseEntity.ok(reviewService.getMyReviewsForProject(AuthenticatedUser.email(authentication), projectId));
    }

    // 1-1. 기존 경로 호환 (reviewerId가 본인이 아니면 403)
    @GetMapping("/project/{projectId}/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewResponseDto>> getProjectReviewsByReviewer(
            @PathVariable Long projectId,
            @PathVariable Long reviewerId,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.getMyReviewsForProject(AuthenticatedUser.email(authentication), projectId, reviewerId));
    }

    // 2. 리뷰 다중 저장 (본문 reviewer_id는 무시하고 토큰의 사용자로 저장)
    @PostMapping("/bulk")
    public ResponseEntity<String> saveProjectReviews(@RequestBody List<ReviewBulkSaveRequestDto> reviews,
                                                     Authentication authentication) {
        reviewService.saveBulkReviews(AuthenticatedUser.email(authentication), reviews);
        return ResponseEntity.ok("Reviews saved successfully");
    }

    // 3. 내가 받은 리뷰 조회 (마이페이지용)
    @GetMapping("/received/me")
    public ResponseEntity<List<ReviewResponseDto>> getMyReceivedReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getReceivedReviews(AuthenticatedUser.email(authentication)));
    }

    // 3-1. 기존 경로 호환 (userId가 본인이 아니면 403)
    @GetMapping("/received/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> getReceivedReviewsByUser(@PathVariable Long userId,
                                                                           Authentication authentication) {
        return ResponseEntity.ok(reviewService.getReceivedReviews(AuthenticatedUser.email(authentication), userId));
    }
}
