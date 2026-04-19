import api from "./api";

const API_URL = "/api/application";

export const applicationService = {
    apply: async (projectId) => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");

        const res = await api.post(API_URL, {
            project_id: projectId,
            applicant_id: user.user_id,
            support_role: "지원자",
            message: "",
            status: "pending"
        });

        return res.data;
    },

    cancel: async (applicationId) => {
        const res = await api.delete(`${API_URL}/${applicationId}`);
        return res.data;
    },

    getMyApplications: async () => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");
        const res = await api.get(`${API_URL}/user/${user.user_id}`);
        return Array.isArray(res.data) ? res.data : [];
    },

    getProjectApplications: async (projectId) => {
        const res = await api.get(`${API_URL}/project/${projectId}`);
        return Array.isArray(res.data) ? res.data : [];
    },

    acceptApplication: async (applicationId) => {
        const res = await api.patch(`${API_URL}/${applicationId}`, {
            status: "accepted"
        });
        return res.data;
    },

    rejectApplication: async (applicationId) => {
        const res = await api.patch(`${API_URL}/${applicationId}`, {
            status: "rejected"
        });
        return res.data;
    }
};