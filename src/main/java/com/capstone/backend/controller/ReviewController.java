package com.capstone.backend.controller;

import com.capstone.backend.dto.ReviewBulkSaveRequestDto;
import com.capstone.backend.dto.ReviewResponseDto;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.service.ReviewService;
import jakarta.validation.Valid;
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

    @GetMapping("/project/{projectId}/mine")
    public ResponseEntity<List<ReviewResponseDto>> getProjectMyReviews(
            @PathVariable Long projectId,
            Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getMyReviewsForProject(projectId, authenticatedEmail(authentication), null);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/project/{projectId}/reviewer/{reviewerId:\\d+}")
    public ResponseEntity<List<ReviewResponseDto>> getProjectMyReviewsLegacy(
            @PathVariable Long projectId,
            @PathVariable Long reviewerId,
            Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getMyReviewsForProject(
                projectId,
                authenticatedEmail(authentication),
                reviewerId
        );
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> saveProjectReviews(
            @RequestBody List<@Valid ReviewBulkSaveRequestDto> reviews,
            Authentication authentication) {
        reviewService.saveBulkReviews(reviews, authenticatedEmail(authentication));
        return ResponseEntity.ok("Reviews saved successfully");
    }

    @GetMapping("/received/me")
    public ResponseEntity<List<ReviewResponseDto>> getMyReceivedReviews(Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getReceivedReviews(authenticatedEmail(authentication), null);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/received/{userId:\\d+}")
    public ResponseEntity<List<ReviewResponseDto>> getMyReceivedReviewsLegacy(
            @PathVariable Long userId,
            Authentication authentication) {
        List<ReviewResponseDto> reviews = reviewService.getReceivedReviews(authenticatedEmail(authentication), userId);
        return ResponseEntity.ok(reviews);
    }

    private String authenticatedEmail(Authentication authentication) {
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }
        return authentication.getName();
    }
}
