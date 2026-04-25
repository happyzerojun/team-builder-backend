package com.capstone.backend.service;

import com.capstone.backend.dto.ProjectResponseDto;
import com.capstone.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

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