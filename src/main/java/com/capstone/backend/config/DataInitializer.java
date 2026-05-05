package com.capstone.backend.config;

import com.capstone.backend.entity.TechStack;
import com.capstone.backend.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TechStackRepository techStackRepository;

    @Override
    public void run(String... args) {
        // 원래 data.sql에 있던 12개 전체 목록 + Docker 추가
        List<String> initialStacks = Arrays.asList(
                "Java", "Spring Boot", "Python", "Node.js", "JavaScript",
                "TypeScript", "React", "Vue.js", "Flutter", "AWS",
                "MySQL", "Figma", "Docker"
        );

        System.out.println("🛠️ [데이터 초기화 시작] 기술 스택 체크 중...");

        for (String stackName : initialStacks) {
            // DB에 해당 이름이 없을 때만 저장 (중복 에러 방지 핵심)
            if (!techStackRepository.existsByName(stackName)) {
                TechStack stack = TechStack.builder()
                        .name(stackName)
                        .createdAt(LocalDateTime.now()) // NOW() 대체
                        .updatedAt(LocalDateTime.now())
                        .build();

                techStackRepository.save(stack);
                System.out.println("🚀 [신규 등록] " + stackName);
            }
        }
        System.out.println("✅ [초기화 완료] 모든 기술 스택이 준비되었습니다.");
    }
}