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

import java.util.List;
import java.util.Map;
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

    /** Gemini가 돌려준 ID를 DB에서 다시 읽어 실제로 존재하는 글만 남기고, 제목은 DB 값으로 채운다 */
    public List<RecommendationDto> resolve(List<AiResult> aiResults) {
        List<AiResult> accepted = aiResults.stream()
                .filter(r -> r != null && r.projectId() != null)
                .limit(MAX_RECOMMENDATIONS)
                .toList();
        if (accepted.isEmpty()) {
            return List.of();
        }
        Map<Long, Project> projects = projectRepository.findAllById(
                        accepted.stream().map(AiResult::projectId).toList())
                .stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        return accepted.stream()
                .filter(r -> projects.containsKey(r.projectId()))
                .map(r -> RecommendationDto.builder()
                        .project_id(r.projectId())
                        .title(projects.get(r.projectId()).getTitle())
                        .matching_score(r.matchingScore() == null ? 0 : r.matchingScore())
                        .reason(r.reason())
                        .build())
                .toList();
    }
}
