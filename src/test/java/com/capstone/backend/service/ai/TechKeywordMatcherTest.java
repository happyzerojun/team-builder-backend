package com.capstone.backend.service.ai;

import com.capstone.backend.entity.TechStack;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.service.event.TechStackCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-10: 키워드 맵이 DataInitializer보다 먼저 만들어져 비어 버리던 문제 */
@ExtendWith(MockitoExtension.class)
@DisplayName("기술 스택 키워드 매칭")
class TechKeywordMatcherTest {

    @Mock
    private TechStackRepository techStackRepository;

    private static TechStack stack(long id, String name, String keywords) {
        return TechStack.builder().id(id).name(name).keywords(keywords).build();
    }

    @Test
    @DisplayName("기동 시점에 비어 있었어도 첫 요청에서 지연 로드한다")
    void lazilyLoadsWhenEmpty() {
        // 빈 DB로 기동: ApplicationReadyEvent 시점에는 아직 아무것도 없다고 가정
        when(techStackRepository.findAll())
                .thenReturn(List.of())
                .thenReturn(List.of(stack(10L, "Spring Boot", "스프링,스프링부트")));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();
        assertThat(matcher.size()).isZero();

        assertThat(matcher.match("스프링 프로젝트 찾아줘")).contains(10L);
        assertThat(matcher.match("springboot 쓰는 곳")).contains(10L);
        verify(techStackRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("실행 중 새 기술 스택이 생기면(이벤트) 다시 적재해 바로 매칭된다 (TS-20)")
    void reloadsOnTechStackCreated() {
        when(techStackRepository.findAll())
                .thenReturn(List.of(stack(1L, "Java", null)))
                .thenReturn(List.of(stack(1L, "Java", null), stack(99L, "Zig", "지그")));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();
        assertThat(matcher.match("zig 프로젝트")).isEmpty();

        matcher.onTechStackCreated(new TechStackCreatedEvent(99L, "Zig"));

        assertThat(matcher.match("zig 프로젝트")).contains(99L);
        assertThat(matcher.match("지그 쓰는 팀")).contains(99L);
    }

    @Test
    @DisplayName("주기 재적재가 실패하면 기존 맵을 그대로 쓴다")
    void periodicReloadFailureKeepsPreviousMap() {
        when(techStackRepository.findAll())
                .thenReturn(List.of(stack(1L, "Java", null)))
                .thenThrow(new IllegalStateException("DB down"));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();

        matcher.reloadPeriodically();

        assertThat(matcher.match("java")).contains(1L);
    }

    @Test
    @DisplayName("이미 적재돼 있으면 요청마다 DB를 조회하지 않는다")
    void doesNotReloadWhenLoaded() {
        when(techStackRepository.findAll()).thenReturn(List.of(stack(1L, "Java", null)));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();

        matcher.match("java");
        matcher.match("java");
        verify(techStackRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("가장 긴 키워드가 이긴다: javascript 요청에 java가 걸리지 않음")
    void longestKeywordWins() {
        when(techStackRepository.findAll()).thenReturn(List.of(
                stack(1L, "Java", null),
                stack(2L, "JavaScript", "자바스크립트")));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();

        assertThat(matcher.match("JavaScript 프로젝트")).contains(2L);
        assertThat(matcher.match("자바스크립트")).contains(2L);
        assertThat(matcher.match("Java 백엔드")).contains(1L);
    }

    @Test
    @DisplayName("빈 키워드(\"a,,b\")는 모든 문장에 걸리므로 무시한다")
    void ignoresBlankKeywords() {
        when(techStackRepository.findAll()).thenReturn(List.of(stack(3L, "Go", "golang,, ")));
        TechKeywordMatcher matcher = new TechKeywordMatcher(techStackRepository);
        matcher.onApplicationReady();

        assertThat(matcher.match("파이썬 프로젝트")).isEmpty();
        assertThat(matcher.match(null)).isEmpty();
    }
}
