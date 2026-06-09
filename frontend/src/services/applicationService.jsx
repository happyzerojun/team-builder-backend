import api from "./api";

const API_URL = "/api/application";

export const applicationService = {
    apply: async (projectId, formData) => {
        const res = await api.post(API_URL, {
            project_id: projectId,
            support_role: formData.supportRole,
            message: formData.message,
            experience: formData.experience,
            contactType: formData.contactType,
            contactValue: formData.contactValue,
            status: "PENDING"
        });

        return res.data;
    },

    cancel: async (applicationId) => {
        const res = await api.delete(`${API_URL}/${applicationId}`);
        return res.data;
    },

    getMyApplications: async () => {
        const res = await api.get(`${API_URL}/me`);
        return Array.isArray(res.data) ? res.data : [];
    },

    getProjectApplications: async (projectId) => {
        const res = await api.get(`${API_URL}/project/${projectId}`);
        return Array.isArray(res.data) ? res.data : [];
    },

    acceptApplication: async (applicationId) => {
        const res = await api.patch(`${API_URL}/${applicationId}`, {
            status: "ACCEPTED"
        });
        return res.data;
    },

    rejectApplication: async (applicationId) => {
        const res = await api.patch(`${API_URL}/${applicationId}`, {
            status: "REJECTED"
        });
        return res.data;
    }
};
