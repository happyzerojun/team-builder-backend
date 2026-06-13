# Tasks: MyPage 모집글 표시 버그 수정

## Task 1: Backend - ProjectRepository에 리더별 조회 메서드 추가
- [x] `ProjectRepository.java`에 `List<Project> findByLeaderId(Long leaderId)` 메서드 추가

## Task 2: Backend - ProjectService에 유저별 프로젝트 조회 메서드 추가
- [x] `ProjectService.java`에 `getProjectsByLeaderId(Long leaderId)` 메서드 추가
- [x] `projectRepository.findByLeaderId(leaderId)` 호출 후 `ProjectResponseDto::from`으로 변환하여 `List<ProjectResponseDto>` 반환

## Task 3: Backend - ProjectController에 유저별 프로젝트 조회 엔드포인트 추가
- [x] `ProjectController.java`에 `GET /api/projects/user/{userId}` 엔드포인트 추가
- [x] `projectService.getProjectsByLeaderId(userId)` 호출하여 `ResponseEntity<List<ProjectResponseDto>>` 반환

## Task 4: Frontend - projectService에 유저별 프로젝트 조회 함수 추가
- [x] `frontend/src/services/projectService.js`에 `getMyProjects(userId)` 함수 추가
- [x] `GET /api/projects/user/{userId}` 호출, 응답이 배열인지 확인 후 반환

## Task 5: Frontend - MyPage.jsx 데이터 로딩 로직 수정
- [x] `getAllProjects()` 호출을 `getMyProjects(savedUser.user_id)` 호출로 교체하여 내가 리더인 프로젝트 직접 조회
- [-] 지원 목록 매칭 로직 수정: 빈 프로젝트 배열 매칭 대신 `ApplicationResponseDto`의 `projectId`, `projectTitle` 필드를 직접 사용
- [~] 불필요한 `allProjects` state 및 관련 로직 제거 (또는 사용하지 않도록 변경)
