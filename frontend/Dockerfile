# 1. 가볍고 빠른 Node.js 20버전(Alpine 리눅스)을 가져옵니다.
FROM node:20-alpine

# 2. 도커 컨테이너 내부의 작업 폴더를 /app 으로 지정합니다.
WORKDIR /app

# 3. 라이브러리 목록이 적힌 package.json을 먼저 복사합니다.
COPY package*.json ./

# 4. 무원님이 사용한 프론트엔드 라이브러리들을 설치합니다.
RUN npm install

# 5. 나머지 모든 프론트엔드 소스 코드를 복사합니다.
COPY . .

# 🔥 [핵심 포인트] Vite 서버를 도커에서 열 때는 무조건 --host 옵션이 필요합니다!
CMD ["npm", "run", "dev", "--", "--host"]