package com.capstone.backend.service.ai;

import com.capstone.backend.entity.Project;

import java.util.stream.Collectors;

/**
 * Gemini 프롬프트에 넣을 후보 모집글 스냅샷.
 * 트랜잭션 안에서 엔티티를 값 객체로 바꿔 두어, 트랜잭션 밖(Gemini 대기 중)에서 지연 로딩이 일어나지 않게 한다 (TS-12)
 */
public record ProjectCandidate(Long id, String title, String stacks, String term, String content) {

    /** 긴 본문이 프롬프트(토큰 비용·지연)를 키우지 않도록 자른다 */
    static final int MAX_CONTENT_LENGTH = 300;

    public static ProjectCandidate from(Project p) {
        String stacks = p.getProjectTechStacks().isEmpty() ? "지정안됨"
                : p.getProjectTechStacks().stream()
                        .map(pts -> pts.getTechStack().getName())
                        .collect(Collectors.joining(", "));
        String content = p.getContent() == null ? "" : p.getContent();
        if (content.length() > MAX_CONTENT_LENGTH) {
            content = content.substring(0, MAX_CONTENT_LENGTH) + "…";
        }
        return new ProjectCandidate(p.getId(), p.getTitle(), stacks, p.getTerm(), content);
    }

    public String toPromptLine() {
        return String.format("ID: %d | 제목: %s | 스택: %s | 기간: %s | 내용: %s", id, title, stacks, term, content);
    }
}
