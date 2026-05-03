package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {

    private String name;
    private String nickname;
    private String jobRole;
    private String organization;
    private String introduction;

    // 🚨 프론트에서 "tags": ["Java"] 로 보내면 백엔드의 "techStacks" 리스트에 쏙 들어갑니다!
    @JsonProperty("tags")
    private List<String> techStacks;

    // Base64로 인코딩된 이미지 문자열을 받을 곳
    private String profileImg;
}