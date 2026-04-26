package com.capstone.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // 👈 이거 임포트하세요!
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_tech_stack")
@Getter
@Setter // 🚨 1. 이거 꼭 추가해야 합니다! (관계를 맺어줄 때 필요)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_tech_stack_id") // 🚨 2. 원래 설계하신 SQL 이름과 맞췄습니다.
    private Long id;

    @JsonIgnore // 🚨 [핵심] JSON으로 바꿀 때 유저 정보는 그리지 마! (무한루프 방지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id", nullable = false)
    private TechStack techStack;
}