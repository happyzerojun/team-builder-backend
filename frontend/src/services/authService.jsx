import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

export const authService = {

    signup: async (userData) => {
        const res = await axios.post(`${API_URL}/signup`, userData);
        return res.data;
    },

    login: async (credentials) => {
        const res = await axios.post(`${API_URL}/login`, credentials);

        if (res.data && res.data.accessToken) {
            const token = res.data.accessToken;

            localStorage.setItem("token", token);
            localStorage.setItem("isLoggedIn", "true");

            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

            try {
                const userRes = await axios.get(`${API_URL}/me`);

                const user = {
                    user_id: userRes.data.user_id,
                    email: userRes.data.email,
                    name: userRes.data.name
                };

                localStorage.setItem("user", JSON.stringify(user));
            } catch {
                localStorage.setItem("user", JSON.stringify({
                    user_id: null,
                    email: credentials.email,
                    name: credentials.email
                }));
            }
        }

        return res.data;
    },

    getSocialLoginUrl: async (provider) => {
        const res = await axios.get(`${API_URL}/oauth2/url/${provider}`);
        return res.data;
    },

    logout: () => {
        localStorage.clear();
        window.location.href = '/login';
    },

    getCurrentUser: () => {
        try {
            return JSON.parse(localStorage.getItem("user"));
        } catch {
            return null;
        }
    }
};