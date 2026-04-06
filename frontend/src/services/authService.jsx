import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

export const authService = {

    signup: async (userData) => {
        const response = await axios.post(`${API_URL}/signup`, userData);
        return response.data;
    },

    login: async (credentials) => {
        const response = await axios.post(`${API_URL}/login`, credentials);
        
        if (response.data && response.data.accessToken) {
            const token = response.data.accessToken;
            localStorage.setItem("token", token);
            localStorage.setItem("isLoggedIn", "true");
            
            try {
                const userRes = await axios.get(`${API_URL}/me`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                localStorage.setItem("user", JSON.stringify(userRes.data));
            } catch (e) {
                localStorage.setItem("user", JSON.stringify({ id: credentials.email }));
            }
        }
        return response.data;
    },

    getSocialLoginUrl: async (provider) => {
        const response = await axios.get(`${API_URL}/oauth2/url/${provider}`);
        return response.data;
    },

    logout: () => {
        localStorage.clear(); 
        window.location.href = '/login'; 
    },

    getCurrentUser: () => {
        return JSON.parse(localStorage.getItem("user"));
    }
};