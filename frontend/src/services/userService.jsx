import api from "./api";

const API_BASE_URL = "/api/users";

export const getUserProfile = async () => {
    try {
        const res = await api.get("/api/auth/me");
        return res.data;
    } catch {
        return null;
    }
};

export const getUserById = async (userId) => {
    try {
        const myInfo = JSON.parse(localStorage.getItem("user") || "{}");

        const res = await api.get(`${API_BASE_URL}/${userId}`);

        return {
            ...res.data,
            isMe: String(userId) === String(myInfo.user_id)
        };
    } catch {
        return null;
    }
};

export const updateUserProfile = async (newInfo) => {
    const res = await api.put(`${API_BASE_URL}/me/profile`, newInfo);

    const currentUser = JSON.parse(localStorage.getItem("user") || "{}");

    const updatedUser = {
        ...currentUser,
        ...newInfo
    };

    localStorage.setItem("user", JSON.stringify(updatedUser));

    return res.data;
};