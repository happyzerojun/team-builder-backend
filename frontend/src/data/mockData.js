// 📁 src/data/mockData.js
// 실제 서버 연동 전까지 사용할 가짜(더미) 데이터입니다.
// 나중에 백엔드 API가 준비되면 이 데이터를 API 호출로 교체하면 됩니다.

export const MOCK_POSTS = [
  {
    id: 1,
    title: "AI 기반 일정 관리 앱 팀원 모집",
    description:
      "ChatGPT API를 활용해 자연어로 일정을 등록하고 관리할 수 있는 모바일 앱을 개발합니다. 사용자가 '다음 주 화요일 오후 3시에 병원 예약'처럼 자연어로 입력하면 자동으로 캘린더에 등록되는 기능을 구현할 예정입니다.",
    category: "앱 개발",
    roles: ["백엔드 개발자", "iOS 개발자"],
    headcount: 3,
    tags: ["React Native", "Node.js", "OpenAI API", "MongoDB"],
    author: "김지훈",
    duration: "장기",
    level: "고수",
    hasTeamExp: "있음",
    createdAt: "2025-03-10",
    applyType: "google",
    googleFormLink: "https://docs.google.com/forms/d/e/YOUR_FORM_ID/viewform"
  },
  {
    id: 2,
    title: "중고거래 플랫폼 클론 코딩 (포트폴리오용)",
    description:
      "당근마켓 스타일의 중고거래 플랫폼을 처음부터 직접 만들어보는 프로젝트입니다. 실시간 채팅, 위치 기반 검색, 이미지 업로드 등의 기능을 구현할 예정이며, 취업 포트폴리오로 활용할 수 있습니다.",
    category: "웹 개발",
    roles: ["프론트엔드 개발자", "디자이너"],
    headcount: 4,
    tags: ["React", "Spring Boot", "MySQL", "WebSocket"],
    author: "박소연",
    duration: "단기",
    level: "중급",      
    hasTeamExp: "있음",
    createdAt: "2025-03-09",
    applyType: "kakao",
    kakaoLink: "https://open.kakao.com/o/YOUR_ROOM_ID"
  },
  {
    id: 3,
    title: "운동 루틴 공유 커뮤니티 개발",
    description:
      "헬스, 요가, 러닝 등 다양한 운동 루틴을 공유하고 함께 챌린지를 진행하는 커뮤니티 플랫폼입니다. 운동 기록 시각화, SNS 연동 공유 기능을 포함할 예정입니다.",
    category: "웹/앱 개발",
    roles: ["풀스택 개발자", "UI/UX 디자이너"],
    headcount: 3,
    tags: ["Next.js", "TypeScript", "PostgreSQL", "Tailwind"],
    author: "이민준",
    duration: "단기",
    level: "초보",      
    hasTeamExp: "없음",
    createdAt: "2025-03-08",
    applyType: "kakao",
    kakaoLink: "https://open.kakao.com/o/YOUR_ROOM_ID"
  },
  {
    id: 4,
    title: "대학교 강의 리뷰 플랫폼",
    description:
      "재학생들이 수강한 강의에 대한 솔직한 리뷰를 남기고 열람할 수 있는 플랫폼입니다. 학교 이메일 인증을 통한 신뢰성 있는 리뷰 시스템을 구축하고자 합니다.",
    category: "웹 개발",
    roles: ["백엔드 개발자", "프론트엔드 개발자"],
    headcount: 2,
    tags: ["Vue.js", "Django", "SQLite", "Python"],
    author: "최예림",
    duration: "단기",
    level: "초보",      
    hasTeamExp: "없음",
    createdAt: "2025-03-07",
    applyType: "google",
    googleFormLink: "https://docs.google.com/forms/d/e/YOUR_FORM_ID/viewform"
  },
  {
    id: 5,
    title: "실시간 협업 코드 에디터",
    description:
      "Google Docs처럼 여러 명이 동시에 같은 코드를 편집할 수 있는 실시간 협업 에디터를 만듭니다. 코딩 면접이나 팀 프로젝트에서 사용할 수 있는 도구를 목표로 합니다.",
    category: "웹 개발",
    roles: ["풀스택 개발자"],
    headcount: 2,
    tags: ["React", "Socket.io", "Express", "Redis"],
    author: "정도현",
    duration: "장기",
    level: "고수",      
    hasTeamExp: "있음",
    createdAt: "2025-03-06",
    applyType: "kakao",
    kakaoLink: "https://open.kakao.com/o/YOUR_ROOM_ID"
  },
  {
    id: 6,
    title: "여행 경비 정산 앱 (더치페이)",
    description:
      "여행 중 발생하는 공동 경비를 자동으로 정산해주는 앱입니다. 영수증 촬영으로 자동 금액 입력, 1/N 또는 비율 정산, 카카오페이 송금 연동을 목표로 합니다.",
    category: "앱 개발",
    roles: ["Android 개발자", "백엔드 개발자"],
    headcount: 3,
    tags: ["Flutter", "Firebase", "Dart", "REST API"],
    author: "한수진",
    duration: "단기",
    level: "초보",      
    hasTeamExp: "있음",
    createdAt: "2025-03-05",
    applyType: "google",
    googleFormLink: "https://docs.google.com/forms/d/e/YOUR_FORM_ID/viewform"
  },
];

// 필터링에 사용할 태그 목록
export const ALL_TAGS = [
  "React", "Vue.js", "Next.js", "TypeScript",
  "Node.js", "Spring Boot", "Django", "Express",
  "React Native", "Flutter", "MySQL", "MongoDB",
  "PostgreSQL", "Firebase", "Tailwind", "Socket.io",
];
