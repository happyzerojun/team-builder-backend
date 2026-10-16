package com.capstone.backend.entity;

import java.util.Arrays;

/**
 * 모집글 상태의 단일 정의.
 * DB와 API 응답에는 기존 데이터·프론트와 호환되도록 한글 라벨("모집중" 등)을 그대로 저장/반환하고,
 * 코드에서는 이 enum만 사용한다. 이전에는 "모집중" 리터럴이 서비스·더미 데이터에 흩어져 있고
 * 엔티티 주석은 RECRUITING을 말하고 있어 어떤 값이 맞는지 코드만 보고 알 수 없었다 (TS-09)
 */
public enum ProjectStatus {
    RECRUITING("모집중"),
    PROCEEDING("진행중"),
    COMPLETED("종료됨");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    /** DB 저장값이자 API 응답값 */
    public String label() {
        return label;
    }

    /**
     * 한글 라벨("모집중", "모집 중") 또는 enum 이름("RECRUITING", 대소문자 무시)을 받아들인다.
     * 알 수 없는 값은 IllegalArgumentException(400).
     */
    public static ProjectStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("모집 상태 값이 필요합니다.");
        }
        String normalized = raw.replace(" ", "").trim();
        return Arrays.stream(values())
                .filter(s -> s.label.equals(normalized) || s.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 모집 상태입니다: " + raw));
    }

    /** 모집중 → 진행중 → 종료됨, 한 단계씩 앞으로만 */
    public boolean canMoveTo(ProjectStatus next) {
        return next.ordinal() == this.ordinal() + 1;
    }
}
