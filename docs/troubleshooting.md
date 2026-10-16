# 트러블슈팅 기록

캡스톤 종료 후 셀프 코드 리뷰로 찾은 문제와 해결 과정이에요. 형식은 **증상 → 원인 → 해결 → 재발 방지**예요.

| ID | 날짜 | 분류 | 제목 | 상태 |
|---|---|---|---|---|
| TS-01 | 2026-03 ~ 05 | 협업 | 공용 파일(`build.gradle`, `application.yml`, `User`, CORS)에 병합 충돌이 몰림 | 운영 규칙으로 대응 |
| TS-02 | 2026-10-14 | API | catch-all 예외 핸들러가 내부 예외 메시지를 응답으로 노출 | 해결 |
| TS-03 | 2026-10-14 | AI | Gemini 호출에 타임아웃 없음, 키워드 매칭 비결정적, API 키가 URL에 노출 | 해결 (커넥션 점유는 TS-12) |
| TS-04 | 2026-10-14 | 도메인 | 지원서 상태가 아무 문자열이나 저장되고, 중복 지원을 조회로만 막음 | 해결 |
| TS-05 | 2026-10-14 | 보안 | `anyRequest().permitAll()` 상태로 배포, 요청 본문의 사용자 ID를 신뢰 | 해결 (2026-10-16) |
| TS-06 | 2026-10-14 | 도메인 | 포지션 정원 메서드(`increaseCurrentCount`)가 어디서도 호출되지 않음 | 해결 (2026-10-16) |
| TS-07 | 2026-10-14 | 테스트 | CI를 붙이자 `@WebMvcTest` 7건이 컨텍스트 로딩 실패 | 해결 |
| TS-08 | 2026-10-16 | API | 에러 응답에 기계 판독용 코드가 없고, 깨진 JSON·없는 URL이 500 | 해결 |
| TS-09 | 2026-10-16 | 도메인 | 모집 상태 "모집중"·RECRUITING 혼재, 아무 문자열이나 저장 | 해결 (DB 값 유지) |
| TS-10 | 2026-10-16 | AI | 키워드 맵이 기술 스택 초기 데이터보다 먼저 만들어져 비어 버림 | 해결 |
| TS-11 | 2026-10-16 | AI | AI가 후보가 아니었던 프로젝트 ID를 돌려줘도 통과 | 해결 |
| TS-12 | 2026-10-16 | 성능 | Gemini 응답을 기다리는 동안 DB 커넥션 점유 (트랜잭션 + OSIV) | 해결 |
| TS-13 | 2026-10-16 | 성능 | 모집글 목록 N+1 쿼리 | 해결 |
| TS-14 | 2026-10-16 | 운영 | `ddl-auto: update`가 운영 스키마를 직접 변경 | 부분 해결 (환경변수 분리, 마이그레이션 도구는 남은 일) |
| TS-15 | 2026-10-16 | 보안 | 프론트 번들에 Google 클라이언트 시크릿, 소셜 로그인이 서버 JWT 없이 동작 | 해결 (시크릿 재발급 필요) |
| TS-16 | 2026-10-16 | 동시성 | 리뷰 중복을 조회로만 막아 동시 요청 시 같은 리뷰가 2건 이상 저장 | 해결 (운영 DB 중복 확인 필요) |
| TS-17 | 2026-10-16 | 화면/API | 마이페이지 "내가 만든 프로젝트"가 전체 목록 첫 페이지를 걸러 써서 비어 있음 | 해결 |
| TS-18 | 2026-10-16 | API | 모집글 수정 시 기술 스택이 저장되지 않음, 생성 응답의 기술 스택이 빈 목록 | 해결 |
| TS-19 | 2026-10-16 | 도메인 | 화면에 포지션 입력이 없어 정원 제한이 API로 만든 글에만 적용 | 해결 (`support_role` 문자열 연결 유지) |
| TS-20 | 2026-10-16 | AI | 실행 중 새로 생긴 기술 스택이 재시작 전까지 키워드 맵에 없음 | 해결 |

---

### TS-01 공용 파일에 병합 충돌이 몰림
- **증상:** "build.gradle 병합 충돌 해결", "application.yml 및 User.java 병합 충돌 최종 해결", "CORS 브랜치 충돌 해결" 같은 충돌 해결 커밋이 반복됨.
- **원인:** 인증·AI·Swagger·프로필 기능이 모두 같은 설정 파일과 `User` 엔티티를 수정함. 브랜치를 나눠도 그 파일에서 만남.
- **해결:** main을 기능 브랜치로 먼저 병합해 **브랜치에서** 충돌을 풀고, 빌드 확인 후 PR 머지(PR 18건). rebase는 push 전 개인 브랜치에서만.
- **재발 방지:** 설정은 `application-{profile}.yml`로 분리, 엔티티 변경은 별도 PR, PR마다 CI 빌드(`.github/workflows/ci.yml` 추가).

### TS-02 catch-all 예외 핸들러가 내부 예외 메시지를 응답으로 노출
- **증상:** 어떤 예외든 500 + `"서버 내부 오류가 발생했습니다. 원인: " + e.getMessage()`로 응답. SQL 오류라면 테이블·제약 이름이 그대로 나감. `IllegalArgumentException("존재하지 않는 유저입니다.")` 같은 클라이언트 잘못도 500.
- **원인:** 디버깅 편의를 위해 `@ExceptionHandler(Throwable.class)`를 추가했음. `Throwable`은 `OutOfMemoryError` 같은 `Error`까지 잡음. 로그는 `System.out` + `printStackTrace`.
- **해결:** `IllegalArgumentException` → 400, `IllegalStateException` → 409, `DataIntegrityViolationException` → 409, 나머지 `Exception` → 500 + 고정 문구. 원인은 `log.error`로만 남김.
- **재발 방지:** 응답에는 사용자에게 보여 줄 문구만, 원인은 로그에. `Throwable`/`Error`는 잡지 않아요.

### TS-03 Gemini 호출: 타임아웃·키워드 매칭·API 키 위치
- **증상/위험:**
  - `new RestTemplate()`은 타임아웃이 없어 Gemini가 응답하지 않으면 요청 스레드가 계속 대기.
  - 키워드 매칭이 `contains` + `findFirst`(HashMap 순회 순서)라 "javascript" 요청에 "java"가 걸리는 등 결과가 실행마다 달라질 수 있음.
  - 스택을 못 찾았을 때 대체 목록 `findAll(PageRequest.of(0, 50))`에 정렬이 없어 "최신 50건"이 아님.
  - API 키를 `?key=` 쿼리스트링으로 보내 접근·프록시 로그에 남을 수 있음.
- **해결:** connect 3초·read 15초 타임아웃, 가장 긴 키워드 우선 매칭(공백 제거 정규화 포함), `createdAt DESC` 정렬, 키는 `x-goog-api-key` 헤더로 전송.
- **남은 일 → TS-12에서 해결:** `@Transactional(readOnly = true)` 안에서 Gemini를 호출해 응답을 기다리는 동안 DB 커넥션을 쥐고 있었음.

### TS-04 지원서 상태와 중복 지원
- **증상:** `updateStatus(String)`이 아무 값이나 저장 → 오타 저장, 거절→수락 역행 가능. 중복 지원은 `existsBy...` 조회 후 저장이라 동시 요청 두 건이 모두 통과 가능.
- **해결:** `Application.updateStatus()`가 `PENDING → ACCEPTED/REJECTED`만 허용(같은 상태 재요청은 멱등, 소문자 요청·기존 데이터 정규화). `UNIQUE(applicant_id, project_id)` 추가, 위반 시 409.
- **재발 방지:** `ApplicationStatusTest`로 전이 규칙 고정.
- **주의:** `ddl-auto=update`로 유니크 제약이 추가돼요. 기존 데이터에 같은 사용자·프로젝트 지원서가 2건 이상 있으면 제약 추가가 실패하니 먼저 정리가 필요해요.

### TS-05 인가 없이 배포된 API
- **증상:** 프론트 연동 테스트를 위해 연 `anyRequest().permitAll()`이 그대로 배포됨. 지원서 생성은 본문의 `applicant_id`, 내 지원 목록은 URL의 `userId`, 글 작성은 본문의 `leader_id`, 리뷰는 본문의 `reviewer_id`를 사용. 토큰 없이도 남의 이름으로 지원하거나, 남의 글을 수정하거나, 남의 지원서를 수락할 수 있었음(IDOR). AI 추천은 `userId`가 없으면 `2L`(특정 사용자)로 처리.
- **원인:** 인증(누구인가)은 JWT 필터가 하고 있었지만 인가(무엇을 할 수 있는가)가 없었음. 프론트가 ID를 본문·URL로 보내고 있어 백엔드만 막으면 화면이 깨지는 구조라 미뤄 둠.
- **해결:**
  - **기본 닫힘:** 공개 경로(모집글·기술 스택 GET, 회원가입·로그인·`/api/auth/me`, OAuth2, Swagger)만 `permitAll`, 나머지는 `authenticated()`. 401/403도 `{code, message}` JSON.
  - **신원은 토큰에서만:** 컨트롤러는 `AuthenticatedUser.email(authentication)`으로 요청자를 꺼내고, 본문·URL의 사용자 ID는 쓰지 않음. 기존 경로(`/application/user/{id}`, `/review/received/{id}`)는 본인 ID일 때만 허용하고 `/me` 경로를 추가.
  - **소유권 검사(서비스 계층):** 글 수정·삭제·상태 변경·팀원 관리·지원 수락/거절은 작성자만, 지원 취소는 지원자 본인만, 지원서 목록은 팀장(전체)·팀원(수락된 것만), 리뷰는 종료된 프로젝트의 팀원끼리만. 위반 시 403.
  - 작성자를 팀장으로 등록하는 것을 글 생성 트랜잭션 안으로 옮김(프론트의 두 번째 호출이 실패하면 팀장 없는 팀이 남던 문제).
  - 실제로 지우지 않고 "삭제 성공 (임시)"만 응답하던 `DELETE /api/projects/{id}` 구현.
- **프론트 변경:** `user_id` 전송 제거, `/me` 경로 사용, AI 추천을 토큰을 붙이는 공용 axios 인스턴스로, 비로그인 방문자는 내 지원/리뷰 조회를 생략(401 알림 방지), 로그아웃 시 토큰 삭제.
  - 같이 발견: 팀원 "제외" 버튼이 수락된 지원서를 `REJECTED`로 바꾸는 방식이라 TS-04 전이 규칙(대기 중일 때만 수락/거절)에 막혀 409가 나고 있었음 → 팀원 제외 전용 API 사용.
- **트레이드오프:** 소유권을 URL 패턴(`@PreAuthorize`)이 아니라 서비스에서 검사해 규칙이 도메인 옆에 있지만, 새 API를 만들 때 검사를 빠뜨릴 수 있음 → 기본 닫힘으로 최소한 로그인은 강제하고, 서비스 단위 테스트로 403을 고정.
- **재발 방지:** `ProjectControllerSecurityTest`, `ApplicationControllerSecurityTest`(인가 규칙), `ApplicationServiceTest`·`ProjectServiceTest`·`ReviewServiceTest`(소유권), `TeamFlowIntegrationTest`(전 흐름).

### TS-06 포지션 정원 메서드가 호출되지 않음
- **증상:** `ProjectPosition.increaseCurrentCount()`가 정원 초과를 막는다고 README에 적혀 있었지만 어디서도 호출되지 않아, 수락을 몇 번 해도 `current_count`가 그대로였음.
- **원인:** 지원서의 `support_role`이 문자열이라 어느 포지션인지 연결되지 않았고, 엔티티 메서드는 "읽고 → 비교하고 → 쓰는" 방식이라 호출했더라도 동시 수락 두 건이 같은 값(예: 2/3)을 읽고 둘 다 3을 써서 정원을 넘길 수 있음(lost update).
- **해결:**
  - 수락 시 조건부 UPDATE: `UPDATE project_position SET current_count = current_count + 1 WHERE id = ? AND current_count < required_count`. 영향 행 0이면 409, 같은 트랜잭션의 지원서 상태 변경도 롤백. InnoDB는 UPDATE 시 행 잠금을 잡고 최신 커밋 값으로 WHERE를 다시 평가하므로 동시에 와도 정원을 넘지 않음.
  - 같은 지원서에 대한 수락 요청 두 건(버튼 연타·두 탭)은 지원서 행 `SELECT ... FOR UPDATE`로 직렬화. 멱등 재요청은 다시 세지 않음(`updateStatus`가 실제 변경 여부를 반환).
  - 수락된 팀원의 지원 취소·팀원 제외 시 `current_count - 1`(0 미만 방지).
  - 포지션과 지원서는 `support_role = role_name`으로 연결. 포지션을 정하지 않은 글(기존 데이터, 현재 화면)은 정원 제한 없음. 모집글 생성 API에 `positions`(선택)를 추가하고 응답에 현재 인원을 포함.
- **트레이드오프:** 낙관적 락(`@Version`)은 기존 행의 version이 NULL이라 마이그레이션이 필요하고 충돌 시 재시도 로직이 필요함. 비관적 락을 포지션 행에 거는 방식은 같은 포지션 수락이 직렬화되는 점은 같지만 SELECT~UPDATE 사이 잠금 시간이 더 김. 조건부 UPDATE는 쿼리 하나로 끝나지만 엔티티의 `currentCount` 값이 영속성 컨텍스트와 어긋날 수 있어 수락 흐름에서는 그 값을 다시 쓰지 않음.
- **남은 일:** ~~화면에 포지션 입력 UI가 없어 지금은 API로 만든 글에만 정원이 적용됨~~ → TS-19에서 해결. `support_role` 문자열 대신 `application.position_id` FK로 바꾸는 것은 데이터 이전이 필요해 보류.
- **재발 방지:** `PositionSeatsTest`, `ApplicationServiceTest`, `PositionCapacityIntegrationTest`(MySQL에서 정원 1명 포지션에 4건 동시 수락 → 1건만 성공).

### TS-07 CI를 붙이자 `@WebMvcTest` 7건이 컨텍스트 로딩 실패
- **증상:** 첫 CI 실행에서 `21 tests completed, 7 failed`. `AuthControllerApiTest`, `HomeControllerApiTest` 전부 `IllegalStateException at DefaultCacheAwareContextLoaderDelegate` / `NoSuchBeanDefinitionException`.
- **원인:** CI가 없어서 그동안 아무도 몰랐던 기존 실패예요.
  1. `AuthController`가 `UserService`를 주입받도록 바뀌었는데 테스트에 `@MockBean UserService`가 없음.
  2. 메인 클래스의 `@EnableJpaAuditing`이 웹 슬라이스 테스트(`@WebMvcTest`)에서도 JPA 메타모델을 요구함.
  3. 컨텍스트가 뜨자 `/api/auth/me` 테스트 2건이 다시 실패. 응답이 문자열(email)에서 JSON(`userId`, `email`, `name`)으로 바뀌었는데 테스트는 옛 계약을 검증하고 있었음.
- **해결:** `@EnableJpaAuditing`을 `config/JpaAuditingConfig`로 옮기고, 테스트에 `UserService` 목을 추가. `/me` 테스트는 현재 JSON 계약(비로그인 `userId=0`, 로그인 시 사용자 정보)으로 갱신. 7 failed → 2 failed → 0.
- **재발 방지:** PR마다 CI가 테스트를 돌려요. 컨트롤러 생성자에 의존성을 추가하면 해당 `@WebMvcTest`도 같은 PR에서 고쳐요.

### TS-08 에러 응답에 코드가 없고, 클라이언트 잘못이 500
- **증상:** 에러 응답이 `{"message": "..."}`뿐이라 프론트가 문구를 비교해야 에러 종류를 구분할 수 있었음. 깨진 JSON 본문, 숫자 자리의 문자열, 없는 URL(Spring 6.1의 `NoResourceFoundException`)이 catch-all에 걸려 500. 필터 단계의 401은 본문이 없었음.
- **해결:** `ErrorResponse(code, message)` + `ErrorCode`(`VALIDATION_FAILED`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `METHOD_NOT_ALLOWED`, `CONFLICT`, `INTERNAL_ERROR`). `NotFoundException`(404)·`ForbiddenException`(403) 추가, Security 필터 단계 401/403도 같은 JSON. `message`는 그대로라 기존 프론트 코드 영향 없음.
- **재발 방지:** `GlobalExceptionHandlerTest`(standalone MockMvc).

### TS-09 모집 상태 문자열 혼재
- **증상:** DB·프론트는 `"모집중"/"진행중"/"종료됨"`, 엔티티 주석은 `RECRUITING, PROCEEDING, COMPLETED`. 상태 변경 API는 아무 문자열이나 저장해 오타나 "종료됨 → 모집중" 역행이 가능.
- **해결:** `ProjectStatus` enum 한 곳에서 정의하고 DB에는 기존 한글 라벨을 그대로 저장(데이터 마이그레이션 없음). 입력은 라벨·enum 이름 모두 허용, 전이는 모집중 → 진행중 → 종료됨 한 단계씩(멱등). 프론트 비교 리터럴도 `constants/projectStatus.js`로 모음.
- **트레이드오프:** `@Enumerated(STRING)`으로 enum 이름을 저장하는 게 정석이지만 기존 행을 바꿔야 해서, 엔티티 필드는 문자열로 두고 변경 메서드만 enum을 받게 했음. DB에 정의 밖 값이 있으면 상태 변경 시 400.
- **재발 방지:** `ProjectStatusTest`.

### TS-10 키워드 맵이 비어 버리는 초기화 순서
- **증상:** 빈 DB로 처음 기동하면 AI 추천의 기술 스택 매칭이 전혀 되지 않고(항상 최신 50건 대체), 재시작하면 정상.
- **원인:** `AiService`의 `@PostConstruct`(빈 생성 시점)에서 맵을 만들었는데, 기술 스택을 넣는 `DataInitializer`는 `CommandLineRunner`라 컨텍스트가 다 뜬 뒤에 실행됨.
- **해결:** `TechKeywordMatcher`로 분리해 `ApplicationReadyEvent`(모든 Runner 이후)에 적재하고, 그래도 비어 있으면 첫 요청에서 지연 로드. 맵은 불변 스냅샷으로 통째 교체(적재 중 반쯤 찬 맵 노출 방지). 빈 키워드(`"a,,b"`)는 모든 문장에 매칭되므로 제외.
- **남은 일:** ~~실행 중 새로 생긴 기술 스택(프로필 태그 저장 시 자동 생성)은 재시작 전까지 맵에 없음~~ → TS-20에서 해결.
- **재발 방지:** `TechKeywordMatcherTest`.

### TS-11 AI가 후보가 아니었던 ID를 돌려줘도 통과
- **증상/위험:** 환각 방지가 `findAllById` 존재 여부뿐이라, AI가 이번 요청의 후보가 아니었던(다른 스택·오래된) 실제 프로젝트 ID를 지어내면 그대로 추천됨.
- **해결:** 이번 요청의 후보 ID `Set`으로 먼저 거른 뒤 DB 재조회. 중복 ID 제거, 최대 3건, 점수 0~100 보정, 제목은 AI가 아닌 DB 값. 프롬프트에도 "목록의 ID 중에서만" 지시(1차 방어, 서버 필터가 최종 방어).
- **재발 방지:** `AiCandidateReaderTest`, `AiRecommendationIntegrationTest`(존재하지 않는 ID 응답 포함).

### TS-12 Gemini 응답을 기다리는 동안 DB 커넥션 점유
- **증상/위험:** `AiService.getRecommendation` 전체가 `@Transactional(readOnly = true)`라 Gemini 응답(read timeout 15초)을 기다리는 동안 커넥션을 쥐고 있음. HikariCP 기본 풀은 10개라 AI 요청 10건이 동시에 대기하면 AI와 무관한 API까지 커넥션을 기다림.
- **원인 1 — 트랜잭션 범위:** 같은 클래스 안에서 `@Transactional` 메서드를 불러도 프록시를 거치지 않아 트랜잭션이 나뉘지 않음.
- **원인 2 — OSIV:** Spring Boot 기본값 `open-in-view=true`는 요청이 끝날 때까지 EntityManager를 열어 두고, 첫 트랜잭션에서 얻은 커넥션을 요청 끝까지 유지함. 트랜잭션만 분리해서는 커넥션이 반환되지 않음.
- **해결:**
  - DB 구간을 별도 빈 `AiCandidateReader`로 분리: 후보 조회(트랜잭션 1) → `GeminiClient` 호출(트랜잭션 없음) → 후보 필터·재조회(트랜잭션 2). 후보는 트랜잭션 안에서 값 객체(`ProjectCandidate`)로 변환하고 본문은 300자로 잘라 프롬프트 크기 제한.
  - `spring.jpa.open-in-view: false`. 끄기 전에 컨트롤러·DTO 변환의 지연 로딩을 전수 확인: `ProjectService`에 트랜잭션이 없어 목록·상세·팀원 조회의 `leader`·`projectTechStacks` 지연 로딩이 OSIV에 기대고 있었음 → 클래스 레벨 `@Transactional(readOnly = true)` 추가. 나머지(`ApplicationService`, `ReviewService`, `UserService.getUserProfile`, `TechStackService`)는 이미 트랜잭션 안에서 DTO로 변환, `AuthController.me`는 기본 컬럼만 사용.
- **트레이드오프:** OSIV를 끄면 커넥션 점유 시간이 트랜잭션으로 줄지만, 이후 컨트롤러·뷰에서 지연 로딩을 쓰면 `LazyInitializationException`이 남. 서비스가 DTO까지 만들어 반환하는 규칙을 지켜야 함. 트랜잭션 두 번 사이에 글이 삭제될 수 있어 재조회 단계에서 없는 글은 뺌.
- **재발 방지:** `AiServiceTest`, `AiRecommendationIntegrationTest`(Gemini 대기 중 트랜잭션·EntityManager 미바인딩 검증), `TeamFlowIntegrationTest`(OSIV off로 전체 API 흐름 호출).

### TS-13 모집글 목록 N+1
- **증상:** 목록 10건 응답을 만들 때 작성자 이름(`leader`), 기술 스택 연결, 기술 스택, (추가된) 포지션을 글마다 따로 조회.
- **해결:** `hibernate.default_batch_fetch_size: 100`으로 지연 로딩을 `IN (...)` 한 번으로 묶고, 목록 `findAll(Pageable)`에는 `@EntityGraph(leader)`, 팀원 목록에는 `@EntityGraph(applicant)`.
- **트레이드오프:** 컬렉션까지 fetch join하면 쿼리는 1개지만 페이징을 메모리에서 함(HHH90003004)·카테시안 곱이 생김. N:1은 EntityGraph, 1:N은 배치 페치로 나눔. 배치 크기가 크면 IN 절이 길어지지만 페이지 크기(≤50) 수준이라 문제 없음.
- **재발 방지:** `ProjectListQueryCountIntegrationTest`(작성자가 모두 다른 6건 목록의 쿼리 수 상한, Hibernate Statistics).

### TS-14 `ddl-auto: update`가 운영 스키마를 직접 변경 (부분 해결)
- **위험:** 기동할 때마다 Hibernate가 엔티티와 스키마를 비교해 컬럼·제약을 추가함. 타입 변경·컬럼 삭제·데이터 이전은 하지 않고, 실패해도 경고만 남기고 기동함(TS-04의 유니크 제약처럼). 무엇이 언제 바뀌었는지 기록이 없음.
- **이번에 한 일:** `ddl-auto: ${JPA_DDL_AUTO:update}`로 환경변수 분리, docker-compose·`.env.example`에 노출. 기본값 update는 유지.
- **바로 validate로 바꾸지 않은 이유:** 현재 EC2 배포와 로컬 compose가 프로필 없이 같은 설정을 쓰고, 이번 PR에도 스키마 변경(지원서 유니크 제약은 TS-04)이 있음. 운영 스키마가 엔티티와 맞는지 확인하지 않고 validate로 바꾸면 기동 자체가 실패할 수 있음.
- **남은 일:** ① 운영 DB에서 `SHOW CREATE TABLE`로 스키마 확인 → ② Flyway 도입(현재 스키마를 V1 baseline으로) → ③ 운영 `JPA_DDL_AUTO=validate`.

### TS-15 프론트 번들의 클라이언트 시크릿과 JWT 없는 소셜 로그인
- **증상:** `LoginPage`가 Google·Kakao 인가 코드를 브라우저에서 직접 토큰으로 교환함. 이를 위해 **Google 클라이언트 시크릿과 Kakao REST 키가 프론트 코드(=누구나 받는 JS 번들, 공개 저장소)에 들어 있었음.** 또 이 흐름은 우리 서버의 JWT를 받지 않아 로그인된 것처럼 보이기만 하고, `user_id` 자리에 Google/Kakao 계정 ID가 들어가 서버 API가 모두 실패함. 백엔드에는 Spring Security OAuth2 로그인이 이미 구현돼 있었음.
- **해결:** 소셜 버튼 → `{API}/oauth2/authorization/{provider}`(백엔드가 처리), 성공 시 `OAuth2AuthenticationSuccessHandler`가 `/oauth/callback?accessToken=...`으로 돌려보내고 프론트가 토큰 저장 후 `/api/auth/me`로 사용자 확인. `OAUTH2_REDIRECT_URI` 기본값을 프론트 개발 서버(5173)로 정정.
- **반드시 할 일:** 시크릿은 커밋 이력에 남아 있으므로 Google Cloud Console에서 클라이언트 시크릿을 재발급(기존 폐기), Kakao REST 키도 재발급. 각 콘솔에 백엔드 리디렉션 URI(`{백엔드}/login/oauth2/code/google`, `/kakao`)를 등록하고, 백엔드 환경변수(`GOOGLE_CLIENT_ID` 등)를 설정.

### TS-16 리뷰 중복을 조회로만 막음
- **증상/위험:** `saveBulkReviews`가 `existsByProjectIdAndReviewerIdAndRevieweeId` 조회 후 저장. 같은 리뷰 저장 요청 두 건(버튼 연타·두 탭)이 동시에 오면 둘 다 "없음"을 읽고 둘 다 INSERT → 같은 프로젝트에서 같은 팀원에게 리뷰 2건, 평점 평균이 왜곡됨. TS-04(중복 지원)와 같은 "조회 후 쓰기(check-then-act)" 경쟁.
- **원인:** MySQL 기본 격리 수준(REPEATABLE READ)의 일반 SELECT는 잠금 없는 스냅샷 읽기라 다른 트랜잭션의 커밋 전 INSERT를 보지 못함. 애플리케이션 조회로는 동시성을 보장할 수 없음.
- **해결:**
  - `review`에 `UNIQUE(reviewer_id, reviewee_id, project_id)`(`uk_review_reviewer_reviewee_project`). 두 번째 INSERT는 첫 트랜잭션의 커밋을 기다렸다가 중복 키로 실패.
  - 서비스는 저장 후 `flush()`로 위반을 메서드 안에서 확인하고, 제약 이름이 리뷰 유니크 제약이면 `ConflictException`(409, "이미 리뷰를 남긴 팀원이 있습니다.")으로 변환. 묶음 중 한 건이라도 중복이면 전체 롤백. 다른 무결성 위반은 그대로 전파.
  - `existsBy` 조회는 친절한 메시지와 불필요한 INSERT를 줄이는 빠른 검사로 남김(최종 판정은 DB).
- **트레이드오프:** 비관적 락(`SELECT ... FOR UPDATE`)은 "없는 행"을 잠그려면 갭 락이 필요해 범위가 넓고 데드락이 잦음. 유니크 제약은 잠금 범위가 인덱스 키 하나이고 코드가 단순하지만, 예외를 제약 이름으로 구분해야 하고(DB 종류에 따라 메시지가 다름) 실패 요청이 INSERT까지 가야 알 수 있음.
- **반드시 할 일(운영):** `ddl-auto=update`는 제약 추가가 실패하면 경고만 남기고 기동함(TS-14). 운영 DB에 이미 중복 리뷰가 있으면 제약이 생기지 않으니, 배포 전에 아래로 확인·정리:
  ```sql
  -- 중복 확인
  SELECT reviewer_id, reviewee_id, project_id, COUNT(*) AS cnt
  FROM review GROUP BY reviewer_id, reviewee_id, project_id HAVING cnt > 1;
  -- 배포 후 제약 생성 확인
  SHOW INDEX FROM review WHERE Key_name = 'uk_review_reviewer_reviewee_project';
  ```
- **재발 방지:** `ReviewServiceTest`(제약 위반 → 409, 다른 위반 → 재전파), `ReviewConcurrencyIntegrationTest`(MySQL에서 같은 리뷰 6건 동시 저장 → 1건 성공·5건 409·DB 1건, 조회를 거치지 않는 저장도 제약이 차단, API 재요청 409).

### TS-17 마이페이지 "내가 만든 프로젝트"가 비어 있음
- **증상:** 마이페이지가 `GET /api/projects`(첫 페이지 9건)를 받아 `leader_id`로 걸렀음. 설계상으로도 다른 사람 글이 많으면 내 글이 빠지는 구조였고, 실제로는 응답이 Spring `Page` 객체인데 `Array.isArray(data)`로 검사해 **항상 빈 목록**이었음. 지원 현황·참여 중 목록도 이 빈 목록에서 프로젝트를 찾아 붙이는 방식이라 함께 비었음.
- **원인:** "내 글" 같은 사용자별 필터를 서버가 아니라 클라이언트가 공개 목록으로 처리함. 페이지 응답 형식 변경(배열 → `Page`)이 이 화면에 반영되지 않았음.
- **해결:**
  - `GET /api/projects/me`(로그인 필요, 작성자 = 토큰 사용자, 페이징 기본 10건·최신순). `leader`는 `@EntityGraph`, 기술 스택·포지션은 배치 페치(TS-13). `leader_id`는 FK 인덱스가 있어 작성자 글만 읽음.
  - `SecurityConfig`에서 `/api/projects/me`를 공개 GET 규칙(`/api/projects/**`)보다 먼저 `authenticated()`로 지정. MVC는 리터럴 경로 `/me`가 `/{projectId}`보다 우선.
  - 지원서 응답에 `projectStatus`·`projectRegion`을 추가해, 지원 현황·참여 중 목록을 지원서 응답만으로 구성(필드 추가만, 기존 필드 유지). 팀장 자동 등록 지원서(`팀장`)는 두 목록에서 제외.
  - 화면은 첫 페이지 + "더 보기".
- **트레이드오프:** 페이지 크기 상한은 Spring Data 기본(2000)에 맡김. 작성자 글이 수천 건이 될 서비스가 아니라 별도 상한·커서 페이징은 두지 않음. 정렬(`created_at DESC`)은 작성자 글 안에서 filesort지만 건수가 작아 복합 인덱스 `(leader_id, created_at)`은 보류.
- **재발 방지:** `ProjectControllerSecurityTest`(비로그인 401, 토큰 사용자, `/me` 경로 매칭), `MyProjectsIntegrationTest`(12건 페이징, 남의 최신 글 제외), `TeamFlowIntegrationTest`(지원서 응답 필드).

### TS-18 모집글 수정 시 기술 스택 미반영
- **증상:** 수정 화면에서 기술 스택을 바꿔도 저장되지 않음. `ProjectRequestDto.techStackIds`를 받기만 하고 `updateProject`가 쓰지 않았음. 같이 발견: 생성 시 연결 행만 `ProjectTechStackRepository.saveAll`로 저장하고 엔티티 컬렉션에는 넣지 않아 **생성 응답의 `techStacks`가 빈 목록**.
- **해결:** `Project.replaceTechStacks()` — 유지되는 스택의 연결 행은 그대로 두고, 빠진 것만 컬렉션에서 제거(orphanRemoval로 DELETE), 새 것만 추가(cascade로 INSERT). `techStackIds`가 null이면 유지, 빈 목록이면 모두 해제, 중복 ID는 한 번만, 없는 ID는 무시(생성의 기존 동작과 같음). 생성도 같은 메서드를 써 응답에 포함.
- **트레이드오프:** "전부 지우고 다시 넣기"가 코드는 짧지만 수정할 때마다 연결 행 ID가 바뀌고 쿼리가 늘어남. 차이만 반영하면 변경이 없을 때 쿼리가 없음.
- **남은 일:** 글 작성 화면의 기술 스택 ID(1~43)가 `DataInitializer` 삽입 순서에 기대는 하드코딩이라, DB의 ID가 다르면 다른 스택이 저장되거나 무시될 수 있음 → 화면이 `GET /api/tech-stacks`를 쓰도록 바꾸는 것이 다음 단계.
- **재발 방지:** `ProjectUpdateIntegrationTest`(교체·필드 없음 유지·빈 목록 해제, 생성 응답 포함).

### TS-19 화면에 포지션 입력이 없어 정원 제한이 API로 만든 글에만 적용
- **증상:** TS-06에서 포지션 정원 로직을 넣었지만 글 작성·수정 화면에 포지션 입력이 없어, 화면으로 만든 글은 항상 "포지션 없음 = 정원 제한 없음". 지원 화면은 기술 스택 이름을 `", "`로 이어 `support_role`로 보내 포지션과 연결될 수도 없었음. 수정 API는 `positions`를 무시.
- **해결:**
  - 작성·수정 화면: 역할명·인원 입력(추천 프리셋 + 직접 입력, 최대 10개). 수정 화면은 합류 인원을 보여 주고 그보다 적게 줄이기·삭제·이름 변경을 막음. 지원 화면: 포지션이 있는 글은 포지션 하나를 골라 지원(`support_role` = 포지션 이름, 마감 포지션 비활성), 없는 글은 기존 방식 유지. 상세 화면에 포지션별 현재/정원.
  - 수정 API: `positions`가 null이면 유지, 목록이면 이름 기준으로 맞춤. 인원 변경은 `UPDATE ... SET required_count = ? WHERE id = ? AND current_count <= ?`, 삭제는 `DELETE ... WHERE id = ? AND current_count = 0`. 영향 행 0이면 409, 같은 요청의 다른 변경도 롤백. 반영 후 `flush/clear`하고 모집글을 다시 읽어 응답.
  - 엔티티 필드를 바꿔 dirty checking으로 저장하지 않은 이유: Hibernate의 UPDATE는 기본적으로 모든 컬럼을 쓰므로, 읽어 둔 `current_count`(예: 1)를 그대로 써서 그 사이 커밋된 수락(+1)을 덮어쓸 수 있음(lost update). 조건부 쿼리는 `required_count`만 바꾸고, InnoDB가 행 잠금 후 최신 커밋 값으로 조건을 다시 평가.
  - 같은 이름 두 번·`팀장`(작성자 자리 이름)·0명·11개 이상은 400.
  - `PositionSeats.take`: 포지션이 있는 글에서 지원서의 역할과 맞는 포지션이 없으면(수정으로 삭제된 포지션, 포지션 추가 전 지원서) 수락을 409로 막음. 이전에는 맞는 포지션이 없으면 정원 검사 없이 수락됐음.
- **트레이드오프:** 포지션을 이름(`role_name` = `support_role`)으로 연결하므로 이름 변경은 "삭제 + 추가"이고, 합류 인원이 있으면 막힘. `application.position_id` FK가 정석이지만 기존 지원서 데이터 이전이 필요해 이번에는 하지 않음(마이그레이션 없음). `(project_id, role_name)` 유니크 제약도 기존 데이터 확인이 필요해 보류(같은 작성자가 동시에 두 번 수정하는 경우만 해당).
- **재발 방지:** `PositionSeatsTest`(포지션 있는 글의 없는 역할 수락 409), `ProjectUpdateIntegrationTest`(변경·삭제·추가 동기화, 합류 인원 미만 축소·합류 포지션 삭제 409와 전체 롤백, 400 입력, 삭제된 포지션 지원서 수락 409, 인원 축소와 수락 동시 실행 시 정원 초과 없음).

### TS-20 실행 중 새로 생긴 기술 스택이 키워드 맵에 없음
- **증상:** 프로필 태그 저장 시 처음 보는 태그는 `tech_stack`에 자동 저장되지만, AI 추천의 키워드 맵(TS-10)은 기동 시 한 번만 적재돼 재시작 전까지 그 스택으로 매칭되지 않음. DB에서 직접 바꾼 `keywords`도 같음.
- **해결:**
  - `UserService`가 새 기술 스택을 저장하면 `TechStackCreatedEvent` 발행 → `TechKeywordMatcher`가 `@TransactionalEventListener(AFTER_COMMIT)`로 다시 적재. 커밋 전에 적재하면 다른 트랜잭션에서 아직 안 보이고, 롤백되면 없는 스택이 맵에 남으므로 커밋 후. AFTER_COMMIT 시점의 조회는 끝난 트랜잭션에 참여하지 않도록 `REQUIRES_NEW` 읽기 전용.
  - 주기 재적재(`@Scheduled`, 기본 10분, `TECH_KEYWORDS_RELOAD_INTERVAL_MS`): 이벤트가 닿지 않는 변경(DB 직접 수정, 서버 여러 대일 때 다른 인스턴스에서 생긴 스택)을 따라잡음. 실패하면 기존 맵 유지.
- **트레이드오프:** 이벤트는 같은 JVM 안에서만 전달됨. 서버가 여러 대면 다른 인스턴스는 주기 재적재까지 최대 10분 늦음(즉시 반영하려면 Redis pub/sub 등 필요). 재적재는 기술 스택 전체 조회(수십~수백 행)라 비용이 작고, 맵을 통째로 교체해 읽는 쪽은 잠금이 없음.
- **재발 방지:** `TechKeywordMatcherTest`(이벤트 후 매칭, 주기 재적재 실패 시 유지), `TechKeywordRefreshIntegrationTest`(MySQL: 프로필 저장 → 재시작 없이 매칭).
