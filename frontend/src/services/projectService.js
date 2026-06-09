import api from "./api";
const API_URL = "/api/projects";
const APPLICATION_API = "/api/application";

export const projectService = {
    // 💡 [수정 포인트] MainPage에서 넘겨주는 page와 size 파라미터를 받아 axios 쿼리 스트링으로 전달합니다.
    getAllProjects: async (page = 0, size = 9) => {
        try {
            // Axios의 params 옵션을 사용하면 자동으로 URL 뒤에 ?page=0&size=9 를 붙여줍니다.
            const res = await api.get(API_URL, {
                params: {
                    page: page,
                    size: size
                }
            });
            return res.data;
        } catch (e) {
            console.error(e);
            // 에러 발생 시 MainPage의 크래시를 방지하기 위해 페이징 객체 기본 구조를 반환합니다.
            return { content: [], totalPages: 1 };
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
        const res = await api.post(API_URL, {
            title: data.title,
            content: data.content,
            region: data.region,
            status: "모집중",
            term: data.term,
            techStackIds: data.techStackIds,
            meetingType: data.meetingType,
            isLocalOnly: data.isLocalOnly
        });
        return res.data;
    },
    updateProject: async (projectId, data) => {
        const res = await api.put(`${API_URL}/${projectId}`, {
            title: data.title,
            content: data.content,
            region: data.region,
            status: data.status,
            term: data.term,
            techStackIds: data.techStackIds,
            meetingType: data.meetingType,
            isLocalOnly: data.isLocalOnly
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
        await api.post(APPLICATION_API, {
            project_id: projectId,
            support_role: "지원자",
            message: "",
            status: "PENDING"
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
    updateProjectStatus: async (projectId, status) => {
        const res = await api.patch(`${API_URL}/${projectId}/status`, { status });
        return res.data;
    },
    removeProjectMember: async (projectId, memberId) => {
        const res = await api.delete(`${API_URL}/${projectId}/members/${memberId}`);
        return res.data;
    },
    addProjectMember: async (projectId, userId) => {
        const res = await api.post(`${API_URL}/${projectId}/members`, {
            user_id: userId
        });
        return res.data;
    },
};
