import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

export const authService = {

    signup: async (userData) => {
        const response = await axios.post(`${API_URL}/signup`, userData);
        return response.data;
    },

    login: async (credentials) => {
        const response = await axios.post(`${API_URL}/login`, credentials);
        
        if (response.data) {

            const token = response.data.accessToken || response.data.token;
            
            if (token) {
                localStorage.setItem("token", token);
                localStorage.setItem("isLoggedIn", "true");
                
                const userInfo = response.data.user || { id: credentials.email };
                localStorage.setItem("user", JSON.stringify(userInfo));
            }
        }
        return response.data;
    },

    logout: () => {
        localStorage.clear(); 
    },

    getCurrentUser: () => {
        return JSON.parse(localStorage.getItem("user"));
    }
};