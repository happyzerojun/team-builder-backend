package com.capstone.backend.service;

import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectTechStack;
import com.capstone.backend.entity.TechStack;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.ProjectRepository;
import com.capstone.backend.repository.ProjectTechStackRepository;
import com.capstone.backend.repository.TechStackRepository;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.repository.ApplicationRepository;
import com.capstone.backend.global.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("모집중", "진행중", "완료됨");

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TechStackRepository techStackRepository;
    private final ProjectTechStackRepository projectTechStackRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto request, String email) {
        User leader = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Project project = Project.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .region(request.getRegion())
            .status("모집중")  // ← 이것만 남기고
            .term(String.valueOf(request.getTerm()))
            .leader(leader)
            .meetingType(request.getMeetingType())
            .isLocalOnly(request.getIsLocalOnly())
            // .status(request.getStatus())  ← 이 줄 삭제!
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

        return ProjectResponseDto.from(savedProject);
    }

    // 1. 전체 프로젝트 조회 (페이징 버전으로 변경)
    public Page<ProjectResponseDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(ProjectResponseDto::from);
    }

    public ProjectResponseDto getProjectById(Long projectId) {
    return projectRepository.findById(projectId)
            .map(ProjectResponseDto::from)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    }

    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto request, String email) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        requireLeader(project, email);

        project.updateTitle(request.getTitle());
        project.updateContent(request.getContent());
        project.updateRegion(request.getRegion());
        project.updateTerm(String.valueOf(request.getTerm()));
        project.updateMeetingType(request.getMeetingType());
        project.updateIsLocalOnly(request.getIsLocalOnly());

        return ProjectResponseDto.from(project);
    }

    @Transactional
    public void deleteProject(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        requireLeader(project, email);
        projectRepository.delete(project);
    }

    public List<MemberResponseDto> getProjectMembers(Long projectId) {
        return applicationRepository.findByProjectIdAndStatus(projectId, "ACCEPTED")
                .stream()
                .map(app -> new MemberResponseDto(
                        app.getApplicant().getId(),
                        app.getApplicant().getName(),
                        app.getSupportRole()
                ))
                .toList();
    }

    @Transactional
    public ProjectResponseDto updateProjectStatus(Long projectId, String status, String email) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        requireLeader(project, email);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("프로젝트 상태가 올바르지 않습니다.");
        }

    project.updateStatus(status);

    return ProjectResponseDto.from(project);
    }

    @Transactional
    public void removeProjectMember(Long projectId, Long memberId, String email) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    requireLeader(project, email);
    Application application = applicationRepository
        .findByProjectIdAndApplicantIdAndStatus(projectId, memberId, "ACCEPTED")
        .orElseThrow(() -> new IllegalArgumentException("해당 팀원을 찾을 수 없습니다."));
    applicationRepository.delete(application);
    }

    @Transactional
    public void addProjectMember(Long projectId, Long userId, String email) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    requireLeader(project, email);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    Application application = Application.builder()
            .project(project)
            .applicant(user)
            .supportRole("팀장")
            .message("")
            .status("ACCEPTED")
            .build();

    applicationRepository.save(application);
    }

    private void requireLeader(Project project, String email) {
        if (!project.getLeader().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("프로젝트 팀장만 수행할 수 있습니다.");
        }
    }

}
