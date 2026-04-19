import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 10000,
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error?.response?.status;
        const url = error?.config?.url || "";

        if (status === 401 && !url.includes("/api/auth/login")) {
            alert("로그인이 만료되었거나 인증이 필요합니다.");
        }

        if (status === 404) {
            alert("요청한 주소를 찾을 수 없습니다.");
        }

        return Promise.reject(error);
    }
);

export default api;