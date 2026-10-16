package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewBulkSaveRequestDto {
    @JsonProperty("project_id")
    private Long projectId;

    // 하위 호환용으로 받기만 하고 쓰지 않는다. 작성자는 JWT의 사용자 (TS-05)
    @JsonProperty("reviewer_id")
    private Long reviewerId;

    @JsonProperty("reviewee_id")
    private Long revieweeId;

    private Integer rating;
    private String comment;
}