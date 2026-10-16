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
- **남은 일:** 화면에 포지션 입력 UI가 없어 지금은 API로 만든 글에만 정원이 적용됨. `support_role` 문자열 대신 `application.position_id` FK로 바꾸는 것은 데이터 이전이 필요해 보류.
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
- **남은 일:** 실행 중 새로 생긴 기술 스택(프로필 태그 저장 시 자동 생성)은 재시작 전까지 맵에 없음.
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

