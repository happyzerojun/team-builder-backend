# 트러블슈팅 기록

캡스톤 종료 후 셀프 코드 리뷰로 찾은 문제와 해결 과정이에요. 형식은 **증상 → 원인 → 해결 → 재발 방지**예요.

| ID | 날짜 | 분류 | 제목 | 상태 |
|---|---|---|---|---|
| TS-01 | 2026-03 ~ 05 | 협업 | 공용 파일(`build.gradle`, `application.yml`, `User`, CORS)에 병합 충돌이 몰림 | 운영 규칙으로 대응 |
| TS-02 | 2026-10-14 | API | catch-all 예외 핸들러가 내부 예외 메시지를 응답으로 노출 | 해결 |
| TS-03 | 2026-10-14 | AI | Gemini 호출에 타임아웃 없음, 키워드 매칭 비결정적, API 키가 URL에 노출 | 해결 (커넥션 점유는 남은 일) |
| TS-04 | 2026-10-14 | 도메인 | 지원서 상태가 아무 문자열이나 저장되고, 중복 지원을 조회로만 막음 | 해결 |
| TS-05 | 2026-10-14 | 보안 | `anyRequest().permitAll()` 상태로 배포, 요청 본문의 사용자 ID를 신뢰 | 남은 일 (프론트 연동과 함께) |
| TS-06 | 2026-10-14 | 도메인 | 포지션 정원 메서드(`increaseCurrentCount`)가 어디서도 호출되지 않음 | 남은 일 |
| TS-07 | 2026-10-14 | 테스트 | CI를 붙이자 `@WebMvcTest` 7건이 컨텍스트 로딩 실패 | 해결 |

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
- **남은 일:** `@Transactional(readOnly = true)` 안에서 Gemini를 호출해 응답을 기다리는 동안 DB 커넥션을 쥐고 있음. 단, Spring Boot 기본값인 **OSIV(`spring.jpa.open-in-view=true`)** 때문에 트랜잭션만 빼서는 커넥션이 반환되지 않아요. 후보 조회를 별도 서비스로 분리하고 OSIV를 끄는 작업을 지연 로딩 영향 범위 확인과 함께 진행할 예정이에요.

### TS-04 지원서 상태와 중복 지원
- **증상:** `updateStatus(String)`이 아무 값이나 저장 → 오타 저장, 거절→수락 역행 가능. 중복 지원은 `existsBy...` 조회 후 저장이라 동시 요청 두 건이 모두 통과 가능.
- **해결:** `Application.updateStatus()`가 `PENDING → ACCEPTED/REJECTED`만 허용(같은 상태 재요청은 멱등, 소문자 요청·기존 데이터 정규화). `UNIQUE(applicant_id, project_id)` 추가, 위반 시 409.
- **재발 방지:** `ApplicationStatusTest`로 전이 규칙 고정.
- **주의:** `ddl-auto=update`로 유니크 제약이 추가돼요. 기존 데이터에 같은 사용자·프로젝트 지원서가 2건 이상 있으면 제약 추가가 실패하니 먼저 정리가 필요해요.

### TS-05 인가 없이 배포된 API (남은 일)
- 프론트 연동 테스트를 위해 연 `anyRequest().permitAll()`이 그대로 남아 있고, 지원서 생성은 본문의 `applicant_id`, 내 지원 목록은 URL의 `userId`를 사용해요. 다른 사람 이름으로 지원하거나 남의 지원 내역을 볼 수 있는 구조(IDOR)예요.
- 프론트가 본문·URL로 ID를 보내고 있어 백엔드만 바꾸면 화면이 깨져요. **토큰의 사용자로 식별 + 소유권 검사(`@PreAuthorize`) + 기본 닫힘** 설정을 프론트 수정과 한 PR에서 진행할 예정이에요.

### TS-06 포지션 정원 메서드가 호출되지 않음 (남은 일)
- `ProjectPosition.increaseCurrentCount()`가 정원 초과를 막지만, 지원서의 `support_role`이 문자열이라 어느 포지션인지 연결되지 않아 수락 흐름에서 호출되지 않아요.
- `application.position_id` FK 추가 후, 수락 시 `UPDATE ... SET current_count = current_count + 1 WHERE id = ? AND current_count < required_count` 조건부 UPDATE로 동시 수락까지 막을 예정이에요.

### TS-07 CI를 붙이자 `@WebMvcTest` 7건이 컨텍스트 로딩 실패
- **증상:** 첫 CI 실행에서 `21 tests completed, 7 failed`. `AuthControllerApiTest`, `HomeControllerApiTest` 전부 `IllegalStateException at DefaultCacheAwareContextLoaderDelegate` / `NoSuchBeanDefinitionException`.
- **원인:** CI가 없어서 그동안 아무도 몰랐던 기존 실패예요.
  1. `AuthController`가 `UserService`를 주입받도록 바뀌었는데 테스트에 `@MockBean UserService`가 없음.
  2. 메인 클래스의 `@EnableJpaAuditing`이 웹 슬라이스 테스트(`@WebMvcTest`)에서도 JPA 메타모델을 요구함.
- **해결:** `@EnableJpaAuditing`을 `config/JpaAuditingConfig`로 옮기고, 테스트에 `UserService` 목을 추가.
- **재발 방지:** PR마다 CI가 테스트를 돌려요. 컨트롤러 생성자에 의존성을 추가하면 해당 `@WebMvcTest`도 같은 PR에서 고쳐요.

