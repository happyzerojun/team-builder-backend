package com.capstone.backend.controller;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 특정 프로젝트에서 내가 작성한 리뷰 조회
    @GetMapping("/project/{projectId}/me")
    public ResponseEntity<List<ReviewResponseDto>> getProjectMyReviews(
            @PathVariable Long projectId,
            Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getMyReviewsForProject(projectId, authentication.getName());
        return ResponseEntity.ok(reviews);
    }

    // 2. 리뷰 다중 저장
    @PostMapping("/bulk")
    public ResponseEntity<String> saveProjectReviews(@RequestBody List<ReviewBulkSaveRequestDto> reviews,
                                                      Authentication authentication) {
        reviewService.saveBulkReviews(reviews, authentication.getName());
        return ResponseEntity.ok("Reviews saved successfully");
    }

    // 3. 내가 받은 리뷰 조회 (마이페이지용)
    @GetMapping("/received/me")
    public ResponseEntity<List<ReviewResponseDto>> getMyReceivedReviews(Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getReceivedReviews(authentication.getName());
        return ResponseEntity.ok(reviews);
    }
}
