package com.capstone.backend.service;

import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.capstone.backend.entity.Project;

import com.capstone.backend.dto.ProjectRequestDto;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

private final UserRepository userRepository;

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
            .build();

    return ProjectResponseDto.from(projectRepository.save(project));
}

    private final ProjectRepository projectRepository;

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
}