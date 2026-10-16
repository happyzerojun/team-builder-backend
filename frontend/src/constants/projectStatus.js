// 모집글 상태 값. 백엔드 ProjectStatus enum의 라벨과 같아야 한다 (TS-09).
// 화면 곳곳에 "모집중" 같은 리터럴을 직접 쓰지 말고 이 상수를 비교에 사용한다.
export const PROJECT_STATUS = Object.freeze({
    RECRUITING: "모집중",
    PROCEEDING: "진행중",
    COMPLETED: "종료됨",
});
