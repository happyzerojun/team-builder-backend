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

import java.util.List;
import com.capstone.backend.dto.MemberResponseDto;
import com.capstone.backend.entity.Application;
import com.capstone.backend.repository.ApplicationRepository;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TechStackRepository techStackRepository;
    private final ProjectTechStackRepository projectTechStackRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto request) {
        User leader = userRepository.findById(request.getLeader_id())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Project project = Project.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .region(request.getRegion())
                .status(request.getStatus())
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

        return ProjectResponseDto.from(savedProject);
    }

    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponseDto::from)
                .toList();
    }

    public ProjectResponseDto getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectResponseDto::from)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
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
    public ProjectResponseDto updateProjectStatus(Long projectId, String status) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

    project.updateStatus(status);

    return ProjectResponseDto.from(project);
    }
}