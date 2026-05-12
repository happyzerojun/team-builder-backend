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
        List<String> initialStacks = Arrays.asList(
                "React",
                "Vue",
                "Angular",
                "Next.js",
                "Svelte",
                "TypeScript",
                "JavaScript",
                "HTML/CSS",
                "Tailwind",

                "Spring Boot",
                "Node.js",
                "Django",
                "FastAPI",
                "Flask",
                "Express",
                "NestJS",
                "Java",
                "Python",
                "Go",
                "Kotlin",

                "MySQL",
                "PostgreSQL",
                "MongoDB",
                "Redis",
                "Oracle",
                "SQLite",
                "Firebase",

                "AWS",
                "Docker",
                "Kubernetes",
                "CI/CD",
                "GCP",
                "Azure",
                "Nginx",
                "Linux",

                "Git",
                "Figma",
                "Unity",
                "Flutter",
                "React Native",
                "Swift",
                "Kotlin(Android)",
                "Kotlin(iOS)"
        );

        System.out.println("🛠️ [데이터 초기화 시작] 기술 스택 체크 중...");

        for (String stackName : initialStacks) {
            if (!techStackRepository.existsByName(stackName)) {
                TechStack stack = TechStack.builder()
                        .name(stackName)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                techStackRepository.save(stack);
                System.out.println("🚀 [신규 등록] " + stackName);
            }
        }

        System.out.println("✅ [초기화 완료] 모든 기술 스택이 준비되었습니다.");
    }
}