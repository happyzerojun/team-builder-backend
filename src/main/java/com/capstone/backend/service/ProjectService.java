package com.capstone.backend.service;

import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.entity.ProjectStatus;
import com.capstone.backend.entity.ProjectTechStack;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.ForbiddenException;
import com.capstone.backend.global.exception.NotFoundException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.repository.ProjectPositionRepository;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ProjectTechStackRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final ProjectTechStackRepository projectTechStackRepository;
    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final PositionSeats positionSeats;

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

        if (request.getTechStackIds() != null && !request.getTechStackIds().isEmpty()) {
            List<TechStack> techStacks = techStackRepository.findAllById(request.getTechStackIds());

            List<ProjectTechStack> projectTechStacks = techStacks.stream()
                    .map(techStack -> ProjectTechStack.builder()
                            .project(savedProject)
                            .techStack(techStack)
                            .build())
                    .toList();

            projectTechStackRepository.saveAll(projectTechStacks);
        }

        if (request.getPositions() != null && !request.getPositions().isEmpty()) {
            List<ProjectPosition> positions = request.getPositions().stream()
                    .map(p -> ProjectPosition.builder()
                            .project(savedProject)
                            .roleName(p.getRoleName().trim())
                            .requiredCount(p.getRequiredCount())
                            .currentCount(0)
                            .build())
                    .toList();
            if (positions.stream().map(ProjectPosition::getRoleName).distinct().count() != positions.size()) {
                throw new IllegalArgumentException("같은 이름의 포지션을 두 번 등록할 수 없습니다.");
            }
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

    public ProjectResponseDto getProjectById(Long projectId) {
        return ProjectResponseDto.from(findProject(projectId));
    }

    @Transactional
    public ProjectResponseDto updateProject(String email, Long projectId, ProjectRequestDto request) {
        Project project = findProjectAsLeader(email, projectId);

        project.updateTitle(request.getTitle());
        project.updateContent(request.getContent());
        project.updateRegion(request.getRegion());
        project.updateTerm(String.valueOf(request.getTerm()));
        project.updateMeetingType(request.getMeetingType());
        project.updateIsLocalOnly(request.getIsLocalOnly());

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
