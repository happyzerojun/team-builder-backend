import api from "./api";

const API_URL = "/api/review";

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
        const res = await api.get(`${API_URL}/received/me`);
        return Array.isArray(res.data) ? res.data : [];
    }
};
