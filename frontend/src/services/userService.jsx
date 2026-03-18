export const getUserProfile = () => {
    const savedUser = localStorage.getItem("user");
    return savedUser ? JSON.parse(savedUser) : { name: "강무원", id: "muwon123", bio: "반갑습니다!" };
};

export const updateUserProfile = (newInfo) => {
    // 현재 저장된 정보와 새 정보 합치기
    const currentUser = getUserProfile();
    const updatedUser = { ...currentUser, ...newInfo };

    // 로컬 스토리지에 저장 (백엔드가 생기면 여기서 axios.put을 호출?)
    localStorage.setItem("user", JSON.stringify(updatedUser));
    return updatedUser;
};