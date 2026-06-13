# Design: MyPage 모집글 표시 버그 수정

## Overview

MyPage에서 "내가 작성한 모집글"과 "내가 지원한 모집글"이 표시되지 않는 버그를 수정한다. 근본 원인은 프론트엔드에서 페이지네이션 응답을 배열로 잘못 처리하는 것이며, 추가적으로 전체 프로젝트를 가져와 클라이언트에서 필터링하는 비효율적 구조를 개선한다.

## Fix Strategy

### 접근 방식: 백엔드에 유저별 전용 API 추가 + 프론트엔드 수정

전체 프로젝트를 가져와서 클라이언트에서 필터링하는 기존 방식 대신, 백엔드에 유저별 프로젝트 조회 API를 추가하고 프론트엔드에서 이를 직접 호출한다.

## Changes

### 1. Backend: ProjectRepository에 리더별 조회 메서드 추가

**파일**: `src/main/java/com/capstone/backend/repository/ProjectRepository.java`

```java
List<Project> findByLeaderId(Long leaderId);
```

### 2. Backend: ProjectService에 유저별 프로젝트 조회 메서드 추가

**파일**: `src/main/java/com/capstone/backend/service/ProjectService.java`

```java
public List<ProjectResponseDto> getProjectsByLeaderId(Long leaderId) {
    return projectRepository.findByLeaderId(leaderId).stream()
            .map(ProjectResponseDto::from)
            .toList();
}
```

### 3. Backend: ProjectController에 유저별 프로젝트 조회 엔드포인트 추가

**파일**: `src/main/java/com/capstone/backend/controller/ProjectController.java`

```java
// 유저가 리더인 프로젝트 조회 (GET /api/projects/user/{userId})
@GetMapping("/user/{userId}")
public ResponseEntity<List<ProjectResponseDto>> getProjectsByUser(@PathVariable Long userId) {
    return ResponseEntity.ok(projectService.getProjectsByLeaderId(userId));
}
```

### 4. Frontend: projectService에 유저별 프로젝트 조회 함수 추가

**파일**: `frontend/src/services/projectService.js`

```javascript
getMyProjects: async (userId) => {
    try {
        const res = await api.get(`${API_URL}/user/${userId}`);
        return Array.isArray(res.data) ? res.data : [];
    } catch (e) {
        console.error(e);
        return [];
    }
},
```

### 5. Frontend: MyPage.jsx 데이터 로딩 로직 수정

**파일**: `frontend/src/pages/user/MyPage.jsx`

기존에 전체 프로젝트를 가져와서 필터링하던 로직을 다음과 같이 변경:

1. `projectService.getMyProjects(userId)` 호출로 내가 리더인 프로젝트 직접 조회
2. `applicationService.getMyApplications()` 반환값에서 `projectId`, `projectTitle`을 직접 사용하여 지원한 모집글 표시 (전체 프로젝트 매칭 불필요)

```javascript
// 변경 전: 전체 프로젝트 가져와서 필터링
const data = await projectService.getAllProjects();
const projects = Array.isArray(data) ? data : [];
const myCreatedProjects = projects.filter(p => String(p.leader_id) === String(savedUser.user_id));

// 변경 후: 유저별 API 직접 호출
const myCreatedProjects = await projectService.getMyProjects(savedUser.user_id);
setMyLead(myCreatedProjects);
```

지원한 프로젝트 매칭도 변경:

```javascript
// 변경 전: 빈 projects 배열에서 매칭 시도
const matchedProject = projects.find(p => String(p.project_id) === String(app.projectId));

// 변경 후: ApplicationResponseDto의 projectId, projectTitle을 직접 사용
const applied = myApplications
    .filter(app => ["PENDING", "ACCEPTED", "REJECTED", "pending", "accepted", "rejected"].includes(app.status))
    .map(app => ({
        project_id: app.projectId,
        title: app.projectTitle,
        application_id: app.applicationId,
        application_status: app.status
    }));
```

## Data Flow (수정 후)

```
MyPage 로드
  ├─ GET /api/projects/user/{userId} → 내가 리더인 프로젝트 목록 (List)
  ├─ GET /api/application/user/{userId} → 내 지원 내역 (List with projectId, projectTitle)
  └─ GET /api/users/profile → 유저 프로필 정보
```

## Regression Prevention

- `GET /api/projects?page=0&size=10` 기존 페이지네이션 엔드포인트는 변경하지 않음
- `MainPage`의 기존 프로젝트 목록 조회 로직은 그대로 유지
- 기존 지원/취소 API 동작은 변경하지 않음

## Out of Scope

- 소셜 로그인 시 user_id 불일치 문제 (별도 이슈로 관리)
- 프로필 수정 기능
- 프로젝트 검색/필터 기능
