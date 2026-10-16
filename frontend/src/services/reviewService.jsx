import api from "./api";

const API_URL = "/api/review";

const hasToken = () => Boolean(localStorage.getItem("token"));

// 작성자·조회 대상은 서버가 JWT로 식별한다. URL·본문에 내 user_id를 넣지 않는다 (TS-05)
export const reviewService = {
    getProjectMyReviews: async (projectId) => {
        const res = await api.get(`${API_URL}/project/${projectId}/me`);
        return Array.isArray(res.data) ? res.data : [];
    },

    saveProjectReviews: async (projectId, reviews) => {
        const payload = reviews.map((r) => ({
            project_id: projectId,
            reviewee_id: r.reviewee_id,
            rating: r.rating,
            comment: r.comment
        }));

        const res = await api.post(`${API_URL}/bulk`, payload);
        return res.data;
    },

    getMyReceivedReviews: async () => {
        if (!hasToken()) return [];
        const res = await api.get(`${API_URL}/received/me`);
        return Array.isArray(res.data) ? res.data : [];
    }
};
