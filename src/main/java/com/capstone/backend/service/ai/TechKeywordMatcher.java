package com.capstone.backend.service.ai;

import com.capstone.backend.entity.TechStack;
import com.capstone.backend.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 사용자 요청 문장에서 기술 스택을 찾는 키워드 → tech_stack_id 맵.
 *
 * 이전에는 AiService의 @PostConstruct에서 맵을 만들었는데, @PostConstruct는 빈 생성 시점이라
 * 기술 스택을 넣는 DataInitializer(CommandLineRunner)보다 먼저 돌았다.
 * 빈 DB로 처음 기동하면 맵이 비어 재시작 전까지 키워드 매칭이 전혀 되지 않았다 (TS-10)
 *
 * - ApplicationReadyEvent: 모든 CommandLineRunner가 끝난 뒤 적재
 * - 그래도 비어 있으면 첫 요청에서 한 번 더 적재(지연 로드)
 * - 맵은 통째로 교체(불변 스냅샷)해 적재 중인 요청이 반쯤 찬 맵을 보지 않게 한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TechKeywordMatcher {

    private final TechStackRepository techStackRepository;

    private volatile Map<String, Long> lookup = Map.of();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        reload();
    }

    public synchronized void reload() {
        Map<String, Long> next = new HashMap<>();
        for (TechStack stack : techStackRepository.findAll()) {
            putKeyword(next, stack.getName(), stack.getId());
            if (stack.getKeywords() != null) {
                for (String keyword : stack.getKeywords().split(",")) {
                    putKeyword(next, keyword, stack.getId());
                }
            }
        }
        lookup = Map.copyOf(next);
        log.info("[TechKeywordMatcher] 키워드 맵 적재 완료: {}개", next.size());
    }

    /** 가장 긴 키워드 우선. "javascript" 요청에 "java"가 걸리지 않는다 (TS-03) */
    public Optional<Long> match(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Optional.empty();
        }
        Map<String, Long> snapshot = lookup;
        if (snapshot.isEmpty()) {
            synchronized (this) {
                if (lookup.isEmpty()) {
                    reload();
                }
                snapshot = lookup;
            }
        }
        String normalizedPrompt = normalize(prompt);
        return snapshot.entrySet().stream()
                .filter(entry -> normalizedPrompt.contains(entry.getKey()))
                .max(Comparator.comparingInt((Map.Entry<String, Long> e) -> e.getKey().length())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getValue);
    }

    int size() {
        return lookup.size();
    }

    private static void putKeyword(Map<String, Long> map, String keyword, Long id) {
        if (keyword == null || id == null) {
            return;
        }
        String normalized = normalize(keyword);
        if (!normalized.isEmpty()) {          // "a,,b" 같은 빈 키워드는 모든 문장에 매칭되므로 제외
            map.putIfAbsent(normalized, id);
        }
    }

    static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace(" ", "").trim();
    }
}
