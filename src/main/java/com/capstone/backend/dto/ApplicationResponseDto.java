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
    private String applicantName;
    private Long projectId;
    private String projectTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}