package com.capstone.backend.service;

import com.capstone.backend.dto.AiRecommendResponse;
import com.capstone.backend.dto.RecommendationDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AiRecommendResponse getRecommendation(String userPrompt) {

        // 🔥 현재는 테스트용 유저 ID 고정
        Long userId = 2L;

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 유저 기술스택 문자열 생성
        String userTechStacks = user.getUserTechStacks().stream()
                .map(uts -> uts.getTechStack().getName())
                .collect(Collectors.joining(", "));

        // 전체 프로젝트 조회
        List<Project> projects = projectRepository.findAll();

        // 프로젝트 목록 문자열 생성
        String projectData = projects.stream()
                .map(project -> String.format("""
                        제목: %s
                        지역: %s
                        모임방식: %s
                        지역제한: %s
                        설명: %s
                        """,
                        project.getTitle(),
                        project.getRegion(),
                        project.getMeetingType(),
                        project.getIsLocalOnly(),
                        project.getContent()
                ))
                .collect(Collectors.joining("\n\n"));

        // 🔥 AI에게 전달할 최종 데이터
        String finalPrompt = """
                [유저 정보]
                지역: %s
                기술스택: %s

                [프로젝트 목록]
                %s

                [사용자 요청]
                %s
                """.formatted(
                user.getRegion(),
                userTechStacks,
                projectData,
                userPrompt
        );

        // 콘솔 출력 (팀원 테스트용)
        System.out.println(finalPrompt);

        // 🔥 현재는 더미 추천 결과 반환
        List<RecommendationDto> recommendations = projects.stream()
                .limit(5)
                .map(project -> RecommendationDto.builder()
                        .project_id(project.getId())
                        .title(project.getTitle())
                        .matching_score((int) (Math.random() * 30) + 70)
                        .reason(generateReason(project))
                        .build())
                .collect(Collectors.toList());

        return AiRecommendResponse.builder()
                .recommendations(recommendations)
                .build();
    }

    private String generateReason(Project project) {

        String meetingText =
                "대면".equals(project.getMeetingType())
                        ? project.getRegion() + " 지역 기반 대면 프로젝트입니다."
                        : "비대면으로 자유롭게 참여 가능합니다.";

        return meetingText + " 현재 모집중이며 기술 스택과의 연관성이 높습니다.";
    }
}