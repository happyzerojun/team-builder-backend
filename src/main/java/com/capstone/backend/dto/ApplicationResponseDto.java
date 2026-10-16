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
    private String applicantName;  // ← 추가
    private Long projectId;
    private String projectTitle;   // ← 추가
    // 마이페이지 지원 현황·참여 중 목록이 전체 모집글 목록을 다시 받아 맞춰 보지 않도록 함께 내려준다 (TS-17)
    private String projectStatus;
    private String projectRegion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}