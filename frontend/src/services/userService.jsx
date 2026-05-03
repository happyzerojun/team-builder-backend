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


//  * 2. 특정 유저의 정보 가져오기 (타인 프로필 조회 등)
//  * [GET] /api/users/{userId}
//  * @param {number|string} userId 조회할 유저의 PK(id)
//  * @returns {Object|null} 유저 데이터 + 본인 여부(isMe) 포함

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

// /**
//  * 3. 내 프로필 정보 수정하기
//  * [PUT] /api/users/me/profile
//  * @param {Object} newInfo 수정할 프로필 데이터 (Payload)
//  * @returns {Object} 서버에서 반환한 최신 유저 정보
//  */
export const updateUserProfile = async (newInfo) => {
    // 1. API 통신
    const res = await api.put(`${API_BASE_URL}/me/profile`, newInfo);

    // 2. 기존 브라우저 저장소에 있던 내 전체 정보를 일단 꺼내옵니다.
    const currentUser = JSON.parse(localStorage.getItem("user") || "{}");

    // 3. 기존 정보 위에 백엔드가 돌려준 최신 정보를 덮어씌웁니다. (전체 데이터 보존!)
    const updatedUser = {
        ...currentUser,
        ...res.data,
        // 🚨 혹시 백엔드가 id로 주고 프론트는 user_id를 쓴다면 맞춰줍니다.
        user_id: res.data.id || res.data.user_id || currentUser.user_id
    };

    // 4. 안전하게 다시 저장!
    localStorage.setItem("user", JSON.stringify(updatedUser));

    return res.data;
};