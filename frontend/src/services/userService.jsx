import api from "./api"; // 커스텀 설정된 axios 인스턴스 (BaseURL, Header 설정 등이 포함됨)

const API_BASE_URL = "/api/users"; // 유저 관련 API 공통 경로

/**
 * 1. 내 프로필 정보 가져오기 (현재 로그인한 사용자 전용)
 * [GET] /api/users/me
 * @returns {Object|null} 유저 정보 객체 또는 실패 시 null
 */
export const getUserProfile = async () => {
    try {
        const res = await api.get("/api/auth/me");
        return res.data;
    } catch {
        return null;
    }
};

/**
 * 2. 특정 유저의 정보 가져오기 (타인 프로필 조회 등)
 * [GET] /api/users/{userId}
 * @param {number|string} userId 조회할 유저의 PK(id)
 * @returns {Object|null} 유저 데이터 + 본인 여부(isMe) 포함
 */
export const getUserById = async (userId) => {
    try {
        // 로컬스토리지에서 현재 로그인한 유저 정보를 꺼내옵니다. (본인 확인용)
        const myInfo = JSON.parse(localStorage.getItem("user") || "{}");

        // 특정 ID의 유저 정보를 요청합니다.
        const res = await api.get(`${API_BASE_URL}/${userId}`);

        // 응답 데이터에 '이 프로필이 나 자신인지' 판단하는 flag(isMe)를 추가합니다.
        // String()으로 감싸는 이유는 PK 타입(int/long)이 다를 경우 생기는 비교 오류를 방지하기 위함입니다.
        return {
            ...res.data,
            isMe: String(userId) === String(myInfo.user_id)
        };
    } catch (error) {
        console.error("유저 정보 조회 실패:", error);
        return null;
    }
};

/**
 * 3. 내 프로필 정보 수정하기
 * [PUT] /api/users/me/profile
 * @param {Object} newInfo 수정할 프로필 데이터 (Payload)
 * @returns {Object} 서버에서 반환한 최신 유저 정보
 */
export const updateUserProfile = async (newInfo) => {
    // 백엔드의 UserController @PutMapping("/me/profile")와 통신합니다.
    // 사진(Base64), 닉네임, 기술스택 리스트 등이 newInfo에 담겨 전송됩니다.
    const res = await api.put(`${API_BASE_URL}/me/profile`, newInfo);

    // 🚨 [주의] 백엔드 응답(res.data)에 user_id, email, name이 반드시 포함되어야 합니다.
    // 만약 백엔드에서 "성공"이라는 문자열만 보내면 아래 객체는 모두 undefined가 됩니다.
    const updatedUser = {
        user_id: res.data.user_id, // 서버에서 리턴한 PK
        email: res.data.email,     // 서버에서 리턴한 이메일
        name: res.data.name        // 서버에서 리턴한 이름(혹은 닉네임)
    };

    // 💾 수정한 정보를 브라우저 저장소(LocalStorage)에 갱신합니다.
    // 이렇게 해야 페이지를 새로고침해도 상단 헤더에 바뀐 내 정보가 즉시 반영됩니다.
    localStorage.setItem("user", JSON.stringify(updatedUser));

    return res.data; // 최신화된 전체 유저 데이터를 반환
};