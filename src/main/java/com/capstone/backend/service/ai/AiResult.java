package com.capstone.backend.service.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Gemini가 돌려주는 추천 한 건. 신뢰하지 않는 입력이라 AiCandidateReader.sanitize()를 거친다 */
public record AiResult(
        @JsonProperty("project_id") Long projectId,
        @JsonProperty("matching_score") Integer matchingScore,
        @JsonProperty("reason") String reason
) {
}
