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
// 🚨 [추가 1] 스프링 부트가 이 엔티티의 시간을 자동으로 감시하도록 설정합니다.
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

    // 🚨 [추가 2] 데이터가 생성될 때의 시간
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 🚨 [추가 3] 데이터가 마지막으로 수정될 때의 시간
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}