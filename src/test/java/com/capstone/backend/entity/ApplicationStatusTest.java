package com.capstone.backend.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** TS-04: 지원서 상태 전이 규칙 회귀 테스트 (Spring 컨텍스트 없음) */
@DisplayName("지원서 상태 전이")
class ApplicationStatusTest {

    private Application pending() {
        return Application.builder().status("PENDING").build();
    }

    @Test
    @DisplayName("대기 중인 지원서는 수락·거절할 수 있다")
    void pendingCanBeAcceptedOrRejected() {
        Application a = pending();
        a.updateStatus("ACCEPTED");
        assertThat(a.getStatus()).isEqualTo("ACCEPTED");

        Application r = pending();
        r.updateStatus("rejected");          // 소문자 요청도 정규화
        assertThat(r.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("같은 상태 재요청은 멱등")
    void sameStatusIsIdempotent() {
        Application a = pending();
        a.updateStatus("ACCEPTED");
        a.updateStatus("ACCEPTED");
        assertThat(a.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("거절된 지원서를 수락하는 역행은 409(IllegalStateException)")
    void cannotGoBackwards() {
        Application a = pending();
        a.updateStatus("REJECTED");
        assertThatThrownBy(() -> a.updateStatus("ACCEPTED")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("알 수 없는 상태 문자열은 400(IllegalArgumentException)")
    void unknownStatusRejected() {
        assertThatThrownBy(() -> pending().updateStatus("ACEPTED")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pending().updateStatus(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
