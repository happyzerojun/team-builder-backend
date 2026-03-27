import axios from 'axios';

const USER_API_URL = "https://69c516f18a5b6e2dec2bcb85.mockapi.io/api/user";

export const getUserProfile = async () => {
    try {
        const savedUser = JSON.parse(localStorage.getItem("user"));
        if (!savedUser || !savedUser.id) return null;

        // 서버에 내 ID(숫자 등)로 데이터 요청
        const response = await axios.get(`${USER_API_URL}/${savedUser.id}`);
        return response.data;
    } catch (error) {
        console.error("내 프로필 로드 실패 (DB):", error);
        return null;
    }
};


export const getUserById = async (userId) => {
    try {
        const myInfo = JSON.parse(localStorage.getItem("user") || "{}");

        const targetId = userId || myInfo.id;

        if (!targetId) return null;

        const response = await axios.get(`${USER_API_URL}/${targetId}`);

        return {
            ...response.data,
            isMe: !userId || String(userId) === String(myInfo.id)
        };
    } catch (error) {
        console.error("유저 정보 로드 실패 (DB 404 등):", error);
        return null;
    }
};

export const updateUserProfile = async (newInfo) => {
    try {
        const savedUser = JSON.parse(localStorage.getItem("user"));
        if (!savedUser || !savedUser.id) throw new Error("로그인이 필요합니다.");

        const response = await axios.put(`${USER_API_URL}/${savedUser.id}`, newInfo);

        localStorage.setItem("user", JSON.stringify(response.data));

        return response.data;
    } catch (error) {
        console.error("프로필 수정 실패 (DB):", error);
        return null;
    }
};