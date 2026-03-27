import axios from 'axios';

const MOCK_API_URL = 'https://69c516f18a5b6e2dec2bcb85.mockapi.io/api/projects'; 

export const applyToProject = async (postId) => {
    try {

        const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
        const currentUserName = savedUser.name || savedUser.nickname;

        if (!currentUserName) {
            alert("로그인이 필요한 서비스입니다.");
            return false;
        }

        const response = await axios.get(`${MOCK_API_URL}/${postId}`);
        const targetPost = response.data;

        if (targetPost && (targetPost.author === currentUserName || targetPost.leader === currentUserName)) {
            alert("❌ 본인이 작성한 프로젝트에는 지원할 수 없습니다.");
            return false;
        }

        const appliedIds = JSON.parse(localStorage.getItem("appliedProjects") || "[]");
        if (appliedIds.includes(postId)) {
            alert("이미 지원한 프로젝트입니다.");
            return false;
        }

        const newIds = [...appliedIds, postId];
        localStorage.setItem("appliedProjects", JSON.stringify(newIds));
        
        alert("🚀 지원이 완료되었습니다!");
        return true;

    } catch (error) {
        console.error("지원 처리 중 에러:", error);
        alert("프로젝트 정보를 불러올 수 없습니다.");
        return false;
    }
};

export const getAppliedIds = () => {
    return JSON.parse(localStorage.getItem("appliedProjects")) || [];
};