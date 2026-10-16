package com.capstone.backend.service.ai;

import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 추천의 DB 구간(트랜잭션)만 담당한다.
 *
 * AiService가 같은 클래스 안에서 @Transactional 메서드를 부르면 프록시를 거치지 않아 트랜잭션이 걸리지 않는다.
 * 그래서 DB 구간을 별도 빈으로 분리했다: 후보 조회(트랜잭션 1) → Gemini 호출(트랜잭션 없음) → 결과 재조회(트랜잭션 2) (TS-12)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiCandidateReader {

    static final int MAX_CANDIDATES = 50;
    static final int MAX_RECOMMENDATIONS = 3;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TechKeywordMatcher techKeywordMatcher;

    /** 요청 문장의 기술 스택이 쓰인 최신 모집글 최대 50건, 없으면 전체 최신 50건 */
    public List<ProjectCandidate> loadCandidates(String email, String userPrompt) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요."));

        PageRequest latest = PageRequest.of(0, MAX_CANDIDATES, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Project> projects = techKeywordMatcher.match(userPrompt)
                .map(techStackId -> projectRepository.findByTechStackId(techStackId, latest).getContent())
                .orElse(List.of());
        if (projects.isEmpty()) {
            projects = projectRepository.findAll(latest).getContent();
        }
        return projects.stream().map(ProjectCandidate::from).toList();
    }

    /**
     * Gemini 응답 중 이번 요청의 후보였던 ID만 남기고, DB에서 다시 읽어 제목을 채운다.
     * findAllById만으로는 "DB에는 있지만 후보가 아니었던" 프로젝트(다른 스택, 오래된 글)를 AI가 지어내도 통과했다 (TS-11)
     */
    public List<RecommendationDto> resolve(List<AiResult> aiResults, Set<Long> candidateIds) {
        List<AiResult> accepted = sanitize(aiResults, candidateIds);
        if (accepted.isEmpty()) {
            return List.of();
        }
        Map<Long, Project> projects = projectRepository.findAllById(
                        accepted.stream().map(AiResult::projectId).toList())
                .stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        return accepted.stream()
                .filter(r -> projects.containsKey(r.projectId()))   // Gemini 대기 중 삭제된 글
                .map(r -> RecommendationDto.builder()
                        .project_id(r.projectId())
                        .title(projects.get(r.projectId()).getTitle())   // 제목은 AI가 아닌 DB 값
                        .matching_score(r.matchingScore())
                        .reason(r.reason())
                        .build())
                .toList();
    }

    /**
     * 후보 ID 집합 밖의 ID 제거, 중복 제거(첫 항목 유지), 최대 3건, 점수 0~100 보정.
     */
    static List<AiResult> sanitize(List<AiResult> aiResults, Set<Long> candidateIds) {
        if (aiResults == null || aiResults.isEmpty() || candidateIds == null || candidateIds.isEmpty()) {
            return List.of();
        }
        Map<Long, AiResult> unique = new LinkedHashMap<>();
        for (AiResult r : aiResults) {
            if (r == null || r.projectId() == null) {
                continue;
            }
            if (!candidateIds.contains(r.projectId())) {
                log.warn("[AI] 후보에 없던 project_id {} 응답을 버림", r.projectId());
                continue;
            }
            int score = r.matchingScore() == null ? 0 : Math.max(0, Math.min(100, r.matchingScore()));
            unique.putIfAbsent(r.projectId(), new AiResult(r.projectId(), score, r.reason()));
            if (unique.size() == MAX_RECOMMENDATIONS) {
                break;
            }
        }
        return new ArrayList<>(unique.values());
    }
}
