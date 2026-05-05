package com.capstone.backend.dto;

import com.capstone.backend.entity.Review;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

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

    private Integer rating;
    private String comment;

    // Entity를 DTO로 변환하는 편의 메서드
    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .projectId(review.getProject().getId()) // Project 엔티티의 ID (getter 이름에 맞게 수정 필요할 수 있음)
                .reviewerId(review.getReviewer().getId()) // User 엔티티의 ID
                .revieweeId(review.getReviewee().getId()) // User 엔티티의 ID
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }
}