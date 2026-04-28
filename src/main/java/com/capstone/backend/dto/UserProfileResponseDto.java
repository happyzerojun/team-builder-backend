package com.capstone.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class UserProfileResponseDto {
    private String email;
    private String name;
    private String nickname;
    private String jobRole;
    private String organization;
    private String introduction;
    private String profileImg;

    // 🚨 TechStack 객체가 아니라 문자열 리스트로 반환 (무한루프 방지)
    private List<String> techStacks;
}