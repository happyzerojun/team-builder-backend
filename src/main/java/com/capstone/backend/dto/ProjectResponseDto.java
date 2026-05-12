package com.capstone.backend.dto;

import com.capstone.backend.entity.Project;
import java.util.List;

public record ProjectResponseDto(
        Long project_id,
        String title,
        String content,
        String region,
        String status,
        Long leader_id,
        String author_name,
        String term,

        String meetingType,
        Boolean isLocalOnly,

        List<TechStackDto> techStacks
) {
    public static ProjectResponseDto from(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getContent(),
                project.getRegion(),
                project.getStatus(),
                project.getLeader().getId(),
                project.getLeader().getName(),
                project.getTerm(),

                project.getMeetingType(),
                project.getIsLocalOnly(),

                project.getProjectTechStacks() == null
                        ? List.of()
                        : project.getProjectTechStacks().stream()
                            .map(projectTechStack -> new TechStackDto(
                                    projectTechStack.getTechStack().getId(),
                                    projectTechStack.getTechStack().getName()
                            ))
                            .toList()
        );
    }
}