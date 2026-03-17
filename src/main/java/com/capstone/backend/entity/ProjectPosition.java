package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_position")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id") // SQL 설계와 ID 이름 통일
    private Long id;

    @Column(name = "role_name", nullable = false, length = 30) // SQL의 role_name과 매핑
    private String positionName;

    @Column(nullable = false)
    private Integer requiredCount; // 모집 정원

    @Builder.Default
    @Column(nullable = false)
    private Integer currentCount = 0; // 현재 참여 인원 (기본값 0)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // 시간 기록이 필요하다면 아래 필드들도 추가하세요 (선택사항)
    /*
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    */
}