package com.capstone.backend.service;

import com.capstone.backend.dto.ApplicationRequestDto;
import com.capstone.backend.dto.ApplicationResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.NotFoundException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 지원서 도메인. 모든 메서드는 JWT의 email로 요청자를 식별한다.
 * 이전에는 본문의 applicant_id·URL의 userId를 그대로 믿어 다른 사람 이름으로 지원하거나
 * 남의 지원서를 수락/삭제할 수 있었다(IDOR) (TS-05)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    static final String ACCEPTED = "ACCEPTED";

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PositionSeats positionSeats;

    /**
     * 1. 프로젝트 지원하기. 지원자는 토큰의 사용자 (본문의 applicant_id는 무시)
     */
    @Transactional
    public ApplicationResponseDto createApplication(String email, ApplicationRequestDto.Create requestDto) {
        User applicant = currentUser(email);
        if (requestDto.getProject_id() == null) {
            throw new IllegalArgumentException("지원할 프로젝트가 필요합니다.");
        }
        Project project = projectRepository.findById(requestDto.getProject_id())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다."));

        if (isLeader(project, applicant.getId())) {
            throw new ConflictException("본인이 작성한 모집글에는 지원할 수 없습니다.");
        }
        if (!project.isRecruiting()) {
            throw new ConflictException("모집이 마감된 프로젝트입니다.");
        }
        // 빠른 실패용 조회. 동시 요청은 UNIQUE(applicant_id, project_id)가 최종 차단한다 (TS-04)
        if (applicationRepository.existsByApplicantIdAndProjectId(applicant.getId(), project.getId())) {
            throw new ConflictException("이미 지원한 프로젝트입니다.");
        }
        // 포지션을 정해 둔 모집글이면 그 포지션 중 하나로, 정원이 남은 곳에만 지원할 수 있다 (TS-06)
        positionSeats.checkApplicable(project.getId(), requestDto.getSupport_role());

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

        return convertToDto(applicationRepository.save(application));
    }

    /**
     * 2. 지원 취소: 지원자 본인만
     */
    @Transactional
    public void deleteApplication(String email, Long applicationId) {
        User me = currentUser(email);
        Application application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 지원서입니다."));

        if (!Objects.equals(application.getApplicant().getId(), me.getId())) {
            throw new ForbiddenException("본인의 지원서만 취소할 수 있습니다.");
        }
        if (isLeader(application.getProject(), me.getId())) {
            throw new ConflictException("팀장은 자신의 모집글에서 빠질 수 없습니다.");
        }
        positionSeats.releaseIfAccepted(application);   // 수락된 팀원이 나가면 정원 -1 (TS-06)
        applicationRepository.delete(application);
    }

    /**
     * 3. 내 지원 내역 (토큰의 사용자)
     */
    public List<ApplicationResponseDto> getMyApplications(String email) {
        User me = currentUser(email);
        return applicationRepository.findByApplicantId(me.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * 3-1. 기존 경로(/user/{userId}) 호환: 본인 것만 조회 가능
     */
    public List<ApplicationResponseDto> getApplicationsByUserId(String email, Long userId) {
        User me = currentUser(email);
        if (!Objects.equals(me.getId(), userId)) {
            throw new ForbiddenException("다른 사용자의 지원 내역은 조회할 수 없습니다.");
        }
        return getMyApplications(email);
    }

    /**
     * 4. 프로젝트별 지원서 목록
     * - 팀장: 전체(대기·수락·거절)
     * - 수락된 팀원: 팀원(ACCEPTED) 지원서만 (대기 중인 지원자의 메시지는 노출하지 않음)
     * - 그 외: 403
     */
    public List<ApplicationResponseDto> getApplicationsByProjectId(String email, Long projectId) {
        User me = currentUser(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다."));

        List<Application> applications = applicationRepository.findByProjectId(projectId);
        if (isLeader(project, me.getId())) {
            return applications.stream().map(this::convertToDto).toList();
        }
        boolean member = applications.stream()
                .anyMatch(a -> Objects.equals(a.getApplicant().getId(), me.getId()) && isAccepted(a));
        if (!member) {
            throw new ForbiddenException("프로젝트 팀장 또는 팀원만 볼 수 있습니다.");
        }
        return applications.stream().filter(ApplicationService::isAccepted).map(this::convertToDto).toList();
    }

    /**
     * 5. 지원서 수락/거절: 모집글 작성자(팀장)만
     */
    @Transactional
    public ApplicationResponseDto updateStatus(String email, Long applicationId, String status) {
        User me = currentUser(email);
        // 지원서 행 잠금: 같은 지원서 수락 요청이 동시에 와도 한 건만 PENDING → ACCEPTED를 본다 (TS-06)
        Application application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 지원서입니다."));

        Project project = application.getProject();
        if (!isLeader(project, me.getId())) {
            throw new ForbiddenException("모집글 작성자만 지원서를 처리할 수 있습니다.");
        }

        boolean changed = application.updateStatus(status);
        if (changed && application.isAccepted()) {
            if (!project.isRecruiting()) {
                throw new ConflictException("모집 중인 프로젝트에서만 지원서를 수락할 수 있습니다.");
            }
            positionSeats.take(project.getId(), application.getSupportRole());
        }
        return convertToDto(application);
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }

    static boolean isLeader(Project project, Long userId) {
        return project.getLeader() != null && Objects.equals(project.getLeader().getId(), userId);
    }

    static boolean isAccepted(Application application) {
        return application.isAccepted();
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
}
