package com.capstone.backend.service;

import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.dto.PositionRequestDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.entity.ProjectStatus;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.NotFoundException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 모집글 도메인. 변경 API는 모두 모집글 작성자(팀장)만 호출할 수 있다 (TS-05)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    static final String LEADER_ROLE = "팀장";
    static final String MEMBER_ROLE = "팀원";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TechStackRepository techStackRepository;
    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final PositionSeats positionSeats;
    private final EntityManager entityManager;

    /**
     * 작성자는 토큰의 사용자(본문의 leader_id는 무시).
     * 작성자를 팀원(ACCEPTED, "팀장")으로 등록하는 것도 같은 트랜잭션에서 한다.
     * 이전에는 프론트가 글 생성 후 /members를 한 번 더 호출해, 두 번째 호출이 실패하면 팀장 없는 팀이 남았다.
     */
    @Transactional
    public ProjectResponseDto createProject(String email, ProjectRequestDto request) {
        User leader = currentUser(email);

        Project project = Project.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .region(request.getRegion())
                .status(ProjectStatus.RECRUITING.label()) // 새 글은 항상 모집중. 요청 본문의 status는 무시한다
                .term(String.valueOf(request.getTerm()))
                .leader(leader)
                .meetingType(request.getMeetingType())
                .isLocalOnly(request.getIsLocalOnly())
                .build();

        Project savedProject = projectRepository.save(project);

        // 연결 행은 cascade(ALL)로 함께 저장된다. 이전에는 연결 행만 따로 저장해 생성 응답의 techStacks가 비어 있었다
        if (request.getTechStackIds() != null) {
            savedProject.replaceTechStacks(findTechStacks(request.getTechStackIds()));
        }

        if (request.getPositions() != null && !request.getPositions().isEmpty()) {
            List<ProjectPosition> positions = normalizePositions(request.getPositions()).entrySet().stream()
                    .map(p -> ProjectPosition.builder()
                            .project(savedProject)
                            .roleName(p.getKey())
                            .requiredCount(p.getValue())
                            .currentCount(0)
                            .build())
                    .toList();
            savedProject.getProjectPositions().addAll(projectPositionRepository.saveAll(positions));
        }

        applicationRepository.save(Application.builder()
                .project(savedProject)
                .applicant(leader)
                .supportRole(LEADER_ROLE)
                .message("")
                .status(ApplicationService.ACCEPTED)
                .build());

        return ProjectResponseDto.from(savedProject);
    }

    public Page<ProjectResponseDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(ProjectResponseDto::from);
    }

    /** 토큰 사용자가 작성한 글만 (TS-17) */
    public Page<ProjectResponseDto> getMyProjects(String email, Pageable pageable) {
        User me = currentUser(email);
        return projectRepository.findByLeaderId(me.getId(), pageable)
                .map(ProjectResponseDto::from);
    }

    public ProjectResponseDto getProjectById(Long projectId) {
        return ProjectResponseDto.from(findProject(projectId));
    }

    @Transactional
    public ProjectResponseDto updateProject(String email, Long projectId, ProjectRequestDto request) {
        Project project = findProjectAsLeader(email, projectId);

        if (request.getPositions() != null) {
            syncPositions(project, normalizePositions(request.getPositions()));
            // 포지션 변경은 조건부 UPDATE/DELETE(영속성 컨텍스트를 거치지 않음)라, 반영한 뒤 모집글을 다시 읽어
            // 이후 변경과 응답이 오래된 포지션 값을 쓰지 않게 한다
            entityManager.flush();
            entityManager.clear();
            project = findProject(projectId);
        }

        project.updateTitle(request.getTitle());
        project.updateContent(request.getContent());
        project.updateRegion(request.getRegion());
        project.updateTerm(String.valueOf(request.getTerm()));
        project.updateMeetingType(request.getMeetingType());
        project.updateIsLocalOnly(request.getIsLocalOnly());
        // null이면 기존 스택 유지, 빈 목록이면 모두 해제 (TS-18)
        if (request.getTechStackIds() != null) {
            project.replaceTechStacks(findTechStacks(request.getTechStackIds()));
        }

        return ProjectResponseDto.from(project);
    }

    /**
     * 이전에는 아무것도 지우지 않고 "삭제 성공 (임시)"만 응답했다.
     * 리뷰는 project_id FK가 있어 먼저 지우고, 지원서·포지션·스택 연결은 cascade로 함께 삭제한다.
     */
    @Transactional
    public void deleteProject(String email, Long projectId) {
        Project project = findProjectAsLeader(email, projectId);
        reviewRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    public List<MemberResponseDto> getProjectMembers(Long projectId) {
        return applicationRepository.findByProjectIdAndStatus(projectId, ApplicationService.ACCEPTED)
                .stream()
                .map(app -> new MemberResponseDto(
                        app.getApplicant().getId(),
                        app.getApplicant().getName(),
                        app.getSupportRole()
                ))
                .toList();
    }

    @Transactional
    public ProjectResponseDto updateProjectStatus(String email, Long projectId, String status) {
        Project project = findProjectAsLeader(email, projectId);
        project.changeStatus(ProjectStatus.from(status));
        return ProjectResponseDto.from(project);
    }

    @Transactional
    public void removeProjectMember(String email, Long projectId, Long memberId) {
        Project project = findProjectAsLeader(email, projectId);
        if (Objects.equals(project.getLeader().getId(), memberId)) {
            throw new ConflictException("팀장은 팀에서 제외할 수 없습니다.");
        }
        Long applicationId = applicationRepository
                .findByProjectIdAndApplicantIdAndStatus(projectId, memberId, ApplicationService.ACCEPTED)
                .orElseThrow(() -> new NotFoundException("해당 팀원을 찾을 수 없습니다."))
                .getId();
        // 잠근 뒤 다시 확인: 그 사이 본인이 지원을 취소했을 수 있다
        Application application = applicationRepository.findByIdForUpdate(applicationId)
                .filter(Application::isAccepted)
                .orElseThrow(() -> new NotFoundException("해당 팀원을 찾을 수 없습니다."));
        positionSeats.releaseIfAccepted(application);   // 정원 -1 (TS-06)
        applicationRepository.delete(application);
    }

    /**
     * 팀장이 사용자를 팀원으로 바로 추가. 이미 팀원이면 아무것도 하지 않는다(멱등).
     * 대기/거절된 지원서가 있으면 지원서 수락 흐름을 쓰도록 409.
     */
    @Transactional
    public void addProjectMember(String email, Long projectId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("추가할 사용자 ID가 필요합니다.");
        }
        Project project = findProjectAsLeader(email, projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        Optional<Application> existing = applicationRepository.findByApplicantIdAndProjectId(userId, projectId);
        if (existing.isPresent()) {
            if (ApplicationService.isAccepted(existing.get())) {
                return;
            }
            throw new ConflictException("이미 지원서가 있는 사용자입니다. 지원서를 수락해주세요.");
        }

        applicationRepository.save(Application.builder()
                .project(project)
                .applicant(user)
                .supportRole(Objects.equals(project.getLeader().getId(), userId) ? LEADER_ROLE : MEMBER_ROLE)
                .message("")
                .status(ApplicationService.ACCEPTED)
                .build());
    }

    /**
     * 수정 요청의 포지션 목록에 맞춘다. 포지션은 이름(role_name)으로 지원서(support_role)와 연결되므로 이름이 식별자다.
     * 이름을 바꾸면 "기존 삭제 + 새로 추가"로 처리된다(합류한 팀원이 있으면 삭제가 막혀 409) (TS-19)
     */
    private void syncPositions(Project project, Map<String, Integer> requested) {
        LocalDateTime now = LocalDateTime.now();
        Set<String> existingNames = new HashSet<>();
        for (ProjectPosition existing : project.getProjectPositions()) {
            existingNames.add(existing.getRoleName());
            Integer requiredCount = requested.get(existing.getRoleName());
            if (requiredCount == null) {
                if (projectPositionRepository.deleteIfEmpty(existing.getId()) == 0) {
                    throw new ConflictException("'" + existing.getRoleName() + "' 포지션에는 합류한 팀원이 있어 삭제할 수 없습니다.");
                }
            } else if (!requiredCount.equals(existing.getRequiredCount())
                    && projectPositionRepository.updateRequiredCountIfFits(existing.getId(), requiredCount, now) == 0) {
                throw new ConflictException("'" + existing.getRoleName() + "' 포지션의 모집 인원은 현재 합류 인원보다 적게 줄일 수 없습니다.");
            }
        }
        List<ProjectPosition> added = requested.entrySet().stream()
                .filter(entry -> !existingNames.contains(entry.getKey()))
                .map(entry -> ProjectPosition.builder()
                        .project(project)
                        .roleName(entry.getKey())
                        .requiredCount(entry.getValue())
                        .currentCount(0)
                        .build())
                .toList();
        projectPositionRepository.saveAll(added);
    }

    /** 이름 앞뒤 공백 제거, 같은 이름 두 번·예약어(팀장) 금지. 입력 순서를 유지한다 */
    private static Map<String, Integer> normalizePositions(List<PositionRequestDto> positions) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        for (PositionRequestDto position : positions) {
            String roleName = position.getRoleName().trim();
            if (LEADER_ROLE.equals(roleName)) {
                throw new IllegalArgumentException("'" + LEADER_ROLE + "'은(는) 포지션 이름으로 쓸 수 없습니다.");
            }
            if (normalized.putIfAbsent(roleName, position.getRequiredCount()) != null) {
                throw new IllegalArgumentException("같은 이름의 포지션을 두 번 등록할 수 없습니다.");
            }
        }
        return normalized;
    }

    /** 중복 ID는 한 번만, 없는 ID는 무시한다(생성 API의 기존 동작과 같음) */
    private List<TechStack> findTechStacks(List<Long> techStackIds) {
        List<Long> ids = techStackIds.stream().filter(Objects::nonNull).distinct().toList();
        return ids.isEmpty() ? List.of() : techStackRepository.findAllById(ids);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("프로젝트를 찾을 수 없습니다."));
    }

    private Project findProjectAsLeader(String email, Long projectId) {
        User me = currentUser(email);
        Project project = findProject(projectId);
        if (!ApplicationService.isLeader(project, me.getId())) {
            throw new ForbiddenException("모집글 작성자만 할 수 있습니다.");
        }
        return project;
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }
}
