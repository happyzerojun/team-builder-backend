import axios from 'axios';

const API_BASE_URL = "http://localhost:8080/api/users";

// 내 프로필 정보 가져오기
export const getUserProfile = async () => {
    try {
        const token = localStorage.getItem("token");
        if (!token) return null;

        const response = await axios.get(`${API_BASE_URL}/me`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("내 프로필 로드 실패 (DB):", error);
        return null;
    }
};

//유저 정보 가져오기 (남의 프로필 볼 때)
export const getUserById = async (userId) => {
    try {
        const myInfo = JSON.parse(localStorage.getItem("user") || "{}");
        const token = localStorage.getItem("token");
        
        if (!userId) return null;

        const response = await axios.get(`${API_BASE_URL}/${userId}`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        return {
            ...response.data, 
            isMe: String(userId) === String(myInfo.id)
        };
    } catch (error) {
        console.error("유저 정보 로드 실패:", error);
        return null;
    }
};

//내 프로필 수정
export const updateUserProfile = async (newInfo) => {
    try {
        const token = localStorage.getItem("token");
        if (!token) throw new Error("로그인이 필요합니다.");

        const response = await axios.put(`${API_BASE_URL}/me/profile`, newInfo, {
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        localStorage.setItem("user", JSON.stringify(response.data));

        return response.data;
    } catch (error) {
        console.error("프로필 수정 실패:", error);
        throw error; 
    }
};