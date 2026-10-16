package com.capstone.backend.service;

import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ProjectPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 포지션 정원(current_count) 증감을 한 곳에서 처리한다 (TS-06).
 * 호출하는 서비스의 트랜잭션 안에서 실행된다. 지원서는 support_role 문자열로 포지션(role_name)과 연결된다.
 * 포지션을 정하지 않은 모집글(기존 데이터·현재 화면)은 정원 제한이 없다.
 */
@Component
@RequiredArgsConstructor
public class PositionSeats {

    private final ProjectPositionRepository projectPositionRepository;

    /** 지원 시점의 빠른 검사. 최종 판정은 수락 시 take()의 조건부 UPDATE */
    public void checkApplicable(Long projectId, String roleName) {
        List<ProjectPosition> positions = projectPositionRepository.findByProjectId(projectId);
        if (positions.isEmpty()) {
            return;
        }
        ProjectPosition position = positions.stream()
                .filter(p -> Objects.equals(p.getRoleName(), roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("모집하지 않는 포지션입니다: " + roleName));
        if (position.isFull()) {
            throw new ConflictException("'" + position.getRoleName() + "' 포지션은 정원이 찼습니다.");
        }
    }

    /** 정원 +1. 영향 행 0 = 정원 초과 → 409, 호출한 트랜잭션(지원서 상태 변경)도 롤백 */
    public void take(Long projectId, String roleName) {
        Optional<ProjectPosition> position = find(projectId, roleName);
        if (position.isPresent()
                && projectPositionRepository.increaseIfAvailable(position.get().getId(), LocalDateTime.now()) == 0) {
            throw new ConflictException("'" + position.get().getRoleName() + "' 포지션은 정원이 찼습니다.");
        }
    }

    /** 수락된 지원서가 삭제(지원 취소·팀원 제외)될 때 정원 -1 */
    public void releaseIfAccepted(Application application) {
        if (!application.isAccepted()) {
            return;
        }
        find(application.getProject().getId(), application.getSupportRole())
                .ifPresent(p -> projectPositionRepository.decreaseIfPositive(p.getId(), LocalDateTime.now()));
    }

    private Optional<ProjectPosition> find(Long projectId, String roleName) {
        if (roleName == null) {
            return Optional.empty();
        }
        return projectPositionRepository.findByProjectIdAndRoleName(projectId, roleName);
    }
}
