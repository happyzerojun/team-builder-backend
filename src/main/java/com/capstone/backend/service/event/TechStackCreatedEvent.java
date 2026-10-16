package com.capstone.backend.service.event;

/**
 * 실행 중 새 기술 스택이 저장됨(프로필 태그 저장 시 자동 생성 등).
 * TechKeywordMatcher가 커밋 후 키워드 맵을 다시 적재한다 (TS-20)
 */
public record TechStackCreatedEvent(Long techStackId, String name) {
}
