package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewBulkSaveRequestDto {
    @NotNull(message = "project_id는 필수입니다.")
    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("reviewer_id")
    private Long reviewerId;

    @NotNull(message = "reviewee_id는 필수입니다.")
    @JsonProperty("reviewee_id")
    private Long revieweeId;

    @NotNull(message = "rating은 필수입니다.")
    @Min(value = 1, message = "rating은 1 이상이어야 합니다.")
    @Max(value = 5, message = "rating은 5 이하여야 합니다.")
    private Integer rating;

    @Size(max = 500, message = "comment는 500자 이하여야 합니다.")
    private String comment;
}
