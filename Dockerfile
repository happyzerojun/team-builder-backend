FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY ../../../../../.. .

# 🔥 영준님 추가: 윈도우 환경에서 넘어온 gradlew에 리눅스 실행 권한 부여!
RUN chmod +x ./gradlew

RUN ./gradlew build -x test

# 주의: build/libs/ 폴더 안에 실제로 저 이름의 jar 파일이 생기는지 확인 필요!
CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]