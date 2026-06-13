# Bugfix Requirements Document

## Introduction

마이페이지(MyPage)에서 "내가 작성한 모집글"과 "내가 지원한 모집글" 목록이 항상 빈 배열로 표시되는 버그. 근본 원인은 `projectService.getAllProjects()`가 Spring의 `Page` 객체(`{ content: [...], totalPages: N }`)를 반환하는데, `MyPage.jsx`에서 이를 단순 배열로 취급하여 `Array.isArray(data)`가 항상 `false`가 되어 프로젝트 목록이 `[]`로 설정되는 것이다. 프로젝트 목록이 비어있으므로, 지원 내역의 프로젝트 매칭도 모두 실패한다.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN 로그인된 사용자가 MyPage를 열고 `projectService.getAllProjects()`가 호출되면 THEN 반환값은 `Page` 객체(`{ content: [...], totalPages: N }`)인데, `Array.isArray(data)`로 검사하여 항상 `[]`이 되어 프로젝트 목록이 비어있다

1.2 WHEN 프로젝트 목록이 빈 배열인 상태에서 `projects.filter(p => p.leader_id === user_id)`로 내가 만든 프로젝트를 필터링하면 THEN 결과는 항상 빈 배열이므로 "내가 만든 프로젝트"가 표시되지 않는다

1.3 WHEN `applicationService.getMyApplications()`가 지원 내역을 정상적으로 반환하더라도, 해당 지원의 `projectId`를 빈 프로젝트 배열에서 찾으면 THEN `matchedProject`는 항상 `null`이 되어 "내가 지원한 모집글"이 표시되지 않는다

1.4 WHEN `ApplicationResponseDto`에 `project` 필드가 없으므로 `if (app.project)` 조건이 항상 `false`이고, fallback으로 빈 프로젝트 목록에서 매칭을 시도하면 THEN 지원한 프로젝트 정보를 얻을 수 없다

### Expected Behavior (Correct)

2.1 WHEN 로그인된 사용자가 MyPage를 열면 THEN 시스템은 해당 사용자가 리더인 모든 프로젝트 목록을 정상적으로 불러와 "내가 만든 프로젝트" 섹션에 표시해야 한다 (SHALL)

2.2 WHEN 로그인된 사용자가 MyPage를 열면 THEN 시스템은 해당 사용자의 지원 내역과 관련 프로젝트 정보를 정상적으로 불러와 "내가 지원한 모집글" 섹션에 표시해야 한다 (SHALL)

2.3 WHEN `getAllProjects()` 응답이 `Page` 객체 형태로 올 때 THEN 시스템은 `content` 배열을 올바르게 추출하여 사용해야 한다 (SHALL)

2.4 WHEN 지원 내역(ApplicationResponseDto)에 프로젝트 제목 정보(`projectTitle`)가 포함되어 있을 때 THEN 시스템은 별도의 프로젝트 목록 매칭 없이도 지원한 프로젝트 정보를 표시할 수 있어야 한다 (SHALL)

### Unchanged Behavior (Regression Prevention)

3.1 WHEN 사용자가 메인 페이지에서 프로젝트 목록을 조회할 때 THEN 시스템은 기존처럼 페이지네이션된 프로젝트 목록을 정상적으로 표시해야 한다 (SHALL CONTINUE TO)

3.2 WHEN 사용자가 프로젝트에 지원하거나 지원을 취소할 때 THEN 시스템은 기존 지원/취소 기능이 정상 작동해야 한다 (SHALL CONTINUE TO)

3.3 WHEN 사용자가 프로젝트 상세 페이지를 조회할 때 THEN 시스템은 기존처럼 단일 프로젝트 정보를 정상적으로 반환해야 한다 (SHALL CONTINUE TO)

3.4 WHEN MyPage에서 유저 프로필 정보(이름, 이메일, 소속, 기술스택 등)를 표시할 때 THEN 시스템은 기존처럼 프로필 정보를 정상적으로 표시해야 한다 (SHALL CONTINUE TO)

3.5 WHEN 사용자가 참여 중인 프로젝트(수락된 지원 중 리더가 아닌 것)를 조회할 때 THEN 시스템은 기존 분류 로직을 유지하며 정상적으로 표시해야 한다 (SHALL CONTINUE TO)
