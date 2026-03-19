package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Setter
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // 중복 제거 및 준원님 초기값 설정 병합
    @Column(name = "experience_level", nullable = false, length = 20)
    @Builder.Default
    private String experienceLevel = "BEGINNER";

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    // --- 준원님이 추가한 OAuth2(소셜 로그인) 관련 필드 ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Setter
    private AuthProvider provider = AuthProvider.LOCAL; // AuthProvider Enum이 있어야 정상 작동합니다.

    @Setter
    @Column(unique = true)
    private String providerId;

    // --- 영준님이 추가한 기술 스택 양방향 매핑 필드 ---
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}