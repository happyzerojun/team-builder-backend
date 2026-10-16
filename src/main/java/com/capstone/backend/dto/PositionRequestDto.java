package com.capstone.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 모집 포지션(예: 백엔드 2명). 지원서의 support_role이 roleName과 같으면 그 포지션의 정원을 쓴다 (TS-06) */
@Getter
@NoArgsConstructor
public class PositionRequestDto {

    @NotBlank(message = "포지션 이름을 입력해주세요.")
    @Size(max = 30, message = "포지션 이름은 30자 이하로 입력해주세요.")
    private String roleName;

    @NotNull(message = "모집 인원을 입력해주세요.")
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다.")
    @Max(value = 100, message = "모집 인원은 100명 이하로 입력해주세요.")
    private Integer requiredCount;
}
