package com.capstone.backend.service.ai;

import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-11: AI가 돌려준 ID는 "DB에 존재"가 아니라 "이번 요청의 후보였음"까지 확인한다 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI 응답 검증")
class AiCandidateReaderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TechKeywordMatcher techKeywordMatcher;

    private AiCandidateReader reader() {
        return new AiCandidateReader(userRepository, projectRepository, techKeywordMatcher);
    }

    @Test
    @DisplayName("후보 집합 밖의 ID는 DB에 있더라도 버린다")
    void dropsIdsOutsideCandidates() {
        List<AiResult> results = AiCandidateReader.sanitize(List.of(
                new AiResult(1L, 90, "후보"),
                new AiResult(999L, 99, "DB에는 있지만 후보가 아니었던 글")), Set.of(1L, 2L));

        assertThat(results).extracting(AiResult::projectId).containsExactly(1L);
    }

    @Test
    @DisplayName("중복 ID 제거(첫 항목 유지), 최대 3건, 점수 0~100 보정, null 무시")
    void dedupesLimitsAndClamps() {
        List<AiResult> results = AiCandidateReader.sanitize(List.of(
                new AiResult(1L, 150, "a"),
                new AiResult(1L, 10, "중복"),
                new AiResult(null, 50, "id 없음"),
                new AiResult(2L, -5, "b"),
                new AiResult(3L, null, "c"),
                new AiResult(4L, 80, "4번째")), Set.of(1L, 2L, 3L, 4L));

        assertThat(results).containsExactly(
                new AiResult(1L, 100, "a"),
                new AiResult(2L, 0, "b"),
                new AiResult(3L, 0, "c"));
    }

    @Test
    @DisplayName("후보 밖 ID만 있으면 DB를 조회하지 않는다")
    void doesNotQueryWhenNothingSurvives() {
        List<RecommendationDto> recommendations = reader().resolve(
                List.of(new AiResult(999L, 90, "환각")), Set.of(1L));

        assertThat(recommendations).isEmpty();
        verify(projectRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("제목은 AI가 아닌 DB 값, 재조회 사이에 삭제된 글은 제외")
    void resolvesTitlesFromDb() {
        Project project = Project.builder().id(1L).title("DB 제목").build();
        when(projectRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(project));

        List<RecommendationDto> recommendations = reader().resolve(List.of(
                new AiResult(1L, 90, "이유"),
                new AiResult(2L, 80, "그새 삭제됨")), Set.of(1L, 2L));

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getTitle()).isEqualTo("DB 제목");
        assertThat(recommendations.get(0).getMatching_score()).isEqualTo(90);
    }
}
