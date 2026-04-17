package com.capstone.backend.dto;

import lombok.Getter;

@Getter
public class UserProfileUpdateRequestDto {
    private String experienceLevel; // 예: BEGINNER, INTERMEDIATE 등
    private String githubUrl;
    private String baekjoonId;
}