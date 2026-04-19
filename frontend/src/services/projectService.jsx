import api from "./api";

const API_URL = "http://localhost:8080/api/projects";
const APPLICATION_API = "http://localhost:8080/api/application";
const API_URL = "/api/post";
const APPLICATION_API = "/api/application";

export const projectService = {
    getAllProjects: async () => {
        try {
            const res = await api.get(API_URL);
            return res.data;
        } catch (e) {
            console.error(e);
            return [];
        }
    },

    getProjectById: async (projectId) => {
        try {
            const res = await api.get(`${API_URL}/${projectId}`);
            return res.data;
        } catch (e) {
            console.error(e);
            return null;
        }
    },

    createProject: async (data) => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");

        const res = await api.post(API_URL, {
            title: data.title,
            content: data.content,
            region: data.region,
            status: "모집중",
            leader_id: user.user_id,
            term: data.term
        });

        return res.data;
    },

    updateProject: async (projectId, data) => {
        const res = await api.put(`${API_URL}/${projectId}`, {
            title: data.title,
            content: data.content,
            region: data.region,
            status: data.status,
            term: data.term
        });

        return res.data;
    },

    deleteProject: async (projectId) => {
        try {
            await api.delete(`${API_URL}/${projectId}`);
            return true;
        } catch (e) {
            console.error(e);
            return false;
        }
    },

    applyToProject: async (projectId) => {
        const user = JSON.parse(localStorage.getItem("user") || "{}");

        await api.post(APPLICATION_API, {
            project_id: projectId,
            applicant_id: user.user_id,
            support_role: "지원자",
            message: "",
            status: "pending"
        });

        return true;
    },

    cancelApplication: async (applicationId) => {
        await api.delete(`${APPLICATION_API}/${applicationId}`);
        return true;
    },

    getProjectMembers: async (projectId) => {
        const res = await api.get(`${API_URL}/${projectId}/members`);
        return Array.isArray(res.data) ? res.data : [];
    },

    removeProjectMember: async (projectId, memberId) => {
        const res = await api.delete(`${API_URL}/${projectId}/members/${memberId}`);
        return res.data;
    },

    updateProjectStatus: async (projectId, status) => {
        const res = await api.patch(`${API_URL}/${projectId}/status`, { status });
        return res.data;
    }
};