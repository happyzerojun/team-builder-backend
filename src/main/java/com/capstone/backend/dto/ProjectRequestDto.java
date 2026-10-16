package com.capstone.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

/**
 * 모집글 생성/수정 요청.
 * - leader_id는 받지 않는다. 작성자는 JWT의 사용자 (TS-05)
 * - status는 받지 않는다. 새 글은 모집중, 변경은 PATCH /status (TS-09)
 * - 필수 값이 비면 DB NOT NULL 위반이 "중복된 데이터"(409)로 보이던 것을 400으로 먼저 막는다
 */
@Getter
public class ProjectRequestDto {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "프로젝트 내용을 입력해주세요.")
    private String content;

    @NotBlank(message = "지역을 선택해주세요.")
    @Size(max = 30, message = "지역은 30자 이하로 입력해주세요.")
    private String region;

    @NotNull(message = "프로젝트 기간을 선택해주세요.")
    @Positive(message = "프로젝트 기간은 1개월 이상이어야 합니다.")
    private Integer term;

    private String meetingType;
    private Boolean isLocalOnly;
    private List<Long> techStackIds;

    /** 선택: 포지션별 모집 인원. 생성 시에만 반영되고, 수정 요청에서는 무시한다 */
    @Valid
    private List<PositionRequestDto> positions;
}
