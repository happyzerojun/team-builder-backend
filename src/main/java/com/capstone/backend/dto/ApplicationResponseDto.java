package com.capstone.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponseDto {
    private Long applicationId;
    private String supportRole;
    private String message;
    private String status;
    private Long applicantId;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드 예시
    // public static ApplicationResponseDto from(Application application) { ... }
}