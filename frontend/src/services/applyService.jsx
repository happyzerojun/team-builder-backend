//지원하기 저장 (지금은 로컬스토리지, 나중에 POST /api/apply)
export const applyToProject = (postId) => {
    const appliedIds = JSON.parse(localStorage.getItem("appliedProjects")) || [];
    if (!appliedIds.includes(postId)) {
        const newIds = [...appliedIds, postId];
        localStorage.setItem("appliedProjects", JSON.stringify(newIds));
        return true;
    }
    return false;
};

//지원한 ID 목록 가져오기 (지금은 로컬스토리지, 나중에 GET /api/my-applications)
export const getAppliedIds = () => {
    return JSON.parse(localStorage.getItem("appliedProjects")) || [];
};