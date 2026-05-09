package com.capstone.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class ApplicationRequestDto {

    @Getter
    @NoArgsConstructor
    public static class Create {
        private Long project_id;     // 카멜케이스(projectId)로 변경하고 @JsonProperty 적용을 권장하지만, 프론트엔드 키 값에 맞췄습니다.
        private Long applicant_id;
        private String support_role;
        private String message;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateStatus {
        private String status; // "accepted" or "rejected"
    }
}