import api from "./api";

const API_URL = "/api/auth";

export const authService = {
    signup: async (userData) => {
        const res = await api.post(`${API_URL}/signup`, userData);
        return res.data;
    },

    login: async (credentials) => {
        const res = await api.post(`${API_URL}/login`, credentials);

        if (res.data && res.data.accessToken) {
            await authService.completeSocialLogin(res.data.accessToken);
        }

        return res.data;
    },

    startSocialLogin: async (provider) => {
        const res = await api.get(`${API_URL}/oauth2/url/${provider}`);
        window.location.assign(new URL(res.data, api.defaults.baseURL).toString());
    },

    completeSocialLogin: async (token) => {
        localStorage.setItem("token", token);
        try {
            const userRes = await api.get(`${API_URL}/me`);
            localStorage.setItem("user", JSON.stringify({
                user_id: userRes.data.userId,
                email: userRes.data.email,
                name: userRes.data.name
            }));
            localStorage.setItem("isLoggedIn", "true");
        } catch (error) {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            localStorage.removeItem("isLoggedIn");
            throw error;
        }
    },

    logout: () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        localStorage.removeItem("isLoggedIn");
        window.location.href = "/login";
    },

    getCurrentUser: () => {
        try {
            return JSON.parse(localStorage.getItem("user"));
        } catch {
            return null;
        }
    }
};
