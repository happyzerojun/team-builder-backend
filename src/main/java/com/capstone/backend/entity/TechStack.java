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
@Table(name = "tech_stack")
@EntityListeners(AuditingEntityListener.class)
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_stack_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    // 🔥 [핵심 추가] DB에 일괄 반영한 keywords(한글/줄임말 키워드) 컬럼과 매핑합니다.
    // 별도의 @Column 설정을 생략하면 필드명 그대로 keywords 컬럼과 자동으로 맵핑됩니다.
    private String keywords;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}