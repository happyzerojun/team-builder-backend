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

    /**
     * 선택: 포지션별 모집 인원 (TS-19)
     * - 생성: 비우면 정원 제한 없는 글
     * - 수정: null이면 유지, 목록이면 그 목록으로 맞춘다(이름이 같은 포지션은 인원만 변경, 빠진 포지션 삭제, 새 포지션 추가).
     *   합류 인원보다 적게 줄이거나 합류한 팀원이 있는 포지션을 지우면 409
     */
    @Valid
    @Size(max = 10, message = "포지션은 10개까지 등록할 수 있습니다.")
    private List<PositionRequestDto> positions;
}
