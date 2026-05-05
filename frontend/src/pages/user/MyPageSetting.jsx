import React, { useState, useEffect } from 'react';
import './MyPageSetting.css';
import { useNavigate } from 'react-router-dom';
import { getUserProfile, updateUserProfile } from '../../services/userService';

const MyPageSetting = () => {
    const navigate = useNavigate();

    // 추천 기술 스택 목록
    const recommendedTags = [
        'React', 'Vue.js', 'Next.js', 'TypeScript', 'Node.js',
        'Spring Boot', 'Java', 'Python', 'Django', 'Express',
        'MySQL', 'MongoDB', 'PostgreSQL', 'Docker', 'AWS'
    ];

    const [profileImg, setProfileImg] = useState(null);
    const [loading, setLoading] = useState(true);

    // 폼 데이터 상태 (백엔드 DTO 규격과 일치)
    const [formData, setFormData] = useState({
        nickname: '',
        jobRole: '',
        organization: '',
        introduction: '',
        tags: []
    });

    const [customTag, setCustomTag] = useState('');

    // 1. 초기 데이터 로드 (마이페이지 정보 가져오기)
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setLoading(true);
                const profile = await getUserProfile(); // 서버에서 데이터 가져오기
                const savedUser = JSON.parse(localStorage.getItem("user") || "{}");

                // [디버깅 로그] 이 로그를 개발자 도구(F12) 콘솔에서 꼭 확인하세요!
                console.log("서버 응답 데이터:", profile);
                console.log("로컬 저장 데이터:", savedUser);

                // 1. 데이터 병합 전략: 서버 데이터가 있으면 우선하되, 없으면 로컬 데이터로 보충
                const baseUser = { ...savedUser, ...profile };

                // 2. 폼 데이터 세팅 (이름표가 다를 경우를 대비해 모두 체크)
                setFormData({
                    nickname: baseUser.nickname || baseUser.name || '',
                    // jobRole(카멜케이스)과 job_role(스네이크케이스) 둘 다 확인
                    jobRole: baseUser.jobRole || baseUser.job_role || '',
                    organization: baseUser.organization || '',
                    introduction: baseUser.introduction || '',
                    // 백엔드 DTO에서 @JsonProperty("tags")를 썼으므로 tags로 올 겁니다.
                    tags: baseUser.tags || baseUser.techStacks || []
                });

                // 3. 프로필 이미지 세팅
                if (baseUser.profileImg || baseUser.profile_img) {
                    setProfileImg(baseUser.profileImg || baseUser.profile_img);
                }
            } catch (error) {
                console.error("프로필 불러오기 실패:", error);
                // profile이 없어도 로컬 데이터로라도 보여주기 위해 에러 처리를 유연하게 합니다.
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    // 2. 이미지 변경 핸들러 (Base64 변환)
    const handleImgChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onloadend = () => {
            setProfileImg(reader.result);
        };
        reader.readAsDataURL(file);
    };

    // 3. 입력 필드 변경 핸들러
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value
        }));
    };

    // 4. 태그 클릭 핸들러 (추가/삭제 토글)
    const handleTagClick = (tag) => {
        setFormData((prev) => ({
            ...prev,
            tags: prev.tags.includes(tag)
                ? prev.tags.filter((t) => t !== tag)
                : [...prev.tags, tag]
        }));
    };

    // 5. 커스텀 태그 추가 핸들러
    const handleAddCustomTag = (e) => {
        if (e.key === 'Enter' || e.type === 'click') {
            e.preventDefault();
            const trimmedTag = customTag.trim();
            if (!trimmedTag) return;
            if (formData.tags.includes(trimmedTag)) return;

            setFormData((prev) => ({
                ...prev,
                tags: [...prev.tags, trimmedTag]
            }));
            setCustomTag('');
        }
    };

    // 6. 저장 버튼 핸들러 (백엔드로 전송)
    const handleSave = async () => {
        if (!formData.nickname.trim()) {
            alert("닉네임을 입력해주세요!");
            return;
        }

        try {
            const payload = {
                name: formData.nickname,
                nickname: formData.nickname,
                jobRole: formData.jobRole,
                organization: formData.organization,
                introduction: formData.introduction,
                tags: formData.tags,
                profileImg: profileImg
            };

            await updateUserProfile(payload);

            // 로컬 스토리지 업데이트
            const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
            const updatedUser = {
                ...currentUser,
                ...payload
            };
            localStorage.setItem("user", JSON.stringify(updatedUser));

            alert("프로필이 성공적으로 저장되었습니다!");
            navigate('/mypage');
        } catch (error) {
            console.error("프로필 저장 실패:", error);
            alert("프로필 저장에 실패했습니다.");
        }
    };

    if (loading) {
        return <div className="ms-container">데이터를 불러오는 중...</div>;
    }

    return (
        <div className="ms-container">
            <div className="ms-card">
                <h2 className="ms-title">프로필 수정</h2>

                {/* 프로필 이미지 섹션 */}
                <div className="ms-profile-img-wrap">
                    <label htmlFor="profile-upload" className="ms-img-label-wrapper">
                        <div className="ms-img-box">
                            {profileImg ? (
                                <img src={profileImg} alt="프로필 미리보기" className="ms-uploaded-img" />
                            ) : (
                                <div className="ms-default-img">👤</div>
                            )}
                            <div className="ms-img-overlay">편집</div>
                        </div>
                        <input
                            type="file"
                            id="profile-upload"
                            className="ms-hidden-input"
                            accept="image/*"
                            onChange={handleImgChange}
                        />
                    </label>
                    <span className="ms-img-instruction">이미지를 클릭하여 변경</span>
                </div>

                {/* 기본 정보 입력 섹션 */}
                <div className="ms-input-group">
                    <label>닉네임</label>
                    <input
                        className="ms-input"
                        name="nickname"
                        placeholder="사용하실 닉네임을 입력하세요"
                        value={formData.nickname}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>소속</label>
                    <input
                        className="ms-input"
                        name="organization"
                        placeholder="학교 또는 직장"
                        value={formData.organization}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>희망 직무</label>
                    <input
                        className="ms-input"
                        name="jobRole"
                        placeholder="예: 프론트엔드 개발자"
                        value={formData.jobRole}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>자기소개</label>
                    <textarea
                        className="ms-textarea"
                        name="introduction"
                        placeholder="자신을 자유롭게 소개해주세요"
                        value={formData.introduction}
                        onChange={handleChange}
                    />
                </div>

                {/* 기술 스택 섹션 */}
                <div className="ms-input-group">
                    <label>기술 스택</label>
                    <div className="ms-selected-tags">
                        {formData.tags.map((tag) => (
                            <span key={tag} className="ms-tag-active" onClick={() => handleTagClick(tag)}>
                                #{tag} <span className="ms-tag-remove">✕</span>
                            </span>
                        ))}
                    </div>

                    <div className="ms-tag-pool">
                        {recommendedTags.map((tag) => (
                            <button
                                key={tag}
                                type="button"
                                className={`ms-tag-btn ${formData.tags.includes(tag) ? 'selected' : ''}`}
                                onClick={() => handleTagClick(tag)}
                            >
                                {tag}
                            </button>
                        ))}
                        <div className="ms-custom-tag-wrap">
                            <input
                                type="text"
                                className="ms-tag-input"
                                placeholder="직접 입력..."
                                value={customTag}
                                onChange={(e) => setCustomTag(e.target.value)}
                                onKeyDown={handleAddCustomTag}
                            />
                            <button type="button" className="ms-tag-add-btn" onClick={handleAddCustomTag}>+</button>
                        </div>
                    </div>
                </div>

                {/* 하단 버튼 */}
                <div className="ms-button-group">
                    <button className="ms-cancel-btn" onClick={() => navigate('/mypage')}>취소</button>
                    <button className="ms-save-btn" onClick={handleSave}>저장하기</button>
                </div>
            </div>
        </div>
    );
};

export default MyPageSetting;