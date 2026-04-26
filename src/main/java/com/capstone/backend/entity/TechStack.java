package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*; // @Builder, @AllArgsConstructor 등을 위해 필요합니다.
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tech_stack")
@Getter
@Builder // 🚨 1. 이 녀석이 있어야 .builder() 메서드가 생깁니다!
@AllArgsConstructor // 🚨 2. 빌더가 내부적으로 사용하기 위해 모든 인자를 받는 생성자가 필수입니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stack_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}