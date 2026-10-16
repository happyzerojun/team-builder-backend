package com.capstone.backend.service.ai;

import com.capstone.backend.entity.TechStack;
import com.capstone.backend.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import com.capstone.backend.service.event.TechStackCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
 *
 * 실행 중 새로 생긴 기술 스택(프로필 태그 저장 시 자동 생성)은 재시작 전까지 맵에 없었다 (TS-20)
 * - TechStackCreatedEvent: 저장한 트랜잭션이 커밋된 뒤 다시 적재(롤백되면 적재하지 않음)
 * - 주기 재적재(기본 10분): 이벤트가 닿지 않는 변경(DB에서 직접 바꾼 keywords, 다른 서버 인스턴스에서 생긴 스택)을 따라잡는다
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

    /**
     * 커밋 후 실행. AFTER_COMMIT 시점에는 원래 트랜잭션이 이미 끝났으므로 조회를 새 읽기 전용 트랜잭션에서 한다.
     * fallbackExecution: 트랜잭션 밖에서 발행돼도 바로 적재한다
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onTechStackCreated(TechStackCreatedEvent event) {
        log.info("[TechKeywordMatcher] 새 기술 스택 '{}' 반영을 위해 다시 적재", event.name());
        reload();
    }

    @Scheduled(initialDelayString = "${app.tech-keywords.reload-interval-ms:600000}",
            fixedDelayString = "${app.tech-keywords.reload-interval-ms:600000}")
    public void reloadPeriodically() {
        try {
            reload();
        } catch (RuntimeException e) {
            // DB 일시 장애 등: 기존 맵을 그대로 쓰고 다음 주기에 다시 시도한다
            log.warn("[TechKeywordMatcher] 주기 재적재 실패, 기존 맵 유지: {}", e.getMessage());
        }
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
        int before = lookup.size();
        lookup = Map.copyOf(next);
        if (before != next.size()) {   // 주기 재적재가 변경 없을 때마다 로그를 남기지 않게
            log.info("[TechKeywordMatcher] 키워드 맵 적재 완료: {}개", next.size());
        }
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
