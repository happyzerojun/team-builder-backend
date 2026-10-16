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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "project")
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(nullable = false, length = 20)
    private String term;

    // DB에는 ProjectStatus의 한글 라벨("모집중", "진행중", "종료됨")이 저장된다. 값의 정의는 ProjectStatus 한 곳 (TS-09)
    @Column(nullable = false, length = 30)
    private String status;

    // 🚨 [핵심 1] 팀장(User) 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    // 🚨 [핵심 2] 프로젝트가 요구하는 기술 스택 (N:M)
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTechStack> projectTechStacks = new ArrayList<>();

    // 🚨 [핵심 3] 이 프로젝트에 들어온 지원서들
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 🚨 [새로 추가] 이 프로젝트에서 모집하는 포지션 리스트 (N:M 구조가 아닌 1:N 구조입니다)
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectPosition> projectPositions = new ArrayList<>();

    @Column(name = "meeting_type", length = 20)
    private String meetingType;

    @Column(name = "is_local_only")
    private Boolean isLocalOnly;

    public ProjectStatus getProjectStatus() {
        return ProjectStatus.from(this.status);
    }

    public boolean isRecruiting() {
        return getProjectStatus() == ProjectStatus.RECRUITING;
    }

    /**
     * 모집중 → 진행중 → 종료됨 순서로만 바꿀 수 있다. 같은 상태 재요청은 멱등.
     * 이전에는 아무 문자열이나 저장돼 "종료됨" → "모집중" 역행이나 오타 저장이 가능했다 (TS-09)
     */
    public void changeStatus(ProjectStatus next) {
        ProjectStatus current = getProjectStatus();
        if (current == next) {
            return;
        }
        if (!current.canMoveTo(next)) {
            throw new IllegalStateException(
                    "'" + current.label() + "' 상태에서 '" + next.label() + "'(으)로 바꿀 수 없습니다.");
        }
        this.status = next.label();
    }

    // 수정관련 
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateTerm(String term) {
        this.term = term;
    }

    public void updateMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public void updateIsLocalOnly(Boolean isLocalOnly) {
        this.isLocalOnly = isLocalOnly;
    }
}