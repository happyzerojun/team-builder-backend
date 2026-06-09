package com.capstone.backend.service;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    // 🚨 User와 Project 엔티티를 찾기 위해 두 레포지토리가 추가로 필요합니다!
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * 1. 프로젝트 지원하기 (DB에 저장)
     */
    @Transactional
    public ApplicationResponseDto createApplication(ApplicationRequestDto.Create requestDto, String email) {
        User applicant = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        // 이미 지원한 내역인지 검증 (선택)
        if (applicationRepository.existsByApplicantIdAndProjectId(applicant.getId(), requestDto.getProject_id())) {
            throw new IllegalArgumentException("이미 지원한 프로젝트입니다.");
        }

        Project project = projectRepository.findById(requestDto.getProject_id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        if (project.getLeader().getId().equals(applicant.getId())) {
            throw new IllegalArgumentException("본인이 생성한 프로젝트에는 지원할 수 없습니다.");
        }

        // DTO -> Entity 변환
        Application application = Application.builder()
                .project(project)
                .applicant(applicant)
                .supportRole(requestDto.getSupport_role())
                .message(requestDto.getMessage())
                .experience(requestDto.getExperience())
                .contactType(requestDto.getContactType())
                .contactValue(requestDto.getContactValue())
                .status("PENDING")
                .build();

        // DB에 저장
        Application savedApplication = applicationRepository.save(application);

        return convertToDto(savedApplication);
    }

    /**
     * 2. 지원 취소하기
     */
    @Transactional
    public void deleteApplication(Long applicationId, String email) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지원서입니다. ID: " + applicationId));
        if (!application.getApplicant().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("본인의 지원서만 취소할 수 있습니다.");
        }

        applicationRepository.delete(application);
    }

    /**
     * 3. 유저별 지원 내역 조회
     */
    public List<ApplicationResponseDto> getApplicationsByUserEmail(String email) {
    User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    List<Application> applications = applicationRepository.findByApplicantId(user.getId());
    return applications.stream()
            .map(app -> {
                app.getApplicant().getName();
                app.getProject().getTitle();
                return convertToDto(app);
            })
            .collect(Collectors.toList());
}

    /**
     * 4. 프로젝트별 지원자 목록 조회
     */
    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId, String email) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
    requireLeader(project, email);
    List<Application> applications = applicationRepository.findByProjectId(projectId);
    return applications.stream()
            .map(app -> {
                // LAZY 로딩 강제 초기화
                app.getApplicant().getName();
                app.getProject().getTitle();
                return convertToDto(app);
            })
            .collect(Collectors.toList());
}

    /**
     * 5. 지원서 상태 변경 (수락/거절)
     */
    @Transactional
    public ApplicationResponseDto updateStatus(Long applicationId, String status, String email) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지원서입니다. ID: " + applicationId));
        requireLeader(application.getProject(), email);
        if (!List.of("ACCEPTED", "REJECTED").contains(status)) {
            throw new IllegalArgumentException("지원 상태가 올바르지 않습니다.");
        }

        application.updateStatus(status);
        return convertToDto(application);
    }

    private ApplicationResponseDto convertToDto(Application application) {
    return ApplicationResponseDto.builder()
            .applicationId(application.getId())
            .supportRole(application.getSupportRole())
            .message(application.getMessage())
            .status(application.getStatus())
            .applicantId(application.getApplicant().getId())
            .applicantName(application.getApplicant().getName())
            .projectId(application.getProject().getId())
            .projectTitle(application.getProject().getTitle())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
}

    private void requireLeader(Project project, String email) {
        if (!project.getLeader().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("프로젝트 팀장만 지원자를 관리할 수 있습니다.");
        }
    }
}
