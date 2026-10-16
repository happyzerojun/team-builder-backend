import api from "./api";

const API_URL = "/api/application";

const hasToken = () => Boolean(localStorage.getItem("token"));

export const applicationService = {
    // 지원자는 서버가 JWT로 식별한다. applicant_id를 보내지 않는다 (TS-05)
    apply: async (projectId, formData) => {
        const res = await api.post(API_URL, {
            project_id: projectId,
            support_role: formData.supportRole,
            message: formData.message,
            experience: formData.experience,
            contactType: formData.contactType,
            contactValue: formData.contactValue
        });

        return res.data;
    },

    cancel: async (applicationId) => {
        const res = await api.delete(`${API_URL}/${applicationId}`);
        return res.data;
    },

    // 비로그인 방문자는 호출하지 않는다(401 알림 방지)
    getMyApplications: async () => {
        if (!hasToken()) {
            return [];
        }

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
