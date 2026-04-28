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

    // --- 유저 기본 정보 ---
    @Column(name = "experience_level", nullable = false, length = 20)
    @Builder.Default
    private String experienceLevel = "BEGINNER";

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    @Column(length = 50)
    private String nickname;

    @Column(name = "job_role", length = 50)
    private String jobRole;

    @Column(length = 100)
    private String organization;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImg;

    // --- 소셜 로그인 (준원님 필드 유지) ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Setter
    private AuthProvider provider = AuthProvider.LOCAL;

    @Setter
    @Column(unique = true)
    private String providerId;

    // --- 🚨 [정석 반영] 기술 스택 매핑 (영준님 설계 유지 및 강화) ---
    // 기존의 임시 tags(List<String>)는 삭제했습니다.
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    // --- 메타 데이터 ---
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 🚨 프로필 업데이트 비즈니스 로직
     * @param newStacks : 서비스 레이어에서 변환되어 넘어온 새로운 기술 스택 리스트
     */
// User.java 내부의 메서드 수정

    public void updateProfile(String name, String nickname, String jobRole,
                              String organization, String introduction,
                              List<UserTechStack> newStacks, String profileImg) {
        this.name = name;
        this.nickname = nickname;
        this.jobRole = jobRole;
        this.organization = organization;
        this.introduction = introduction;
        this.profileImg = profileImg;

        // 1. 기존 관계를 끊습니다. (orphanRemoval=true에 의해 DB에서도 삭제됨)
        this.userTechStacks.clear();

        // 2. 새로운 관계를 맺어줍니다.
        if (newStacks != null) {
            newStacks.forEach(stack -> {
                // 🚨 이 부분이 핵심입니다!
                // 연결 테이블 객체(stack)에게 "너의 주인은 나(this)야"라고 알려줘야
                // DB 외래키(FK) 칸에 내 user_id가 쏙 들어갑니다.
                stack.setUser(this);
                this.userTechStacks.add(stack);
            });
        }
    }
}