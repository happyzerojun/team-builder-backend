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
@Builder // <--- 1. 이 부분을 추가하세요! (빌더 패턴 사용 가능하게 함)
@AllArgsConstructor // <--- 2. 빌더를 쓰려면 모든 필드를 인자로 받는 생성자가 필수입니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 자동으로 시간을 가져옴
public class User {

    // User 클래스 내부에 추가하세요
    @Builder.Default // 빌더 사용 시 리스트 초기화 보장
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 증가 (AI)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100) // 필수 값 + 중복 불가 (UQ)
    private String email;

    @Column(nullable = false, length = 255) // 필수 값 (NN)
    private String password;

    @Column(name = "experience_level", nullable = false, length = 20)
    private String experienceLevel;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}