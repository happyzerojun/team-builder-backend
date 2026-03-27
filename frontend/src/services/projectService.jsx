import axios from "axios";

const API_URL = "https://69c516f18a5b6e2dec2bcb85.mockapi.io/api/projects";

export const projectService = {
    getAllProjects: async () => {
        try {
            const response = await axios.get(API_URL);
            return response.data;
        } catch (error) {
            console.error("데이터 로딩 실패:", error);
            return [];
        }
    },

    getProjectById: async (id) => {
        try {
            const response = await axios.get(`${API_URL}/${id}`);
            return response.data;
        } catch (error) {
            console.error("프로젝트 상세 로딩 실패:", error);
            return null;
        }
    },

    createProject: async (projectData) => {
        try {
            const response = await axios.post(API_URL, projectData);
            return response.data;
        } catch (error) {
            console.error("프로젝트 생성 실패:", error);
            throw error;
        }
    },

    updateProject: async (id, updatedData) => {
        try {
            const response = await axios.put(`${API_URL}/${id}`, updatedData);
            return response.data;
        } catch (error) {
            console.error("프로젝트 업데이트 실패:", error);
            throw error; 
        }
    },

    deleteProject: async (projectId) => {
        try {
            await axios.delete(`${API_URL}/${projectId}`);
            return true;
        } catch (error) {
            console.error("프로젝트 삭제 실패:", error);
            return false;
        }
    },
};