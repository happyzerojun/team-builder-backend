import { MOCK_POSTS } from "../data/mockData";

export const applyToProject = (postId) => {

    const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
    const currentUserName = savedUser.nickname || savedUser.name;
    if (!currentUserName) {
        alert("로그인이 필요한 서비스입니다.");
        return false;
    }
    const targetPost = MOCK_POSTS.find(p => p.id === postId);

    if (targetPost && targetPost.author === currentUserName) {
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
};

export const getAppliedIds = () => {
    return JSON.parse(localStorage.getItem("appliedProjects")) || [];
};