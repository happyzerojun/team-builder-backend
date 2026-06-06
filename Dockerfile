# === 1단계: 빌드 스테이지 (의존성 캐싱 및 컴파일) ===
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1. 영준님의 프로젝트 구조(상대경로)에 맞춰 Gradle 설정 파일만 먼저 쏙 빼서 복사합니다.
COPY ../../../../../../gradlew .
COPY ../../../../../../gradle gradle
COPY ../../../../../../build.gradle .
COPY ../../../../../../settings.gradle .

# 🔥 [영준님 커스텀 1] 윈도우 환경에서 넘어온 gradlew에 리눅스 실행 권한 부여!
RUN chmod +x ./gradlew

# 2. 소스 코드가 없는 상태에서 라이브러리만 먼저 다운로드하여 도커 캐시에 굽습니다.
# 이 레이어 덕분에 앞으로 자바 코드가 바뀌어도 12분씩 기다리지 않습니다.
RUN ./gradlew dependencies --no-daemon

# 3. 의존성 다운로드가 끝나면 이제 전체 소스 코드를 복사합니다.
COPY ../../../../../.. .

# 4. 컴파일 및 빌드 진행
RUN ./gradlew build -x test --no-daemon


# === 2단계: 실행 스테이지 (최종 경량화 이미지 생성) ===
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 1단계 빌드 스테이지에서 완성된 jar 파일만 쏙 빼옵니다.
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# 🔥 [영준님 커스텀 2] 기존에 쓰시던 UTF-8 인코딩 옵션들을 그대로 유지하여 실행합니다.
CMD ["java", "-Dfile.encoding=UTF-8", "-Dserver.servlet.encoding.charset=UTF-8", "-jar", "app.jar"]