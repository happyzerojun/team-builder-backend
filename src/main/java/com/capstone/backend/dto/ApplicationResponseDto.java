package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty; 
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponseDto {
    
    @JsonProperty("application_id") // 👈 2. 리액트의 app.application_id와 매칭
    private Long applicationId;
    
    private String supportRole;
    private String message;
    private String status;
    private Long applicantId;
    private String applicantName;  
    
    @JsonProperty("project_id")     // 👈 3. 리액트의 app.project_id와 매칭
    private Long projectId;
    
    private String projectTitle;  
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}