package com.capstone.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty; // 👈 이 임포트가 꼭 있어야 해요!

@Getter
@Builder
public class ApplicationResponseDto {
    private Long applicationId;
    private String supportRole;
    private String message;
    private String status;
    private Long applicantId;
    private String applicantName;  // ← 추가
    
    @JsonProperty("project_id")    // 👈 프론트에는 project_id로 나가지만, 자바 컴파일은 안 깨지게!
    private Long projectId;        // 👈 project_id로 고쳤던 걸 다시 'projectId'로 원상복구!
    
    private String projectTitle;   // ← 추가
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}