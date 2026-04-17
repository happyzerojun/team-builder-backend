import axios from 'axios';

const API_BASE_URL = "http://localhost:8080/api/users";

export const getUserProfile = async () => {
    try {
        const res = await axios.get(`${API_BASE_URL}/me`);
        return res.data;
    } catch {
        return null;
    }
};

export const getUserById = async (userId) => {
    try {
        const myInfo = JSON.parse(localStorage.getItem("user") || "{}");

        const res = await axios.get(`${API_BASE_URL}/${userId}`);

        return {
            ...res.data,
            isMe: String(userId) === String(myInfo.user_id)
        };
    } catch {
        return null;
    }
};

export const updateUserProfile = async (newInfo) => {
    const res = await axios.put(`${API_BASE_URL}/me/profile`, newInfo);

    const updatedUser = {
        user_id: res.data.user_id,
        email: res.data.email,
        name: res.data.name
    };

    localStorage.setItem("user", JSON.stringify(updatedUser));

    return res.data;
};