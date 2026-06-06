-- 프론트엔드 프레임워크 / 라이브러리 (1~5)

UPDATE tech_stack SET keywords = 'react,리액트,리엑트,프론트,프론트엔드' WHERE tech_stack_id = 1;

UPDATE tech_stack SET keywords = 'vue,뷰,뷰제이에스' WHERE tech_stack_id = 2;

UPDATE tech_stack SET keywords = 'angular,앵귤러,엔귤러' WHERE tech_stack_id = 3;

UPDATE tech_stack SET keywords = 'next.js,next,넥스트,넥스트제이에스' WHERE tech_stack_id = 4;

UPDATE tech_stack SET keywords = 'svelte,스벨트' WHERE tech_stack_id = 5;



-- 기본 언어 및 마크업 (6~9)

UPDATE tech_stack SET keywords = 'typescript,타입스크립트,ts' WHERE tech_stack_id = 6;

UPDATE tech_stack SET keywords = 'javascript,자바스크립트,js' WHERE tech_stack_id = 7;

UPDATE tech_stack SET keywords = 'html/css,html,css,퍼블리싱,퍼블리셔,마크업' WHERE tech_stack_id = 8;

UPDATE tech_stack SET keywords = 'tailwind,테일윈드,테일윈드css' WHERE tech_stack_id = 9;



-- 백엔드 프레임워크 / 런타임 (10~16)

UPDATE tech_stack SET keywords = 'spring boot,spring,스프링,스프링부트,부트,백엔드' WHERE tech_stack_id = 10;

UPDATE tech_stack SET keywords = 'node.js,node,노드,노드제이에스' WHERE tech_stack_id = 11;

UPDATE tech_stack SET keywords = 'django,장고' WHERE tech_stack_id = 12;

UPDATE tech_stack SET keywords = 'fastapi,패스트api,패스트에이피아이' WHERE tech_stack_id = 13;

UPDATE tech_stack SET keywords = 'flask,플라스크' WHERE tech_stack_id = 14;

UPDATE tech_stack SET keywords = 'express,익스프레스' WHERE tech_stack_id = 15;

UPDATE tech_stack SET keywords = 'nestjs,네스트,네스트제이에스' WHERE tech_stack_id = 16;



-- 백엔드 프로그래밍 언어 (17~20)

UPDATE tech_stack SET keywords = 'java,자바' WHERE tech_stack_id = 17;

UPDATE tech_stack SET keywords = 'python,파이썬' WHERE tech_stack_id = 18;

UPDATE tech_stack SET keywords = 'go,golang,고랭,고언어' WHERE tech_stack_id = 19;

UPDATE tech_stack SET keywords = 'kotlin,코틀린' WHERE tech_stack_id = 20;



-- 데이터베이스 / 캐시 (21~27)

UPDATE tech_stack SET keywords = 'mysql,마이sql,마이시퀄' WHERE tech_stack_id = 21;

UPDATE tech_stack SET keywords = 'postgresql,포스트그레,포스트그레스,포그레' WHERE tech_stack_id = 22;

UPDATE tech_stack SET keywords = 'mongodb,몽고,몽고db' WHERE tech_stack_id = 23;

UPDATE tech_stack SET keywords = 'redis,레디스' WHERE tech_stack_id = 24;

UPDATE tech_stack SET keywords = 'oracle,오라클' WHERE tech_stack_id = 25;

UPDATE tech_stack SET keywords = 'sqlite,에스큐엘라이트' WHERE tech_stack_id = 26;

UPDATE tech_stack SET keywords = 'firebase,파이어베이스,파베' WHERE tech_stack_id = 27;



-- 클라우드 / 데브옵스 / 인프라 (28~36)

UPDATE tech_stack SET keywords = 'aws,아마존,에이더블유에스' WHERE tech_stack_id = 28;

UPDATE tech_stack SET keywords = 'docker,도커' WHERE tech_stack_id = 29;

UPDATE tech_stack SET keywords = 'kubernetes,쿠버네티스,쿠버,k8s' WHERE tech_stack_id = 30;

UPDATE tech_stack SET keywords = 'ci/cd,지속적통합,배포자동화,젠킨스,액션스' WHERE tech_stack_id = 31;

UPDATE tech_stack SET keywords = 'gcp,구글클라우드,지씨피' WHERE tech_stack_id = 32;

UPDATE tech_stack SET keywords = 'azure,애저' WHERE tech_stack_id = 33;

UPDATE tech_stack SET keywords = 'nginx,엔진엑스' WHERE tech_stack_id = 34;

UPDATE tech_stack SET keywords = 'linux,리눅스' WHERE tech_stack_id = 35;

UPDATE tech_stack SET keywords = 'git,깃,깃허브,github' WHERE tech_stack_id = 36;



-- 기획 / 디자인 / 게임 / 앱 (37~43)

UPDATE tech_stack SET keywords = 'figma,피그마,디자인,기획' WHERE tech_stack_id = 37;

UPDATE tech_stack SET keywords = 'unity,유니티,게임,게임개발' WHERE tech_stack_id = 38;

UPDATE tech_stack SET keywords = 'flutter,플러터,플러터앱' WHERE tech_stack_id = 39;

UPDATE tech_stack SET keywords = 'react native,rn,리액트네이티브,리엑트네이티브' WHERE tech_stack_id = 40;

UPDATE tech_stack SET keywords = 'swift,스위프트,ios,아이폰앱' WHERE tech_stack_id = 41;

UPDATE tech_stack SET keywords = 'kotlin(android),안드로이드,안드,android' WHERE tech_stack_id = 42;

UPDATE tech_stack SET keywords = 'kotlin(ios),ios,아이오에스,아이폰,케이엠엠,kmm' WHERE tech_stack_id = 43;USE team_builder;

SET NAMES utf8mb4;



DROP PROCEDURE IF EXISTS team_builder.InsertDummyProjects;



DELIMITER $$



CREATE PROCEDURE team_builder.InsertDummyProjects()

BEGIN

-- 1. 제어 및 카운터 변수

DECLARE i INT DEFAULT 1;

DECLARE tech_stack_id INT;

DECLARE secondary_stack_id INT;

DECLARE category INT;


-- 2. 동적 조립용 매트릭스 텍스트 변수

DECLARE tech_name VARCHAR(50);

DECLARE txt_prefix VARCHAR(100);

DECLARE txt_domain VARCHAR(150);

DECLARE txt_feature VARCHAR(255);

DECLARE txt_goal VARCHAR(255);


DECLARE rand_title VARCHAR(255);

DECLARE rand_content TEXT;

DECLARE rand_region VARCHAR(50);

DECLARE rand_term VARCHAR(20);

DECLARE rand_meeting VARCHAR(20);


-- 3. 매핑용 관계 고유 ID 변수

DECLARE current_project_id INT;

DECLARE position_loop INT;

DECLARE start_pos_idx INT;

DECLARE v_leader_id INT;



-- [보완 3] 외래키 제약조건 예방 안전장치

-- user 테이블에 데이터가 단 한 건도 없는 극단적 상황을 대비한 동적 ID 확보

BEGIN

DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET v_leader_id = 1;

SELECT id INTO v_leader_id FROM user LIMIT 1;

IF v_leader_id IS NULL THEN

SET v_leader_id = 1; -- 유저가 비어있다면 임시 안전 ID 설정

END IF;

END;



-- [보완 4, 5] 대량 적재 성능 최적화 및 안전 격리

-- 외래키 체크를 일시 중단하여 자식 테이블 트렁케이트 시 발생하는 무결성 락을 방지합니다.

SET FOREIGN_KEY_CHECKS = 0;


TRUNCATE TABLE project_tech_stack;

TRUNCATE TABLE project_position;

TRUNCATE TABLE project;


-- [보완 4] RBAR 부하 방지용 단일 통트랜잭션 시작

START TRANSACTION;



-- 1번부터 1000번까지 완전 독립적인 프로젝트 루프 시작

WHILE i <= 1000 DO

-- 43개 기술 스택 ID 매핑 (ID: 1 ~ 43 순환)

SET tech_stack_id = MOD(i - 1, 43) + 1;


-- 기술 스택 명칭 매칭

SET tech_name = ELT(tech_stack_id,

'React', 'Vue3', 'Angular', 'Next.js', 'Svelte', 'TypeScript', 'JavaScript', 'HTML/CSS', 'Tailwind CSS',

'Spring Boot', 'Node.js', 'Django', 'FastAPI', 'Flask', 'Express', 'NestJS', 'Java', 'Python', 'Go(Golang)', 'Kotlin Spring',

'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Oracle', 'SQLite', 'Firebase',

'AWS', 'Docker', 'Kubernetes', 'GitHub Actions', 'GCP', 'Azure', 'Nginx', 'Linux',

'Git', 'Figma', 'Unity', 'Flutter', 'React Native', 'Swift(SwiftUI)', 'Kotlin(Android)', 'Kotlin Multiplatform'

);



-- 기술 스택 기반 4대 직렬 도메인 분류

IF tech_stack_id <= 9 THEN SET category = 1; -- Frontend

ELSEIF tech_stack_id <= 20 THEN SET category = 2; -- Backend

ELSEIF tech_stack_id <= 35 THEN SET category = 3; -- DB / Infra / DevOps

ELSE SET category = 4; -- Mobile / Game / Collab

END IF;



-- 메타데이터 조합 무작위성 부여

SET rand_region = ELT(FLOOR(1 + RAND() * 6), '서울 마포구', '서울 강남구', '광주 북구', '부산 해운대구', '대전 유성구', '비대면 재택');

SET rand_term = ELT(FLOOR(1 + RAND() * 5), '1', '2', '3', '6', '12');

SET rand_meeting = IF(rand_region = '비대면 재택', '비대면', '대면/혼합');



-- 문장 데이터 무작위 조립 (수학적 확률 분산)

SET txt_prefix = ELT(FLOOR(1 + RAND() * 10), '실무 레벨의', '취업 포트폴리오용', '단기 고속 런칭 목적의', '사이드 허슬형', '트렌디한 기술 집약적', '대규모 트래픽 대비용', '오픈소스 기여 목적의', '현업자 멘토링 연계형', '핵심 기능 위주의 MVP', '아키텍처 리서치 중심');

SET txt_goal = ELT(FLOOR(1 + RAND() * 10), '실제 사용자를 모아 지표를 개선하는 경험을 지향합니다.', '기획과 디자인이 이미 완료되어 개발에만 집중하는 구조입니다.', '올해 안 글로벌 스토어 출시 및 런칭을 목표로 달립니다.', '포트폴리오에 확실한 한 줄을 남길 수 있는 고난도 챌린지입니다.', '코드 리뷰와 리팩토링을 격주로 빡세게 진행하며 성장합니다.', '현업 인프라 아키텍처를 그대로 프로젝트에 이식해봅니다.', '테스트 코드 작성과 CI/CD 구축 파이프라인 완성이 필수 조건입니다.', '매주 대면 오프라인 모임을 통해 몰입도 높게 스프린트를 진행합니다.', '수익 창출 시 정산 분배를 오픈하여 투명하게 정산합니다.', '기초부터 심화까지 스터디를 병행하며 집요하게 빌딩합니다.');



IF category = 1 THEN

SET txt_domain = ELT(FLOOR(1 + RAND() * 9), '어드민 대시보드', '커뮤니티 포탈 웹', '이커머스 프론트엔드', '실시간 데이터 시각화 패널', '디지털 포트폴리오 쇼케이스', '전사 공통 디자인 시스템 가이드', '웹기반 마크다운 문서 에디터', '개인 블로그 오픈 플랫폼', '구독형 서비스 반응형 랜딩페이지');

SET txt_feature = ELT(FLOOR(1 + RAND() * 5), '컴포넌트 렌더링 최적화 및 내부 상태 트리 제어', '글로벌 상태 관리 아키텍처 정립과 비동기 스트림 파이프라인 처리', 'SEO 검색 엔진 최적화 봇 대응 및 웹 접근성 표준 지침 준수', '다크모드 완전 테마 전환 인터랙션 및 마이크로 인터랙션 모션', '반응형 UI 플렉서블 레이아웃과 모듈러 디자인 컴포넌트 팩 적용');

ELSEIF category = 2 THEN

SET txt_domain = ELT(FLOOR(1 + RAND() * 8), '대용량 정산 시스템 코어', '실시간 알림 및 인앱 메시징 백엔드', '공공데이터 연동 대량 집계 API', '비동기 분산 트래픽 처리 인프라', '마이크로서비스 아키텍처(MSA) 코어 뱅킹', 'PG사 결제 연동 및 이중 검증 모듈', '보안 인증 및 권한 통제 게이트웨이', '웹 크롤링 엔진 및 데이터 파이프라인');

SET txt_feature = ELT(FLOOR(1 + RAND() * 5), '비동기 논블로킹 I/O 스레드 모델 구조 안정화 및 커스텀 제어', 'RESTful API 엔드포인트 명세 자동화 및 정밀 유효성 검증 레이어', '객체지향 SOLID 5대 원칙에 입각한 가독성 높은 클린 코드 리팩토링', '분산 트랜잭션 정합성 보장 및 무거운 스케줄러 배치 고도화', '보안 보안 필터링 레이어링 및 OAuth2 기반 소셜 통합 인증망 연동');

ELSEIF category = 3 THEN

SET txt_domain = ELT(FLOOR(1 + RAND() * 8), '관계형 데이터 모델링 및 인덱스 정밀 튜닝망', '포스트 데이터 인프라 기반 공간 반경 검색 엔진', '대용량 가상 서비스 접속 로그 수집 및 샤딩 스토어', '인메모리 고속 분산 락 및 캐싱 추상화 레이어', '가상 VPC 네트워크 파티셔닝 및 클라우드 인프라 아키텍처', 'CI/CD 빌드 통합 및 무중단 롤링 배포 자동화 파이프라인', '컨테이너 오케스트레이션 및 가상 노드 복구 인프라', '리버스 프록시 스케일아웃 및 SSL 포워딩 로드 밸런싱 세팅');

SET txt_feature = ELT(FLOOR(1 + RAND() * 5), '실행계획(Explain) 분석을 통한 Slow Query 병목 구간 인덱스 최적화', '격리 수준(Isolation Level) 제어 및 복잡한 데드락 트래킹 인프라 구축', '멀티 스테이지 빌드 파이프라인을 통한 도커 컨테이너 이미지 경량화', 'HPA 오토스케일링 및 가상 파드 자가 치유(Self-healing) 아키텍처 검증', 'Linux CLI 우분투 환경 시스템 자동화 및 크론탭 백업 스크립트 연동');

ELSE

SET txt_domain = ELT(FLOOR(1 + RAND() * 6), '하이브리드 크로스플랫폼 모바일 앱', '네이티브 단독 아이폰/안드로이드 앱', '3D 쿼터뷰 로그라이크 가상 모바일 게임', '실시간 위치 기반 카풀 매칭 서비스', '전사 표준 모바일 웹 와이어프레임 가이드 시스템', '공통 비즈니스 로직을 하나로 공유하는 크로스 멀티플랫폼');

SET txt_feature = ELT(FLOOR(1 + RAND() * 5), '기기 로컬 하드웨어 센서(GPS, 비콘, 푸시 알림) 직접 제어 및 권한 최적화', '오브젝트 가상 물리 충돌 판정 및 유한상태머신(FSM) AI 알고리즘 고도화', '피그마 컴포넌트 변수화 기반 프론트엔드 개발자와의 매끄러운 핸드오프 리드', 'Photon Engine 네트워크 동기화 룸 빌딩 및 렌더러 드로우콜 가속화', '선언형 레이아웃 엔진 최신 컴포넌트 구조 기반 상태 관리 흐름 설계');

END IF;



SET rand_title = CONCAT(txt_prefix, ' ', tech_name, ' 기반 ', txt_domain);

SET rand_content = CONCAT('안녕하세요! 이번 스프린트는 ', tech_name, ' 생태계를 코어로 삼아 ', txt_domain, ' 서비스를 구축하는 것을 골자로 합니다. 메인 태스크로 ', txt_feature, ' 기술 요구사항을 완성도 있게 해결해 나갈 예정입니다. ', txt_goal);



-- 1) 부모 프로젝트 테이블 행 삽입

INSERT INTO project (

title, content, region, term, status, leader_id, meeting_type, is_local_only, created_at, updated_at

) VALUES (

CONCAT('[ID-', i, '] ', rand_title),

rand_content,

rand_region,

rand_term,

'모집중',

v_leader_id,

rand_meeting,

0,

DATE_SUB(NOW(), INTERVAL i * 15 MINUTE),

DATE_SUB(NOW(), INTERVAL i * 15 MINUTE)

);



SET current_project_id = LAST_INSERT_ID();



-- [보완 2] 모집 포지션 중복 등록 원천 차단 알고리즘 (순환 시프트 기법)

SET position_loop = FLOOR(1 + RAND() * 2);

SET start_pos_idx = FLOOR(1 + RAND() * 4);


INSERT INTO project_position (role_name, required_count, current_count, project_id, created_at, updated_at)

VALUES (

ELT(start_pos_idx, '백엔드 개발자', '프론트엔드 개발자', '디자이너', '기획자'),

FLOOR(2 + RAND() * 2), FLOOR(RAND() * 2), current_project_id, NOW(), NOW()

);


IF position_loop = 2 THEN

INSERT INTO project_position (role_name, required_count, current_count, project_id, created_at, updated_at)

VALUES (

ELT(MOD(start_pos_idx, 4) + 1, '백엔드 개발자', '프론트엔드 개발자', '디자이너', '기획자'),

FLOOR(2 + RAND() * 2), FLOOR(RAND() * 2), current_project_id, NOW(), NOW()

);

END IF;



-- [보완 1] 메인 스택과 보조 스택 간의 복합 유니크 키 충돌 우회 기법

INSERT INTO project_tech_stack (project_id, tech_stack_id) VALUES (current_project_id, tech_stack_id);



SET secondary_stack_id = CASE

WHEN category = 1 THEN 36 -- Git

WHEN category = 2 THEN 21 -- MySQL

WHEN category = 3 THEN 29 -- Docker

ELSE 37 -- Figma

END;



-- 핵심 보완: 메인 기술과 보조 기술이 다를 때만 보조 스택 추가

IF tech_stack_id <> secondary_stack_id THEN

INSERT INTO project_tech_stack (project_id, tech_stack_id) VALUES (current_project_id, secondary_stack_id);

END IF;



SET i = i + 1;

END WHILE;


-- 전체 트랜잭션 정상 커밋 및 외래키 잠금 복구

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;


END $$



DELIMITER ;



-- 프로시저 무결성 실행 및 결과 도출

CALL team_builder.InsertDummyProjects();

SELECT COUNT(*) AS '최종 적재 완료된 완전 독립 프로젝트 수' FROM project; 