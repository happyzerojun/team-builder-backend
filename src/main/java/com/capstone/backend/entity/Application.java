package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
// 같은 사용자의 같은 프로젝트 중복 지원을 DB가 최종 차단한다.
// existsBy 조회 후 저장만으로는 동시 요청 두 건이 모두 통과할 수 있었다 (TS-04)
@Table(name = "application", uniqueConstraints =
        @UniqueConstraint(name = "uk_application_applicant_project", columnNames = {"applicant_id", "project_id"}))
@EntityListeners(AuditingEntityListener.class)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @Column(name = "support_role", length = 300)
    private String supportRole;

    @Column(length = 500)
    private String message;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String experience;

    @Column(name = "contact_type", length = 30)
    private String contactType;

    @Column(name = "contact_value", length = 255)
    private String contactValue;

    /**
     * 지원서 상태 전이: PENDING → ACCEPTED / REJECTED 만 허용한다.
     * 이전에는 아무 문자열이나 저장돼 오타("ACEPTED")나 거절→수락 같은 역행이 가능했다 (TS-04)
     *
     * @return 상태가 실제로 바뀌었으면 true, 같은 상태 재요청(멱등)이면 false.
     *         호출 측은 true일 때만 정원을 늘린다(버튼 연타로 두 번 세지 않게) (TS-06)
     */
    public boolean updateStatus(String newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("변경할 상태가 필요합니다.");
        }
        String next = newStatus.trim().toUpperCase();
        if (!next.equals("ACCEPTED") && !next.equals("REJECTED") && !next.equals("PENDING")) {
            throw new IllegalArgumentException("알 수 없는 지원 상태입니다: " + newStatus);
        }
        String current = this.status == null ? "PENDING" : this.status.trim().toUpperCase(); // 예전 소문자 데이터 호환
        if (next.equals(current)) {
            return false; // 같은 상태로의 재요청은 멱등 처리 (버튼 연타)
        }
        if (!"PENDING".equals(current) || next.equals("PENDING")) {
            throw new IllegalStateException("대기 중인 지원서만 수락하거나 거절할 수 있습니다. 현재 상태: " + current);
        }
        this.status = next;
        return true;
    }

    public boolean isAccepted() {
        return status != null && "ACCEPTED".equalsIgnoreCase(status.trim());
    }
}