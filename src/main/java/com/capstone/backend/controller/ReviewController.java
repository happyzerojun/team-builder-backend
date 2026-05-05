package com.capstone.backend.controller;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 특정 프로젝트에서 내가 작성한 리뷰 조회
    @GetMapping("/project/{projectId}/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewResponseDto>> getProjectMyReviews(
            @PathVariable Long projectId,
            @PathVariable Long reviewerId) {
        List<ReviewResponseDto> reviews = reviewService.getMyReviewsForProject(projectId, reviewerId);
        return ResponseEntity.ok(reviews);
    }

    // 2. 리뷰 다중 저장
    @PostMapping("/bulk")
    public ResponseEntity<String> saveProjectReviews(@RequestBody List<ReviewBulkSaveRequestDto> reviews) {
        reviewService.saveBulkReviews(reviews);
        return ResponseEntity.ok("Reviews saved successfully");
    }

    // 3. 내가 받은 리뷰 조회 (마이페이지용)
    @GetMapping("/received/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> getMyReceivedReviews(@PathVariable Long userId) {
        List<ReviewResponseDto> reviews = reviewService.getReceivedReviews(userId);
        return ResponseEntity.ok(reviews);
    }
}