package com.capstone.backend.dto;

import com.capstone.backend.entity.ProjectPosition;

public record PositionDto(
        Long position_id,
        String roleName,
        Integer requiredCount,
        Integer currentCount
) {
    public static PositionDto from(ProjectPosition position) {
        return new PositionDto(position.getId(), position.getRoleName(), position.getRequiredCount(), position.getCurrentCount());
    }
}
