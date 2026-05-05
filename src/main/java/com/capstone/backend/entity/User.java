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

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;


    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String nickname;

    @Column(name = "job_role", length = 50)
    private String jobRole;

    @Column(length = 100)
    private String organization;

    @Column(name = "experience_level", nullable = false, length = 20)
    @Builder.Default
    private String experienceLevel = "BEGINNER";

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImg;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    // 소셜 로그인 필드 유지
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(unique = true)
    private String providerId;

    // 🚨 [핵심] N:M 관계 매핑
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 🚨 [새로 추가] 소셜 로그인 시 정보를 업데이트하는 전용 메서드
    public void updateOAuthInfo(String name, AuthProvider provider, String providerId) {
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    // 🚨 [핵심] 프로필 업데이트 비즈니스 로직
    public void updateProfile(String name, String nickname, String jobRole,
                              String organization, String introduction,
                              List<UserTechStack> newStacks, String profileImg) {
        this.name = name;
        this.nickname = nickname;
        this.jobRole = jobRole;
        this.organization = organization;
        this.introduction = introduction;
        this.profileImg = profileImg;

        this.userTechStacks.clear(); // 기존 기술 스택 초기화

        if (newStacks != null && !newStacks.isEmpty()) {
            for (UserTechStack stack : newStacks) {
                stack.setUser(this); // 외래키 주인 설정!
                this.userTechStacks.add(stack);
            }
        }

    }
}