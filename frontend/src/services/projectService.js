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
    // 내가 작성한 모집글 (작성자 = 토큰의 사용자). 전체 목록 첫 페이지를 걸러 쓰면 10번째 이후 글이 빠진다 (TS-17)
    getMyProjects: async (page = 0, size = 10) => {
        const res = await api.get(`${API_URL}/me`, { params: { page, size } });
        return res.data;
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
    // 작성자는 서버가 JWT로 식별하고, 작성자를 팀원으로 등록하는 것도 서버가 같은 트랜잭션에서 한다 (TS-05)
    createProject: async (data) => {
        const res = await api.post(API_URL, {
            title: data.title,
            content: data.content,
            region: data.region,
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
            message: ""
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