package com.capstone.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** TS-09: 모집 상태 값은 ProjectStatus 한 곳에서 정의하고, DB에는 기존 한글 라벨을 저장한다 */
@DisplayName("모집글 상태")
class ProjectStatusTest {

    private Project project(String status) {
        return Project.builder().status(status).build();
    }

    @Test
    @DisplayName("한글 라벨과 enum 이름을 모두 해석한다 (공백·대소문자 무시)")
    void parsesLabelAndName() {
        assertThat(ProjectStatus.from("모집중")).isEqualTo(ProjectStatus.RECRUITING);
        assertThat(ProjectStatus.from("모집 중")).isEqualTo(ProjectStatus.RECRUITING);
        assertThat(ProjectStatus.from("recruiting")).isEqualTo(ProjectStatus.RECRUITING);
        assertThat(ProjectStatus.from("진행중")).isEqualTo(ProjectStatus.PROCEEDING);
        assertThat(ProjectStatus.from("COMPLETED")).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("알 수 없는 값은 400(IllegalArgumentException)")
    void rejectsUnknown() {
        assertThatThrownBy(() -> ProjectStatus.from("모집완료")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProjectStatus.from(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모집중 → 진행중 → 종료됨으로만 바뀌고, DB 값은 한글 라벨 그대로")
    void movesForwardAndStoresLabel() {
        Project p = project("모집중");
        p.changeStatus(ProjectStatus.PROCEEDING);
        assertThat(p.getStatus()).isEqualTo("진행중");
        p.changeStatus(ProjectStatus.COMPLETED);
        assertThat(p.getStatus()).isEqualTo("종료됨");
    }

    @Test
    @DisplayName("같은 상태 재요청은 멱등")
    void sameStatusIsIdempotent() {
        Project p = project("진행중");
        p.changeStatus(ProjectStatus.PROCEEDING);
        assertThat(p.getStatus()).isEqualTo("진행중");
    }

    @Test
    @DisplayName("단계 건너뛰기·역행은 409(IllegalStateException)")
    void rejectsSkipAndBackwards() {
        assertThatThrownBy(() -> project("모집중").changeStatus(ProjectStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> project("종료됨").changeStatus(ProjectStatus.RECRUITING))
                .isInstanceOf(IllegalStateException.class);
    }
}
