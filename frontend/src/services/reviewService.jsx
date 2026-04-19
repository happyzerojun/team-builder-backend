import api from "./api";

const API_URL = "/api/review";

export const reviewService = {
    getProjectMyReviews: async (projectId) => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");
        const res = await api.get(`${API_URL}/project/${projectId}/reviewer/${user.user_id}`);
        return Array.isArray(res.data) ? res.data : [];
    },

    saveProjectReviews: async (projectId, reviews) => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");

        const payload = reviews.map((r) => ({
            project_id: projectId,
            reviewer_id: user.user_id,
            reviewee_id: r.reviewee_id,
            rating: r.rating,
            comment: r.comment
        }));

        const res = await api.post(`${API_URL}/bulk`, payload);
        return res.data;
    },

    getMyReceivedReviews: async () => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");
        const res = await api.get(`${API_URL}/received/${user.user_id}`);
        return Array.isArray(res.data) ? res.data : [];
    }
};