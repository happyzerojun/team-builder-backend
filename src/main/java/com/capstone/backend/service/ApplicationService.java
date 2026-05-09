package com.capstone.backend.service;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
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
    public ApplicationResponseDto createApplication(ApplicationRequestDto.Create requestDto) {
        // 이미 지원한 내역인지 검증 (선택)
        if (applicationRepository.existsByApplicantIdAndProjectId(requestDto.getApplicant_id(), requestDto.getProject_id())) {
            throw new IllegalArgumentException("이미 지원한 프로젝트입니다.");
        }

        // 🚨 객체 찾기: 전달받은 ID로 DB에서 실제 User와 Project 엔티티를 꺼내옵니다.
        User applicant = userRepository.findById(requestDto.getApplicant_id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Project project = projectRepository.findById(requestDto.getProject_id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // DTO -> Entity 변환
        Application application = Application.builder()
                .project(project)       // 🚨 ID가 아닌 객체를 넣음
                .applicant(applicant)   // 🚨 ID가 아닌 객체를 넣음
                .supportRole(requestDto.getSupport_role())
                .message(requestDto.getMessage())
                .status("pending")
                .build();

        // DB에 저장
        Application savedApplication = applicationRepository.save(application);

        return convertToDto(savedApplication);
    }

    /**
     * 2. 지원 취소하기
     */
    @Transactional
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지원서입니다. ID: " + applicationId));

        applicationRepository.delete(application);
    }

    /**
     * 3. 유저별 지원 내역 조회
     */
    public List<ApplicationResponseDto> getApplicationsByUserId(Long userId) {
        List<Application> applications = applicationRepository.findByApplicantId(userId);
        return applications.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 4. 프로젝트별 지원자 목록 조회
     */
    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId) {
        List<Application> applications = applicationRepository.findByProjectId(projectId);
        return applications.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 5. 지원서 상태 변경 (수락/거절)
     */
    @Transactional
    public ApplicationResponseDto updateStatus(Long applicationId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지원서입니다. ID: " + applicationId));

        application.updateStatus(status);
        return convertToDto(application);
    }

    /**
     * 🚨 Entity를 DTO로 변환하는 공통 내부 메서드 (빨간 줄 해결 핵심!)
     */
    private ApplicationResponseDto convertToDto(Application application) {
        return ApplicationResponseDto.builder()
                .applicationId(application.getId()) // getApplicationId() -> getId() 로 수정
                .supportRole(application.getSupportRole())
                .message(application.getMessage())
                .status(application.getStatus())
                .applicantId(application.getApplicant().getId()) // getApplicantId() -> getApplicant().getId() 로 수정
                .projectId(application.getProject().getId())     // getProjectId() -> getProject().getId() 로 수정
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}