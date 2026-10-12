# === 1단계: 빌드 (의존성 레이어 캐싱 후 컴파일) ===
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle 설정 파일만 먼저 복사해 의존성 다운로드 레이어를 캐싱
# → 소스 코드만 바뀌면 의존성을 다시 받지 않아 빌드 시간이 크게 줄어듦
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
# Windows에서 커밋된 gradlew에 실행 권한 부여
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew build -x test --no-daemon

# === 2단계: 실행 (빌드 산출물 jar만 포함) ===
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-Dfile.encoding=UTF-8", "-Dserver.servlet.encoding.charset=UTF-8", "-jar", "app.jar"]
