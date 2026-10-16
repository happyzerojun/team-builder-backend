package com.capstone.backend.service;

import com.capstone.backend.entity.Application;
import com.capstone.backend.entity.Project;
import com.capstone.backend.entity.ProjectPosition;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.ProjectPositionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TS-06: 포지션 정원 증감 규칙 */
@ExtendWith(MockitoExtension.class)
@DisplayName("포지션 정원")
class PositionSeatsTest {

    @Mock
    private ProjectPositionRepository projectPositionRepository;

    @InjectMocks
    private PositionSeats positionSeats;

    private final Project project = Project.builder().id(10L).build();

    private ProjectPosition position(int required, int current) {
        return ProjectPosition.builder().id(5L).project(project).roleName("백엔드")
                .requiredCount(required).currentCount(current).build();
    }

    @Test
    @DisplayName("조건부 UPDATE가 0행이면(정원 초과) 409")
    void takeFailsWhenNoRowUpdated() {
        when(projectPositionRepository.findByProjectIdAndRoleName(10L, "백엔드")).thenReturn(Optional.of(position(1, 1)));
        when(projectPositionRepository.increaseIfAvailable(eq(5L), any())).thenReturn(0);

        assertThatThrownBy(() -> positionSeats.take(10L, "백엔드")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("포지션을 정하지 않은 글은 정원 제한 없음")
    void noPositionMeansNoLimit() {
        when(projectPositionRepository.findByProjectIdAndRoleName(10L, "백엔드")).thenReturn(Optional.empty());

        assertThatCode(() -> positionSeats.take(10L, "백엔드")).doesNotThrowAnyException();
        verify(projectPositionRepository, never()).increaseIfAvailable(anyLong(), any());
    }

    @Test
    @DisplayName("수락된 지원서만 정원을 돌려준다")
    void releaseOnlyAccepted() {
        Application pending = Application.builder().project(project).supportRole("백엔드").status("PENDING").build();
        positionSeats.releaseIfAccepted(pending);
        verify(projectPositionRepository, never()).decreaseIfPositive(anyLong(), any());

        Application accepted = Application.builder().project(project).supportRole("백엔드").status("ACCEPTED").build();
        when(projectPositionRepository.findByProjectIdAndRoleName(10L, "백엔드")).thenReturn(Optional.of(position(2, 1)));
        positionSeats.releaseIfAccepted(accepted);
        verify(projectPositionRepository).decreaseIfPositive(eq(5L), any());
    }

    @Test
    @DisplayName("포지션이 있는 글: 없는 포지션 지원은 400, 찬 포지션 지원은 409")
    void checkApplicable() {
        when(projectPositionRepository.findByProjectId(10L)).thenReturn(List.of(position(1, 1)));

        assertThatThrownBy(() -> positionSeats.checkApplicable(10L, "디자이너")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> positionSeats.checkApplicable(10L, "백엔드")).isInstanceOf(ConflictException.class);
    }
}
