package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewBulkSaveRequestDto {
    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("reviewer_id")
    private Long reviewerId;

    @JsonProperty("reviewee_id")
    private Long revieweeId;

    private Integer rating;
    private String comment;
}