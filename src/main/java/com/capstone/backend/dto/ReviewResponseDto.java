package com.capstone.backend.dto;

import com.capstone.backend.entity.Review;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    @JsonProperty("review_id")
    private Long reviewId;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("reviewer_id")
    private Long reviewerId;

    @JsonProperty("reviewee_id")
    private Long revieweeId;

    @JsonProperty("reviewer_name")
    private String reviewerName;

    @JsonProperty("reviewee_name")
    private String revieweeName;

    @JsonProperty("project_title")
    private String projectTitle;

    private Integer rating;
    private String comment;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .projectId(review.getProject().getId())
                .reviewerId(review.getReviewer().getId())
                .revieweeId(review.getReviewee().getId())
                .reviewerName(resolveDisplayName(review.getReviewer().getNickname(), review.getReviewer().getName()))
                .revieweeName(resolveDisplayName(review.getReviewee().getNickname(), review.getReviewee().getName()))
                .projectTitle(review.getProject().getTitle())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private static String resolveDisplayName(String nickname, String name) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        return name;
    }
}
