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
            localStorage.setItem("user", JSON.stringify(response.data));
            localStorage.setItem("isLoggedIn", "true");
        }
        return response.data;
    },
    logout: () => {
        localStorage.removeItem("user");
        localStorage.removeItem("isLoggedIn");
    }
};