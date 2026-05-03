package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("tags") // 🚨 프론트에서 넘어오는 JSON의 "tags" 키를 이 필드에 매핑
    private List<String> techStacks;
}