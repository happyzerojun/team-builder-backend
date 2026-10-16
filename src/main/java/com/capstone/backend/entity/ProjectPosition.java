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

    // 정원 증감은 엔티티 메서드가 아니라 ProjectPositionRepository의 조건부 UPDATE로 한다.
    // 이전 increaseCurrentCount()는 어디서도 호출되지 않았고, 호출했더라도 "읽고-비교-쓰기"라
    // 동시 수락 시 정원을 넘길 수 있었다 (TS-06)

    public boolean isFull() {
        return requiredCount != null && currentCount != null && currentCount >= requiredCount;
    }
}