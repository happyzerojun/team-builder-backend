package com.capstone.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UserProfileUpdateRequestDto {
    // 프론트의 payload 키값과 100% 일치해야 합니다!
    private String name;
    private String nickname;
    private String jobRole;    // 프론트 job_role과 매핑
    private String organization;
    private String introduction;
    private List<String> tags;  // 배열 데이터는 List로!
    private String profileImg;  // Base64 긴 텍스트
}