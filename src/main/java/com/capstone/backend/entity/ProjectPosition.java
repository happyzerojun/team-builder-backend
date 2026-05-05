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
@Table(name = "project_position")
@EntityListeners(AuditingEntityListener.class)
public class ProjectPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long id;

    // 예: "백엔드", "프론트엔드", "UI/UX 디자이너"
    @Column(name = "role_name", length = 30)
    private String roleName;

    // 모집해야 할 총 인원
    @Column(name = "required_count")
    private Integer requiredCount;

    // 현재 합류(승인) 완료된 인원
    @Column(name = "current_count")
    @Builder.Default
    private Integer currentCount = 0;

    // 🚨 [핵심] 어떤 프로젝트의 모집 포지션인가?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- 비즈니스 로직 ---

    // 팀장이 지원자를 '승인(ACCEPTED)' 했을 때 호출할 메서드
    public void increaseCurrentCount() {
        if (this.currentCount < this.requiredCount) {
            this.currentCount += 1;
        } else {
            throw new IllegalStateException("이미 해당 포지션의 모집 인원이 꽉 찼습니다.");
        }
    }
}